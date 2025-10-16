package com.parking.core.payment.Requests;

import jakarta.persistence.Embeddable;

/**
 * Represents a user's address with details such as country, city, and address line.
 *
 * @param country The country where the user resides.
 * @param city The city where the user resides.
 * @param line1 The first line of the user's address (e.g., street address or PO box).
 */

@Embeddable
public record UserAddress(
    String country,
    String city,
    String line1
) {}
