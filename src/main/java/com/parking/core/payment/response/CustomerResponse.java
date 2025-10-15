package com.parking.core.payment.response;

import java.util.Map;

import com.parking.core.enums.Currencies;
import com.parking.core.payment.handlers.UserAddress;

public record CustomerResponse(
    String id,
    String object,
    UserAddress address,
    long balance,
    Currencies currency,
    long created,
    String email,
    Map<String,Object> metadata
) {
}
