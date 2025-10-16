package com.parking.core.payment.Requests;

import java.math.BigDecimal;

public record UserTax(
    Boolean active,
    String country,
    BigDecimal percentage,
    String state
) {}
