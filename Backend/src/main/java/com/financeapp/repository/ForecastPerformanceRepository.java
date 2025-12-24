package com.financeapp.repository;

import com.financeapp.entity.ForecastPerformance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ForecastPerformanceRepository extends JpaRepository<ForecastPerformance, Long> {
}


