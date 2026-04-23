package com.walletservice.rest;


import com.walletservice.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AccountControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private ResponseEntity<Map> createAccount(Object body) {
        return restTemplate.postForEntity("/accounts", body, Map.class);
    }

    @Test
    void createAccount_success() {
        ResponseEntity<Map> response = createAccount(
                Map.of("customerId", 1, "country", "EE", "currencies", List.of("EUR", "USD")));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).containsKey("accountId");
        assertThat(response.getBody()).containsKey("customerId");
        assertThat(response.getBody()).containsKey("balances");
        assertThat((List<?>) response.getBody().get("balances")).hasSize(2);
    }

    @Test
    void createAccount_invalidCurrency_returnsBadRequest() {
        ResponseEntity<Map> response = createAccount(
                Map.of("customerId", 1, "country", "EE", "currencies", List.of("XXX")));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createAccount_missingFields_returnsBadRequest() {
        ResponseEntity<Map> response = createAccount(Map.of());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsKey("errorCode");
    }

    @Test
    void getAccount_success() {
        ResponseEntity<Map> created = createAccount(
                Map.of("customerId", 2, "country", "LV", "currencies", List.of("EUR")));
        Long accountId = ((Number) created.getBody().get("accountId")).longValue();

        ResponseEntity<Map> response = restTemplate.getForEntity("/accounts/" + accountId, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("accountId")).isNotNull();
        assertThat(response.getBody().get("customerId")).isNotNull();
    }

    @Test
    void getAccount_notFound_returnsNotFound() {
        ResponseEntity<Map> response = restTemplate.getForEntity("/accounts/999999", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().get("errorCode")).isEqualTo("ACCOUNT_NOT_FOUND");
    }

    @Test
    void createAccount_duplicateCurrency_returnsBadRequest() {
        ResponseEntity<Map> response = createAccount(
                Map.of("customerId", 1, "country", "EE", "currencies", List.of("EUR", "EUR")));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("errorCode")).isEqualTo("INVALID_CURRENCY");
        assertThat(response.getBody().get("message").toString()).contains("Duplicate currency");
    }
}
