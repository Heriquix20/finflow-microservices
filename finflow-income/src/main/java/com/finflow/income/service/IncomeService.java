package com.finflow.income.service;

import com.finflow.income.dto.IncomeRequest;
import com.finflow.income.dto.IncomeResponse;
import com.finflow.income.model.Income;
import com.finflow.income.producer.IncomeEventProducer;
import com.finflow.income.repository.IncomeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class IncomeService {

    private final IncomeRepository incomeRepository;
    private final IncomeEventProducer incomeEventProducer;

    public IncomeService(IncomeRepository incomeRepository, IncomeEventProducer incomeEventProducer) {
        this.incomeRepository = incomeRepository;
        this.incomeEventProducer = incomeEventProducer;
    }

    public IncomeResponse createIncome(IncomeRequest request, String userId) {
        Income income = new Income();
        applyRequestToIncome(income, request);
        income.setUserId(userId);
        income.setCreatedAt(LocalDateTime.now());

        Income savedIncome = incomeRepository.save(income);
        incomeEventProducer.publishCreatedEvent(savedIncome);

        return toResponse(savedIncome);
    }

    public List<IncomeResponse> getAllIncomes(String userId) {
        return incomeRepository.findAllByUserIdOrderByDateDescCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public IncomeResponse getIncomeById(String id, String userId) {
        return toResponse(findIncomeByIdAndUserId(id, userId));
    }

    public IncomeResponse updateIncome(String id, IncomeRequest request, String userId) {
        Income income = findIncomeByIdAndUserId(id, userId);
        applyRequestToIncome(income, request);

        Income updatedIncome = incomeRepository.save(income);
        incomeEventProducer.publishUpdatedEvent(updatedIncome);

        return toResponse(updatedIncome);
    }

    public void deleteIncome(String id, String userId) {
        Income income = findIncomeByIdAndUserId(id, userId);

        incomeRepository.delete(income);
        incomeEventProducer.publishDeletedEvent(income.getId(), income.getUserId(), income.getDate());
    }

    public BigDecimal getMonthlySummary(int month, int year, String userId) {
        return incomeRepository.findAllByUserIdOrderByDateDescCreatedAtDesc(userId)
                .stream()
                .filter(income -> income.getDate() != null)
                .filter(income -> income.getDate().getMonthValue() == month && income.getDate().getYear() == year)
                .map(Income::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Income findIncomeByIdAndUserId(String id, String userId) {
        return incomeRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Income not found."));
    }

    private void applyRequestToIncome(Income income, IncomeRequest request) {
        income.setDescription(request.getDescription());
        income.setAmount(request.getAmount());
        income.setCategory(request.getCategory());
        income.setDate(request.getDate());
    }

    private IncomeResponse toResponse(Income income) {
        return new IncomeResponse(
                income.getId(),
                income.getDescription(),
                income.getAmount(),
                income.getCategory(),
                income.getDate(),
                income.getUserId(),
                income.getCreatedAt()
        );
    }
}
