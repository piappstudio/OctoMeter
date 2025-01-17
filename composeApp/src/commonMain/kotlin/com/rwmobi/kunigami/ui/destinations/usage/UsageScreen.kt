/*
 * Copyright (c) 2024. Ryan Wong
 * https://github.com/ryanw-mobile
 * Sponsored by RW MobiMedia UK Limited
 *
 */

package com.rwmobi.kunigami.ui.destinations.usage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import com.rwmobi.kunigami.ui.components.DemoModeCtaAdaptive
import com.rwmobi.kunigami.ui.components.ErrorScreenHandler
import com.rwmobi.kunigami.ui.components.LargeTitleWithIcon
import com.rwmobi.kunigami.ui.components.LoadingScreen
import com.rwmobi.kunigami.ui.components.MessageActionScreen
import com.rwmobi.kunigami.ui.components.ScrollbarMultiplatform
import com.rwmobi.kunigami.ui.components.koalaplot.VerticalBarChart
import com.rwmobi.kunigami.ui.composehelper.conditionalBlur
import com.rwmobi.kunigami.ui.composehelper.generateGYRHueColorPalette
import com.rwmobi.kunigami.ui.destinations.usage.components.RateGroupCells
import com.rwmobi.kunigami.ui.destinations.usage.components.RateGroupTitle
import com.rwmobi.kunigami.ui.destinations.usage.components.TariffProjectionsCardAdaptive
import com.rwmobi.kunigami.ui.destinations.usage.components.TitleNavigationBar
import com.rwmobi.kunigami.ui.extensions.partitionList
import com.rwmobi.kunigami.ui.model.chart.RequestedChartLayout
import com.rwmobi.kunigami.ui.model.consumption.ConsumptionGroupWithPartitions
import com.rwmobi.kunigami.ui.theme.getDimension
import kotlinx.datetime.Instant
import kunigami.composeapp.generated.resources.Res
import kunigami.composeapp.generated.resources.bolt
import kunigami.composeapp.generated.resources.error_screen_no_data_description_no_auth
import kunigami.composeapp.generated.resources.error_screen_no_data_title
import kunigami.composeapp.generated.resources.file_dotted
import kunigami.composeapp.generated.resources.kwh
import kunigami.composeapp.generated.resources.provide_api_key
import kunigami.composeapp.generated.resources.usage_demo_introduction
import kunigami.composeapp.generated.resources.usage_energy_consumption_breakdown
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun UsageScreen(
    modifier: Modifier = Modifier,
    uiState: UsageUIState,
    uiEvent: UsageUIEvent,
) {
    if (uiState.errorMessages.isNotEmpty()) {
        val errorMessage = remember(uiState) { uiState.errorMessages[0] }
        val errorMessageText = errorMessage.message

        LaunchedEffect(errorMessage.id) {
            uiEvent.onShowSnackbar(errorMessageText)
            uiEvent.onErrorShown(errorMessage.id)
        }
    }

    val dimension = LocalDensity.current.getDimension()
    val lazyListState = rememberLazyListState()
    val colorPalette = remember {
        generateGYRHueColorPalette(
            saturation = 0.6f,
            lightness = 0.6f,
        )
    }

    Box(modifier = modifier) {
        when (uiState.requestedScreenType) {
            is UsageScreenType.Error -> {
                ErrorScreenHandler(
                    modifier = Modifier.fillMaxSize(),
                    specialErrorScreen = uiState.requestedScreenType.specialErrorScreen,
                    onRefresh = {
                        uiEvent.onInitialLoad()
                    },
                )
            }

            // We need to retain the navigation bar even for no data
            UsageScreenType.Chart -> {
                ScrollbarMultiplatform(
                    modifier = Modifier.fillMaxSize(),
                    enabled = uiState.consumptionGroupedCells.isNotEmpty(),
                    lazyListState = lazyListState,
                ) { contentModifier ->
                    Column(
                        modifier = contentModifier
                            .fillMaxSize()
                            .conditionalBlur(enabled = uiState.isLoading),
                    ) {
                        with(uiState.consumptionQueryFilter) {
                            TitleNavigationBar(
                                modifier = Modifier
                                    .background(color = MaterialTheme.colorScheme.secondary)
                                    .fillMaxWidth()
                                    .height(height = dimension.minListItemHeight),
                                currentPresentationStyle = presentationStyle,
                                title = getConsumptionPeriodString(),
                                canNavigateBack = canNavigateBackward(accountMoveInDate = uiState.userProfile?.account?.movedInAt ?: Instant.DISTANT_PAST),
                                canNavigateForward = canNavigateForward(),
                                onNavigateBack = uiEvent.onPreviousTimeFrame,
                                onNavigateForward = uiEvent.onNextTimeFrame,
                                onSwitchPresentationStyle = { uiEvent.onSwitchPresentationStyle(it) },
                            )
                        }

                        // Pre-calculate the list of (rateGroup.title, partitionedItems)
                        val consumptionGroupsWithPartitions = remember(uiState.consumptionGroupedCells, uiState.requestedUsageColumns) {
                            uiState.consumptionGroupedCells.map { rateGroup ->
                                ConsumptionGroupWithPartitions(
                                    title = rateGroup.title,
                                    partitionedItems = rateGroup.consumptions.partitionList(columns = uiState.requestedUsageColumns),
                                )
                            }
                        }
                        val shouldHideLastConsumptionGroupColumn = remember(consumptionGroupsWithPartitions) {
                            consumptionGroupsWithPartitions.all {
                                it.partitionedItems.last().isEmpty()
                            }
                        }

                        LazyColumn(
                            contentPadding = PaddingValues(bottom = dimension.grid_4),
                            state = lazyListState,
                        ) {
                            if (uiState.isDemoMode == true) {
                                item(key = "demoCta") {
                                    DemoModeCtaAdaptive(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(all = dimension.grid_2),
                                        description = stringResource(resource = Res.string.usage_demo_introduction),
                                        ctaButtonLabel = stringResource(resource = Res.string.provide_api_key),
                                        onCtaButtonClicked = uiEvent.onNavigateToAccountTab,
                                        useWideLayout = uiState.requestedAdaptiveLayout != WindowWidthSizeClass.Compact,
                                    )
                                }
                            }

                            if (uiState.consumptionGroupedCells.isEmpty()) {
                                if (!uiState.isLoading) {
                                    item(key = "noData") {
                                        MessageActionScreen(
                                            modifier = Modifier.fillMaxSize(),
                                            icon = painterResource(resource = Res.drawable.file_dotted),
                                            text = stringResource(resource = Res.string.error_screen_no_data_title),
                                            description = stringResource(resource = Res.string.error_screen_no_data_description_no_auth),
                                        )
                                    }
                                }
                            } else {
                                uiState.barChartData?.let { barChartData ->
                                    item(key = "chart") {
                                        BoxWithConstraints {
                                            val constraintModifier = when (uiState.requestedChartLayout) {
                                                is RequestedChartLayout.Portrait -> {
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .aspectRatio(4 / 3f)
                                                }

                                                is RequestedChartLayout.LandScape -> {
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .height(uiState.requestedChartLayout.requestedMaxHeight)
                                                }
                                            }

                                            VerticalBarChart(
                                                modifier = constraintModifier.padding(all = dimension.grid_2),
                                                showToolTipOnClick = uiState.showToolTipOnClick,
                                                entries = barChartData.verticalBarPlotEntries,
                                                yAxisRange = uiState.consumptionRange,
                                                yAxisTitle = stringResource(resource = Res.string.kwh),
                                                colorPalette = colorPalette,
                                                labelGenerator = { index ->
                                                    barChartData.labels[index]
                                                },
                                                tooltipGenerator = { index ->
                                                    barChartData.tooltips[index]
                                                },
                                            )
                                        }
                                    }
                                }

                                item(key = "tariffAndProjections") {
                                    TariffProjectionsCardAdaptive(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(
                                                horizontal = dimension.grid_3,
                                                vertical = dimension.grid_1,
                                            ),
                                        layoutType = uiState.requestedAdaptiveLayout,
                                        tariffSummary = uiState.userProfile?.tariffSummary,
                                        insights = uiState.insights,
                                        mpan = uiState.userProfile?.selectedMpan,
                                    )
                                }

                                item(key = "headingConsumptionBreakdowns") {
                                    LargeTitleWithIcon(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(all = dimension.grid_2),
                                        icon = painterResource(resource = Res.drawable.bolt),
                                        label = stringResource(resource = Res.string.usage_energy_consumption_breakdown),
                                    )
                                }

                                consumptionGroupsWithPartitions.forEach { consumptionGroupWithPartitions ->
                                    item(key = "${consumptionGroupWithPartitions.title}Title") {
                                        RateGroupTitle(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(
                                                    vertical = dimension.grid_2,
                                                    horizontal = dimension.grid_4,
                                                ),
                                            consumptionGroupWithPartitions = consumptionGroupWithPartitions,
                                        )
                                    }

                                    val maxRows = consumptionGroupWithPartitions.partitionedItems.maxOf { it.size }
                                    items(maxRows) { rowIndex ->
                                        RateGroupCells(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(
                                                    horizontal = dimension.grid_4,
                                                    vertical = dimension.grid_0_25,
                                                ),
                                            partitionedItems = consumptionGroupWithPartitions.partitionedItems,
                                            shouldHideLastColumn = shouldHideLastConsumptionGroupColumn,
                                            rowIndex = rowIndex,
                                            maxInRange = uiState.consumptionRange.endInclusive,
                                            presentationStyle = uiState.consumptionQueryFilter.presentationStyle,
                                            colorPalette = colorPalette,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (uiState.isLoading) {
            LoadingScreen(modifier = Modifier.fillMaxSize())
        }
    }

    LaunchedEffect(true) {
        uiEvent.onInitialLoad()
    }

    LaunchedEffect(uiState.consumptionGroupedCells) {
        lazyListState.scrollToItem(index = 0)
    }

    LaunchedEffect(uiState.requestScrollToTop) {
        if (uiState.requestScrollToTop) {
            lazyListState.scrollToItem(index = 0)
            uiEvent.onScrolledToTop()
        }
    }
}
