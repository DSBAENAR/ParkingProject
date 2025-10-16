package com.parking.core.payment.Requests;

import com.parking.core.enums.Currencies;

/**
 * Represents information about a product, including its description, price, currency, and tax rate.
 *
 * @param description A brief description of the product.
 * @param amount The price of the product in the smallest unit of the specified currency (e.g., cents for USD).
 * @param currency The currency in which the product's price is specified.
 * @param taxRateId The identifier for the tax rate applicable to the product.
 */
public record ProductInfo(
    String description,
    Long amount,
    Currencies currency,
    String taxRateId
) {}
