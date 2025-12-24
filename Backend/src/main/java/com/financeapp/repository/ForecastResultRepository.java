package com.financeapp.repository;

import com.financeapp.entity.ForecastResult;
import com.financeapp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ForecastResultRepository extends JpaRepository<ForecastResult, Long> {

    Page<ForecastResult> findByUser(User user, Pageable pageable);

    @Query("SELECT fr FROM ForecastResult fr WHERE fr.user.id = :userId AND fr.targetDate BETWEEN :from AND :to ORDER BY fr.targetDate")
    List<ForecastResult> findForUserBetween(@Param("userId") Long userId,
                                            @Param("from") LocalDate from,
                                            @Param("to") LocalDate to);
}


