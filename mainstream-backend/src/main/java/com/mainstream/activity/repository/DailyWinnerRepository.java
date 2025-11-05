package com.mainstream.activity.repository;

import com.mainstream.activity.entity.DailyWinner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyWinnerRepository extends JpaRepository<DailyWinner, Long> {

    List<DailyWinner> findByWinnerDateOrderByCategoryAsc(LocalDate date);

    Optional<DailyWinner> findByWinnerDateAndCategory(LocalDate date, DailyWinner.WinnerCategory category);

    List<DailyWinner> findByUserIdOrderByWinnerDateDesc(Long userId);

    @Query("SELECT COUNT(dw) FROM DailyWinner dw WHERE dw.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    @Query("SELECT dw FROM DailyWinner dw WHERE dw.winnerDate >= :startDate ORDER BY dw.winnerDate DESC, dw.category ASC")
    List<DailyWinner> findRecentWinners(@Param("startDate") LocalDate startDate);
}
