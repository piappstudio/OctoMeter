/*
 * Copyright (c) 2024. Ryan Wong
 * https://github.com/ryanw-mobile
 * Sponsored by RW MobiMedia UK Limited
 *
 */

@file:OptIn(ExperimentalLayoutApi::class)

package com.rwmobi.kunigami.ui.destinations.tariffs.components

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import com.rwmobi.kunigami.domain.model.product.ProductDirection
import com.rwmobi.kunigami.domain.model.product.ProductFeature
import com.rwmobi.kunigami.domain.model.product.ProductSummary
import com.rwmobi.kunigami.ui.components.CommonPreviewSetup
import com.rwmobi.kunigami.ui.components.TagWithIcon
import com.rwmobi.kunigami.ui.theme.getDimension
import kotlinx.datetime.Instant
import kunigami.composeapp.generated.resources.Res
import kunigami.composeapp.generated.resources.tariffs_fixed_term_months
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ProductItemAdaptive(
    modifier: Modifier = Modifier,
    productSummary: ProductSummary,
    useWideLayout: Boolean = false,
) {
    if (useWideLayout) {
        ProductItemWide(
            modifier = modifier,
            productSummary = productSummary,
        )
    } else {
        ProductItemCompact(
            modifier = modifier,
            productSummary = productSummary,
        )
    }
}

@Composable
private fun ProductItemCompact(
    modifier: Modifier = Modifier,
    productSummary: ProductSummary,
) {
    val dimension = LocalDensity.current.getDimension()

    Column(
        modifier = modifier.padding(
            vertical = dimension.grid_1,
            horizontal = dimension.grid_2,
        ),
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            text = productSummary.displayName,
        )

        if (productSummary.fullName != productSummary.displayName) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                style = MaterialTheme.typography.titleSmall,
                text = productSummary.fullName,
            )
        }

        Spacer(modifier = Modifier.size(size = dimension.grid_1))

        productSummary.term?.let {
            Text(
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium,
                text = stringResource(resource = Res.string.tariffs_fixed_term_months, it),
            )

            Spacer(modifier = Modifier.size(size = dimension.grid_1))
        }

        if (productSummary.features.isNotEmpty()) {
            val currentDensity = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(currentDensity.density, fontScale = 1f),
            ) {
                FlowRow(modifier = Modifier.padding(vertical = dimension.grid_1)) {
                    productSummary.features.forEach {
                        TagWithIcon(
                            modifier = Modifier.padding(end = dimension.grid_0_5),
                            icon = painterResource(resource = it.iconResource),
                            text = stringResource(resource = it.stringResource),
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.size(size = dimension.grid_1))

        Text(
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyMedium,
            text = productSummary.description,
        )
    }
}

@Composable
internal fun ProductItemWide(
    modifier: Modifier = Modifier,
    productSummary: ProductSummary,
) {
    val dimension = LocalDensity.current.getDimension()

    Row(
        modifier = modifier
            .height(intrinsicSize = IntrinsicSize.Min)
            .padding(
                vertical = dimension.grid_1,
                horizontal = dimension.grid_2,
            ),
        horizontalArrangement = Arrangement.spacedBy(space = dimension.grid_2),
    ) {
        Column(
            modifier = Modifier
                .weight(weight = 1f)
                .fillMaxHeight(),
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                text = productSummary.displayName,
            )

            if (productSummary.fullName != productSummary.displayName) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    style = MaterialTheme.typography.titleSmall,
                    text = productSummary.fullName,
                )
            }

            Spacer(modifier = Modifier.size(size = dimension.grid_1))

            productSummary.term?.let {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium,
                    text = stringResource(resource = Res.string.tariffs_fixed_term_months, it),
                )

                Spacer(modifier = Modifier.size(size = dimension.grid_1))
            }

            if (productSummary.features.isNotEmpty()) {
                val currentDensity = LocalDensity.current
                CompositionLocalProvider(
                    LocalDensity provides Density(currentDensity.density, fontScale = 1f),
                ) {
                    FlowRow(
                        modifier = Modifier.padding(vertical = dimension.grid_1),
                        verticalArrangement = Arrangement.spacedBy(space = dimension.grid_1),
                    ) {
                        productSummary.features.forEach {
                            TagWithIcon(
                                modifier = Modifier.padding(end = dimension.grid_0_5),
                                icon = painterResource(resource = it.iconResource),
                                text = stringResource(resource = it.stringResource),
                            )
                        }
                    }
                }
            }
        }

        Text(
            modifier = Modifier
                .weight(weight = 2f)
                .fillMaxHeight()
                .padding(all = dimension.grid_1),
            style = MaterialTheme.typography.bodyMedium,
            text = productSummary.description,
        )
    }
}

@Preview
@Composable
private fun ProductItemPreview() {
    CommonPreviewSetup {
        ProductItemAdaptive(
            modifier = Modifier.fillMaxWidth(),
            useWideLayout = false,
            productSummary = ProductSummary(
                code = "AGILE-24-04-03",
                direction = ProductDirection.IMPORT,
                fullName = "Agile Octopus April 2024 v1",
                displayName = "Agile Octopus",
                description = "With Agile Octopus, you get access to half-hourly energy prices, tied to wholesale prices and updated daily.  The unit rate is capped at 100p/kWh (including VAT).",
                features = listOf(ProductFeature.VARIABLE, ProductFeature.GREEN),
                term = 12,
                availableFrom = Instant.parse("2024-03-31T23:00:00Z"),
                availableTo = null,
                brand = "OCTOPUS_ENERGY",
            ),
        )

        ProductItemAdaptive(
            modifier = Modifier.fillMaxWidth(),
            useWideLayout = true,
            productSummary = ProductSummary(
                code = "AGILE-24-04-03",
                direction = ProductDirection.IMPORT,
                fullName = "Agile Octopus April 2024 v1",
                displayName = "Agile Octopus",
                description = "With Agile Octopus, you get access to half-hourly energy prices, tied to wholesale prices and updated daily.  The unit rate is capped at 100p/kWh (including VAT).",
                features = listOf(ProductFeature.VARIABLE, ProductFeature.GREEN),
                term = 12,
                availableFrom = Instant.parse("2024-03-31T23:00:00Z"),
                availableTo = null,
                brand = "OCTOPUS_ENERGY",
            ),
        )
    }
}
