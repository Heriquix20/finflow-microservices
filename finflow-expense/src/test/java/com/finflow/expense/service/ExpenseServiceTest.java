package com.finflow.expense.service;

import com.finflow.expense.dto.ExpenseRequest;
import com.finflow.expense.dto.ExpenseResponse;
import com.finflow.expense.model.Expense;
import com.finflow.expense.producer.ExpenseEventProducer;
import com.finflow.expense.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private ExpenseEventProducer expenseEventProducer;

    @InjectMocks
    private ExpenseService expenseService;

    private ExpenseRequest request;
    private Expense expense;

    @BeforeEach
    void setUp() {
        request = new ExpenseRequest(
                "Rent",
                new BigDecimal("1800.00"),
                "Housing",
                LocalDate.of(2026, 4, 24)
        );

        expense = new Expense(
                "expense-1",
                "Rent",
                new BigDecimal("1800.00"),
                "Housing",
                LocalDate.of(2026, 4, 24),
                "user-123",
                LocalDateTime.of(2026, 4, 24, 10, 0)
        );
    }

    @Test
    void createExpenseShouldPersistAndPublishEvent() {
        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);

        ExpenseResponse response = expenseService.createExpense(request, "user-123");

        ArgumentCaptor<Expense> captor = ArgumentCaptor.forClass(Expense.class);
        verify(expenseRepository).save(captor.capture());
        verify(expenseEventProducer).publishCreatedEvent(expense);

        Expense saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo("user-123");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(response.getId()).isEqualTo("expense-1");
    }

    @Test
    void getAllExpensesShouldMapRepositoryResult() {
        when(expenseRepository.findAllByUserIdOrderByDateDescCreatedAtDesc("user-123")).thenReturn(List.of(expense));

        List<ExpenseResponse> responses = expenseService.getAllExpenses("user-123");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getDescription()).isEqualTo("Rent");
    }

    @Test
    void getExpenseByIdShouldThrowWhenExpenseDoesNotExist() {
        when(expenseRepository.findByIdAndUserId("missing", "user-123")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.getExpenseById("missing", "user-123"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException responseStatusException = (ResponseStatusException) ex;
                    assertThat(responseStatusException.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(responseStatusException.getReason()).isEqualTo("Expense not found.");
                });
    }

    @Test
    void updateExpenseShouldPersistChangesAndPublishEvent() {
        when(expenseRepository.findByIdAndUserId("expense-1", "user-123")).thenReturn(Optional.of(expense));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExpenseRequest updateRequest = new ExpenseRequest(
                "Groceries",
                new BigDecimal("650.00"),
                "Food",
                LocalDate.of(2026, 4, 25)
        );

        ExpenseResponse response = expenseService.updateExpense("expense-1", updateRequest, "user-123");

        verify(expenseEventProducer).publishUpdatedEvent(expense);
        assertThat(response.getDescription()).isEqualTo("Groceries");
        assertThat(response.getAmount()).isEqualByComparingTo("650.00");
    }

    @Test
    void deleteExpenseShouldDeleteAndPublishDeleteEvent() {
        when(expenseRepository.findByIdAndUserId("expense-1", "user-123")).thenReturn(Optional.of(expense));

        expenseService.deleteExpense("expense-1", "user-123");

        verify(expenseRepository).delete(expense);
        verify(expenseEventProducer).publishDeletedEvent("expense-1", "user-123", LocalDate.of(2026, 4, 24));
    }

    @Test
    void getMonthlySummaryShouldOnlySumMatchingMonthAndYear() {
        Expense anotherMonth = new Expense(
                "expense-2",
                "Transport",
                new BigDecimal("200.00"),
                "Transport",
                LocalDate.of(2026, 5, 1),
                "user-123",
                LocalDateTime.now()
        );

        when(expenseRepository.findAllByUserIdOrderByDateDescCreatedAtDesc("user-123")).thenReturn(List.of(expense, anotherMonth));

        BigDecimal result = expenseService.getMonthlySummary(4, 2026, "user-123");

        assertThat(result).isEqualByComparingTo("1800.00");
    }
}
