package com.finflow.reports.service;

import com.finflow.reports.client.ExpenseClient;
import com.finflow.reports.client.IncomeClient;
import com.finflow.reports.client.dto.ExpenseResponse;
import com.finflow.reports.client.dto.IncomeResponse;
import com.finflow.reports.dto.CategorySummaryResponse;
import com.finflow.reports.dto.MonthlySummaryResponse;
import com.finflow.reports.model.MonthlySummary;
import com.finflow.reports.repository.MonthlySummaryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ReportsService {

    private final MonthlySummaryRepository monthlySummaryRepository;
    private final IncomeClient incomeClient;
    private final ExpenseClient expenseClient;

    public ReportsService(MonthlySummaryRepository monthlySummaryRepository, IncomeClient incomeClient, ExpenseClient expenseClient) {
        this.monthlySummaryRepository = monthlySummaryRepository;
        this.incomeClient = incomeClient;
        this.expenseClient = expenseClient;
    }

    public void rebuildMonthlySummary(String userId, int month, int year) {
        List<IncomeResponse> incomes = incomeClient.getAllIncomes(userId);
        List<ExpenseResponse> expenses = expenseClient.getAllExpenses(userId);

        BigDecimal totalIncome = incomes.stream()
                .filter(income -> isSameMonth(income.getDate(), month, year))
                .map(IncomeResponse::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = expenses.stream()
                .filter(expense -> isSameMonth(expense.getDate(), month, year))
                .map(ExpenseResponse::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        MonthlySummary summary = findLatestSummary(userId, month, year)
                .orElseGet(MonthlySummary::new);

        summary.setUserId(userId);
        summary.setMonth(month);
        summary.setYear(year);
        summary.setTotalIncome(totalIncome);
        summary.setTotalExpense(totalExpense);
        summary.setBalance(totalIncome.subtract(totalExpense));
        summary.setUpdatedAt(LocalDateTime.now());

        monthlySummaryRepository.save(summary);
    }

    public MonthlySummaryResponse getMonthlySummary(String userId, int month, int year) {
        rebuildMonthlySummary(userId, month, year);

        MonthlySummary summary = findLatestSummary(userId, month, year)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Monthly summary not found."));

        return toResponse(summary);
    }

    public BigDecimal getCurrentBalance(String userId) {
        BigDecimal totalIncome = incomeClient.getAllIncomes(userId)
                .stream()
                .map(IncomeResponse::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = expenseClient.getAllExpenses(userId)
                .stream()
                .map(ExpenseResponse::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalIncome.subtract(totalExpense);
    }

    public List<CategorySummaryResponse> getCategorySummary(String userId, int month, int year) {
        List<ExpenseResponse> expenses = expenseClient.getAllExpenses(userId);

        Map<String, BigDecimal> grouped = expenses.stream()
                .filter(expense -> isSameMonth(expense.getDate(), month, year))
                .filter(expense -> expense.getAmount() != null)
                .collect(Collectors.groupingBy(
                        ExpenseResponse::getCategory,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                ExpenseResponse::getAmount,
                                BigDecimal::add
                        )
                ));

        return grouped.entrySet().stream()
                .map(entry -> new CategorySummaryResponse(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(CategorySummaryResponse::getTotal).reversed())
                .toList();
    }

    public List<MonthlySummaryResponse> getHistory(String userId) {
        LocalDate now = LocalDate.now();
        rebuildMonthlySummary(userId, now.getMonthValue(), now.getYear());

        return monthlySummaryRepository.findTop12ByUserIdOrderByYearDescMonthDesc(userId)
                .stream()
                .collect(Collectors.toMap(
                        summary -> YearMonth.of(summary.getYear(), summary.getMonth()),
                        summary -> summary,
                        this::pickMostRecentSummary
                ))
                .values()
                .stream()
                .sorted(Comparator.comparing(MonthlySummary::getYear)
                        .thenComparing(MonthlySummary::getMonth)
                        .reversed())
                .map(this::toResponse)
                .toList();
    }

    private MonthlySummaryResponse toResponse(MonthlySummary summary) {
        return new MonthlySummaryResponse(
                summary.getUserId(),
                summary.getMonth(),
                summary.getYear(),
                summary.getTotalIncome(),
                summary.getTotalExpense(),
                summary.getBalance(),
                summary.getUpdatedAt()
        );
    }

    private boolean isSameMonth(java.time.LocalDate date, int month, int year) {
        return date != null && date.getMonthValue() == month && date.getYear() == year;
    }

    private java.util.Optional<MonthlySummary> findLatestSummary(String userId, int month, int year) {
        return monthlySummaryRepository.findAllByUserIdAndMonthAndYearOrderByUpdatedAtDesc(userId, month, year)
                .stream()
                .findFirst();
    }

    private MonthlySummary pickMostRecentSummary(MonthlySummary left, MonthlySummary right) {
        LocalDateTime leftUpdatedAt = left.getUpdatedAt() != null ? left.getUpdatedAt() : LocalDateTime.MIN;
        LocalDateTime rightUpdatedAt = right.getUpdatedAt() != null ? right.getUpdatedAt() : LocalDateTime.MIN;
        return rightUpdatedAt.isAfter(leftUpdatedAt) ? right : left;
    }
}
