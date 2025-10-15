package com.parking.core.payment.response;

import java.util.List;

public record BalanceResponse<T>(
    List<T> available,
    String object,
    List<T> connect_reserved,
    String livemode,
    List<T> pending
) {}
