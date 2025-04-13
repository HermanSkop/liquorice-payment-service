package org.example.liquoricepaymentservice.controllers;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.example.liquoricepaymentservice.config.Constants;
import org.example.liquoricepaymentservice.dtos.PaymentRequestDto;
import org.example.liquoricepaymentservice.dtos.PaymentIntentDto;
import org.example.liquoricepaymentservice.services.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(Constants.BASE_PATH + "/payments")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/generate-intent")
    public ResponseEntity<PaymentIntentDto> generatePaymentIntent(@RequestParam("amountCents") int amountCents) throws StripeException {
        PaymentIntent paymentIntent = paymentService.createPaymentIntent(amountCents);
        return ResponseEntity.ok(new PaymentIntentDto(paymentIntent.getClientSecret(), paymentIntent.getId()));
    }

    @PostMapping("/paid")
    public ResponseEntity<Void> completeOrder(@RequestBody PaymentRequestDto orderRequest) throws StripeException {
        paymentService.completePayment(orderRequest.getOrderId());
        return ResponseEntity.ok().build();
    }
}
