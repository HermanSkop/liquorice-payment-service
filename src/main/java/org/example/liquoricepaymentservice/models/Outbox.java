package org.example.liquoricepaymentservice.models;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Builder
@Data
@Document(collection = "outbox")
public class Outbox {
    @Id
    private String id;
    
    private String topic;
    private String payload;
    private OutboxStatus status;
    
    @CreatedDate
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    
    public enum OutboxStatus {
        PENDING,
        PROCESSED,
        FAILED
    }
}
