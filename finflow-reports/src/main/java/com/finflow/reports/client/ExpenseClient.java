package com.finflow.reports.client;

import com.finflow.reports.client.dto.ExpenseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "finflow-expense", url = "${finflow.services.expense.url:http://localhost:8082}")
public interface ExpenseClient {

    @GetMapping("/expenses")
    List<ExpenseResponse> getAllExpenses(@RequestHeader("X-User-Id") String userId);
}
