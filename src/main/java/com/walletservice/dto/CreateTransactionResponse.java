package com.walletservice.dto;


import com.walletservice.domain.Currency;
import com.walletservice.domain.Direction;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "Create transaction response")
public class CreateTransactionResponse {
    private Long transactionId;
    private Long accountId;
    private BigDecimal amount;
    private Currency currency;
    private Direction direction;
    private String description;
    private BigDecimal balanceAfter;
}
