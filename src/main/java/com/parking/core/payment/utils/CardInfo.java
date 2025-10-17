package com.parking.core.payment.utils;


import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Represents information about a payment card.
 * This record encapsulates various details about a card, such as its brand,
 * type, expiration date, and other relevant attributes.
 *
 * @param brand    The brand of the card (e.g., Visa, MasterCard).
 * @param type     The type of the card (e.g., credit, debit).
 * @param country  The country where the card was issued.
 * @param expMonth The expiration month of the card (1-12).
 * @param expYear  The expiration year of the card.
 * @param funding  The funding type of the card (e.g., prepaid, credit).
 * @param last4    The last 4 digits of the card number.
 * @param checks   Additional checks or validations associated with the card.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CardInfo(
    String brand,
    String type,
    String country,
    Integer expMonth,
    Integer expYear,
    String funding,
    String last4,
    Object checks

) {}
