package ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.service.expense;

import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.dto.expense.AttachmentResponseDto;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.dto.expense.ExpenseRequestDto;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.dto.expense.ExpenseResponseDto;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.dto.approval.ApprovalHistoryDto; // For approval history method
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.entity.*;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.exception.ApiException;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.repository.ApprovalHistoryRepository;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.repository.ExpenseCategoryRepository;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.repository.ExpenseRepository;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.repository.UserRepository;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.service.audit.AuditLogService;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.service.notification.EmailService;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.service.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private static final Logger logger = LoggerFactory.getLogger(ExpenseService.class);

    private final ExpenseRepository expenseRepository;
    private final AuditLogService auditLogService;
    private final UserRepository userRepository;
    private final ExpenseCategoryRepository categoryRepository;
    private final FileStorageService fileStorageService;
    private final ApprovalHistoryRepository approvalHistoryRepository;
    private final EmailService emailService; // Injected EmailService

    @Transactional
    public ExpenseResponseDto createExpense(ExpenseRequestDto requestDto, List<MultipartFile> files) {
        User currentUser = getCurrentAuthenticatedUser();
        logger.info("User {} creating expense with description: {}", currentUser.getUsername(), requestDto.getDescription());

        ExpenseCategory category = categoryRepository.findById(requestDto.getCategoryId())
                .orElseThrow(() -> {
                    logger.warn("Category not found with ID: {}", requestDto.getCategoryId());
                    return new ApiException(HttpStatus.NOT_FOUND, "Category not found with ID: " + requestDto.getCategoryId());
                });

        Expense expense = new Expense();
        expense.setDescription(requestDto.getDescription());
        expense.setAmount(requestDto.getAmount());
        expense.setExpenseDate(requestDto.getExpenseDate());
        expense.setCategory(category);
        expense.setUser(currentUser);
        expense.setStatus(ExpenseStatus.SUBMITTED);

        // Save once to get an ID for structured file paths
        Expense tempSavedExpense = expenseRepository.save(expense);
        logger.debug("Temporarily saved expense with ID: {} for file path generation", tempSavedExpense.getId());

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    // Store files in a subdirectory specific to the user and expense ID
                    String subDirectory = "user_" + currentUser.getId() + "/expense_" + tempSavedExpense.getId();
                    String filePath = fileStorageService.storeFile(file, subDirectory);
                    Attachment attachment = new Attachment(file.getOriginalFilename(), file.getContentType(), filePath);
                    tempSavedExpense.addAttachment(attachment); // This also sets attachment.setExpense(this)
                }
            }
        }
        // Save again to persist attachments linked to the expense
        Expense finalSavedExpense = expenseRepository.save(tempSavedExpense);


        auditLogService.logAction(currentUser.getUsername(), "EXPENSE_CREATED", "Expense", finalSavedExpense.getId(), "Amount: " + finalSavedExpense.getAmount() + ", Desc: " + finalSavedExpense.getDescription());
        // Notify manager
        User manager = currentUser.getManager(); // Assuming User entity has getManager()
        if (manager != null) {
            try {
                emailService.sendExpenseSubmissionNotification(finalSavedExpense, manager);
                logger.info("Sent submission notification to manager {} for expense ID {}", manager.getUsername(), finalSavedExpense.getId());
            } catch (Exception e) {
                logger.error("Failed to send submission notification for expense ID {}: {}", finalSavedExpense.getId(), e.getMessage(), e);
            }
        } else {
            logger.warn("No manager found for user {} to send submission notification for expense ID {}", currentUser.getUsername(), finalSavedExpense.getId());
        }

        logger.info("Expense with ID {} created successfully by user {}", finalSavedExpense.getId(), currentUser.getUsername());
        return convertToDto(finalSavedExpense);
    }

    @Transactional(readOnly = true)
    public Page<ExpenseResponseDto> getExpensesForCurrentUser(Pageable pageable) {
        User currentUser = getCurrentAuthenticatedUser();
        Page<Expense> expensesPage = expenseRepository.findByUser(currentUser, pageable);
        return expensesPage.map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public Page<ExpenseResponseDto> getAllExpenses(Pageable pageable) {
        // Authorization (e.g. only ADMIN/MANAGER) should be handled by @PreAuthorize in controller
        Page<Expense> expensesPage = expenseRepository.findAll(pageable);
        return expensesPage.map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public ExpenseResponseDto getExpenseById(Long expenseId) {
        User currentUser = getCurrentAuthenticatedUser();
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Expense not found with ID: " + expenseId));

        boolean isOwner = expense.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
        // Check if current user is the direct manager of the expense owner
        boolean isTheirManager = expense.getUser().getManager() != null &&
                expense.getUser().getManager().getId().equals(currentUser.getId());

        if (!isOwner && !isAdmin && !isTheirManager) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You are not authorized to view this expense.");
        }
        return convertToDto(expense);
    }

    @Transactional
    public ExpenseResponseDto updateExpense(Long expenseId, ExpenseRequestDto requestDto, List<MultipartFile> newFiles) {
        User currentUser = getCurrentAuthenticatedUser();
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Expense not found with ID: " + expenseId));

        boolean isOwner = expense.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"));

        if (!isOwner && !isAdmin) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You are not authorized to update this expense.");
        }
        // Business rule: Prevent updates if expense is already approved or paid (example)
        if (expense.getStatus() == ExpenseStatus.APPROVED || expense.getStatus() == ExpenseStatus.PAID) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot update an expense that is already " + expense.getStatus());
        }
        // Or if SUBMITTED and user is owner, they can update. If PENDING_FINANCE_APPROVAL, only admin might.

        ExpenseCategory category = categoryRepository.findById(requestDto.getCategoryId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Category not found with ID: " + requestDto.getCategoryId()));

        expense.setDescription(requestDto.getDescription());
        expense.setAmount(requestDto.getAmount());
        expense.setExpenseDate(requestDto.getExpenseDate());
        expense.setCategory(category);
        // If an employee updates, status might revert to SUBMITTED if it was REJECTED or if significant changes.
        if (isOwner && (expense.getStatus() == ExpenseStatus.REJECTED || expense.getStatus() == ExpenseStatus.SUBMITTED)) {
            expense.setStatus(ExpenseStatus.SUBMITTED); // Resubmit after changes
        }


        if (newFiles != null && !newFiles.isEmpty()) {
            for (MultipartFile file : newFiles) {
                if (file != null && !file.isEmpty()) {
                    String subDirectory = "user_" + currentUser.getId() + "/expense_" + expense.getId();
                    String filePath = fileStorageService.storeFile(file, subDirectory);
                    Attachment attachment = new Attachment(file.getOriginalFilename(), file.getContentType(), filePath);
                    expense.addAttachment(attachment);
                }
            }
        }
        Expense updatedExpense = expenseRepository.save(expense);
        auditLogService.logAction(currentUser.getUsername(), "EXPENSE_UPDATED", "Expense", updatedExpense.getId(), "Updated expense details."); // Add more change details if needed
        logger.info("Expense with ID {} updated by user {}", updatedExpense.getId(), currentUser.getUsername());
        return convertToDto(updatedExpense);
    }

    @Transactional
    public void deleteExpense(Long expenseId) {
        User currentUser = getCurrentAuthenticatedUser();
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Expense not found with ID: " + expenseId));

        if (!expense.getUser().getId().equals(currentUser.getId()) &&
                !currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"))) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You are not authorized to delete this expense.");
        }
        if (expense.getStatus() == ExpenseStatus.APPROVED || expense.getStatus() == ExpenseStatus.PAID) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot delete an expense that is already " + expense.getStatus());
        }

        expense.getAttachments().forEach(attachment -> {
            if (attachment.getFilePath() != null) {
                fileStorageService.deleteFile(attachment.getFilePath());
            }
        });
        expenseRepository.delete(expense);
        logger.info("Expense with ID {} deleted by user {}", expenseId, currentUser.getUsername());
    }

    // --- Approval Workflow Methods ---
    @Transactional
    public ExpenseResponseDto approveExpense(Long expenseId, String comments) {
        User currentUser = getCurrentAuthenticatedUser(); // This is the approver
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Expense not found with ID: " + expenseId));

        User expenseOwner = expense.getUser();
        ExpenseStatus oldStatus = expense.getStatus();
        ExpenseStatus newStatus;

        boolean isAdmin = currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
        boolean isFinance = currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_FINANCE")); // Assuming ROLE_FINANCE for finance team
        boolean isTheirManager = expenseOwner.getManager() != null && expenseOwner.getManager().getId().equals(currentUser.getId());

        BigDecimal autoApprovalThreshold = new BigDecimal("50.00"); // Make this configurable

        if (oldStatus == ExpenseStatus.SUBMITTED) {
            if (!isTheirManager && !isAdmin) { // Admin can act as a super-manager/override
                throw new ApiException(HttpStatus.FORBIDDEN, "You are not the direct manager for this user's expense or an Admin.");
            }
            // The actor must have a role that permits this first-level approval
            if (!currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_MANAGER") || role.getName().equals("ROLE_ADMIN"))){
                throw new ApiException(HttpStatus.FORBIDDEN, "You do not have manager/admin rights for initial approval.");
            }

            if (expense.getAmount().compareTo(autoApprovalThreshold) <= 0) {
                newStatus = ExpenseStatus.APPROVED;
                comments = (comments == null || comments.isBlank() ? "" : comments + " ") + "[Auto-Approved: Amount under threshold by Manager/Admin]";
                logger.info("Expense ID {} auto-approved by {} due to amount under threshold.", expenseId, currentUser.getUsername());
            } else {
                newStatus = ExpenseStatus.PENDING_FINANCE_APPROVAL;
                logger.info("Expense ID {} approved by {}, pending finance approval.", expenseId, currentUser.getUsername());
            }
        } else if (oldStatus == ExpenseStatus.PENDING_FINANCE_APPROVAL) {
            if (!isAdmin && !isFinance) {
                throw new ApiException(HttpStatus.FORBIDDEN, "Only Finance or Admin can perform this final approval.");
            }
            newStatus = ExpenseStatus.APPROVED;
            logger.info("Expense ID {} finally approved by finance/admin {}.", expenseId, currentUser.getUsername());
        } else {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Expense is not in a state that can be approved. Current status: " + oldStatus);
        }

        expense.setStatus(newStatus);
        Expense updatedExpense = expenseRepository.save(expense);
        ApprovalHistory history = new ApprovalHistory(updatedExpense, currentUser, oldStatus, newStatus, comments);
        approvalHistoryRepository.save(history);

        try {
            emailService.sendExpenseStatusUpdateNotification(updatedExpense, "Your expense status is now " + newStatus + (comments != null ? ". Comments: " + comments : ""));
        } catch (Exception e) {
            logger.error("Failed to send approval status notification for expense ID {}: {}", updatedExpense.getId(), e.getMessage(), e);
        }
        return convertToDto(updatedExpense);
    }

    @Transactional
    public ExpenseResponseDto rejectExpense(Long expenseId, String comments) {
        User currentUser = getCurrentAuthenticatedUser(); // Approver
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Expense not found with ID: " + expenseId));
        User expenseOwner = expense.getUser();

        if (comments == null || comments.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Rejection comments are mandatory.");
        }

        ExpenseStatus oldStatus = expense.getStatus();
        boolean canReject = false;

        boolean isAdmin = currentUser.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"));
        boolean isFinance = currentUser.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_FINANCE"));
        boolean isTheirManager = expenseOwner.getManager() != null && expenseOwner.getManager().getId().equals(currentUser.getId());

        if (oldStatus == ExpenseStatus.SUBMITTED && (isTheirManager || isAdmin)) {
            canReject = true;
        } else if (oldStatus == ExpenseStatus.PENDING_FINANCE_APPROVAL && (isAdmin || isFinance)) {
            canReject = true;
        }

        if(!canReject){
            throw new ApiException(HttpStatus.FORBIDDEN, "You are not authorized to reject this expense in its current state ("+oldStatus+") or for this user.");
        }

        expense.setStatus(ExpenseStatus.REJECTED);
        Expense updatedExpense = expenseRepository.save(expense);
        ApprovalHistory history = new ApprovalHistory(updatedExpense, currentUser, oldStatus, ExpenseStatus.REJECTED, comments);
        approvalHistoryRepository.save(history);
        auditLogService.logAction(currentUser.getUsername(), "EXPENSE_REJECTED", "Expense", updatedExpense.getId(), "Status changed to REJECTED. Comments: " + comments);

        try {
            emailService.sendExpenseStatusUpdateNotification(updatedExpense, "Your expense has been REJECTED. Comments: " + comments);
        } catch (Exception e) {
            logger.error("Failed to send rejection status notification for expense ID {}: {}", updatedExpense.getId(), e.getMessage(), e);
        }
        logger.info("Expense ID {} rejected by user {} with comments.", expenseId, currentUser.getUsername());
        return convertToDto(updatedExpense);
    }

    // Inside ExpenseService.getPendingApprovals()

    @Transactional(readOnly = true)
    public Page<ExpenseResponseDto> getPendingApprovals(Pageable pageable) {
        User currentUser = getCurrentAuthenticatedUser();
        logger.debug("Fetching pending approvals for user: {} with roles: {}", currentUser.getUsername(), currentUser.getRoles().stream().map(Role::getName).collect(Collectors.toList()));

        List<ExpenseStatus> statusesToFetch = new ArrayList<>();

        boolean isManager = currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_MANAGER"));
        boolean isAdmin = currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
        boolean isFinance = currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_FINANCE")); // Create this role if it doesn't exist

        if (isManager) {
            logger.debug("User {} is a Manager, adding SUBMITTED to statusesToFetch.", currentUser.getUsername());
            statusesToFetch.add(ExpenseStatus.SUBMITTED);
            // Optional: If a manager can also directly approve things they previously sent to finance (unlikely)
            // or if they also act as finance sometimes.
            // if (isFinance && !statusesToFetch.contains(ExpenseStatus.PENDING_FINANCE_APPROVAL)) {
            //     statusesToFetch.add(ExpenseStatus.PENDING_FINANCE_APPROVAL);
            // }
        }

        // Admin and Finance roles can see items pending their specific approval.
        // This is separate from the manager's initial queue.
        if (isAdmin || isFinance) {
            logger.debug("User {} is Admin or Finance, adding PENDING_FINANCE_APPROVAL to statusesToFetch.", currentUser.getUsername());
            if (!statusesToFetch.contains(ExpenseStatus.PENDING_FINANCE_APPROVAL)) { // Avoid duplicates if manager is also admin/finance
                statusesToFetch.add(ExpenseStatus.PENDING_FINANCE_APPROVAL);
            }
            // If an Admin should see EVERYTHING submitted, regardless of who is assigned:
            // This would override the manager-specific view for them.
            // if (isAdmin && !statusesToFetch.contains(ExpenseStatus.SUBMITTED)) {
            //    logger.debug("User {} is Admin, also adding SUBMITTED to see all initial submissions.", currentUser.getUsername());
            //    statusesToFetch.add(ExpenseStatus.SUBMITTED);
            // }
        }


        if (statusesToFetch.isEmpty()) {
            logger.debug("No relevant statuses to fetch for user {}, returning empty page.", currentUser.getUsername());
            return Page.empty(pageable);
        }

        logger.debug("Fetching expenses with statuses: {} for user {}", statusesToFetch, currentUser.getUsername());
        // Call the corrected repository method
        return expenseRepository.findAllByStatusIn(statusesToFetch, pageable) // <<<< ENSURE THIS MATCHES REPO
                .map(this::convertToDto);
    }


    @Transactional(readOnly = true)
    public List<ApprovalHistoryDto> getExpenseApprovalHistory(Long expenseId) {
        // The getExpenseById call below already performs an authorization check
        getExpenseById(expenseId);
        List<ApprovalHistory> historyList = approvalHistoryRepository.findByExpenseIdOrderByActionDateAsc(expenseId);
        return historyList.stream().map(this::convertApprovalHistoryToDto).collect(Collectors.toList());
    }

    // --- Helper Methods ---
    public User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal().toString())) {
            logger.warn("Attempt to get current user, but no user authenticated.");
            throw new ApiException(HttpStatus.UNAUTHORIZED, "User not authenticated.");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof User) {
            return (User) principal; // If UserDetails is our User entity
        } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            String username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
            logger.debug("Fetching user from repository for username: {}", username);
            return userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        logger.error("Authenticated user '{}' not found in database during getCurrentAuthenticatedUser.", username);
                        return new ApiException(HttpStatus.NOT_FOUND, "Authenticated user '" + username + "' not found in database.");
                    });
        } else {
            logger.error("Unexpected principal type: {}", principal.getClass().getName());
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected authentication principal type.");
        }
    }

    private ExpenseResponseDto convertToDto(Expense expense) {
        ExpenseResponseDto dto = new ExpenseResponseDto();
        dto.setId(expense.getId());
        dto.setDescription(expense.getDescription());
        dto.setAmount(expense.getAmount());
        dto.setExpenseDate(expense.getExpenseDate());
        dto.setStatus(expense.getStatus());
        if (expense.getUser() != null) {
            dto.setUserId(expense.getUser().getId());
            dto.setUsername(expense.getUser().getUsername());
        }
        if (expense.getCategory() != null) {
            dto.setCategoryId(expense.getCategory().getId());
            dto.setCategoryName(expense.getCategory().getName());
        }
        dto.setCreatedAt(expense.getCreatedAt());
        dto.setUpdatedAt(expense.getUpdatedAt());
        if (expense.getAttachments() != null) {
            dto.setAttachments(expense.getAttachments().stream()
                    .map(this::convertAttachmentToDto)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    private AttachmentResponseDto convertAttachmentToDto(Attachment attachment) {
        AttachmentResponseDto dto = new AttachmentResponseDto();
        dto.setId(attachment.getId());
        dto.setFileName(attachment.getFileName());
        dto.setFileType(attachment.getFileType());
        dto.setUploadedAt(attachment.getUploadedAt());
        return dto;
    }

    private ApprovalHistoryDto convertApprovalHistoryToDto(ApprovalHistory history) {
        ApprovalHistoryDto dto = new ApprovalHistoryDto();
        dto.setId(history.getId());
        dto.setExpenseId(history.getExpense().getId());
        if (history.getApprover() != null) {
            dto.setApproverUserId(history.getApprover().getId());
            dto.setApproverUsername(history.getApprover().getUsername());
        }
        dto.setStatusBefore(history.getStatusBefore());
        dto.setStatusAfter(history.getStatusAfter());
        dto.setComments(history.getComments());
        dto.setActionDate(history.getActionDate());
        return dto;
    }
}