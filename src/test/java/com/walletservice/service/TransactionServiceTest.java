package com.walletservice.service;

import com.walletservice.domain.*;
import com.walletservice.dto.CreateTransactionRequest;
import com.walletservice.dto.CreateTransactionResponse;
import com.walletservice.dto.TransactionDto;
import com.walletservice.exception.ErrorCode;
import com.walletservice.exception.NotFoundException;
import com.walletservice.exception.ValidationException;
import com.walletservice.repository.AccountRepository;
import com.walletservice.repository.BalanceRepository;
import com.walletservice.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private AccountRepository accountRepository;
    @Mock private BalanceRepository balanceRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TransactionService transactionService;

    private Account mockAccount() {
        return Account.builder().id(1L).customerId(1L).country("EE").build();
    }

    private Balance mockBalance(BigDecimal amount) {
        return Balance.builder()
                .accountId(1L)
                .currency(Currency.EUR)
                .availableAmount(amount)
                .build();
    }

    private CreateTransactionRequest mockRequest(Direction direction, BigDecimal amount) {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAccountId(1L);
        request.setAmount(amount);
        request.setCurrency(Currency.EUR);
        request.setDirection(direction);
        request.setDescription("test");
        return request;
    }

    @Test
    void createTransaction_IN_success() {
        when(accountRepository.getAccountById(1L)).thenReturn(mockAccount());

        when(balanceRepository.deposit(1L, Currency.EUR, new BigDecimal("100.00")))
                .thenReturn(mockBalance(new BigDecimal("150.00")));

        doAnswer(invocation -> {
            Transaction tx = invocation.getArgument(0);
            tx.setId(1L);
            return null;
        }).when(transactionRepository).insertTransaction(any(Transaction.class));

        CreateTransactionResponse response = transactionService.createTransaction(
                mockRequest(Direction.IN, new BigDecimal("100.00")));

        assertThat(response.getBalanceAfter()).isEqualByComparingTo("150.00");
        assertThat(response.getDirection()).isEqualTo(Direction.IN);
    }

    @Test
    void createTransaction_OUT_success() {
        when(accountRepository.getAccountById(1L)).thenReturn(mockAccount());

        when(balanceRepository.withdraw(1L, Currency.EUR, new BigDecimal("50.00")))
                .thenReturn(mockBalance(new BigDecimal("150.00")));

        doAnswer(invocation -> {
            Transaction tx = invocation.getArgument(0);
            tx.setId(2L);
            return null;
        }).when(transactionRepository).insertTransaction(any(Transaction.class));

        CreateTransactionResponse response = transactionService.createTransaction(
                mockRequest(Direction.OUT, new BigDecimal("50.00")));

        assertThat(response.getBalanceAfter()).isEqualByComparingTo("150.00");
        assertThat(response.getDirection()).isEqualTo(Direction.OUT);
    }

    @Test
    void createTransaction_accountNotFound_throwsNotFoundException() {
        when(accountRepository.getAccountById(999L)).thenReturn(null);

        CreateTransactionRequest request = mockRequest(Direction.IN, new BigDecimal("100.00"));
        request.setAccountId(999L);

        assertThatThrownBy(() -> transactionService.createTransaction(request))
                .isInstanceOf(NotFoundException.class)
                .extracting(e -> ((NotFoundException) e).getErrorCode())
                .isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND);
    }

    @Test
    void createTransaction_invalidCurrency_throwsValidationException() {
        when(accountRepository.getAccountById(1L)).thenReturn(mockAccount());

        when(balanceRepository.deposit(1L, Currency.EUR, new BigDecimal("100.00")))
                .thenReturn(null);

        assertThatThrownBy(() -> transactionService.createTransaction(
                mockRequest(Direction.IN, new BigDecimal("100.00"))))
                .isInstanceOf(ValidationException.class)
                .extracting(e -> ((ValidationException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_CURRENCY);
    }

    @Test
    void createTransaction_insufficientFunds_throwsValidationException() {
        when(accountRepository.getAccountById(1L)).thenReturn(mockAccount());

        when(balanceRepository.withdraw(1L, Currency.EUR, new BigDecimal("100.00")))
                .thenReturn(null);

        assertThatThrownBy(() -> transactionService.createTransaction(
                mockRequest(Direction.OUT, new BigDecimal("100.00"))))
                .isInstanceOf(ValidationException.class)
                .extracting(e -> ((ValidationException) e).getErrorCode())
                .isEqualTo(ErrorCode.INSUFFICIENT_FUNDS);
    }

    @Test
    void getTransactions_success() {
        when(accountRepository.getAccountById(1L)).thenReturn(mockAccount());
        when(transactionRepository.getTransactionsByAccountId(1L)).thenReturn(List.of(
                Transaction.builder()
                        .id(1L)
                        .accountId(1L)
                        .amount(new BigDecimal("100.00"))
                        .currency(Currency.EUR)
                        .direction(Direction.IN)
                        .description("deposit")
                        .balanceAfter(new BigDecimal("100.00"))
                        .createdAt(LocalDateTime.now())
                        .build()
        ));

        List<TransactionDto> result = transactionService.getTransactions(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAmount()).isEqualByComparingTo("100.00");
        assertThat(result.get(0).getDirection()).isEqualTo(Direction.IN);
    }

    @Test
    void getTransactions_accountNotFound_throwsNotFoundException() {
        when(accountRepository.getAccountById(999L)).thenReturn(null);

        assertThatThrownBy(() -> transactionService.getTransactions(999L))
                .isInstanceOf(NotFoundException.class)
                .extracting(e -> ((NotFoundException) e).getErrorCode())
                .isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND);
    }

    @Test
    void getTransactions_emptyList() {
        when(accountRepository.getAccountById(1L)).thenReturn(mockAccount());
        when(transactionRepository.getTransactionsByAccountId(1L)).thenReturn(List.of());

        List<TransactionDto> result = transactionService.getTransactions(1L);

        assertThat(result).isEmpty();
    }
}
