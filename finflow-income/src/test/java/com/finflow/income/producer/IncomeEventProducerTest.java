package com.finflow.income.producer;

import com.finflow.income.model.Income;
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
class IncomeEventProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private IncomeEventProducer incomeEventProducer;
    private Income income;

    @BeforeEach
    void setUp() {
        incomeEventProducer = new IncomeEventProducer(kafkaTemplate);
        income = new Income(
                "income-1",
                "Salary",
                new BigDecimal("5000.00"),
                "Salary",
                LocalDate.of(2026, 4, 24),
                "user-123",
                LocalDateTime.of(2026, 4, 24, 10, 0)
        );
    }

    @Test
    void publishCreatedEventShouldSendIncomeToCreatedTopic() {
        incomeEventProducer.publishCreatedEvent(income);

        verify(kafkaTemplate).send("income.created", Map.of(
                "id", "income-1",
                "description", "Salary",
                "amount", new BigDecimal("5000.00"),
                "category", "Salary",
                "date", "2026-04-24",
                "userId", "user-123"
        ));
    }

    @Test
    void publishUpdatedEventShouldSendIncomeToUpdatedTopic() {
        incomeEventProducer.publishUpdatedEvent(income);

        verify(kafkaTemplate).send("income.updated", Map.of(
                "id", "income-1",
                "description", "Salary",
                "amount", new BigDecimal("5000.00"),
                "category", "Salary",
                "date", "2026-04-24",
                "userId", "user-123"
        ));
    }

    @Test
    void publishDeletedEventShouldSendIdUserIdAndDateToDeletedTopic() {
        incomeEventProducer.publishDeletedEvent("income-1", "user-123", LocalDate.of(2026, 4, 24));

        verify(kafkaTemplate).send("income.deleted", Map.of(
                "id", "income-1",
                "userId", "user-123",
                "date", "2026-04-24"
        ));
    }
}
