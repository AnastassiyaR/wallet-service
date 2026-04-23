package com.walletservice.dto;


import com.walletservice.domain.Currency;
import com.walletservice.domain.Direction;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "Create transaction request")
public class CreateTransactionRequest {

    @NotNull(message = "Account ID is required")
    @Schema(description = "Account ID", example = "123")
    private Long accountId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Schema(description = "Transaction amount", example = "100.00")
    private BigDecimal amount;

    @NotNull(message = "Currency is required")
    private Currency currency;

    @NotNull(message = "Direction is required")
    private Direction direction;

    @NotBlank(message = "Description is required")
    @Schema(description = "Transaction description", example = "Test deposit")
    private String description;
}
