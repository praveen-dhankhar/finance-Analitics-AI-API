package com.financeapp.repository;

import com.financeapp.entity.ForecastJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ForecastJobRepository extends JpaRepository<ForecastJob, Long> {
}


