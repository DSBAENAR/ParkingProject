package com.parking.core.payment.response;

import java.util.Map;

import com.parking.core.enums.Currencies;
import com.parking.core.payment.Requests.UserTax;

/**
 * Represents the response for an invoice in the payment system.
 * This record encapsulates the details of an invoice, including its identifier,
 * currency, and customer information.
 *
 * @param id       The unique identifier of the invoice.
 * @param currency The currency used in the invoice, represented by the {@link Currencies} enum.
 * @param customer The customer associated with the invoice.
 */
public record InvoiceResponse(
    String id,
    Currencies currency,
    String customer,
    UserTax tax,
    Map<String,Object> productInfo
) {}
