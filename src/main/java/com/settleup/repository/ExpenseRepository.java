package com.settleup.repository;

import com.settleup.entity.ExpenseEntity;
import com.settleup.enums.ExpenseCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<ExpenseEntity, Long> {

    List<ExpenseEntity> findByGroupIdAndIsDeletedFalse(Long groupId);

    @Query("SELECT e FROM ExpenseEntity e WHERE e.group.id = :groupId AND e.isDeleted = false " +
           "AND (:startDate IS NULL OR e.date >= :startDate) " +
           "AND (:endDate IS NULL OR e.date <= :endDate) " +
           "AND (:category IS NULL OR e.category = :category) " +
           "AND (:paidById IS NULL OR e.paidBy.id = :paidById)")
    Page<ExpenseEntity> findGroupExpensesFiltered(
            @Param("groupId") Long groupId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("category") ExpenseCategory category,
            @Param("paidById") Long paidById,
            Pageable pageable
    );
}
