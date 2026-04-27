package com.finflow.reports.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finflow.reports.service.ReportsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Map;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FinancialEventConsumerTest {

    @Mock
    private ReportsService reportsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private FinancialEventConsumer financialEventConsumer;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        financialEventConsumer = new FinancialEventConsumer(reportsService, objectMapper);
    }

    @Test
    void handleIncomeUpsertShouldRebuildSummaryUsingPayloadDate() throws Exception {
        financialEventConsumer.handleIncomeUpsert(objectMapper.writeValueAsString(Map.of(
                "userId", "user-123",
                "date", "2026-04-24"
        )));

        verify(reportsService).rebuildMonthlySummary("user-123", 4, 2026);
    }

    @Test
    void handleExpenseUpsertShouldRebuildSummaryUsingPayloadDate() throws Exception {
        financialEventConsumer.handleExpenseUpsert(objectMapper.writeValueAsString(Map.of(
                "userId", "user-123",
                "date", "2026-05-02"
        )));

        verify(reportsService).rebuildMonthlySummary("user-123", 5, 2026);
    }

    @Test
    void handleDeleteShouldRebuildSummaryForCurrentMonth() throws Exception {
        financialEventConsumer.handleDelete(objectMapper.writeValueAsString(Map.of(
                "userId", "user-123",
                "date", "2026-06-05"
        )));

        verify(reportsService).rebuildMonthlySummary("user-123", 6, 2026);
    }

    @Test
    void handleDeleteShouldFallbackToCurrentMonthWhenDateIsMissing() {
        LocalDate now = LocalDate.now();

        financialEventConsumer.handleDeletePayload(Map.of("userId", "user-123"));

        verify(reportsService).rebuildMonthlySummary("user-123", now.getMonthValue(), now.getYear());
    }
}
