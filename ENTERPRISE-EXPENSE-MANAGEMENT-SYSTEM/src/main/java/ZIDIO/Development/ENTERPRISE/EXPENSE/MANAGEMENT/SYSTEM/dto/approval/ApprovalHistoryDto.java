package ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.dto.approval;

import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.entity.ExpenseStatus;
import lombok.Data;

import java.time.Instant;

@Data
public class ApprovalHistoryDto {
    private Long id;
    private Long expenseId;
    private Long approverUserId;
    private String approverUsername; // Denormalized
    private ExpenseStatus statusBefore;
    private ExpenseStatus statusAfter;
    private String comments;
    private Instant actionDate;
}
