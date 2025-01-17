/*
 * Copyright (c) 2024. Ryan Wong
 * https://github.com/ryanw-mobile
 * Sponsored by RW MobiMedia UK Limited
 *
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package com.rwmobi.kunigami.ui.components.koalaplot

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rwmobi.kunigami.ui.extensions.getPercentageColorIndex
import com.rwmobi.kunigami.ui.theme.getDimension
import io.github.koalaplot.core.ChartLayout
import io.github.koalaplot.core.bar.DefaultVerticalBar
import io.github.koalaplot.core.bar.VerticalBarPlot
import io.github.koalaplot.core.bar.VerticalBarPlotEntry
import io.github.koalaplot.core.style.LineStyle
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.util.toString
import io.github.koalaplot.core.xygraph.AxisStyle
import io.github.koalaplot.core.xygraph.CategoryAxisModel
import io.github.koalaplot.core.xygraph.DoubleLinearAxisModel
import io.github.koalaplot.core.xygraph.TickPosition
import io.github.koalaplot.core.xygraph.XYGraph
import io.github.koalaplot.core.xygraph.XYGraphScope
import io.github.koalaplot.core.xygraph.rememberAxisStyle

@OptIn(ExperimentalKoalaPlotApi::class, ExperimentalComposeUiApi::class)
@Composable
fun VerticalBarChart(
    modifier: Modifier,
    showToolTipOnClick: Boolean,
    colorPalette: List<Color>,
    entries: List<VerticalBarPlotEntry<Int, Double>>,
    yAxisRange: ClosedFloatingPointRange<Double>,
    labelGenerator: (index: Int) -> String?,
    tooltipGenerator: (index: Int) -> String,
    xAxisTitle: String? = null,
    yAxisTitle: String? = null,
    backgroundPlot: @Composable ((scope: XYGraphScope<Int, Double>) -> Unit)? = null,
) {
    val dimension = LocalDensity.current.getDimension()

    Box {
        var showTooltipIndex: Int? by remember { mutableStateOf(null) }
        ChartLayout(modifier = modifier) {
            XYGraph(
                panZoomEnabled = false,
                xAxisModel = CategoryAxisModel(
                    categories = entries.indices.toList(),
                    firstCategoryIsZero = false, // true means first column cut into half
                ),
                xAxisLabels = { index ->
                    labelGenerator(index)?.let { label ->
                        AxisLabel(
                            modifier = Modifier.padding(horizontal = dimension.grid_1),
                            label = label,
                        )
                    }
                },
                xAxisStyle = AxisStyle(
                    color = MaterialTheme.colorScheme.onBackground.copy(
                        alpha = 0.25f,
                    ),
                    majorTickSize = 4.dp,
                    tickPosition = TickPosition.Outside,
                    labelRotation = 90,
                ),
                xAxisTitle = {
                    xAxisTitle?.let {
                        XAxisTitle(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = dimension.grid_1),
                            title = it,
                        )
                    }
                },
                verticalMajorGridLineStyle = null,
                verticalMinorGridLineStyle = null,

                yAxisModel = DoubleLinearAxisModel(
                    range = yAxisRange,
                    minimumMajorTickIncrement = 0.1,
                    minorTickCount = 4,
                    allowZooming = false,
                    allowPanning = false,
                ),
                yAxisStyle = rememberAxisStyle(
                    tickPosition = TickPosition.Outside,
                ),
                yAxisLabels = {
                    AxisLabel(
                        modifier = Modifier.absolutePadding(right = dimension.grid_0_25),
                        label = it.toString(precision = 1),
                    )
                },
                yAxisTitle = {
                    yAxisTitle?.let {
                        YAxisTitle(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(end = dimension.grid_1),
                            title = it,
                        )
                    }
                },
                horizontalMajorGridLineStyle = LineStyle(
                    brush = SolidColor(MaterialTheme.colorScheme.onBackground),
                    strokeWidth = 1.dp,
                    pathEffect = null,
                    alpha = 0.5f,
                    colorFilter = null,
                    blendMode = DrawScope.DefaultBlendMode,
                ),
                horizontalMinorGridLineStyle = LineStyle(
                    brush = SolidColor(MaterialTheme.colorScheme.onBackground),
                    strokeWidth = 1.dp,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f), // Configure dashed pattern
                    alpha = 0.25f,
                    colorFilter = null,
                    blendMode = DrawScope.DefaultBlendMode,
                ),
            ) {
                backgroundPlot?.let { it(this) }

                VerticalBarPlot(
                    data = entries,
                    barWidth = 0.8f,
                    bar = { index ->
                        DefaultVerticalBar(
                            modifier = Modifier
                                .fillMaxWidth()
                                .pointerInput(Unit) {
                                    if (showToolTipOnClick) {
                                        detectTapGestures(
                                            onPress = { _ ->
                                                showTooltipIndex = index
                                                try {
                                                    awaitRelease()
                                                } finally {
                                                    if (showTooltipIndex == index) {
                                                        showTooltipIndex = null
                                                    }
                                                }
                                            },
                                        )
                                    }
                                },
                            brush = SolidColor(
                                colorPalette[
                                    entries[index].y.yMax.getPercentageColorIndex(
                                        maxValue = yAxisRange.endInclusive,
                                    ),
                                ],
                            ),
                            shape = if (entries[index].y.yMin >= 0) {
                                RoundedCornerShape(
                                    topStart = 8.dp,
                                    topEnd = 8.dp,
                                    bottomEnd = 0.dp,
                                    bottomStart = 0.dp,
                                )
                            } else {
                                RoundedCornerShape(
                                    topStart = 0.dp,
                                    topEnd = 0.dp,
                                    bottomEnd = 8.dp,
                                    bottomStart = 8.dp,
                                )
                            },
                            hoverElement = {
                                RichTooltip {
                                    Text(
                                        modifier = Modifier.padding(all = dimension.grid_0_25),
                                        style = MaterialTheme.typography.labelMedium,
                                        textAlign = TextAlign.Center,
                                        text = tooltipGenerator(index),
                                    )
                                }
                            },
                        )
                    },
                )
            }
        }

        showTooltipIndex?.let { index ->
            RichTooltip(
                modifier = Modifier
                    .align(alignment = Alignment.TopCenter)
                    .offset(y = dimension.grid_2),
            ) {
                Text(
                    modifier = Modifier.padding(all = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    text = tooltipGenerator(index),
                )
            }
        }
    }
}
