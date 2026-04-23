package com.walletservice.controller;

import com.walletservice.dto.CreateTransactionRequest;
import com.walletservice.dto.CreateTransactionResponse;
import com.walletservice.dto.TransactionDto;
import com.walletservice.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Transaction management")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create transaction", description = "Creates a transaction on the account")
    public CreateTransactionResponse createTransaction(@Valid @RequestBody CreateTransactionRequest request) {
        return transactionService.createTransaction(request);
    }

    @GetMapping("/{accountId}")
    @Operation(summary = "Get transactions", description = "Returns list of transactions for account")
    public List<TransactionDto> getTransactions(@PathVariable Long accountId) {
        return transactionService.getTransactions(accountId);
    }
}
