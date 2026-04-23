package com.finflow.income.producer;

import com.finflow.income.model.Income;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class IncomeEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public IncomeEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishCreatedEvent(Income income) {
        kafkaTemplate.send("income.created", income);
    }

    public void publishUpdatedEvent(Income income) {
        kafkaTemplate.send("income.updated", income);
    }

    public void publishDeletedEvent(String id, String userId) {
        kafkaTemplate.send("income.deleted", Map.of(
                "id", id,
                "userId", userId
        ));
    }
}
