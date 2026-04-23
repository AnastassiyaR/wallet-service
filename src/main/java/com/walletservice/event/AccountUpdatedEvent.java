package com.walletservice.event;


import java.math.BigDecimal;

public record AccountUpdatedEvent(
        Long accountId,
        String currency,
        BigDecimal newBalance
) {}
