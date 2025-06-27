package ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.repository;

import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.dto.report.ExpenseByCategoryDto;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.dto.report.ExpenseTrendDto;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.entity.Expense;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.entity.ExpenseStatus;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {
    Page<Expense> findByUser(User user, Pageable pageable);
    List<Expense> findByUserId(Long userId);
//    Page<Expense> findAllByStatus(List<ExpenseStatus> status, Pageable pageable);
    List<Expense> findListByStatus(ExpenseStatus status);
    Page<Expense> findAllByStatusIn(List<ExpenseStatus> statuses, Pageable pageable);

    List<Expense> findAllByStatusAndExpenseDateBetween(ExpenseStatus status, LocalDate startDate, LocalDate endDate);
    // In ExpenseRepository.java
    List<Expense> findAllByStatusAndUserIdIn(ExpenseStatus status, List<Long> userIds);
    // For pending approvals
    // boolean existsByCategoryId(Long categoryId); // If you want to check before deleting a category

    // Query for monthly trend (only for APPROVED expenses)
    // NOTE: Date formatting functions are database-specific in JPQL/SQL.
    // This uses TO_CHAR for PostgreSQL. Adjust for other DBs if necessary.
    @Query("SELECT new ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.dto.report.ExpenseTrendDto(TO_CHAR(e.expenseDate, 'YYYY-MM'), SUM(e.amount)) " +
            "FROM Expense e " +
            "WHERE e.status = ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.entity.ExpenseStatus.APPROVED AND e.expenseDate BETWEEN :startDate AND :endDate " + // Use Enum reference
            "GROUP BY TO_CHAR(e.expenseDate, 'YYYY-MM') " +
            "ORDER BY TO_CHAR(e.expenseDate, 'YYYY-MM') ASC")
    List<ExpenseTrendDto> findMonthlyExpenseTrends(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Query for breakdown by category (only for APPROVED expenses)
    @Query("SELECT new ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.dto.report.ExpenseByCategoryDto(c.name, SUM(e.amount)) " +
            "FROM Expense e JOIN e.category c " +
            "WHERE e.status = ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.entity.ExpenseStatus.APPROVED AND e.expenseDate BETWEEN :startDate AND :endDate " + // Use Enum reference
            "GROUP BY c.name " +
            "ORDER BY SUM(e.amount) DESC")
    List<ExpenseByCategoryDto> findExpenseBreakdownByCategory(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Add if needed for checking category usage before deletion
    boolean existsByCategoryId(Long categoryId);
}