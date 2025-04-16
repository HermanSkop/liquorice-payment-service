package org.example.liquoricepaymentservice.services.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.liquoricepaymentservice.models.Outbox;
import org.example.liquoricepaymentservice.repositories.OutboxRepository;
import org.example.liquoricepaymentservice.services.kafka.KafkaProducerService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OutboxProcessorService {
    
    private final OutboxRepository outboxRepository;
    private final KafkaProducerService kafkaProducerService;
    private final OutboxFailureService outboxFailureService;

    @Scheduled(fixedRateString = "${outbox.processing.rate:5000}")
    public void processOutbox() {
        List<Outbox> pendingMessages = outboxRepository.findByStatusOrderByCreatedAtAsc(Outbox.OutboxStatus.PENDING);
        
        for (Outbox message : pendingMessages) {
            processMessage(message);
        }
    }

    @Transactional
    protected void processMessage(Outbox message) {
        try {
            kafkaProducerService.send(message.getTopic(), message.getPayload());

            message.setStatus(Outbox.OutboxStatus.PROCESSED);
            message.setProcessedAt(LocalDateTime.now());
            outboxRepository.save(message);

            log.debug("Successfully processed outbox message: {}", message.getId());
        } catch (Exception e) {
            log.error("Failed to process outbox message: {}", message.getId(), e);
            outboxFailureService.markMessageAsFailed(message);
            throw e;
        }
    }
}
