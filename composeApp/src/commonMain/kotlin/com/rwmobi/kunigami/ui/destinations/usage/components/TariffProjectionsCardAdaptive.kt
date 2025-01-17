/*
 * Copyright (c) 2024. Ryan Wong
 * https://github.com/ryanw-mobile
 * Sponsored by RW MobiMedia UK Limited
 *
 */

package com.rwmobi.kunigami.ui.destinations.usage.components

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rwmobi.kunigami.domain.model.product.TariffSummary
import com.rwmobi.kunigami.ui.components.CommonPreviewSetup
import com.rwmobi.kunigami.ui.components.TariffSummaryCardAdaptive
import com.rwmobi.kunigami.ui.model.consumption.Insights
import com.rwmobi.kunigami.ui.previewsampledata.TariffSamples
import com.rwmobi.kunigami.ui.theme.getDimension
import kunigami.composeapp.generated.resources.Res
import kunigami.composeapp.generated.resources.account_mpan
import kunigami.composeapp.generated.resources.usage_current_tariff
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TariffProjectionsCardAdaptive(
    modifier: Modifier = Modifier,
    mpan: String?,
    tariffSummary: TariffSummary?,
    insights: Insights?,
    layoutType: WindowWidthSizeClass = WindowWidthSizeClass.Compact,
) {
    when (layoutType) {
        WindowWidthSizeClass.Compact -> {
            TariffProjectionsCardLinear(
                modifier = modifier,
                mpan = mpan,
                tariffSummary = tariffSummary,
                insights = insights,
            )
        }

        WindowWidthSizeClass.Medium -> {
            TariffProjectionsCardLTwoColumns(
                modifier = modifier,
                mpan = mpan,
                tariffSummary = tariffSummary,
                insights = insights,
            )
        }

        else -> {
            TariffProjectionsCardThreeColumns(
                modifier = modifier,
                mpan = mpan,
                tariffSummary = tariffSummary,
                insights = insights,
            )
        }
    }
}

@Composable
private fun TariffProjectionsCardLinear(
    modifier: Modifier = Modifier,
    mpan: String?,
    tariffSummary: TariffSummary?,
    insights: Insights?,
) {
    val dimension = LocalDensity.current.getDimension()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(space = dimension.grid_1),
    ) {
        tariffSummary?.let {
            TariffSummaryCardAdaptive(
                modifier = Modifier.fillMaxWidth(),
                layoutType = WindowWidthSizeClass.Compact,
                heading = mpan?.let { stringResource(resource = Res.string.account_mpan, mpan) } ?: stringResource(resource = Res.string.usage_current_tariff).uppercase(),
                headingTextAlign = TextAlign.Center,
                tariffSummary = it,
            )
        }

        insights?.let { insights ->
            InsightsCard(
                modifier = Modifier.fillMaxWidth(),
                insights = insights,
            )

            AnnualProjectionCardAdaptive(
                modifier = Modifier.fillMaxWidth(),
                insights = insights,
                layoutType = WindowWidthSizeClass.Expanded,
            )
        }
    }
}

@Composable
private fun TariffProjectionsCardLTwoColumns(
    modifier: Modifier = Modifier,
    mpan: String?,
    tariffSummary: TariffSummary?,
    insights: Insights?,
) {
    val dimension = LocalDensity.current.getDimension()

    Row(
        modifier = modifier.fillMaxWidth()
            .height(intrinsicSize = IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(space = dimension.grid_1),
    ) {
        insights?.let { insights ->
            Column(
                modifier = Modifier.weight(weight = 0.6f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(space = dimension.grid_1),
            ) {
                InsightsCard(
                    modifier = Modifier.weight(weight = 1f).fillMaxWidth(),
                    insights = insights,
                )

                AnnualProjectionCardAdaptive(
                    modifier = Modifier.weight(weight = 1f).fillMaxWidth(),
                    insights = insights,
                    layoutType = WindowWidthSizeClass.Compact,
                )
            }
        }

        tariffSummary?.let {
            TariffSummaryCardAdaptive(
                modifier = Modifier.weight(weight = 0.4f).fillMaxHeight(),
                layoutType = WindowWidthSizeClass.Compact,
                heading = mpan?.let { stringResource(resource = Res.string.account_mpan, mpan) } ?: stringResource(resource = Res.string.usage_current_tariff).uppercase(),
                headingTextAlign = TextAlign.Center,
                tariffSummary = it,
            )
        }
    }
}

@Composable
private fun TariffProjectionsCardThreeColumns(
    modifier: Modifier = Modifier,
    mpan: String?,
    tariffSummary: TariffSummary?,
    insights: Insights?,
) {
    val dimension = LocalDensity.current.getDimension()

    Row(
        modifier = modifier.fillMaxWidth()
            .height(intrinsicSize = IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(space = dimension.grid_1),
    ) {
        tariffSummary?.let {
            TariffSummaryCardAdaptive(
                modifier = Modifier.weight(weight = 1f).fillMaxHeight(),
                layoutType = WindowWidthSizeClass.Compact,
                heading = mpan?.let { stringResource(resource = Res.string.account_mpan, mpan) } ?: stringResource(resource = Res.string.usage_current_tariff).uppercase(),
                headingTextAlign = TextAlign.Center,
                tariffSummary = it,
            )
        }

        insights?.let { insights ->
            InsightsCard(
                modifier = Modifier.weight(weight = 1f).fillMaxHeight(),
                insights = insights,
            )

            AnnualProjectionCardAdaptive(
                modifier = Modifier.weight(weight = 1f).fillMaxHeight(),
                insights = insights,
                layoutType = WindowWidthSizeClass.Compact,
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    val insights = Insights(
        consumptionAggregateRounded = 85.115,
        consumptionTimeSpan = 303,
        roughCost = 90.988,
        consumptionDailyAverage = 32.611,
        costDailyAverage = 12.434,
        consumptionAnnualProjection = 30.235,
        costAnnualProjection = 11.487,
    )

    CommonPreviewSetup {
        TariffProjectionsCardAdaptive(
            modifier = Modifier.padding(all = 16.dp),
            tariffSummary = TariffSamples.agileFlex221125,
            insights = insights,
            layoutType = WindowWidthSizeClass.Expanded,
            mpan = "1200000123456",
        )

        TariffProjectionsCardAdaptive(
            modifier = Modifier.padding(all = 16.dp),
            tariffSummary = TariffSamples.agileFlex221125,
            insights = insights,
            layoutType = WindowWidthSizeClass.Medium,
            mpan = "1200000123456",
        )

        TariffProjectionsCardAdaptive(
            modifier = Modifier.padding(all = 16.dp),
            tariffSummary = TariffSamples.agileFlex221125,
            insights = insights,
            layoutType = WindowWidthSizeClass.Compact,
            mpan = "1200000123456",
        )
    }
}
