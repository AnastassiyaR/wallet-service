package com.walletservice.event;


import java.util.List;

public record AccountCreatedEvent(
        Long accountId,
        Long customerId,
        List<String> currencies
) {}
