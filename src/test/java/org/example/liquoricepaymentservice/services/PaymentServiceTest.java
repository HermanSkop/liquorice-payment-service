package org.example.liquoricepaymentservice.services;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import org.example.liquoricepaymentservice.models.Outbox;
import org.example.liquoricepaymentservice.repositories.OutboxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private OutboxRepository outboxRepository;

    @Mock
    private OrderApiService orderApiService;

    @Mock
    private PaymentIntent mockPaymentIntent;

    @Mock
    private Refund mockRefund;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(outboxRepository, orderApiService);
        ReflectionTestUtils.setField(paymentService, "paidTopic", "paid-orders");
    }

    @Test
    void createPaymentIntent_Success() throws StripeException {
        try (MockedStatic<PaymentIntent> mockedStatic = mockStatic(PaymentIntent.class)) {
            when(PaymentIntent.create(anyMap())).thenReturn(mockPaymentIntent);

            PaymentIntent result = paymentService.createPaymentIntent(1000);

            assertEquals(mockPaymentIntent, result);
            
            ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
            mockedStatic.verify(() -> PaymentIntent.create(paramsCaptor.capture()));
            
            Map<String, Object> capturedParams = paramsCaptor.getValue();
            assertEquals(1000, capturedParams.get("amount"));
            assertEquals("usd", capturedParams.get("currency"));
            assertTrue(((Map<?, ?>)capturedParams.get("automatic_payment_methods")).containsKey("enabled"));
        }
    }

    @Test
    void completePayment_Success() throws StripeException {
        try (MockedStatic<PaymentIntent> mockedStatic = mockStatic(PaymentIntent.class)) {
            String orderId = "order123";
            String paymentIntentId = "pi_123";
            when(orderApiService.getPaymentIntentId(orderId)).thenReturn(paymentIntentId);
            mockedStatic.when(() -> PaymentIntent.retrieve(paymentIntentId)).thenReturn(mockPaymentIntent);
            when(mockPaymentIntent.getStatus()).thenReturn("succeeded");
            when(outboxRepository.save(any(Outbox.class))).thenAnswer(i -> i.getArgument(0));

            paymentService.completePayment(orderId);

            ArgumentCaptor<Outbox> outboxCaptor = ArgumentCaptor.forClass(Outbox.class);
            verify(outboxRepository).save(outboxCaptor.capture());

            Outbox capturedOutbox = outboxCaptor.getValue();
            assertEquals("paid-orders", capturedOutbox.getTopic());
            assertEquals(orderId, capturedOutbox.getPayload());
            assertEquals(Outbox.OutboxStatus.PENDING, capturedOutbox.getStatus());
        }
    }

    @Test
    void completePayment_FailedPayment() {
        try (MockedStatic<PaymentIntent> mockedStatic = mockStatic(PaymentIntent.class)) {
            String orderId = "order123";
            String paymentIntentId = "pi_123";
            when(orderApiService.getPaymentIntentId(orderId)).thenReturn(paymentIntentId);
            mockedStatic.when(() -> PaymentIntent.retrieve(paymentIntentId)).thenReturn(mockPaymentIntent);
            when(mockPaymentIntent.getStatus()).thenReturn("requires_payment_method");

            assertThrows(IllegalStateException.class, () -> paymentService.completePayment(orderId));
            verify(outboxRepository, never()).save(any());
        }
    }

    @Test
    void refund_Success() throws StripeException {
        String paymentIntentId = "pi_123";
        try (MockedStatic<Refund> mockedStatic = mockStatic(Refund.class)) {
            when(Refund.create(anyMap())).thenReturn(mockRefund);

            paymentService.refund(paymentIntentId);

            ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
            mockedStatic.verify(() -> Refund.create(paramsCaptor.capture()));
            
            Map<String, Object> capturedParams = paramsCaptor.getValue();
            assertEquals(paymentIntentId, capturedParams.get("payment_intent"));
        }
    }
}