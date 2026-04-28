package com.finflow.reports.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finflow.reports.service.ReportsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.Map;

@Component
public class FinancialEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(FinancialEventConsumer.class);

    private final ReportsService reportsService;
    private final ObjectMapper objectMapper;

    public FinancialEventConsumer(ReportsService reportsService, ObjectMapper objectMapper) {
        this.reportsService = reportsService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = {"income.created", "income.updated"})
    public void handleIncomeUpsert(String payload) {
        processEvent(parsePayload(payload));
    }

    @KafkaListener(topics = {"expense.created", "expense.updated"})
    public void handleExpenseUpsert(String payload) {
        processEvent(parsePayload(payload));
    }

    @KafkaListener(topics = {"income.deleted", "expense.deleted"})
    public void handleDelete(String payload) {
        Map<String, Object> parsedPayload = parsePayload(payload);
        if (parsedPayload.isEmpty()) {
            return;
        }

        handleDeletePayload(parsedPayload);
    }

    void handleDeletePayload(Map<String, Object> payload) {
        Optional<String> userId = extractUserId(payload);
        if (userId.isEmpty()) {
            return;
        }

        LocalDate eventDate = extractDate(payload).orElse(LocalDate.now());
        reportsService.rebuildMonthlySummary(userId.get(), eventDate.getMonthValue(), eventDate.getYear());
    }

    private void processEvent(Map<String, Object> payload) {
        Optional<String> userId = extractUserId(payload);
        Optional<LocalDate> eventDate = extractDate(payload);

        if (userId.isEmpty() || eventDate.isEmpty()) {
            return;
        }

        reportsService.rebuildMonthlySummary(userId.get(), eventDate.get().getMonthValue(), eventDate.get().getYear());
    }

    private Map<String, Object> parsePayload(String payload) {
        try {
            return objectMapper.readValue(payload, new TypeReference<>() {});
        } catch (JsonProcessingException ex) {
            log.warn("Ignoring financial event with invalid JSON payload: {}", payload);
            return Map.of();
        }
    }

    private Optional<String> extractUserId(Map<String, Object> payload) {
        Object value = payload.get("userId");
        if (value instanceof String userId && !userId.isBlank()) {
            return Optional.of(userId);
        }

        log.warn("Ignoring financial event without valid userId: {}", payload);
        return Optional.empty();
    }

    private Optional<LocalDate> extractDate(Map<String, Object> payload) {
        Object value = payload.get("date");
        if (!(value instanceof String dateValue) || dateValue.isBlank()) {
            return Optional.empty();
        }

        try {
            return Optional.of(LocalDate.parse(dateValue));
        } catch (DateTimeParseException ex) {
            log.warn("Ignoring financial event with invalid date '{}': {}", dateValue, payload);
            return Optional.empty();
        }
    }
}
