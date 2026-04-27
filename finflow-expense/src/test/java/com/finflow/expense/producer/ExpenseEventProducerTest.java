package com.finflow.expense.producer;

import com.finflow.expense.model.Expense;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExpenseEventProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private ExpenseEventProducer expenseEventProducer;
    private Expense expense;

    @BeforeEach
    void setUp() {
        expenseEventProducer = new ExpenseEventProducer(kafkaTemplate);
        expense = new Expense(
                "expense-1",
                "Rent",
                new BigDecimal("1800.00"),
                "Housing",
                LocalDate.of(2026, 4, 24),
                "user-123",
                LocalDateTime.of(2026, 4, 24, 10, 0)
        );
    }

    @Test
    void publishCreatedEventShouldSendExpenseToCreatedTopic() {
        expenseEventProducer.publishCreatedEvent(expense);

        verify(kafkaTemplate).send("expense.created", Map.of(
                "id", "expense-1",
                "description", "Rent",
                "amount", new BigDecimal("1800.00"),
                "category", "Housing",
                "date", "2026-04-24",
                "userId", "user-123"
        ));
    }

    @Test
    void publishUpdatedEventShouldSendExpenseToUpdatedTopic() {
        expenseEventProducer.publishUpdatedEvent(expense);

        verify(kafkaTemplate).send("expense.updated", Map.of(
                "id", "expense-1",
                "description", "Rent",
                "amount", new BigDecimal("1800.00"),
                "category", "Housing",
                "date", "2026-04-24",
                "userId", "user-123"
        ));
    }

    @Test
    void publishDeletedEventShouldSendIdUserIdAndDateToDeletedTopic() {
        expenseEventProducer.publishDeletedEvent("expense-1", "user-123", LocalDate.of(2026, 4, 24));

        verify(kafkaTemplate).send("expense.deleted", Map.of(
                "id", "expense-1",
                "userId", "user-123",
                "date", "2026-04-24"
        ));
    }
}
