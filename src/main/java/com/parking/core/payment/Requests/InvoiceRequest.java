package com.parking.core.payment.Requests;

import com.parking.core.enums.Currencies;
import com.parking.core.model.Product;


/**
 * Represents a request to generate an invoice.
 *
 * @param customer The name or identifier of the customer requesting the invoice.
 * @param currency The currency in which the invoice will be issued.
 * @param product  The product for which the invoice is being requested.
 */
public record InvoiceRequest(
    String customer,
    Currencies currency,
    Product product
) {}
