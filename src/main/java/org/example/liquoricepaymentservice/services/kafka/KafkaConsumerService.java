package org.example.liquoricepaymentservice.services.kafka;

import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.liquoricepaymentservice.services.PaymentService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final PaymentService paymentService;

    @KafkaListener(topics = "${kafka.topics.refunded-intents}", groupId = "${spring.kafka.consumer.group-id}")
    public void handlePayment(@Payload String paymentIntentId) throws StripeException {
        paymentService.refund(paymentIntentId);
        log.info("Payment intent {} refunded", paymentIntentId);
    }
}