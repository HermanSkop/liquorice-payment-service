package org.example.liquoricepaymentservice.services.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaProducerService {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void send(String topic, String message) {
        log.debug("Sending message to topic {}: {}", topic, message);
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, message);
        kafkaTemplate.send(record);
    }
}
