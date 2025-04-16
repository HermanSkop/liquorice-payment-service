package org.example.liquoricepaymentservice.services;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import lombok.RequiredArgsConstructor;
import org.example.liquoricepaymentservice.models.Outbox;
import org.example.liquoricepaymentservice.repositories.OutboxRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymentService {
    private final OutboxRepository outboxRepository;
    private final OrderApiService orderServiceClient;

    @Value("${kafka.topics.complete-payments}")
    private String paidTopic;

    public PaymentIntent createPaymentIntent(int amountCents) throws StripeException {
        Map<String, Object> params = new HashMap<>();
        params.put("amount", amountCents);
        params.put("currency", "usd");
        params.put("automatic_payment_methods", Map.of("enabled", true));
        return PaymentIntent.create(params);
    }

    @Transactional
    public void completePayment(String orderId) throws StripeException {
        String paymentIntentId = orderServiceClient.getPaymentIntentId(orderId);
        
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        String paymentStatus = paymentIntent.getStatus();
        if (!"succeeded".equals(paymentStatus)) {
            throw new IllegalStateException("Payment not completed. Current status: " + paymentStatus);
        }

        Outbox outboxEntry = Outbox.builder()
            .topic(paidTopic)
            .payload(orderId)
            .status(Outbox.OutboxStatus.PENDING)
            .build();

        outboxRepository.save(outboxEntry);
    }

    public void refund(String paymentIntentId) throws StripeException {
        Map<String, Object> refundParams = new HashMap<>();
        refundParams.put("payment_intent", paymentIntentId);
        com.stripe.model.Refund.create(refundParams);
    }
}
