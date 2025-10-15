package com.parking.core.payment.Requests;

import com.parking.core.enums.Currencies;

/**
 * Represents a request to create an invoice.
 *
 * @param customer The name or identifier of the customer for whom the invoice is being created.
 * @param currency The currency in which the invoice will be issued, represented by the {@link Currencies} enum.
 */
public record InvoiceRequest(
    String customer,
    Currencies currency
) {}
