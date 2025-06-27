package ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.security.oauth2;

import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.entity.Role;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.entity.User;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.exception.ApiException; // Your custom exception
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.repository.RoleRepository;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.repository.UserRepository;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.service.audit.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService { // Extend OidcUserService

    private static final Logger logger = LoggerFactory.getLogger(CustomOidcUserService.class);
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuditLogService auditLogService; // For logging OAuth2 user creation/login

    @Override
    @Transactional // Ensure operations are within a transaction
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest); // Delegate to default to get OidcUser
        // oidcUser.getAttributes() contains user info from Google (email, name, sub, etc.)
        // oidcUser.getIdToken() / oidcUser.getUserInfo()

        String email = oidcUser.getEmail();
        if (email == null) {
            logger.error("Email not found from OAuth2 provider for user: {}", oidcUser.getName());
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider.");
        }

        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            // User exists, update if necessary (e.g., name from provider)
            // For simplicity, we won't update existing user details from provider here
            logger.info("Existing user {} logged in via OAuth2 (Google).", user.getUsername());
            auditLogService.logAction(user.getUsername(), "USER_OAUTH2_LOGIN", "OAuth2 Login", user.getId(), "User logged in via Google.");
        } else {
            // New user, register them
            logger.info("New user with email {} detected via OAuth2 (Google). Registering...", email);
            user = new User();
            user.setEmail(email);

            // Create a unique username, e.g., from email prefix or a UUID if names clash
            String username = email.split("@")[0];
            if (userRepository.existsByUsername(username)) {
                username = username + "_" + UUID.randomUUID().toString().substring(0, 4);
            }
            user.setUsername(username);

            // Password is not needed for OAuth2-only users, but our entity requires it.
            // Set a random, unguessable password. They won't use this to log in via form.
            user.setPassword(UUID.randomUUID().toString()); // Or use a PasswordEncoder with a random string

            user.setFirstName(oidcUser.getGivenName());
            user.setLastName(oidcUser.getFamilyName());
            user.setEnabled(true);

            Role defaultRole = roleRepository.findByName("ROLE_EMPLOYEE")
                    .orElseThrow(() -> {
                        logger.error("Default role ROLE_EMPLOYEE not found for OAuth2 user registration.");
                        return new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Default role ROLE_EMPLOYEE not found!");
                    });
            Set<Role> roles = new HashSet<>();
            roles.add(defaultRole);
            user.setRoles(roles);

            user = userRepository.save(user);
            logger.info("New user {} registered successfully via OAuth2 (Google).", user.getUsername());
            auditLogService.logAction(user.getUsername(), "USER_OAUTH2_REGISTERED", "User", user.getId(), "New user registered via Google: " + user.getUsername());
        }

        // We need to return an OidcUser that also implements our UserDetails (i.e., our User entity)
        // Spring Security will use the 'authorities' from this returned UserDetails object.
        // Option 1: Create a new OidcUserDecorator that wraps our User entity (cleaner)
        // Option 2: Make our User entity implement OidcUser (more coupling) - Let's try to adapt our User for now for simplicity.

        // For Option 2, our User entity needs to be adaptable to carry OIDC claims if needed
        // or we just use the principal that Spring Security expects.
        // The important part is that the Authentication principal becomes our User entity.
        // Spring Security typically wraps our UserDetails in a relevant OidcUser implementation
        // if we configure a UserDetailsService.
        //
        // Here, since we are in OidcUserService, the goal is to process the OIDC user
        // and ensure our *local* User representation is correctly loaded or created.
        // The 'user' variable above IS our local User entity.
        // We will rely on a custom AuthenticationSuccessHandler (next step) to issue JWT.

        // The OidcUser returned by super.loadUser(userRequest) is good enough for Spring Security's internal processing.
        // Our main goal here was to ensure the user exists in our DB.
        // The Authentication object's principal will be this oidcUser,
        // but our success handler will fetch our local User.
        return oidcUser; // Or a custom OidcUser implementation that wraps your User entity
    }
}
