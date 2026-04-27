package com.finflow.reports.client.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ClientResponseDtoTest {

    @Test
    void shouldMapIncomeResponseFields() {
        IncomeResponse response = new IncomeResponse();
        LocalDate date = LocalDate.of(2026, 4, 23);
        LocalDateTime createdAt = LocalDateTime.of(2026, 4, 23, 10, 30);

        response.setId("income-1");
        response.setDescription("Salary");
        response.setAmount(BigDecimal.valueOf(5000));
        response.setCategory("Job");
        response.setDate(date);
        response.setUserId("user-123");
        response.setCreatedAt(createdAt);

        assertThat(response.getId()).isEqualTo("income-1");
        assertThat(response.getDescription()).isEqualTo("Salary");
        assertThat(response.getAmount()).isEqualByComparingTo("5000");
        assertThat(response.getCategory()).isEqualTo("Job");
        assertThat(response.getDate()).isEqualTo(date);
        assertThat(response.getUserId()).isEqualTo("user-123");
        assertThat(response.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void shouldMapExpenseResponseFields() {
        ExpenseResponse response = new ExpenseResponse();
        LocalDate date = LocalDate.of(2026, 4, 24);
        LocalDateTime createdAt = LocalDateTime.of(2026, 4, 24, 8, 15);

        response.setId("expense-1");
        response.setDescription("Rent");
        response.setAmount(BigDecimal.valueOf(1800));
        response.setCategory("Housing");
        response.setDate(date);
        response.setUserId("user-123");
        response.setCreatedAt(createdAt);

        assertThat(response.getId()).isEqualTo("expense-1");
        assertThat(response.getDescription()).isEqualTo("Rent");
        assertThat(response.getAmount()).isEqualByComparingTo("1800");
        assertThat(response.getCategory()).isEqualTo("Housing");
        assertThat(response.getDate()).isEqualTo(date);
        assertThat(response.getUserId()).isEqualTo("user-123");
        assertThat(response.getCreatedAt()).isEqualTo(createdAt);
    }
}
