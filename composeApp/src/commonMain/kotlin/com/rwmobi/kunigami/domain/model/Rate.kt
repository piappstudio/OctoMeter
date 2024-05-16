/*
 * Copyright (c) 2024. Ryan Wong
 * https://github.com/ryanw-mobile
 * Sponsored by RW MobiMedia UK Limited
 *
 */

package com.rwmobi.kunigami.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.datetime.Instant

@Immutable
data class Rate(
    val vatExclusivePrice: Double,
    val vatInclusivePrice: Double,
    val validFrom: Instant,
    val validTo: Instant?,
    val paymentMethod: PaymentMethod,
)
