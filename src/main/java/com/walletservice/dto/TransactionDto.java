package com.walletservice.dto;


import com.walletservice.domain.Currency;
import com.walletservice.domain.Direction;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionDto {
    private Long transactionId;
    private Long accountId;
    private BigDecimal amount;
    private Currency currency;
    private Direction direction;
    private String description;
    private BigDecimal balanceAfter;
    private LocalDateTime createdAt;
}
