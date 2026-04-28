package com.finflow.reports.controller;

import com.finflow.reports.dto.CategorySummaryResponse;
import com.finflow.reports.dto.MonthlySummaryResponse;
import com.finflow.reports.service.ReportsService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Validated
@RestController
@RequestMapping("/reports")
public class ReportsController {

    private static final String USER_ID_HEADER = "X-User-Id";
    private final ReportsService reportsService;

    public ReportsController(ReportsService reportsService) {
        this.reportsService = reportsService;
    }

    @GetMapping("/monthly-summary")
    public ResponseEntity<MonthlySummaryResponse> getMonthlySummary(
            @RequestParam @Min(1) @Max(12) int month,
            @RequestParam @Min(1) int year,
            @RequestHeader(USER_ID_HEADER) @NotBlank String userId
    ) {
        return ResponseEntity.ok(reportsService.getMonthlySummary(userId, month, year));
    }

    @GetMapping("/balance")
    public ResponseEntity<BigDecimal> getCurrentBalance(
            @RequestHeader(USER_ID_HEADER) @NotBlank String userId
    ) {
        return ResponseEntity.ok(reportsService.getCurrentBalance(userId));
    }

    @GetMapping("/by-category")
    public ResponseEntity<List<CategorySummaryResponse>> getByCategory(
            @RequestParam @Min(1) @Max(12) int month,
            @RequestParam @Min(1) int year,
            @RequestHeader(USER_ID_HEADER) @NotBlank String userId
    ) {
        return ResponseEntity.ok(reportsService.getCategorySummary(userId, month, year));
    }

    @GetMapping("/history")
    public ResponseEntity<List<MonthlySummaryResponse>> getHistory(
            @RequestHeader(USER_ID_HEADER) @NotBlank String userId
    ) {
        return ResponseEntity.ok(reportsService.getHistory(userId));
    }
}
