package com.finflow.income.controller;

import com.finflow.income.dto.IncomeRequest;
import com.finflow.income.dto.IncomeResponse;
import com.finflow.income.service.IncomeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/incomes")
public class IncomeController {

    private final IncomeService incomeService;

    public IncomeController(IncomeService incomeService) {
        this.incomeService = incomeService;
    }

    @PostMapping
    public ResponseEntity<IncomeResponse> createIncome(
            @Valid @RequestBody IncomeRequest request,
            @RequestHeader("X-User-Id") String userId
    ) {
        IncomeResponse response = incomeService.createIncome(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<IncomeResponse>> getAllIncomes(
            @RequestHeader("X-User-Id") String userId
    ) {
        return ResponseEntity.ok(incomeService.getAllIncomes(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncomeResponse> getIncomeById(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId
    ) {
        return ResponseEntity.ok(incomeService.getIncomeById(id, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<IncomeResponse> updateIncome(
            @PathVariable String id,
            @Valid @RequestBody IncomeRequest request,
            @RequestHeader("X-User-Id") String userId
    ) {
        return ResponseEntity.ok(incomeService.updateIncome(id, request, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncome(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId
    ) {
        incomeService.deleteIncome(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    public ResponseEntity<BigDecimal> getMonthlySummary(
            @RequestParam int month,
            @RequestParam int year,
            @RequestHeader("X-User-Id") String userId
    ) {
        return ResponseEntity.ok(incomeService.getMonthlySummary(month, year, userId));
    }
}
