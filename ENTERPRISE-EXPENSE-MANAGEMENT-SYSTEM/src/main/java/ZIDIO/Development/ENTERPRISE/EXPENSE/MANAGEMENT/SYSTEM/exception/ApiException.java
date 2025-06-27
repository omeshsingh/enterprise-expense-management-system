package ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {
    private final HttpStatus status;
    // private final String message; // message is inherited from RuntimeException

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
        // this.message = message; // No need to redefine
    }
}