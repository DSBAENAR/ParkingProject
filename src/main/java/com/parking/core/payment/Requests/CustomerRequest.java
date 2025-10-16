package com.parking.core.payment.Requests;


/**
 * Represents a request containing customer information.
 * This record is used to encapsulate the details of a customer.
 *
 * @param id      The unique identifier of the customer.
 * @param name    The name of the customer.
 * @param email   The email address of the customer.
 * @param address The address of the customer, represented by a {@link UserAddress}.
 */
public record CustomerRequest(
    String id,
    String name,
    String email,
    UserAddress address
) {}
