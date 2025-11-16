package com.mainstream.user.repository;

import com.mainstream.user.entity.AccountDeletionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountDeletionLogRepository extends JpaRepository<AccountDeletionLog, Long> {

    Optional<AccountDeletionLog> findByDeletedUserId(Long deletedUserId);

    Page<AccountDeletionLog> findByDeletionTypeOrderByDeletedAtDesc(
        AccountDeletionLog.DeletionType deletionType,
        Pageable pageable
    );

    @Query("SELECT adl FROM AccountDeletionLog adl WHERE adl.deletedAt BETWEEN :startDate AND :endDate ORDER BY adl.deletedAt DESC")
    List<AccountDeletionLog> findByDeletedAtBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT COUNT(adl) FROM AccountDeletionLog adl WHERE adl.deletedAt >= :since")
    long countDeletionsSince(@Param("since") LocalDateTime since);

    @Query("SELECT adl FROM AccountDeletionLog adl WHERE adl.requestedBy = :adminId ORDER BY adl.deletedAt DESC")
    Page<AccountDeletionLog> findByRequestedBy(@Param("adminId") Long adminId, Pageable pageable);

    List<AccountDeletionLog> findByEmailHash(String emailHash);
}
