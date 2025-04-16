package org.example.liquoricepaymentservice.services.outbox;

import org.example.liquoricepaymentservice.models.Outbox;
import org.example.liquoricepaymentservice.repositories.OutboxRepository;
import org.example.liquoricepaymentservice.services.kafka.KafkaProducerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxProcessorServiceTest {

    @Mock
    private OutboxRepository outboxRepository;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @Mock
    private OutboxFailureService outboxFailureService;

    private OutboxProcessorService outboxProcessorService;

    @BeforeEach
    void setUp() {
        outboxProcessorService = new OutboxProcessorService(
                outboxRepository,
                kafkaProducerService,
                outboxFailureService
        );
    }

    @Test
    void processOutbox_Success() {
        Outbox message1 = Outbox.builder()
                .id("1")
                .topic("test-topic")
                .payload("test-payload")
                .status(Outbox.OutboxStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        Outbox message2 = Outbox.builder()
                .id("2")
                .topic("test-topic-2")
                .payload("test-payload-2")
                .status(Outbox.OutboxStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        when(outboxRepository.findByStatusOrderByCreatedAtAsc(Outbox.OutboxStatus.PENDING))
                .thenReturn(Arrays.asList(message1, message2));
        when(outboxRepository.save(any(Outbox.class))).thenAnswer(i -> i.getArgument(0));

        outboxProcessorService.processOutbox();

        verify(kafkaProducerService).send(message1.getTopic(), message1.getPayload());
        verify(kafkaProducerService).send(message2.getTopic(), message2.getPayload());
        verify(outboxRepository, times(2)).save(any(Outbox.class));
        verify(outboxFailureService, never()).markMessageAsFailed(any());
    }

    @Test
    void processOutbox_Empty() {
        when(outboxRepository.findByStatusOrderByCreatedAtAsc(Outbox.OutboxStatus.PENDING))
                .thenReturn(Collections.emptyList());

        outboxProcessorService.processOutbox();

        verify(kafkaProducerService, never()).send(anyString(), anyString());
        verify(outboxRepository, never()).save(any());
        verify(outboxFailureService, never()).markMessageAsFailed(any());
    }
}