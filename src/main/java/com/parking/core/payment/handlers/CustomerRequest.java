package com.parking.core.payment.handlers;

public record CustomerRequest(
    String id,
    String name,
    String email,
    UserAddress address
) {

}
