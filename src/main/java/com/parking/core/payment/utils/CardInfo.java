package com.parking.core.payment.utils;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CardInfo(
    String brand,
    String type,
    String country,
    Integer expMonth,
    Integer expYear,
    String funding,
    String last4,
    Object checks

) {
    
}
