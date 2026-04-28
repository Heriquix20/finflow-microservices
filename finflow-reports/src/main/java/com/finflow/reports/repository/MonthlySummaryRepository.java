package com.finflow.reports.repository;

import com.finflow.reports.model.MonthlySummary;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface MonthlySummaryRepository extends MongoRepository<MonthlySummary, String> {

    Optional<MonthlySummary> findByUserIdAndMonthAndYear(String userId, int month, int year);

    List<MonthlySummary> findAllByUserIdAndMonthAndYearOrderByUpdatedAtDesc(String userId, int month, int year);

    List<MonthlySummary> findTop12ByUserIdOrderByYearDescMonthDesc(String userId);
}
