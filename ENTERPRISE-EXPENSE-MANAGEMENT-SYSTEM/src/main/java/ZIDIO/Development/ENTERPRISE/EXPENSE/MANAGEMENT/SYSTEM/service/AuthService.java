package ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.service;

import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.dto.AuthResponse;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.dto.LoginRequest;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.dto.RegisterRequest;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.entity.Role;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.entity.User;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.exception.ApiException;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.repository.RoleRepository;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.repository.UserRepository;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.security.JwtService;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.service.audit.AuditLogService; // Import AuditLogService
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger; // Import Logger
import org.slf4j.LoggerFactory; // Import LoggerFactory
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class); // Added logger

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuditLogService auditLogService; // Injected AuditLogService

    @Transactional
    public User register(RegisterRequest registerRequest) {
        logger.info("Attempting to register user: {}", registerRequest.getUsername());
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            logger.warn("Registration failed: Username {} already exists.", registerRequest.getUsername());
            throw new ApiException(HttpStatus.BAD_REQUEST, "Username already exists!");
        }
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            logger.warn("Registration failed: Email {} already exists.", registerRequest.getEmail());
            throw new ApiException(HttpStatus.BAD_REQUEST, "Email already exists!");
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEnabled(true); // Users are enabled by default upon registration

        Role defaultRole = roleRepository.findByName("ROLE_EMPLOYEE")
                .orElseThrow(() -> {
                    logger.error("Default role ROLE_EMPLOYEE not found during user registration.");
                    return new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Default role ROLE_EMPLOYEE not found! Ensure it is seeded in the database.");
                });

        Set<Role> roles = new HashSet<>();
        roles.add(defaultRole);
        user.setRoles(roles);

        User savedUser = userRepository.save(user);
        logger.info("User {} registered successfully with ID: {}", savedUser.getUsername(), savedUser.getId());

        // Log user registration action
        try {
            auditLogService.logAction(
                    savedUser.getUsername(), // User performing action (or system if self-registration)
                    "USER_REGISTERED",
                    "User",
                    savedUser.getId(),
                    "New user registered: " + savedUser.getUsername() + " with email: " + savedUser.getEmail()
            );
        } catch (Exception e) {
            logger.error("Failed to log USER_REGISTERED audit event for user {}: {}", savedUser.getUsername(), e.getMessage());
            // Continue even if audit logging fails, main operation succeeded
        }


        return savedUser; // Or return a UserResponseDto to avoid exposing password hash
    }

    public AuthResponse login(LoginRequest loginRequest) {
        logger.info("Attempting login for user: {}", loginRequest.getUsername());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = (User) authentication.getPrincipal(); // Assuming your UserDetails is the User entity
        String jwtToken = jwtService.generateToken(user);

        logger.info("User {} logged in successfully.", user.getUsername());

        // Log successful login action
        try {
            auditLogService.logAction(
                    user.getUsername(),
                    "USER_LOGIN_SUCCESS",
                    "User Login", // Could also be "User" entityName
                    user.getId(),
                    "User successfully logged in."
            );
        } catch (Exception e) {
            logger.error("Failed to log USER_LOGIN_SUCCESS audit event for user {}: {}", user.getUsername(), e.getMessage());
        }

        return new AuthResponse(
                jwtToken,
                user.getUsername(),
                user.getEmail(),
                user.getId()
                // Consider adding roles to AuthResponse if frontend needs them immediately
        );
    }

    // Optional: You might add a method for explicit logout if you want to log it
     public void logout(Authentication authentication) {
         if (authentication != null && authentication.getPrincipal() instanceof User) {
             User user = (User) authentication.getPrincipal();
             logger.info("User {} logging out.", user.getUsername());
             auditLogService.logAction(
                     user.getUsername(),
                     "USER_LOGOUT",
                     "User Logout",
                     user.getId(),
                     "User logged out."
             );
             SecurityContextHolder.clearContext(); // Clear context
         }
     }
}