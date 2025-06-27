package ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.dto.user;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequestDto {
    @Size(max = 50)
    private String firstName;

    @Size(max = 50)
    private String lastName;
    // Add other updatable fields if needed, e.g., email, but handle email verification
}