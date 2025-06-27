package ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.dto.expense;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter; // You can add these individually if @Data gives issues
import lombok.Setter; // You can add these individually if @Data gives issues

@Data // This is the primary annotation that should generate getName()
public class ExpenseCategoryDto {

    private Long id; // For response

    @NotBlank(message = "Category name cannot be blank")
    private String name; // The field from which getName() is generated
}