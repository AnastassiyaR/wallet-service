package com.walletservice.service;


import com.walletservice.domain.Account;
import com.walletservice.domain.Balance;
import com.walletservice.domain.Currency;
import com.walletservice.dto.AccountDto;
import com.walletservice.dto.CreateAccountRequest;
import com.walletservice.dto.CreateAccountResponse;
import com.walletservice.exception.ErrorCode;
import com.walletservice.exception.NotFoundException;
import com.walletservice.repository.AccountRepository;
import com.walletservice.repository.BalanceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    @Mock
    private BalanceRepository balanceRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    void createAccount_success() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setCustomerId(1L);
        request.setCountry("EE");
        request.setCurrencies(List.of(Currency.EUR, Currency.USD));

        doAnswer(invocation -> {
            Account acc = invocation.getArgument(0);
            acc.setId(1L);
            return null;
        }).when(accountRepository).saveAccount(any(Account.class));

        CreateAccountResponse response = accountService.createAccount(request);

        assertThat(response.getAccountId()).isEqualTo(1L);
        assertThat(response.getCustomerId()).isEqualTo(1L);
        assertThat(response.getBalances()).hasSize(2);
        assertThat(response.getBalances().get(0).getCurrency()).isEqualTo(Currency.EUR);
        assertThat(response.getBalances().get(0).getAvailableAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void createAccount_singleCurrency_success() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setCustomerId(2L);
        request.setCountry("LV");
        request.setCurrencies(List.of(Currency.SEK));

        doAnswer(invocation -> {
            Account acc = invocation.getArgument(0);
            acc.setId(2L);
            return null;
        }).when(accountRepository).saveAccount(any(Account.class));

        CreateAccountResponse response = accountService.createAccount(request);

        assertThat(response.getBalances()).hasSize(1);
        assertThat(response.getBalances().get(0).getCurrency()).isEqualTo(Currency.SEK);
    }

    @Test
    void getAccount_success() {
        Account account = Account.builder()
                .id(1L)
                .customerId(1L)
                .country("EE")
                .balances(List.of(Balance.builder()
                        .currency(Currency.EUR)
                        .availableAmount(new BigDecimal("100.00"))
                        .build()
                ))
                .build();

        when(accountRepository.getAccountById(1L)).thenReturn(account);
        when(balanceRepository.getBalancesByAccountId(1L))
                .thenReturn(account.getBalances());

        AccountDto dto = accountService.getAccount(1L);

        assertThat(dto.getAccountId()).isEqualTo(1L);
        assertThat(dto.getCustomerId()).isEqualTo(1L);
        assertThat(dto.getBalances()).hasSize(1);
        assertThat(dto.getBalances().get(0).getCurrency()).isEqualTo(Currency.EUR);
        assertThat(dto.getBalances().get(0).getAvailableAmount()).isEqualByComparingTo("100.00");
    }

    @Test
    void getAccount_notFound_throwsNotFoundException() {
        when(accountRepository.getAccountById(999L)).thenReturn(null);

        assertThatThrownBy(() -> accountService.getAccount(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Account not found")
                .extracting(e -> ((NotFoundException) e).getErrorCode())
                .isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND);
    }
}
