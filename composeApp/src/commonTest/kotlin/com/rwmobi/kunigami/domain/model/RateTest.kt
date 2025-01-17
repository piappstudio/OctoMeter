/*
 * Copyright (c) 2024. Ryan Wong
 * https://github.com/ryanw-mobile
 * Sponsored by RW MobiMedia UK Limited
 *
 */

package com.rwmobi.kunigami.domain.model

import com.rwmobi.kunigami.domain.samples.RateSampleData
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test

class RateTest {

    private val timeZone = TimeZone.UTC

    @Test
    fun `isActive should return true when point of reference is within the valid period`() {
        val rate = RateSampleData.rateWithValidTo
        val pointOfReference = LocalDateTime(
            year = 2023,
            monthNumber = 5,
            dayOfMonth = 5,
            hour = 0,
            minute = 0,
            second = 0,
        ).toInstant(timeZone)

        rate.isActive(pointOfReference) shouldBe true
    }

    @Test
    fun `isActive should return false when point of reference is before the valid period`() {
        val rate = RateSampleData.rateWithValidTo
        val pointOfReference = LocalDateTime(
            year = 2023,
            monthNumber = 4,
            dayOfMonth = 30,
            hour = 23,
            minute = 59,
            second = 59,
        ).toInstant(timeZone)

        rate.isActive(pointOfReference) shouldBe false
    }

    @Test
    fun `isActive should return false when point of reference is after the valid period`() {
        val rate = RateSampleData.rateWithValidTo
        val pointOfReference = LocalDateTime(
            year = 2023,
            monthNumber = 5,
            dayOfMonth = 11,
            hour = 0,
            minute = 0,
            second = 0,
        ).toInstant(timeZone)

        rate.isActive(pointOfReference) shouldBe false
    }

    @Test
    fun `isActive should return true when validTo is null and point of reference is after validFrom`() {
        val rate = RateSampleData.rateWithoutValidTo
        val pointOfReference = LocalDateTime(
            year = 2023,
            monthNumber = 5,
            dayOfMonth = 5,
            hour = 0,
            minute = 0,
            second = 0,
        ).toInstant(timeZone)

        rate.isActive(pointOfReference) shouldBe true
    }

    @Test
    fun `isActive should return false when validTo is null and point of reference is before validFrom`() {
        val rate = RateSampleData.rateWithoutValidTo
        val pointOfReference = LocalDateTime(
            year = 2023,
            monthNumber = 4,
            dayOfMonth = 30,
            hour = 23,
            minute = 59,
            second = 59,
        ).toInstant(timeZone)

        rate.isActive(pointOfReference) shouldBe false
    }
}
