package com.parking.core.payment.Requests;

import java.math.BigDecimal;

/**
 * Represents the tax information associated with a user.
 * This record encapsulates details about the user's tax status, 
 * including whether the tax is active, the country and state of taxation, 
 * and the applicable tax percentage.
 *
 * @param active     Indicates whether the tax is currently active.
 * @param country    The country where the tax is applied.
 * @param percentage The percentage of the tax applied.
 * @param state      The state or region where the tax is applied.
 */
public record UserTax(
    Boolean active,
    String country,
    BigDecimal percentage,
    String state
) {}
