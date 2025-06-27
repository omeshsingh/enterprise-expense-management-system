package ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.controller.approval;

import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.dto.approval.ApprovalHistoryDto;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.dto.approval.ApprovalRequestDto;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.dto.expense.ExpenseResponseDto;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.service.expense.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApprovalController {

    private final ExpenseService expenseService;

    @PostMapping("/expenses/{expenseId}/approve")
    @PreAuthorize("hasAnyRole('MANAGER', 'FINANCE', 'ADMIN')")
    public ResponseEntity<ExpenseResponseDto> approveExpense(
            @PathVariable Long expenseId,
            @Valid @RequestBody(required = false) ApprovalRequestDto approvalRequestDto) {
        String comments = (approvalRequestDto != null && approvalRequestDto.getComments() != null) ? approvalRequestDto.getComments() : null;
        ExpenseResponseDto approvedExpense = expenseService.approveExpense(expenseId, comments);
        return ResponseEntity.ok(approvedExpense);
    }

    @PostMapping("/expenses/{expenseId}/reject")
    @PreAuthorize("hasAnyRole('MANAGER', 'FINANCE', 'ADMIN')")
    public ResponseEntity<ExpenseResponseDto> rejectExpense(
            @PathVariable Long expenseId,
            @Valid @RequestBody ApprovalRequestDto approvalRequestDto) {
        ExpenseResponseDto rejectedExpense = expenseService.rejectExpense(expenseId, approvalRequestDto.getComments());
        return ResponseEntity.ok(rejectedExpense);
    }

    @GetMapping("/approvals/pending")
    @PreAuthorize("hasAnyRole('MANAGER', 'FINANCE', 'ADMIN')")
    public ResponseEntity<Page<ExpenseResponseDto>> getPendingApprovals(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(expenseService.getPendingApprovals(pageable));
    }

    @GetMapping("/expenses/{expenseId}/history")
    @PreAuthorize("isAuthenticated()") // Service layer will do fine-grained auth
    public ResponseEntity<List<ApprovalHistoryDto>> getExpenseApprovalHistory(@PathVariable Long expenseId) {
        return ResponseEntity.ok(expenseService.getExpenseApprovalHistory(expenseId));
    }
}