package com.finflow.income.producer;

import com.finflow.income.model.Income;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

@Component
public class IncomeEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public IncomeEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishCreatedEvent(Income income) {
        kafkaTemplate.send("income.created", toPayload(income));
    }

    public void publishUpdatedEvent(Income income) {
        kafkaTemplate.send("income.updated", toPayload(income));
    }

    public void publishDeletedEvent(String id, String userId, LocalDate date) {
        kafkaTemplate.send("income.deleted", Map.of(
                "id", id,
                "userId", userId,
                "date", date.toString()
        ));
    }

    private Map<String, Object> toPayload(Income income) {
        return Map.of(
                "id", income.getId(),
                "description", income.getDescription(),
                "amount", income.getAmount(),
                "category", income.getCategory(),
                "date", income.getDate().toString(),
                "userId", income.getUserId()
        );
    }
}
