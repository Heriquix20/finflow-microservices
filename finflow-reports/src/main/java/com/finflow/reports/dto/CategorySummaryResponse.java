package com.finflow.reports.dto;

import java.math.BigDecimal;

public class CategorySummaryResponse {

    private String category;
    private BigDecimal total;

    public CategorySummaryResponse() {
    }

    public CategorySummaryResponse(String category, BigDecimal total) {
        this.category = category;
        this.total = total;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getTotal() {
        return total;
    }
}
