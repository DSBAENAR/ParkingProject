package com.parking.core.payment;

public record CardResponse(
    Object customer,
    CardInfo card
) {

}
