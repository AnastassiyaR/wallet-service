package com.walletservice.service;


import com.walletservice.domain.Account;
import com.walletservice.domain.Balance;
import com.walletservice.domain.Currency;
import com.walletservice.dto.AccountDto;
import com.walletservice.dto.CreateAccountRequest;
import com.walletservice.dto.CreateAccountResponse;
import com.walletservice.dto.BalanceDto;
import com.walletservice.event.AccountCreatedEvent;
import com.walletservice.exception.ErrorCode;
import com.walletservice.exception.NotFoundException;
import com.walletservice.exception.ValidationException;
import com.walletservice.repository.AccountRepository;
import com.walletservice.repository.BalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final BalanceRepository balanceRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public CreateAccountResponse createAccount(CreateAccountRequest request) {

        Set<Currency> seen = new HashSet<>();
        for (Currency currency : request.getCurrencies()) {
            if (!seen.add(currency)) {
                throw new ValidationException(ErrorCode.INVALID_CURRENCY,
                        "Duplicate currency in request: " + currency);
            }
        }

        Account account = Account.builder()
                .customerId(request.getCustomerId())
                .country(request.getCountry())
                .build();

        accountRepository.saveAccount(account);

        List<Balance> balances = new ArrayList<>();
        for (Currency currency : request.getCurrencies()) {
            Balance balance = Balance.builder()
                    .accountId(account.getId())
                    .currency(currency)
                    .availableAmount(BigDecimal.ZERO)
                    .build();
            balanceRepository.insertBalance(balance);
            balances.add(balance);
        }

        account.setBalances(balances);
        eventPublisher.publishEvent(new AccountCreatedEvent(
                account.getId(),
                account.getCustomerId(),
                request.getCurrencies().stream().map(Enum::name).toList()
        ));

        return CreateAccountResponse.builder()
                .accountId(account.getId())
                .customerId(account.getCustomerId())
                .balances(balances.stream()
                        .map(b -> BalanceDto.builder()
                                .currency(b.getCurrency())
                                .availableAmount(b.getAvailableAmount())
                                .build())
                        .toList())
                .build();
    }

    public AccountDto getAccount(Long accountId) {
        Account account = accountRepository.getAccountById(accountId);
        if (account == null) {
            throw new NotFoundException(ErrorCode.ACCOUNT_NOT_FOUND, "Account not found: " + accountId);
        }

        List<Balance> balances = balanceRepository.getBalancesByAccountId(accountId);

        return AccountDto.builder()
                .accountId(account.getId())
                .customerId(account.getCustomerId())
                .balances(balances.stream()
                        .map(b -> BalanceDto.builder()
                                .currency(b.getCurrency())
                                .availableAmount(b.getAvailableAmount())
                                .build())
                        .toList())
                .build();
    }
}
