package com.walletservice.service;


import com.walletservice.domain.Account;
import com.walletservice.domain.Balance;
import com.walletservice.domain.Direction;
import com.walletservice.domain.Transaction;
import com.walletservice.dto.CreateTransactionRequest;
import com.walletservice.dto.CreateTransactionResponse;
import com.walletservice.dto.TransactionDto;
import com.walletservice.event.AccountUpdatedEvent;
import com.walletservice.event.TransactionCreatedEvent;
import com.walletservice.exception.ErrorCode;
import com.walletservice.exception.NotFoundException;
import com.walletservice.exception.ValidationException;
import com.walletservice.repository.AccountRepository;
import com.walletservice.repository.BalanceRepository;
import com.walletservice.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountRepository accountRepository;
    private final BalanceRepository balanceRepository;
    private final TransactionRepository transactionRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public CreateTransactionResponse createTransaction(CreateTransactionRequest request) {

        Account account = accountRepository.getAccountById(request.getAccountId());
        if (account == null) {
            throw new NotFoundException(ErrorCode.ACCOUNT_NOT_FOUND, "Account not found: " + request.getAccountId());
        }

        Balance updated;
        if (request.getDirection() == Direction.IN) {
            updated = balanceRepository.deposit(
                    request.getAccountId(),
                    request.getCurrency(),
                    request.getAmount()
            );
        } else if (request.getDirection() == Direction.OUT) {
            updated = balanceRepository.withdraw(
                    request.getAccountId(),
                    request.getCurrency(),
                    request.getAmount()
            );
            if (updated == null) {
                throw new ValidationException(ErrorCode.INSUFFICIENT_FUNDS, "Insufficient funds");
            }
        } else {
            throw new ValidationException(ErrorCode.INVALID_DIRECTION,
                    "Invalid direction: " + request.getDirection());
        }

        if (updated == null) {
            throw new ValidationException(ErrorCode.INVALID_CURRENCY,
                    "Invalid currency for this account");
        }

        Transaction tx = Transaction.builder()
                .accountId(request.getAccountId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .direction(request.getDirection())
                .description(request.getDescription())
                .balanceAfter(updated.getAvailableAmount())
                .build();

        transactionRepository.insertTransaction(tx);

        eventPublisher.publishEvent(new AccountUpdatedEvent(
                request.getAccountId(),
                request.getCurrency().name(),
                updated.getAvailableAmount()
        ));
        eventPublisher.publishEvent(new TransactionCreatedEvent(
                tx.getId(),
                tx.getAccountId(),
                tx.getAmount(),
                tx.getCurrency().name(),
                tx.getDirection().name(),
                tx.getBalanceAfter()
        ));

        return CreateTransactionResponse.builder()
                .transactionId(tx.getId())
                .accountId(tx.getAccountId())
                .amount(tx.getAmount())
                .currency(tx.getCurrency())
                .direction(tx.getDirection())
                .description(tx.getDescription())
                .balanceAfter(tx.getBalanceAfter())
                .build();
    }

    public List<TransactionDto> getTransactions(Long accountId) {
        Account account = accountRepository.getAccountById(accountId);

        if (account == null) {
            throw new NotFoundException(ErrorCode.ACCOUNT_NOT_FOUND, "Account not found: " + accountId);
        }

        return transactionRepository.getTransactionsByAccountId(accountId).stream()
                .map(tx -> TransactionDto.builder()
                        .transactionId(tx.getId())
                        .accountId(tx.getAccountId())
                        .amount(tx.getAmount())
                        .currency(tx.getCurrency())
                        .direction(tx.getDirection())
                        .description(tx.getDescription())
                        .balanceAfter(tx.getBalanceAfter())
                        .createdAt(tx.getCreatedAt())
                        .build())
                .toList();
    }
}
