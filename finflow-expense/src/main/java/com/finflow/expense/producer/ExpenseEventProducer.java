package com.finflow.expense.producer;

import com.finflow.expense.model.Expense;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

@Component
public class ExpenseEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ExpenseEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishCreatedEvent(Expense expense) {
        kafkaTemplate.send("expense.created", toPayload(expense));
    }

    public void publishUpdatedEvent(Expense expense) {
        kafkaTemplate.send("expense.updated", toPayload(expense));
    }

    public void publishDeletedEvent(String id, String userId, LocalDate date) {
        kafkaTemplate.send("expense.deleted", Map.of(
                "id", id,
                "userId", userId,
                "date", date.toString()
        ));
    }

    private Map<String, Object> toPayload(Expense expense) {
        return Map.of(
                "id", expense.getId(),
                "description", expense.getDescription(),
                "amount", expense.getAmount(),
                "category", expense.getCategory(),
                "date", expense.getDate().toString(),
                "userId", expense.getUserId()
        );
    }
}
