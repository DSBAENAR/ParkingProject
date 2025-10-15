package com.parking.core.payment.Requests;

public record UserAddress(
    String country,
    String city,
    String line1
) {
}
