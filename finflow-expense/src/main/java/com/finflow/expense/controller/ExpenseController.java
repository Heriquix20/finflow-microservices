package com.finflow.expense.controller;

import com.finflow.expense.dto.ExpenseRequest;
import com.finflow.expense.dto.ExpenseResponse;
import com.finflow.expense.service.ExpenseService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Validated
@RestController
@RequestMapping("/expenses")
public class ExpenseController {

    private static final String USER_ID_HEADER = "X-User-Id";
    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(
            @Valid @RequestBody ExpenseRequest request,
            @RequestHeader(USER_ID_HEADER) @NotBlank String userId
    ) {
        ExpenseResponse response = expenseService.createExpense(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> getAllExpenses(
            @RequestHeader(USER_ID_HEADER) @NotBlank String userId
    ) {
        return ResponseEntity.ok(expenseService.getAllExpenses(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> getExpenseById(
            @PathVariable @NotBlank String id,
            @RequestHeader(USER_ID_HEADER) @NotBlank String userId
    ) {
        return ResponseEntity.ok(expenseService.getExpenseById(id, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> updateExpense(
            @PathVariable @NotBlank String id,
            @Valid @RequestBody ExpenseRequest request,
            @RequestHeader(USER_ID_HEADER) @NotBlank String userId
    ) {
        return ResponseEntity.ok(expenseService.updateExpense(id, request, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(
            @PathVariable @NotBlank String id,
            @RequestHeader(USER_ID_HEADER) @NotBlank String userId
    ) {
        expenseService.deleteExpense(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    public ResponseEntity<BigDecimal> getMonthlySummary(
            @RequestParam @Min(1) @Max(12) int month,
            @RequestParam @Min(1) int year,
            @RequestHeader(USER_ID_HEADER) @NotBlank String userId
    ) {
        return ResponseEntity.ok(expenseService.getMonthlySummary(month, year, userId));
    }
}
