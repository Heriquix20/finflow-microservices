package com.finflow.expense.controller;

import com.finflow.expense.dto.ExpenseRequest;
import com.finflow.expense.dto.ExpenseResponse;
import com.finflow.expense.handler.GlobalExceptionHandler;
import com.finflow.expense.service.ExpenseService;
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
class ExpenseControllerTest {

    @Mock
    private ExpenseService expenseService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ExpenseController(expenseService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createExpenseShouldReturnCreatedResponse() throws Exception {
        when(expenseService.createExpense(any(ExpenseRequest.class), eq("user-123"))).thenReturn(buildResponse());

        String payload = """
                {
                  "description": "Rent",
                  "amount": 1800.00,
                  "category": "Housing",
                  "date": "2026-04-24"
                }
                """;

        mockMvc.perform(post("/expenses")
                        .header("X-User-Id", "user-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("expense-1"));
    }

    @Test
    void createExpenseShouldReturnBadRequestWhenPayloadIsInvalid() throws Exception {
        String payload = """
                {
                  "description": "",
                  "amount": -5,
                  "category": "",
                  "date": null
                }
                """;

        mockMvc.perform(post("/expenses")
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
    void getAllExpensesShouldReturnOk() throws Exception {
        when(expenseService.getAllExpenses("user-123")).thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/expenses").header("X-User-Id", "user-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("expense-1"));
    }

    @Test
    void updateExpenseShouldReturnUpdatedExpense() throws Exception {
        when(expenseService.updateExpense(eq("expense-1"), any(ExpenseRequest.class), eq("user-123")))
                .thenReturn(buildResponse());

        String payload = """
                {
                  "description": "Rent",
                  "amount": 1800.00,
                  "category": "Housing",
                  "date": "2026-04-24"
                }
                """;

        mockMvc.perform(put("/expenses/expense-1")
                        .header("X-User-Id", "user-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("expense-1"));
    }

    @Test
    void deleteExpenseShouldReturnNoContent() throws Exception {
        doNothing().when(expenseService).deleteExpense("expense-1", "user-123");

        mockMvc.perform(delete("/expenses/expense-1").header("X-User-Id", "user-123"))
                .andExpect(status().isNoContent());

        verify(expenseService).deleteExpense("expense-1", "user-123");
    }

    @Test
    void getMonthlySummaryShouldReturnOk() throws Exception {
        when(expenseService.getMonthlySummary(4, 2026, "user-123")).thenReturn(new BigDecimal("1800.00"));

        mockMvc.perform(get("/expenses/summary")
                        .param("month", "4")
                        .param("year", "2026")
                        .header("X-User-Id", "user-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1800.00));
    }

    private ExpenseResponse buildResponse() {
        return new ExpenseResponse(
                "expense-1",
                "Rent",
                new BigDecimal("1800.00"),
                "Housing",
                LocalDate.of(2026, 4, 24),
                "user-123",
                LocalDateTime.of(2026, 4, 24, 10, 0)
        );
    }
}
