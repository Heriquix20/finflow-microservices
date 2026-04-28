package com.finflow.reports.service;

import com.finflow.reports.client.ExpenseClient;
import com.finflow.reports.client.IncomeClient;
import com.finflow.reports.client.dto.ExpenseResponse;
import com.finflow.reports.client.dto.IncomeResponse;
import com.finflow.reports.dto.CategorySummaryResponse;
import com.finflow.reports.dto.MonthlySummaryResponse;
import com.finflow.reports.model.MonthlySummary;
import com.finflow.reports.repository.MonthlySummaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportsServiceTest {

    @Mock
    private MonthlySummaryRepository monthlySummaryRepository;

    @Mock
    private IncomeClient incomeClient;

    @Mock
    private ExpenseClient expenseClient;

    @InjectMocks
    private ReportsService reportsService;

    private IncomeResponse aprilIncome;
    private ExpenseResponse aprilExpense;

    @BeforeEach
    void setUp() {
        aprilIncome = buildIncomeResponse("income-1", new BigDecimal("5000.00"), "Salary", LocalDate.of(2026, 4, 24), "user-123");
        aprilExpense = buildExpenseResponse("expense-1", new BigDecimal("1800.00"), "Housing", LocalDate.of(2026, 4, 24), "user-123");
    }

    @Test
    void rebuildMonthlySummaryShouldPersistConsolidatedValues() {
        when(incomeClient.getAllIncomes("user-123")).thenReturn(List.of(aprilIncome));
        when(expenseClient.getAllExpenses("user-123")).thenReturn(List.of(aprilExpense));
        when(monthlySummaryRepository.findAllByUserIdAndMonthAndYearOrderByUpdatedAtDesc("user-123", 4, 2026)).thenReturn(List.of());

        reportsService.rebuildMonthlySummary("user-123", 4, 2026);

        ArgumentCaptor<MonthlySummary> captor = ArgumentCaptor.forClass(MonthlySummary.class);
        verify(monthlySummaryRepository).save(captor.capture());

        MonthlySummary saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo("user-123");
        assertThat(saved.getMonth()).isEqualTo(4);
        assertThat(saved.getYear()).isEqualTo(2026);
        assertThat(saved.getTotalIncome()).isEqualByComparingTo("5000.00");
        assertThat(saved.getTotalExpense()).isEqualByComparingTo("1800.00");
        assertThat(saved.getBalance()).isEqualByComparingTo("3200.00");
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void getMonthlySummaryShouldReturnMappedResponse() {
        MonthlySummary summary = new MonthlySummary(
                "summary-1",
                "user-123",
                4,
                2026,
                new BigDecimal("5000.00"),
                new BigDecimal("1800.00"),
                new BigDecimal("3200.00"),
                LocalDateTime.of(2026, 4, 24, 10, 0)
        );

        when(incomeClient.getAllIncomes("user-123")).thenReturn(List.of(aprilIncome));
        when(expenseClient.getAllExpenses("user-123")).thenReturn(List.of(aprilExpense));
        when(monthlySummaryRepository.findAllByUserIdAndMonthAndYearOrderByUpdatedAtDesc("user-123", 4, 2026)).thenReturn(List.of(summary));

        MonthlySummaryResponse response = reportsService.getMonthlySummary("user-123", 4, 2026);

        assertThat(response.getBalance()).isEqualByComparingTo("3200.00");
        assertThat(response.getMonth()).isEqualTo(4);
    }

    @Test
    void getMonthlySummaryShouldThrowWhenSummaryDoesNotExist() {
        when(monthlySummaryRepository.findAllByUserIdAndMonthAndYearOrderByUpdatedAtDesc("user-123", 4, 2026)).thenReturn(List.of());

        assertThatThrownBy(() -> reportsService.getMonthlySummary("user-123", 4, 2026))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException responseStatusException = (ResponseStatusException) ex;
                    assertThat(responseStatusException.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(responseStatusException.getReason()).isEqualTo("Monthly summary not found.");
                });
    }

    @Test
    void getCurrentBalanceShouldAggregateIncomeAndExpenseServices() {
        when(incomeClient.getAllIncomes("user-123")).thenReturn(List.of(
                aprilIncome,
                buildIncomeResponse("income-2", new BigDecimal("700.00"), "Bonus", LocalDate.of(2026, 4, 25), "user-123")
        ));
        when(expenseClient.getAllExpenses("user-123")).thenReturn(List.of(aprilExpense));

        BigDecimal balance = reportsService.getCurrentBalance("user-123");

        assertThat(balance).isEqualByComparingTo("3900.00");
    }

    @Test
    void getCategorySummaryShouldGroupExpensesByCategoryForSelectedMonth() {
        ExpenseResponse groceries = buildExpenseResponse("expense-2", new BigDecimal("200.00"), "Food", LocalDate.of(2026, 4, 10), "user-123");
        ExpenseResponse anotherHousing = buildExpenseResponse("expense-3", new BigDecimal("300.00"), "Housing", LocalDate.of(2026, 4, 11), "user-123");
        ExpenseResponse nextMonth = buildExpenseResponse("expense-4", new BigDecimal("100.00"), "Housing", LocalDate.of(2026, 5, 1), "user-123");

        when(expenseClient.getAllExpenses("user-123")).thenReturn(List.of(aprilExpense, groceries, anotherHousing, nextMonth));

        List<CategorySummaryResponse> responses = reportsService.getCategorySummary("user-123", 4, 2026);

        assertThat(responses).hasSize(2);
        assertThat(responses)
                .anySatisfy(response -> {
                    assertThat(response.getCategory()).isEqualTo("Housing");
                    assertThat(response.getTotal()).isEqualByComparingTo("2100.00");
                })
                .anySatisfy(response -> {
                    assertThat(response.getCategory()).isEqualTo("Food");
                    assertThat(response.getTotal()).isEqualByComparingTo("200.00");
                });
    }

    @Test
    void getHistoryShouldMapRepositoryResults() {
        MonthlySummary summary = new MonthlySummary(
                "summary-1",
                "user-123",
                4,
                2026,
                new BigDecimal("5000.00"),
                new BigDecimal("1800.00"),
                new BigDecimal("3200.00"),
                LocalDateTime.of(2026, 4, 24, 10, 0)
        );

        when(monthlySummaryRepository.findTop12ByUserIdOrderByYearDescMonthDesc("user-123")).thenReturn(List.of(summary));

        List<MonthlySummaryResponse> history = reportsService.getHistory("user-123");

        assertThat(history).hasSize(1);
        assertThat(history.get(0).getBalance()).isEqualByComparingTo("3200.00");
    }

    @Test
    void getHistoryShouldKeepOnlyMostRecentSummaryPerMonth() {
        MonthlySummary older = new MonthlySummary(
                "summary-older",
                "user-123",
                4,
                2026,
                new BigDecimal("4000.00"),
                new BigDecimal("1500.00"),
                new BigDecimal("2500.00"),
                LocalDateTime.of(2026, 4, 24, 9, 0)
        );

        MonthlySummary newer = new MonthlySummary(
                "summary-newer",
                "user-123",
                4,
                2026,
                new BigDecimal("5000.00"),
                new BigDecimal("1800.00"),
                new BigDecimal("3200.00"),
                LocalDateTime.of(2026, 4, 24, 10, 0)
        );

        when(monthlySummaryRepository.findTop12ByUserIdOrderByYearDescMonthDesc("user-123"))
                .thenReturn(List.of(older, newer));

        List<MonthlySummaryResponse> history = reportsService.getHistory("user-123");

        assertThat(history).hasSize(1);
        assertThat(history.get(0).getBalance()).isEqualByComparingTo("3200.00");
    }

    private IncomeResponse buildIncomeResponse(String id, BigDecimal amount, String category, LocalDate date, String userId) {
        IncomeResponse response = new IncomeResponse();
        ReflectionTestUtils.setField(response, "id", id);
        ReflectionTestUtils.setField(response, "description", "Income");
        ReflectionTestUtils.setField(response, "amount", amount);
        ReflectionTestUtils.setField(response, "category", category);
        ReflectionTestUtils.setField(response, "date", date);
        ReflectionTestUtils.setField(response, "userId", userId);
        ReflectionTestUtils.setField(response, "createdAt", LocalDateTime.of(2026, 4, 24, 10, 0));
        return response;
    }

    private ExpenseResponse buildExpenseResponse(String id, BigDecimal amount, String category, LocalDate date, String userId) {
        ExpenseResponse response = new ExpenseResponse();
        ReflectionTestUtils.setField(response, "id", id);
        ReflectionTestUtils.setField(response, "description", "Expense");
        ReflectionTestUtils.setField(response, "amount", amount);
        ReflectionTestUtils.setField(response, "category", category);
        ReflectionTestUtils.setField(response, "date", date);
        ReflectionTestUtils.setField(response, "userId", userId);
        ReflectionTestUtils.setField(response, "createdAt", LocalDateTime.of(2026, 4, 24, 10, 0));
        return response;
    }
}
