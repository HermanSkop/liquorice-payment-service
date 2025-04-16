package org.example.liquoricepaymentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class LiquoricePaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LiquoricePaymentServiceApplication.class, args);
    }

}
