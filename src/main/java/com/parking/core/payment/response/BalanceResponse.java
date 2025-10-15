package com.parking.core.payment.response;

import java.util.List;

/**
 * Represents a response containing balance information.
 *
 * @param <T> The type of the elements in the lists.
 * @param available A list of available balances.
 * @param object A string representing the type of the object.
 * @param connect_reserved A list of balances reserved for connected accounts.
 * @param livemode A string indicating whether the response is in live mode or test mode.
 * @param pending A list of pending balances.
 */
public record BalanceResponse<T>(
    List<T> available,
    String object,
    List<T> connect_reserved,
    String livemode,
    List<T> pending
) {}
