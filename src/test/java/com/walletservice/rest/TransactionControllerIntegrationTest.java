package com.walletservice.rest;


import com.walletservice.BaseIntegrationTest;
import com.walletservice.exception.ErrorCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private Long accountId;

    @BeforeEach
    void setUp() {
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/accounts",
                Map.of("customerId", 1,
                        "country", "EE",
                        "currencies", List.of("EUR", "USD")
                ),
                Map.class);
        Assertions.assertNotNull(response.getBody());
        accountId = ((Number) response.getBody().get("accountId")).longValue();
    }

    private ResponseEntity<Map> createTransaction(long accountId,
                                                  double amount,
                                                  String direction,
                                                  String description) {
        return restTemplate.postForEntity(
                "/transactions",
                Map.of("accountId", accountId,
                        "amount", amount,
                        "currency", "EUR",
                        "direction", direction,
                        "description", description
                ),
                Map.class);
    }

    @Test
    void createTransaction_IN_success() {
        ResponseEntity<Map> response = createTransaction(accountId, 100.00, "IN", "deposit");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().get("transactionId")).isNotNull();
        assertThat(response.getBody().get("balanceAfter")).isEqualTo(100.0);
        assertThat(response.getBody().get("direction")).isEqualTo("IN");
    }

    @Test
    void createTransaction_OUT_success() {
        createTransaction(accountId, 200.00, "IN", "deposit");

        ResponseEntity<Map> response = createTransaction(accountId, 50.00, "OUT", "withdrawal");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().get("balanceAfter")).isEqualTo(150.0);
        assertThat(response.getBody().get("direction")).isEqualTo("OUT");
    }

    @Test
    void createTransaction_insufficientFunds_returns400() {
        ResponseEntity<Map> response = createTransaction(accountId, 99999.00, "OUT", "too much");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("errorCode")).isEqualTo(ErrorCode.INSUFFICIENT_FUNDS.name());
    }

    @Test
    void createTransaction_accountNotFound_returns404() {
        ResponseEntity<Map> response = createTransaction(999999L, 100.00, "IN", "test");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().get("errorCode")).isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND.name());
    }

    @Test
    void createTransaction_missingDescription_returns400() {
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/transactions",
                Map.of("accountId", accountId,
                        "amount", 100.00,
                        "currency", "EUR",
                        "direction", "IN"),
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createTransaction_invalidAmount_returnsBadRequest() {
        ResponseEntity<Map> response = createTransaction(accountId, -10.00, "IN", "test");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getTransactions_success() {
        createTransaction(accountId, 100.00, "IN", "deposit");

        ResponseEntity<List> response = restTemplate.getForEntity("/transactions/" + accountId, List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void getTransactions_emptyList_returnsEmpty() {
        ResponseEntity<List> response = restTemplate.getForEntity("/transactions/" + accountId, List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void getTransactions_accountNotFound_throwsNotFound() {
        ResponseEntity<Map> response = restTemplate.getForEntity("/transactions/999999", Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
