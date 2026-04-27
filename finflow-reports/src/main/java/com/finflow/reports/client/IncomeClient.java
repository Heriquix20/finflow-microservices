package com.finflow.reports.client;

import com.finflow.reports.client.dto.IncomeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "finflow-income", url = "${finflow.services.income.url:http://localhost:8081}")
public interface IncomeClient {

    @GetMapping("/incomes")
    List<IncomeResponse> getAllIncomes(@RequestHeader("X-User-Id") String userId);
}
