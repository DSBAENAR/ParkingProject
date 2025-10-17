package com.parking.core.payment.utils;

import com.parking.core.enums.Currencies;
import com.parking.core.payment.Requests.CustomerRequest;
import com.parking.core.payment.Requests.UserAddress;

public record CardRequest(
    CustomerRequest customer,
    String cvc,
    Long expMonth,
    Long expYear,
    String number,
    Currencies currency


    

) {}
