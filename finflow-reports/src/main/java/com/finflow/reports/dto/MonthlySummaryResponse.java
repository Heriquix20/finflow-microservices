package com.finflow.reports.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MonthlySummaryResponse {

    private String userId;
    private int month;
    private int year;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal balance;
    private LocalDateTime updatedAt;

    public MonthlySummaryResponse() {
    }

    public MonthlySummaryResponse(String userId, int month, int year, BigDecimal totalIncome, BigDecimal totalExpense, BigDecimal balance, LocalDateTime updatedAt) {
        this.userId = userId;
        this.month = month;
        this.year = year;
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.balance = balance;
        this.updatedAt = updatedAt;
    }

    public String getUserId() {
        return userId;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public BigDecimal getTotalExpense() {
        return totalExpense;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
