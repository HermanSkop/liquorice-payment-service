package org.example.liquoricepaymentservice.services;

import org.example.liquoricepaymentservice.dtos.PaymentIntentDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderApiServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private OrderApiService orderApiService;

    @BeforeEach
    void setUp() {
        orderApiService = new OrderApiService(restTemplate);
        ReflectionTestUtils.setField(orderApiService, "orderServiceUrl", "http://order-service");
    }

    @Test
    void getPaymentIntentId_Success() {
        String orderId = "order123";
        String expectedIntentId = "pi_123";
        PaymentIntentDto mockResponse = new PaymentIntentDto("client_secret", expectedIntentId);

        when(restTemplate.getForObject(
                anyString(),
                eq(PaymentIntentDto.class),
                eq(orderId)
        )).thenReturn(mockResponse);

        String result = orderApiService.getPaymentIntentId(orderId);

        assertEquals(expectedIntentId, result);
    }
}