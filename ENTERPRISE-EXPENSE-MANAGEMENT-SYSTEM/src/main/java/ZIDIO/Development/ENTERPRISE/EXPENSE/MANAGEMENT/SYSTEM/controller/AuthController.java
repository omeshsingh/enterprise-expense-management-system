package ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.controller;

import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.dto.AuthResponse;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.dto.LoginRequest;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.dto.RegisterRequest;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.service.AuthService; // Correct import
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.service.notification.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    //email test injection
    private final EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        // Consider creating a UserResponseDto to avoid exposing the full User entity if returning user details
        authService.register(registerRequest); // Service method returns User, but we just send a message
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully!");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse authResponse = authService.login(loginRequest);
        return ResponseEntity.ok(authResponse);
    }

    // Example protected endpoint for testing
    @GetMapping("/hello")
    public ResponseEntity<String> sayHello() {
        // Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // String currentPrincipalName = authentication.getName();
        // return ResponseEntity.ok("Hello " + currentPrincipalName + ", you are authenticated!");
        return ResponseEntity.ok("Hello, you are authenticated!");
    }
    // In any @RestController
// @Autowired (or use constructor injection)
//    private EmailService emailService;

    // === TEST EMAIL ENDPOINT ===
    @GetMapping("/test-email") // Using GET for simplicity of testing in browser/Postman
    public ResponseEntity<String> sendTestEmail() {
        String recipientEmail = "your_personal_email_to_check@example.com"; // <<<< IMPORTANT: CHANGE THIS
        String subject = "Spring Boot Test Email (Brevo)";
        String body = "This is a test email sent from the Spring Boot application using Brevo SMTP.\n\nIf you received this, it's working!";

        try {
            emailService.sendSimpleMessage(recipientEmail, subject, body);
            // Or for HTML:
            // String htmlBody = "<h1>Test HTML Email</h1><p>This is a <b>test</b>.</p>";
            // emailService.sendHtmlMessage(recipientEmail, subject, htmlBody);
            return ResponseEntity.ok("Test email has been queued for sending to " + recipientEmail + "!");
        } catch (Exception e) {
            // Log the exception in EmailService, but controller can return a generic error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send test email: " + e.getMessage());
        }
    }
}