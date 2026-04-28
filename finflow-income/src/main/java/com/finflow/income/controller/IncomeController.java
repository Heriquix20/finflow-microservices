package com.finflow.income.controller;

import com.finflow.income.dto.IncomeRequest;
import com.finflow.income.dto.IncomeResponse;
import com.finflow.income.dto.PagedResponse;
import com.finflow.income.service.IncomeService;
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
@RequestMapping("/incomes")
public class IncomeController {

    private static final String USER_ID_HEADER = "X-User-Id";
    private final IncomeService incomeService;

    public IncomeController(IncomeService incomeService) {
        this.incomeService = incomeService;
    }

    @PostMapping
    public ResponseEntity<IncomeResponse> createIncome(
            @Valid @RequestBody IncomeRequest request,
            @RequestHeader(USER_ID_HEADER) @NotBlank String userId
    ) {
        IncomeResponse response = incomeService.createIncome(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<IncomeResponse>> getAllIncomes(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestHeader(USER_ID_HEADER) @NotBlank String userId
    ) {
        return ResponseEntity.ok(incomeService.getAllIncomes(userId, category, startDate, endDate));
    }

    @GetMapping("/paged")
    public ResponseEntity<PagedResponse<IncomeResponse>> getPagedIncomes(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestHeader(USER_ID_HEADER) @NotBlank String userId
    ) {
        return ResponseEntity.ok(incomeService.getPagedIncomes(userId, page, size, category, startDate, endDate));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncomeResponse> getIncomeById(
            @PathVariable @NotBlank String id,
            @RequestHeader(USER_ID_HEADER) @NotBlank String userId
    ) {
        return ResponseEntity.ok(incomeService.getIncomeById(id, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<IncomeResponse> updateIncome(
            @PathVariable @NotBlank String id,
            @Valid @RequestBody IncomeRequest request,
            @RequestHeader(USER_ID_HEADER) @NotBlank String userId
    ) {
        return ResponseEntity.ok(incomeService.updateIncome(id, request, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncome(
            @PathVariable @NotBlank String id,
            @RequestHeader(USER_ID_HEADER) @NotBlank String userId
    ) {
        incomeService.deleteIncome(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    public ResponseEntity<BigDecimal> getMonthlySummary(
            @RequestParam @Min(1) @Max(12) int month,
            @RequestParam @Min(1) int year,
            @RequestHeader(USER_ID_HEADER) @NotBlank String userId
    ) {
        return ResponseEntity.ok(incomeService.getMonthlySummary(month, year, userId));
    }
}
