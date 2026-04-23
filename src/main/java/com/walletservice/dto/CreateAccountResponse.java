package com.walletservice.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
@Schema(description = "Create account response")
public class CreateAccountResponse {

    @Schema(description = "Account ID", example = "123")
    private Long accountId;

    @Schema(description = "Customer ID", example = "1")
    private Long customerId;

    @Schema(description = "List of balances")
    private List<BalanceDto> balances;
}
