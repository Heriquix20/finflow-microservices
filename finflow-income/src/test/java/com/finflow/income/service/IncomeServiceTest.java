package com.finflow.income.service;

import com.finflow.income.dto.IncomeRequest;
import com.finflow.income.dto.IncomeResponse;
import com.finflow.income.model.Income;
import com.finflow.income.producer.IncomeEventProducer;
import com.finflow.income.repository.IncomeRepository;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IncomeServiceTest {

    @Mock
    private IncomeRepository incomeRepository;

    @Mock
    private IncomeEventProducer incomeEventProducer;

    @InjectMocks
    private IncomeService incomeService;

    private IncomeRequest request;
    private Income income;

    @BeforeEach
    void setUp() {
        request = new IncomeRequest(
                "Salary",
                new BigDecimal("5000.00"),
                "Salary",
                LocalDate.of(2026, 4, 24)
        );

        income = new Income(
                "income-1",
                "Salary",
                new BigDecimal("5000.00"),
                "Salary",
                LocalDate.of(2026, 4, 24),
                "user-123",
                LocalDateTime.of(2026, 4, 24, 10, 0)
        );
    }

    @Test
    void createIncomeShouldPersistAndPublishEvent() {
        when(incomeRepository.save(any(Income.class))).thenReturn(income);

        IncomeResponse response = incomeService.createIncome(request, "user-123");

        ArgumentCaptor<Income> captor = ArgumentCaptor.forClass(Income.class);
        verify(incomeRepository).save(captor.capture());
        verify(incomeEventProducer).publishCreatedEvent(income);

        Income saved = captor.getValue();
        assertThat(saved.getDescription()).isEqualTo("Salary");
        assertThat(saved.getAmount()).isEqualByComparingTo("5000.00");
        assertThat(saved.getCategory()).isEqualTo("Salary");
        assertThat(saved.getDate()).isEqualTo(LocalDate.of(2026, 4, 24));
        assertThat(saved.getUserId()).isEqualTo("user-123");
        assertThat(saved.getCreatedAt()).isNotNull();

        assertThat(response.getId()).isEqualTo("income-1");
        assertThat(response.getUserId()).isEqualTo("user-123");
    }

    @Test
    void getAllIncomesShouldMapRepositoryResult() {
        when(incomeRepository.findAllByUserIdOrderByDateDescCreatedAtDesc("user-123")).thenReturn(List.of(income));

        List<IncomeResponse> responses = incomeService.getAllIncomes("user-123");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo("income-1");
        assertThat(responses.get(0).getAmount()).isEqualByComparingTo("5000.00");
    }

    @Test
    void getIncomeByIdShouldReturnMappedResponse() {
        when(incomeRepository.findByIdAndUserId("income-1", "user-123")).thenReturn(Optional.of(income));

        IncomeResponse response = incomeService.getIncomeById("income-1", "user-123");

        assertThat(response.getId()).isEqualTo("income-1");
        assertThat(response.getDescription()).isEqualTo("Salary");
    }

    @Test
    void getIncomeByIdShouldThrowWhenIncomeDoesNotExist() {
        when(incomeRepository.findByIdAndUserId("missing", "user-123")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> incomeService.getIncomeById("missing", "user-123"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException responseStatusException = (ResponseStatusException) ex;
                    assertThat(responseStatusException.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(responseStatusException.getReason()).isEqualTo("Income not found.");
                });
    }

    @Test
    void updateIncomeShouldPersistChangesAndPublishEvent() {
        when(incomeRepository.findByIdAndUserId("income-1", "user-123")).thenReturn(Optional.of(income));
        when(incomeRepository.save(any(Income.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IncomeRequest updateRequest = new IncomeRequest(
                "Freelance",
                new BigDecimal("2500.00"),
                "Freelance",
                LocalDate.of(2026, 4, 25)
        );

        IncomeResponse response = incomeService.updateIncome("income-1", updateRequest, "user-123");

        verify(incomeEventProducer).publishUpdatedEvent(income);
        assertThat(response.getDescription()).isEqualTo("Freelance");
        assertThat(response.getAmount()).isEqualByComparingTo("2500.00");
        assertThat(response.getCategory()).isEqualTo("Freelance");
        assertThat(response.getDate()).isEqualTo(LocalDate.of(2026, 4, 25));
    }

    @Test
    void deleteIncomeShouldDeleteAndPublishDeleteEvent() {
        when(incomeRepository.findByIdAndUserId("income-1", "user-123")).thenReturn(Optional.of(income));

        incomeService.deleteIncome("income-1", "user-123");

        verify(incomeRepository).delete(income);
        verify(incomeEventProducer).publishDeletedEvent("income-1", "user-123", LocalDate.of(2026, 4, 24));
    }

    @Test
    void getMonthlySummaryShouldOnlySumMatchingMonthAndYear() {
        Income anotherMonth = new Income(
                "income-2",
                "Bonus",
                new BigDecimal("700.00"),
                "Bonus",
                LocalDate.of(2026, 5, 1),
                "user-123",
                LocalDateTime.now()
        );

        when(incomeRepository.findAllByUserIdOrderByDateDescCreatedAtDesc("user-123")).thenReturn(List.of(income, anotherMonth));

        BigDecimal result = incomeService.getMonthlySummary(4, 2026, "user-123");

        assertThat(result).isEqualByComparingTo("5000.00");
    }
}
