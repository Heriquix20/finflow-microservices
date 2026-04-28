package com.finflow.expense.controller;

import com.finflow.expense.dto.ExpenseRequest;
import com.finflow.expense.dto.ExpenseResponse;
import com.finflow.expense.dto.PagedResponse;
import com.finflow.expense.service.ExpenseService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
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
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestHeader(USER_ID_HEADER) @NotBlank String userId
    ) {
        return ResponseEntity.ok(expenseService.getAllExpenses(userId, category, startDate, endDate));
    }

    @GetMapping("/paged")
    public ResponseEntity<PagedResponse<ExpenseResponse>> getPagedExpenses(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestHeader(USER_ID_HEADER) @NotBlank String userId
    ) {
        return ResponseEntity.ok(expenseService.getPagedExpenses(userId, page, size, category, startDate, endDate));
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
