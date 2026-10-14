        package com.example.expo.Client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class MoyasarClient {

    private final RestClient restClient;
    private final String secretKey;

    public MoyasarClient(
            @Value("${moyasar.secret-key:}") String secretKey) {

        this.secretKey = secretKey;

        this.restClient = RestClient.builder()
                .baseUrl("https://api.moyasar.com/v1")
                .build();
    }

    public String verifyPayment(String paymentId) {

        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException(
                    "Moyasar is disabled"
            );
        }

        return restClient.get()
                .uri("/payments/{paymentId}", paymentId)
                .headers(headers ->
                        headers.setBasicAuth(secretKey, ""))
                .retrieve()
                .body(String.class);
    }
}
