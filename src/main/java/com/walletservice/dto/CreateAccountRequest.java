package com.walletservice.dto;


import com.walletservice.domain.Currency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "Create account request")
public class CreateAccountRequest {

    @NotNull(message = "Customer ID is required")
    @Schema(description = "Customer ID", example = "1")
    private Long customerId;

    @NotBlank(message = "Country is required")
    @Size(min = 2, max = 2, message = "Country must be 2 characters")
    @Schema(description = "Country code", example = "EE")
    private String country;

    @NotEmpty(message = "At least one currency is required")
    private List<Currency> currencies;
}
