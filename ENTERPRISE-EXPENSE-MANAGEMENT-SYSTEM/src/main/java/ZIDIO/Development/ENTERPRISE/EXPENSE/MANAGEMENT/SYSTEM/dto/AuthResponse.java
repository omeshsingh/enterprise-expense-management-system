package ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
// @AllArgsConstructor // Use specific constructor if preferred
public class AuthResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private String username;
    private String email;
    private Long userId;

    public AuthResponse(String accessToken, String username, String email, Long userId) {
        this.accessToken = accessToken;
        this.username = username;
        this.email = email;
        this.userId = userId;
    }
}