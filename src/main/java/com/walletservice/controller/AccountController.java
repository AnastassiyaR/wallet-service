package com.walletservice.controller;


import com.walletservice.dto.AccountDto;
import com.walletservice.dto.CreateAccountRequest;
import com.walletservice.dto.CreateAccountResponse;
import com.walletservice.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Account management")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create account", description = "Creates a bank account with balances for given currencies")
    public CreateAccountResponse createAccount(@Valid @RequestBody CreateAccountRequest request) {
        return accountService.createAccount(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get account", description = "Returns account with balances")
    public AccountDto getAccount(@PathVariable Long id) {
        return accountService.getAccount(id);
    }
}
