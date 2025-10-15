package com.parking.core.payment.response;

import java.util.Map;

import com.parking.core.enums.Currencies;
import com.parking.core.payment.Requests.UserAddress;

/**
 * Represents a response containing customer information.
 * This record encapsulates various details about a customer, such as their
 * ID, address, balance, currency, and other metadata.
 *
 * @param id        The unique identifier for the customer.
 * @param object    The type of object, typically used to describe the resource.
 * @param address   The customer's address information, represented by a {@link UserAddress}.
 * @param balance   The customer's current balance, in the smallest currency unit.
 * @param currency  The currency of the balance, represented by {@link Currencies}.
 * @param created   The timestamp (in seconds since epoch) when the customer was created.
 * @param email     The customer's email address.
 * @param metadata  A map of key-value pairs containing additional metadata about the customer.
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
