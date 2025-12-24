package com.financeapp.repository;

import com.financeapp.entity.ForecastAnomaly;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ForecastAnomalyRepository extends JpaRepository<ForecastAnomaly, Long> {
}


