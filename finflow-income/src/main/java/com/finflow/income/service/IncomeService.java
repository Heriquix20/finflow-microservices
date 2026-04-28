package com.finflow.income.service;

import com.finflow.income.dto.IncomeRequest;
import com.finflow.income.dto.IncomeResponse;
import com.finflow.income.dto.PagedResponse;
import com.finflow.income.model.Income;
import com.finflow.income.producer.IncomeEventProducer;
import com.finflow.income.repository.IncomeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    public List<IncomeResponse> getAllIncomes(String userId, String category, LocalDate startDate, LocalDate endDate) {
        return filterIncomes(userId, category, startDate, endDate)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public PagedResponse<IncomeResponse> getPagedIncomes(
            String userId,
            int page,
            int size,
            String category,
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<IncomeResponse> filteredItems = getAllIncomes(userId, category, startDate, endDate);
        int fromIndex = Math.min(page * size, filteredItems.size());
        int toIndex = Math.min(fromIndex + size, filteredItems.size());
        int totalPages = filteredItems.isEmpty() ? 0 : (int) Math.ceil((double) filteredItems.size() / size);

        return new PagedResponse<>(
                filteredItems.subList(fromIndex, toIndex),
                page,
                size,
                filteredItems.size(),
                totalPages,
                toIndex < filteredItems.size(),
                page > 0
        );
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
        return filterIncomes(userId, null, null, null)
                .stream()
                .filter(income -> income.getDate() != null)
                .filter(income -> income.getDate().getMonthValue() == month && income.getDate().getYear() == year)
                .map(Income::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<Income> filterIncomes(String userId, String category, LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);

        return incomeRepository.findAllByUserIdOrderByDateDescCreatedAtDesc(userId)
                .stream()
                .filter(income -> category == null || category.isBlank() || category.equalsIgnoreCase(income.getCategory()))
                .filter(income -> startDate == null || (income.getDate() != null && !income.getDate().isBefore(startDate)))
                .filter(income -> endDate == null || (income.getDate() != null && !income.getDate().isAfter(endDate)))
                .toList();
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "startDate must be before or equal to endDate.");
        }
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
