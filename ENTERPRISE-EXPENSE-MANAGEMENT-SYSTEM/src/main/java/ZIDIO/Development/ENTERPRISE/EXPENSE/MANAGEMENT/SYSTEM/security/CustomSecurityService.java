// In a new file, e.g., SecurityService.java in .security or .service package
package ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.security; // or service

import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.entity.Expense;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.entity.User;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.repository.ExpenseRepository;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.service.expense.ExpenseService; // For getCurrentAuthenticatedUser
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service("securityService") // Bean name for SpEL
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomSecurityService {

    private final ExpenseRepository expenseRepository;
    // private final ExpenseService expenseService; // Or inject UserRepository directly for getCurrentAuthenticatedUser logic

    // To get current user details if needed, but usually Authentication principal is enough
    // private User getCurrentUser(Authentication authentication) {
    //    return expenseService.getCurrentAuthenticatedUserFromPrincipal(authentication.getPrincipal());
    // }

    public boolean isExpenseOwner(Authentication authentication, Long expenseId) {
        String currentUsername = authentication.getName();
        Expense expense = expenseRepository.findById(expenseId).orElse(null);
        return expense != null && expense.getUser().getUsername().equals(currentUsername);
    }

    public boolean isManagerOfExpenseOwner(Authentication authentication, Long expenseId) {
        User manager = (User) authentication.getPrincipal(); // Assuming principal is User entity
        Expense expense = expenseRepository.findById(expenseId).orElse(null);
        if (expense == null || expense.getUser().getManager() == null) {
            return false;
        }
        return expense.getUser().getManager().getId().equals(manager.getId());
    }
}