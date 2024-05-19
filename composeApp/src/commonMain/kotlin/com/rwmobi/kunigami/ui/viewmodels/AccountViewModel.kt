/*
 * Copyright (c) 2024. Ryan Wong
 * https://github.com/ryanw-mobile
 * Sponsored by RW MobiMedia UK Limited
 *
 */

package com.rwmobi.kunigami.ui.viewmodels

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.rwmobi.kunigami.domain.exceptions.IncompleteCredentialsException
import com.rwmobi.kunigami.domain.repository.UserPreferencesRepository
import com.rwmobi.kunigami.domain.usecase.GetTariffRatesUseCase
import com.rwmobi.kunigami.domain.usecase.GetUserAccountUseCase
import com.rwmobi.kunigami.domain.usecase.InitialiseAccountUseCase
import com.rwmobi.kunigami.domain.usecase.UpdateMeterPreferenceUseCase
import com.rwmobi.kunigami.ui.destinations.account.AccountScreenLayout
import com.rwmobi.kunigami.ui.destinations.account.AccountUIState
import com.rwmobi.kunigami.ui.model.ErrorMessage
import com.rwmobi.kunigami.ui.utils.generateRandomLong
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kunigami.composeapp.generated.resources.Res
import kunigami.composeapp.generated.resources.account_error_load_account
import kunigami.composeapp.generated.resources.account_error_load_tariff
import org.jetbrains.compose.resources.getString

class AccountViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val getUserAccountUseCase: GetUserAccountUseCase,
    private val getTariffRatesUseCase: GetTariffRatesUseCase,
    private val initialiseAccountUseCase: InitialiseAccountUseCase,
    private val updateMeterPreferenceUseCase: UpdateMeterPreferenceUseCase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {
    private val _uiState: MutableStateFlow<AccountUIState> = MutableStateFlow(AccountUIState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    fun errorShown(errorId: Long) {
        _uiState.update { currentUiState ->
            val errorMessages = currentUiState.errorMessages.filterNot { it.id == errorId }
            currentUiState.copy(errorMessages = errorMessages)
        }
    }

    fun notifyWindowSizeClassChanged(windowSizeClass: WindowSizeClass) {
        val requestedLayout = when (windowSizeClass.widthSizeClass) {
            WindowWidthSizeClass.Expanded -> AccountScreenLayout.WideWrapped
            WindowWidthSizeClass.Medium -> AccountScreenLayout.Wide
            else -> AccountScreenLayout.Compact
        }

        _uiState.update { currentUiState ->
            currentUiState.copy(
                requestedLayout = requestedLayout,
            )
        }
    }

    fun refresh() {
        _uiState.update { currentUiState ->
            currentUiState.copy(
                isLoading = true,
            )
        }

        viewModelScope.launch(dispatcher) {
            val userAccount = getUserAccountUseCase()
            userAccount.fold(
                onSuccess = { account ->
                    _uiState.update { currentUiState ->
                        currentUiState.copy(
                            account = account,
                            selectedMpan = account.electricityMeterPoints[0].mpan,
                            selectedMeterSerialNumber = account.electricityMeterPoints[0].meterSerialNumbers[0],
                        )
                    }
                },
                onFailure = { throwable ->
                    if (throwable is IncompleteCredentialsException) {
                        _uiState.update { currentUiState ->
                            currentUiState.copy(
                                isDemoMode = true,
                                isLoading = false,
                            )
                        }
                    } else {
                        updateUIForError(message = throwable.message ?: getString(resource = Res.string.account_error_load_account))
                        Logger.e(getString(resource = Res.string.account_error_load_account), throwable = throwable, tag = "AccountViewModel")
                    }
                    return@launch
                },
            )

            val tariffCode = _uiState.value.account?.electricityMeterPoints?.get(0)?.currentAgreement?.tariffCode ?: return@launch

            val tariffRates = getTariffRatesUseCase(
                productCode = extractSegment(tariffCode) ?: "",
                tariffCode = tariffCode,
            )
            val selectedMpan = userPreferencesRepository.getMpan()
            val selectedMeterSerialNumber = userPreferencesRepository.getMeterSerialNumber()

            tariffRates.fold(
                onSuccess = { tariff ->
                    _uiState.update { currentUiState ->
                        currentUiState.copy(
                            isDemoMode = false,
                            tariff = tariff,
                            selectedMpan = selectedMpan,
                            selectedMeterSerialNumber = selectedMeterSerialNumber,
                            isLoading = false,
                        )
                    }
                },
                onFailure = { throwable ->
                    Logger.e(getString(resource = Res.string.account_error_load_tariff), throwable = throwable, tag = "AccountViewModel")

                    _uiState.update { currentUiState ->
                        currentUiState.copy(
                            isDemoMode = false,
                            tariff = null,
                            selectedMpan = selectedMpan,
                            selectedMeterSerialNumber = selectedMeterSerialNumber,
                            isLoading = false,
                        )
                    }
                },
            )
        }
    }

    fun clearCredentials() {
        viewModelScope.launch {
            userPreferencesRepository.clearCredentials()
            refresh()
        }
    }

    fun submitCredentials(
        apiKey: String,
        accountNumber: String,
    ) {
        _uiState.update { currentUiState ->
            currentUiState.copy(
                isLoading = true,
            )
        }

        viewModelScope.launch {
            val result = initialiseAccountUseCase(apiKey = apiKey, accountNumber = accountNumber)

            result.fold(
                onSuccess = {
                    refresh()
                },
                onFailure = { throwable ->
                    updateUIForError(message = throwable.message ?: getString(resource = Res.string.account_error_load_account))
                    Logger.e(getString(resource = Res.string.account_error_load_account), throwable = throwable, tag = "AccountViewModel")
                },
            )
        }
    }

    fun updateMeterSerialNumber(
        mpan: String,
        meterSerialNumber: String,
    ) {
        viewModelScope.launch {
            val result = updateMeterPreferenceUseCase(mpan = mpan, meterSerialNumber = meterSerialNumber)

            result.fold(
                onSuccess = {
                    refresh()
                },
                onFailure = { throwable ->
                    updateUIForError(message = throwable.message ?: getString(resource = Res.string.account_error_load_account))
                    Logger.e(getString(resource = Res.string.account_error_load_account), throwable = throwable, tag = "AccountViewModel")
                },
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
        Logger.v("AccountViewModel", message = { "onCleared" })
    }

    private fun extractSegment(input: String): String? {
        val parts = input.split("-")
        // Check if there are enough parts to remove
        if (parts.size > 3) {
            // Exclude the first two and the last segments
            val relevantParts = parts.drop(2).dropLast(1)
            return relevantParts.joinToString("-")
        }
        return null
    }
}
