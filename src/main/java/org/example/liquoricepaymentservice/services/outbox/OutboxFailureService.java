package org.example.liquoricepaymentservice.services.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.liquoricepaymentservice.models.Outbox;
import org.example.liquoricepaymentservice.repositories.OutboxRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OutboxFailureService {
    
    private final OutboxRepository outboxRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markMessageAsFailed(Outbox message) {
        try {
            message.setStatus(Outbox.OutboxStatus.FAILED);
            outboxRepository.save(message);
        } catch (Exception e) {
            log.error("Failed to mark message as failed: {}", message.getId(), e);
        }
    }
}