/*
 * Copyright (c) 2024. Ryan Wong
 * https://github.com/ryanw-mobile
 * Sponsored by RW MobiMedia UK Limited
 *
 */

package com.rwmobi.kunigami.ui.model.consumption

data class Insights(
    val consumptionAggregateRounded: Double,
    val consumptionTimeSpan: Int,
    val roughCost: Double,
    val consumptionDailyAverage: Double,
    val costDailyAverage: Double,
    val consumptionAnnualProjection: Double,
    val costAnnualProjection: Double,
)
