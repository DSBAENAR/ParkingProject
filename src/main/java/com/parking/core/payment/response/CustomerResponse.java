package com.parking.core.payment.response;

import java.util.Map;

import com.parking.core.enums.Currencies;
import com.parking.core.payment.Requests.UserAddress;

/**
 * Represents the response for a customer in the payment system.
 * This record encapsulates various details about a customer, including
 * their identification, address, balance, and other metadata.
 *
 * @param id        The unique identifier for the customer.
 * @param object    The type of object, typically used to describe the entity.
 * @param address   The address details of the customer, represented by a {@link UserAddress}.
 * @param balance   The current balance of the customer in the smallest currency unit.
 * @param currency  The currency type associated with the customer's balance, represented by {@link Currencies}.
 * @param created   The timestamp (in seconds since epoch) when the customer was created.
 * @param email     The email address of the customer.
 * @param metadata  A map containing additional metadata about the customer.
 */
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
