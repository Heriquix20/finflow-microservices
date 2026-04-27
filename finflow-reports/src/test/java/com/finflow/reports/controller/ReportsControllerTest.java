package com.finflow.reports.controller;

import com.finflow.reports.dto.CategorySummaryResponse;
import com.finflow.reports.dto.MonthlySummaryResponse;
import com.finflow.reports.handler.GlobalExceptionHandler;
import com.finflow.reports.service.ReportsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ReportsControllerTest {

    @Mock
    private ReportsService reportsService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ReportsController(reportsService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getMonthlySummaryShouldReturnOk() throws Exception {
        MonthlySummaryResponse response = new MonthlySummaryResponse(
                "user-123",
                4,
                2026,
                new BigDecimal("5000.00"),
                new BigDecimal("1800.00"),
                new BigDecimal("3200.00"),
                LocalDateTime.of(2026, 4, 24, 10, 0)
        );

        when(reportsService.getMonthlySummary("user-123", 4, 2026)).thenReturn(response);

        mockMvc.perform(get("/reports/monthly-summary")
                        .param("month", "4")
                        .param("year", "2026")
                        .header("X-User-Id", "user-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(3200.00));
    }

    @Test
    void getCurrentBalanceShouldReturnOk() throws Exception {
        when(reportsService.getCurrentBalance("user-123")).thenReturn(new BigDecimal("3200.00"));

        mockMvc.perform(get("/reports/balance").header("X-User-Id", "user-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(3200.00));
    }

    @Test
    void getByCategoryShouldReturnOk() throws Exception {
        when(reportsService.getCategorySummary("user-123", 4, 2026))
                .thenReturn(List.of(new CategorySummaryResponse("Housing", new BigDecimal("1800.00"))));

        mockMvc.perform(get("/reports/by-category")
                        .param("month", "4")
                        .param("year", "2026")
                        .header("X-User-Id", "user-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("Housing"))
                .andExpect(jsonPath("$[0].total").value(1800.00));
    }

    @Test
    void getHistoryShouldReturnOk() throws Exception {
        MonthlySummaryResponse response = new MonthlySummaryResponse(
                "user-123",
                4,
                2026,
                new BigDecimal("5000.00"),
                new BigDecimal("1800.00"),
                new BigDecimal("3200.00"),
                LocalDateTime.of(2026, 4, 24, 10, 0)
        );

        when(reportsService.getHistory("user-123")).thenReturn(List.of(response));

        mockMvc.perform(get("/reports/history").header("X-User-Id", "user-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].month").value(4))
                .andExpect(jsonPath("$[0].balance").value(3200.00));
    }
}
