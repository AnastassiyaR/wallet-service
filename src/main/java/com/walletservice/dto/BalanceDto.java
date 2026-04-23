package com.walletservice.dto;


import com.walletservice.domain.Currency;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class BalanceDto {
    private Currency currency;
    private BigDecimal availableAmount;
}
