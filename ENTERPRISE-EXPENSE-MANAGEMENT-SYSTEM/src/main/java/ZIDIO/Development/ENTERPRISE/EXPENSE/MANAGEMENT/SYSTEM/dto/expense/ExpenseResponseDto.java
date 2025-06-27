package ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.dto.expense;

import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.entity.ExpenseStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
public class ExpenseResponseDto {
    private Long id;
    private String description;
    private BigDecimal amount;
    private LocalDate expenseDate;
    private ExpenseStatus status;
    private Long userId;
    private String username; // Denormalized for convenience
    private Long categoryId;
    private String categoryName; // Denormalized
    private List<AttachmentResponseDto> attachments;
    private Instant createdAt;
    private Instant updatedAt;
}
