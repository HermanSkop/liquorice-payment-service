package org.example.liquoricepaymentservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentIntentDto {
    private String clientSecret;
    private String intentId;
}
