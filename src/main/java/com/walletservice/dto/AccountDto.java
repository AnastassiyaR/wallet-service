package com.walletservice.dto;


import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class AccountDto {
    private Long accountId;
    private Long customerId;
    private List<BalanceDto> balances;
}
