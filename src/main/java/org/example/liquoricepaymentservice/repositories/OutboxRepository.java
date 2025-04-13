package org.example.liquoricepaymentservice.repositories;

import org.example.liquoricepaymentservice.models.Outbox;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OutboxRepository extends MongoRepository<Outbox, String> {
    List<Outbox> findByStatusOrderByCreatedAtAsc(Outbox.OutboxStatus status);
}
