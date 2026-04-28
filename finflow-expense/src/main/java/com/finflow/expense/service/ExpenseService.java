package com.finflow.expense.service;

import com.finflow.expense.dto.ExpenseRequest;
import com.finflow.expense.dto.ExpenseResponse;
import com.finflow.expense.dto.PagedResponse;
import com.finflow.expense.model.Expense;
import com.finflow.expense.producer.ExpenseEventProducer;
import com.finflow.expense.repository.ExpenseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseEventProducer expenseEventProducer;

    public ExpenseService(ExpenseRepository expenseRepository, ExpenseEventProducer expenseEventProducer) {
        this.expenseRepository = expenseRepository;
        this.expenseEventProducer = expenseEventProducer;
    }

    public ExpenseResponse createExpense(ExpenseRequest request, String userId) {
        Expense expense = new Expense();
        applyRequestToExpense(expense, request);
        expense.setUserId(userId);
        expense.setCreatedAt(LocalDateTime.now());

        Expense savedExpense = expenseRepository.save(expense);
        expenseEventProducer.publishCreatedEvent(savedExpense);

        return toResponse(savedExpense);
    }

    public List<ExpenseResponse> getAllExpenses(String userId, String category, LocalDate startDate, LocalDate endDate) {
        return filterExpenses(userId, category, startDate, endDate)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public PagedResponse<ExpenseResponse> getPagedExpenses(
            String userId,
            int page,
            int size,
            String category,
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<ExpenseResponse> filteredItems = getAllExpenses(userId, category, startDate, endDate);
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

    public ExpenseResponse getExpenseById(String id, String userId) {
        return toResponse(findExpenseByIdAndUserId(id, userId));
    }

    public ExpenseResponse updateExpense(String id, ExpenseRequest request, String userId) {
        Expense expense = findExpenseByIdAndUserId(id, userId);
        applyRequestToExpense(expense, request);

        Expense updatedExpense = expenseRepository.save(expense);
        expenseEventProducer.publishUpdatedEvent(updatedExpense);

        return toResponse(updatedExpense);
    }

    public void deleteExpense(String id, String userId) {
        Expense expense = findExpenseByIdAndUserId(id, userId);

        expenseRepository.delete(expense);
        expenseEventProducer.publishDeletedEvent(expense.getId(), expense.getUserId(), expense.getDate());
    }

    public BigDecimal getMonthlySummary(int month, int year, String userId) {
        return filterExpenses(userId, null, null, null)
                .stream()
                .filter(expense -> expense.getDate() != null)
                .filter(expense -> expense.getDate().getMonthValue() == month && expense.getDate().getYear() == year)
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<Expense> filterExpenses(String userId, String category, LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);

        return expenseRepository.findAllByUserIdOrderByDateDescCreatedAtDesc(userId)
                .stream()
                .filter(expense -> category == null || category.isBlank() || category.equalsIgnoreCase(expense.getCategory()))
                .filter(expense -> startDate == null || (expense.getDate() != null && !expense.getDate().isBefore(startDate)))
                .filter(expense -> endDate == null || (expense.getDate() != null && !expense.getDate().isAfter(endDate)))
                .toList();
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "startDate must be before or equal to endDate.");
        }
    }

    private Expense findExpenseByIdAndUserId(String id, String userId) {
        return expenseRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found."));
    }

    private void applyRequestToExpense(Expense expense, ExpenseRequest request) {
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory());
        expense.setDate(request.getDate());
    }

    private ExpenseResponse toResponse(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getDescription(),
                expense.getAmount(),
                expense.getCategory(),
                expense.getDate(),
                expense.getUserId(),
                expense.getCreatedAt()
        );
    }
}
