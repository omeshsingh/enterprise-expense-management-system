package ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.service.attachment; // Or a sub-package like service.storage

import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.entity.Attachment;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.entity.User;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.exception.ApiException;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.repository.AttachmentRepository;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.service.expense.ExpenseService;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.service.storage.FileStorageService; // Assuming this exists
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final FileStorageService fileStorageService;
    private final ExpenseService expenseService; // To reuse getCurrentAuthenticatedUser if needed for auth

    @Transactional(readOnly = true)
    public Attachment getAttachmentByIdAndVerifyAccess(Long attachmentId) {
        User currentUser = expenseService.getCurrentAuthenticatedUser(); // Or a similar method to get current user

        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Attachment not found with ID: " + attachmentId));

        // Authorization check: Ensure the current user owns the expense related to this attachment,
        // or is an Admin/Manager who can view it.
        // This relies on Expense having a User field.
        Long expenseOwnerId = attachment.getExpense().getUser().getId();
        boolean isAdmin = currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
        // Add more specific manager check if needed (e.g., is manager of expenseOwnerId)
        boolean isManagerViewingTeamExpense = currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_MANAGER")) /* && isManagerOf(currentUser, attachment.getExpense().getUser()) */;


        if (!expenseOwnerId.equals(currentUser.getId()) && !isAdmin && !isManagerViewingTeamExpense) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You are not authorized to access this attachment.");
        }
        return attachment;
    }

    public Resource loadAttachmentAsResource(Attachment attachment) {
        return fileStorageService.loadFileAsResource(attachment.getFilePath());
    }
}