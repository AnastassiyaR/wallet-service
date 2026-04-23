package com.walletservice.event;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.walletservice.exception.ErrorCode;
import com.walletservice.exception.EventSerializationException;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.walletservice.config.RabbitConfig.ACCOUNT_EXCHANGE;

@Component
@RequiredArgsConstructor
public class AccountEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public void publishAccountCreated(Long accountId, Long customerId, List<String> currencies) {
        var payload = Map.of(
                "accountId", accountId,
                "customerId", customerId,
                "currencies", currencies
        );
        publish("account.created", payload);
    }

    public void publishAccountUpdated(Long accountId, String currency, BigDecimal newBalance) {
        var payload = Map.of(
                "accountId", accountId,
                "currency", currency,
                "newBalance", newBalance
        );
        publish("account.updated", payload);
    }

    private void publish(String routingKey, Object payload) {
        try {
            String message = objectMapper.writeValueAsString(payload);
            rabbitTemplate.convertAndSend(ACCOUNT_EXCHANGE, routingKey, message);
        } catch (JsonProcessingException e) {
            throw new EventSerializationException(ErrorCode.UNEXPECTED_ERROR, "Failed to serialize event payload");
        }
    }
}
