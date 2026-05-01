package com.banking.system.repository;

import com.banking.system.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByReferenceId(String referenceId);

    @Query("""
            SELECT t FROM Transaction t
            WHERE t.sourceAccount.id = :accountId OR t.destinationAccount.id = :accountId
            ORDER BY t.createdAt DESC
            """)
    Page<Transaction> findAllByAccountId(@Param("accountId") Long accountId, Pageable pageable);

    @Query("""
            SELECT t FROM Transaction t
            WHERE (t.sourceAccount.id = :accountId OR t.destinationAccount.id = :accountId)
            AND t.createdAt BETWEEN :startDate AND :endDate
            ORDER BY t.createdAt DESC
            """)
    Page<Transaction> findByAccountIdAndDateRange(
            @Param("accountId") Long accountId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("""
            SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t
            WHERE t.destinationAccount.id = :accountId
            AND t.type = 'DEPOSIT'
            AND t.status = 'COMPLETED'
            AND t.createdAt >= :since
            """)
    BigDecimal sumDepositsSince(@Param("accountId") Long accountId, @Param("since") LocalDateTime since);
}