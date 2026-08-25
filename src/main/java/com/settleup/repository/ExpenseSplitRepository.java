package com.settleup.repository;

import com.settleup.entity.ExpenseSplitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseSplitRepository extends JpaRepository<ExpenseSplitEntity, Long> {
    List<ExpenseSplitEntity> findByExpenseId(Long expenseId);
    void deleteByExpenseId(Long expenseId);

    @Query("SELECT es FROM ExpenseSplitEntity es WHERE es.expense.group.id = :groupId AND es.expense.isDeleted = false")
    List<ExpenseSplitEntity> findActiveSplitsByGroupId(@Param("groupId") Long groupId);
}
