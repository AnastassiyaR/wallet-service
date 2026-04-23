package com.walletservice.event;


import java.math.BigDecimal;

public record TransactionCreatedEvent(
        Long transactionId,
        Long accountId,
        BigDecimal amount,
        String currency,
        String direction,
        BigDecimal balanceAfter
) {}
