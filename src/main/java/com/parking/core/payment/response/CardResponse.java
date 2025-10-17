package com.parking.core.payment.response;

import com.parking.core.payment.utils.CardInfo;

public record CardResponse(
    Object customer,
    CardInfo card
) {

}
