/*
 * Copyright (c) 2024. Ryan Wong
 * https://github.com/ryanw-mobile
 * Sponsored by RW MobiMedia UK Limited
 *
 */

package com.rwmobi.kunigami.domain.usecase

import com.rwmobi.kunigami.domain.exceptions.except
import com.rwmobi.kunigami.domain.model.product.TariffSummary
import com.rwmobi.kunigami.domain.repository.RestApiRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

class GetTariffRatesUseCase(
    private val restApiRepository: RestApiRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    suspend operator fun invoke(
        productCode: String,
        tariffCode: String,
    ): Result<TariffSummary> {
        return withContext(dispatcher) {
            runCatching {
                restApiRepository.getSimpleProductTariff(
                    productCode = productCode,
                    tariffCode = tariffCode,
                ).fold(
                    onSuccess = { tariff ->
                        tariff
                    },
                    onFailure = { throwable ->
                        throw throwable
                    },
                )
            }.except<CancellationException, _>()
        }
    }
}
