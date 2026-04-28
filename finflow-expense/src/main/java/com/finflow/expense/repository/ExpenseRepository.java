package com.finflow.expense.repository;

import com.finflow.expense.model.Expense;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends MongoRepository<Expense, String> {

    List<Expense> findAllByUserIdOrderByDateDescCreatedAtDesc(String userId);

    Optional<Expense> findByIdAndUserId(String id, String userId);
}
