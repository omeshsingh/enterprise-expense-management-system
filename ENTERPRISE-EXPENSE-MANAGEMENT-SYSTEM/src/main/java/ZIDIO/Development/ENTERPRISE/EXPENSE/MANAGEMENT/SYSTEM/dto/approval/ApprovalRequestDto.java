package ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.dto.approval;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ApprovalRequestDto {

    @Size(max = 1000, message = "Comments cannot exceed 1000 characters")
    private String comments;
}