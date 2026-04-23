package com.walletservice.domain;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Balance {
    private Long id;
    private Long accountId;
    private Currency currency;
    private BigDecimal availableAmount;
}
