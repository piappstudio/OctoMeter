/*
 * Copyright (c) 2024. Ryan Wong
 * https://github.com/ryanw-mobile
 * Sponsored by RW MobiMedia UK Limited
 *
 */

package com.rwmobi.kunigami.ui.viewmodels

import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.rwmobi.kunigami.domain.extensions.roundDownToHour
import com.rwmobi.kunigami.domain.extensions.toLocalDateString
import com.rwmobi.kunigami.domain.extensions.toLocalHourMinuteString
import com.rwmobi.kunigami.domain.model.Rate
import com.rwmobi.kunigami.domain.usecase.GetStandardUnitRateUseCase
import com.rwmobi.kunigami.ui.destinations.agile.AgileUIState
import com.rwmobi.kunigami.ui.model.BarChartData
import com.rwmobi.kunigami.ui.model.ErrorMessage
import com.rwmobi.kunigami.ui.model.RateGroupedCells
import com.rwmobi.kunigami.ui.model.RequestedChartLayout
import com.rwmobi.kunigami.ui.model.ScreenSizeInfo
import com.rwmobi.kunigami.ui.utils.generateRandomLong
import io.github.koalaplot.core.bar.DefaultVerticalBarPlotEntry
import io.github.koalaplot.core.bar.DefaultVerticalBarPosition
import io.github.koalaplot.core.bar.VerticalBarPlotEntry
import io.github.koalaplot.core.util.toString
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.ceil
import kotlin.time.Duration

class AgileViewModel(
    private val getStandardUnitRateUseCase: GetStandardUnitRateUseCase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {
    private val _uiState: MutableStateFlow<AgileUIState> = MutableStateFlow(AgileUIState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    private val rateColumnWidth = 175.dp

    fun errorShown(errorId: Long) {
        _uiState.update { currentUiState ->
            val errorMessages = currentUiState.errorMessages.filterNot { it.id == errorId }
            currentUiState.copy(errorMessages = errorMessages)
        }
    }

    fun refresh() {
        _uiState.update { currentUiState ->
            currentUiState.copy(
                isLoading = true,
            )
        }

        viewModelScope.launch(dispatcher) {
            val currentTime = Clock.System.now().roundDownToHour()
            val productCode = "AGILE-FLEX-22-11-25"
            val tariffCode = "E-1R-AGILE-FLEX-22-11-25-J"
            val periodFrom = currentTime
            val periodTo = currentTime.plus(duration = Duration.parse("1d"))

            getStandardUnitRateUseCase(
                productCode = productCode,
                tariffCode = tariffCode,
                periodFrom = periodFrom,
                periodTo = periodTo,
            ).fold(
                onSuccess = { rates ->
                    _uiState.update { currentUiState ->
                        val rateRange = if (rates.isEmpty()) {
                            0.0..0.0 // Return a default range if the list is empty
                        } else {
                            0.0..ceil(rates.maxOf { it.vatInclusivePrice } * 10) / 10.0
                        }

                        val verticalBarPlotEntries: List<VerticalBarPlotEntry<Int, Double>> = buildList {
                            rates.forEachIndexed { index, rate ->
                                add(
                                    element = DefaultVerticalBarPlotEntry(
                                        x = index,
                                        y = DefaultVerticalBarPosition(yMin = 0.0, yMax = rate.vatInclusivePrice),
                                    ),
                                )
                            }
                        }

                        val labels = generateChartLabels(rates = rates)
                        val rateGroupedCells = groupChartCells(rates = rates)
                        val toolTips = generateChartToolTips(rates = rates)

                        currentUiState.copy(
                            isLoading = false,
                            rateGroupedCells = rateGroupedCells,
                            rateRange = rateRange,
                            barChartData = BarChartData(
                                verticalBarPlotEntries = verticalBarPlotEntries,
                                labels = labels,
                                tooltips = toolTips,
                            ),
                        )
                    }
                },
                onFailure = { throwable ->
                    updateUIForError(message = throwable.message ?: "Error when retrieving rates")
                    Logger.e("AgileViewModel", throwable = throwable, message = { "Error when retrieving rates" })
                },
            )
        }
    }

    private fun generateChartLabels(rates: List<Rate>): Map<Int, String> {
        return buildMap {
            var lastRateValue: Int? = null
            rates.forEachIndexed { index, rate ->
                val currentTime = rate.validFrom.toLocalDateTime(TimeZone.currentSystemDefault()).time.hour
                if (currentTime != lastRateValue) {
                    put(key = index, value = currentTime.toString().padStart(2, '0'))
                    lastRateValue = currentTime
                }
            }
        }
    }

    private fun groupChartCells(rates: List<Rate>): List<RateGroupedCells> {
        return rates
            .groupBy { it.validFrom.toLocalDateString() }
            .map { (date, items) -> RateGroupedCells(title = date, rates = items) }
    }

    private fun generateChartToolTips(rates: List<Rate>): List<String> {
        return rates.map { rate ->
            val timeRange = rate.validFrom.toLocalHourMinuteString() +
                (rate.validTo?.let { "- ${it.toLocalHourMinuteString()}" } ?: "")
            "$timeRange\n${rate.vatInclusivePrice.toString(precision = 2)}p"
        }
    }

    fun notifyScreenSizeChanged(screenSizeInfo: ScreenSizeInfo) {
        _uiState.update { currentUiState ->
            Logger.v("AgileViewModel: ${screenSizeInfo.heightDp}h x ${screenSizeInfo.widthDp}w, isPortrait = ${screenSizeInfo.isPortrait()}")
            val requestedLayout = if (screenSizeInfo.isPortrait()) {
                RequestedChartLayout.Portrait
            } else {
                RequestedChartLayout.LandScape(
                    requestedMaxHeight = screenSizeInfo.heightDp / 2,
                )
            }

            val usageColumns = (screenSizeInfo.widthDp / rateColumnWidth).toInt()

            currentUiState.copy(
                requestedChartLayout = requestedLayout,
                requestedRateColumns = usageColumns,
            )
        }
    }

    fun requestScrollToTop(enabled: Boolean) {
        _uiState.update { currentUiState ->
            currentUiState.copy(
                requestScrollToTop = enabled,
            )
        }
    }

    private fun updateUIForError(message: String) {
        _uiState.update { currentUiState ->
            val newErrorMessages = if (_uiState.value.errorMessages.any { it.message == message }) {
                currentUiState.errorMessages
            } else {
                currentUiState.errorMessages + ErrorMessage(
                    id = generateRandomLong(),
                    message = message,
                )
            }
            currentUiState.copy(
                isLoading = false,
                errorMessages = newErrorMessages,
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        Logger.v("AgileViewModel", message = { "onCleared" })
    }
}
