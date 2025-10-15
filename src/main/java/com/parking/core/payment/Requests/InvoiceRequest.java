package com.parking.core.payment.Requests;

import com.parking.core.enums.Currencies;

public record InvoiceRequest(
    String customer,
    Currencies currency
) {
}
