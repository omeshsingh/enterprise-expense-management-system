package ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.repository;

import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.entity.ApprovalHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalHistoryRepository extends JpaRepository<ApprovalHistory, Long> {
    List<ApprovalHistory> findByExpenseIdOrderByActionDateAsc(Long expenseId);
}