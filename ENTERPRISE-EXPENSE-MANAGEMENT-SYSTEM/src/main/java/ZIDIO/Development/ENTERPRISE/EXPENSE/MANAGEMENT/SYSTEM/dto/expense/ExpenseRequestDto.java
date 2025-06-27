package ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.dto.expense;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExpenseRequestDto {
    @NotBlank(message = "Description cannot be blank")
    private String description;

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Expense date cannot be null")
    @PastOrPresent(message = "Expense date cannot be in the future")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) // Expects yyyy-MM-dd
    private LocalDate expenseDate;

    @NotNull(message = "Category ID cannot be null")
    private Long categoryId;

    // Note: UserId will be taken from authenticated principal
    // Note: Status will be set by the system initially
    // Note: Attachments will be handled separately via MultipartFile
}
