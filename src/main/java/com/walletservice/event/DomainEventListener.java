package com.walletservice.event;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class DomainEventListener {

    private final AccountEventPublisher accountEventPublisher;
    private final TransactionEventPublisher transactionEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(AccountCreatedEvent e) {
        accountEventPublisher.publishAccountCreated(
                e.accountId(),
                e.customerId(),
                e.currencies()
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(AccountUpdatedEvent e) {
        accountEventPublisher.publishAccountUpdated(
                e.accountId(),
                e.currency(),
                e.newBalance()
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(TransactionCreatedEvent e) {
        transactionEventPublisher.publishTransactionCreated(
                e.transactionId(),
                e.accountId(),
                e.amount(),
                e.currency(),
                e.direction(),
                e.balanceAfter()
        );
    }
}
