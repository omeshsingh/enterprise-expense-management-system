package ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.service;

import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.dto.user.UserResponseDto;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.dto.user.UserUpdateRequestDto;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.entity.User;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.exception.ApiException;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils; // For StringUtils.hasText

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    // private final PasswordEncoder passwordEncoder; // Inject if handling password changes

    @Transactional(readOnly = true)
    public UserResponseDto getUserDetailsByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("User not found by username: {}", username);
                    return new ApiException(HttpStatus.NOT_FOUND, "User not found: " + username);
                });
        return UserResponseDto.fromUser(user);
    }

    @Transactional
    public UserResponseDto updateUserProfile(String username, UserUpdateRequestDto updateRequest) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("Attempt to update profile for non-existent user: {}", username);
                    return new ApiException(HttpStatus.NOT_FOUND, "User not found: " + username);
                });

        boolean updated = false;
        if (StringUtils.hasText(updateRequest.getFirstName()) && !updateRequest.getFirstName().equals(user.getFirstName())) {
            user.setFirstName(updateRequest.getFirstName());
            updated = true;
        }
        if (StringUtils.hasText(updateRequest.getLastName()) && !updateRequest.getLastName().equals(user.getLastName())) {
            user.setLastName(updateRequest.getLastName());
            updated = true;
        }

        if (updated) {
            User updatedUser = userRepository.save(user);
            logger.info("User profile updated for username: {}", username);
            return UserResponseDto.fromUser(updatedUser);
        } else {
            logger.info("No changes detected for user profile: {}", username);
            return UserResponseDto.fromUser(user); // Return current state if no changes
        }
    }

    // Add methods for change password, etc., later
}