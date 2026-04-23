package com.walletservice.event;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.walletservice.exception.ErrorCode;
import com.walletservice.exception.EventSerializationException;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

import static com.walletservice.config.RabbitConfig.TRANSACTION_EXCHANGE;

@Component
@RequiredArgsConstructor
public class TransactionEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public void publishTransactionCreated(
            Long transactionId,
            Long accountId,
            BigDecimal amount,
            String currency,
            String direction,
            BigDecimal balanceAfter
    ) {
        var payload = Map.of(
                "transactionId", transactionId,
                "accountId", accountId,
                "amount", amount,
                "currency", currency,
                "direction", direction,
                "balanceAfter", balanceAfter
        );

        publish("transaction.created", payload);
    }

    private void publish(String routingKey, Object payload) {
        try {
            String message = objectMapper.writeValueAsString(payload);
            rabbitTemplate.convertAndSend(TRANSACTION_EXCHANGE, routingKey, message);
        } catch (JsonProcessingException e) {
            throw new EventSerializationException(ErrorCode.UNEXPECTED_ERROR, "Failed to serialize transaction event");
        }
    }
}
