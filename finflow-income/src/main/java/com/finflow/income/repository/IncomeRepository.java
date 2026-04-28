package com.finflow.income.repository;

import com.finflow.income.model.Income;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface IncomeRepository extends MongoRepository<Income, String> {

    List<Income> findAllByUserIdOrderByDateDescCreatedAtDesc(String userId);

    Optional<Income> findByIdAndUserId(String id, String userId);
}
