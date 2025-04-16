package org.example.liquoricepaymentservice.services;

import lombok.RequiredArgsConstructor;
import org.example.liquoricepaymentservice.dtos.PaymentIntentDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class OrderApiService {
    private final RestTemplate restTemplate;

    @Value("${services.order-service.url}")
    private String orderServiceUrl;

    /**
     * Fetches the payment intent ID associated with an order from the order service
     * @param orderId The ID of the order
     * @return The payment intent ID
     */
    public String getPaymentIntentId(String orderId) {
        return Objects.requireNonNull(restTemplate.getForObject(
                orderServiceUrl + "/api/v1/orders/{orderId}/payment-intent",
                PaymentIntentDto.class,
                orderId
        )).getIntentId();
    }
}
