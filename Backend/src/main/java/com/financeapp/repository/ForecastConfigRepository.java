package com.financeapp.repository;

import com.financeapp.entity.ForecastConfig;
import com.financeapp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForecastConfigRepository extends JpaRepository<ForecastConfig, Long> {

    Page<ForecastConfig> findByUser(User user, Pageable pageable);

    @Query("SELECT fc FROM ForecastConfig fc WHERE fc.user.id = :userId AND fc.algorithm = :algorithm")
    List<ForecastConfig> findByUserAndAlgorithm(@Param("userId") Long userId, @Param("algorithm") ForecastConfig.AlgorithmType algorithm);
}


