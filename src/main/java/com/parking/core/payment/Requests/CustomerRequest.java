package com.parking.core.payment.Requests;

public record CustomerRequest(
    String id,
    String name,
    String email,
    UserAddress address
) {

}
