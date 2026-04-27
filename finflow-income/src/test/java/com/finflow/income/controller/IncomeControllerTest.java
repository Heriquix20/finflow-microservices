package com.finflow.income.controller;

import com.finflow.income.dto.IncomeRequest;
import com.finflow.income.dto.IncomeResponse;
import com.finflow.income.handler.GlobalExceptionHandler;
import com.finflow.income.service.IncomeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class IncomeControllerTest {

    @Mock
    private IncomeService incomeService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new IncomeController(incomeService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createIncomeShouldReturnCreatedResponse() throws Exception {
        IncomeResponse response = buildResponse();
        when(incomeService.createIncome(any(IncomeRequest.class), eq("user-123"))).thenReturn(response);

        String payload = """
                {
                  "description": "Salary",
                  "amount": 5000.00,
                  "category": "Salary",
                  "date": "2026-04-24"
                }
                """;

        mockMvc.perform(post("/incomes")
                        .header("X-User-Id", "user-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("income-1"))
                .andExpect(jsonPath("$.description").value("Salary"));
    }

    @Test
    void createIncomeShouldReturnBadRequestWhenPayloadIsInvalid() throws Exception {
        String payload = """
                {
                  "description": "",
                  "amount": -10,
                  "category": "",
                  "date": null
                }
                """;

        mockMvc.perform(post("/incomes")
                        .header("X-User-Id", "user-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed."))
                .andExpect(jsonPath("$.errors.description").exists())
                .andExpect(jsonPath("$.errors.amount").exists())
                .andExpect(jsonPath("$.errors.category").exists())
                .andExpect(jsonPath("$.errors.date").exists());
    }

    @Test
    void getAllIncomesShouldReturnOk() throws Exception {
        when(incomeService.getAllIncomes("user-123")).thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/incomes").header("X-User-Id", "user-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("income-1"));
    }

    @Test
    void updateIncomeShouldReturnUpdatedIncome() throws Exception {
        when(incomeService.updateIncome(eq("income-1"), any(IncomeRequest.class), eq("user-123")))
                .thenReturn(buildResponse());

        String payload = """
                {
                  "description": "Salary",
                  "amount": 5000.00,
                  "category": "Salary",
                  "date": "2026-04-24"
                }
                """;

        mockMvc.perform(put("/incomes/income-1")
                        .header("X-User-Id", "user-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("income-1"));
    }

    @Test
    void deleteIncomeShouldReturnNoContent() throws Exception {
        doNothing().when(incomeService).deleteIncome("income-1", "user-123");

        mockMvc.perform(delete("/incomes/income-1").header("X-User-Id", "user-123"))
                .andExpect(status().isNoContent());

        verify(incomeService).deleteIncome("income-1", "user-123");
    }

    @Test
    void getMonthlySummaryShouldReturnOk() throws Exception {
        when(incomeService.getMonthlySummary(4, 2026, "user-123")).thenReturn(new BigDecimal("5000.00"));

        mockMvc.perform(get("/incomes/summary")
                        .param("month", "4")
                        .param("year", "2026")
                        .header("X-User-Id", "user-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(5000.00));
    }

    private IncomeResponse buildResponse() {
        return new IncomeResponse(
                "income-1",
                "Salary",
                new BigDecimal("5000.00"),
                "Salary",
                LocalDate.of(2026, 4, 24),
                "user-123",
                LocalDateTime.of(2026, 4, 24, 10, 0)
        );
    }
}
