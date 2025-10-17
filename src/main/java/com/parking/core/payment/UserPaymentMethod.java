package com.parking.core.payment;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.parking.core.payment.utils.CardInfo;



public record UserPaymentMethod(
    String id,
    Map<String,Object> billing_details,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    CardInfo cardinfo
) {

}
