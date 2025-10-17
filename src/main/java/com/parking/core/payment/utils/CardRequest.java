package com.parking.core.payment.utils;

import com.parking.core.enums.Currencies;
import com.parking.core.payment.Requests.CustomerRequest;

/**
 * Represents a request to process a payment using a card.
 * This record encapsulates the necessary details for a card payment.
 *
 * @param customer The customer details associated with the card.
 * @param cvc The card verification code (CVC) for the card.
 * @param expMonth The expiration month of the card (1-12).
 * @param expYear The expiration year of the card (e.g., 2025).
 * @param number The card number as a string.
 * @param currency The currency in which the payment is to be processed.
 */
public record CardRequest(
    CustomerRequest customer,
    String cvc,
    Long expMonth,
    Long expYear,
    String number,
    Currencies currency

) {}
