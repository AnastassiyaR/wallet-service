package com.walletservice;


import com.walletservice.config.RabbitConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class RabbitMQPublishingIntegrationTest extends BaseIntegrationTest {

    private static final String TEST_ACCOUNT_QUEUE = "test.account.events";
    private static final String TEST_TRANSACTION_QUEUE = "test.transaction.events";

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    void setUpQueues() {
        Queue accountQueue = new Queue(TEST_ACCOUNT_QUEUE, false, false, true);
        Queue transactionQueue = new Queue(TEST_TRANSACTION_QUEUE, false, false, true);

        rabbitAdmin.declareQueue(accountQueue);
        rabbitAdmin.declareQueue(transactionQueue);

        rabbitAdmin.declareBinding(BindingBuilder
                .bind(accountQueue)
                .to(new TopicExchange(RabbitConfig.ACCOUNT_EXCHANGE))
                .with("account.*"));

        rabbitAdmin.declareBinding(BindingBuilder
                .bind(transactionQueue)
                .to(new TopicExchange(RabbitConfig.TRANSACTION_EXCHANGE))
                .with("transaction.*"));

        rabbitAdmin.purgeQueue(TEST_ACCOUNT_QUEUE);
        rabbitAdmin.purgeQueue(TEST_TRANSACTION_QUEUE);
    }

    private Long createAccountAndGetId() {
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/accounts",
                Map.of("customerId", 99, "country", "EE", "currencies", List.of("EUR", "USD")),
                Map.class);
        return ((Number) response.getBody().get("accountId")).longValue();
    }

    private void createTransaction(long accountId, double amount, String currency,
                                   String direction, String description) {
        restTemplate.postForEntity(
                "/transactions",
                Map.of(
                        "accountId", accountId,
                        "amount", amount,
                        "currency", currency,
                        "direction", direction,
                        "description", description
                ),
                Map.class);
    }

    @Test
    void createAccount_publishesAccountCreatedEvent() {
        createAccountAndGetId();

        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
            Message message = rabbitTemplate.receive(TEST_ACCOUNT_QUEUE);
            assertThat(message).isNotNull();
            String body = new String(message.getBody());
            assertThat(body).contains("customerId");
            assertThat(body).contains("99");
        });
    }

    @Test
    void createTransaction_publishesTransactionCreatedEvent() {
        Long accountId = createAccountAndGetId();

        createTransaction(accountId, 150.00, "EUR", "IN", "test deposit");

        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
            Message message = rabbitTemplate.receive(TEST_TRANSACTION_QUEUE);
            assertThat(message).isNotNull();
            String body = new String(message.getBody());
            assertThat(body).contains("150");
            assertThat(body).contains("EUR");
            assertThat(body).contains("IN");
        });
    }

    @Test
    void createTransaction_publishesAccountUpdatedEvent() {
        Long accountId = createAccountAndGetId();
        rabbitAdmin.purgeQueue(TEST_ACCOUNT_QUEUE);

        createTransaction(accountId, 50.00, "EUR", "IN", "deposit");

        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
            Message message = rabbitTemplate.receive(TEST_ACCOUNT_QUEUE);
            assertThat(message).isNotNull();
            String body = new String(message.getBody());
            assertThat(body).contains(accountId.toString());
        });
    }
}
