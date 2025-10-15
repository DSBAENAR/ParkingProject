package com.parking.core.payment.response;

import com.parking.core.enums.Currencies;

public record InvoiceResponse(
    String id,
    Currencies currency,
    String customer
) {}
