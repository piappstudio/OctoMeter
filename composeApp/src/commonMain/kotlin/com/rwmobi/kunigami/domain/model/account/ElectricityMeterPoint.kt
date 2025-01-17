/*
 * Copyright (c) 2024. Ryan Wong
 * https://github.com/ryanw-mobile
 * Sponsored by RW MobiMedia UK Limited
 *
 */

package com.rwmobi.kunigami.domain.model.account

import androidx.compose.runtime.Immutable

@Immutable
data class ElectricityMeterPoint(
    val mpan: String,
    val meterSerialNumbers: List<String>,
    val currentAgreement: Agreement,
)
