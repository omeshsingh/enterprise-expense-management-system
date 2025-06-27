package ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "approval_history")
@Getter
@Setter
@NoArgsConstructor
public class ApprovalHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", nullable = false)
    private Expense expense;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_user_id", nullable = false)
    private User approver;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_before", length = 50)
    private ExpenseStatus statusBefore;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_after", length = 50, nullable = false)
    private ExpenseStatus statusAfter;

    @Column(columnDefinition = "TEXT")
    private String comments;

    @CreationTimestamp
    @Column(name = "action_date", updatable = false, nullable = false)
    private Instant actionDate;

    public ApprovalHistory(Expense expense, User approver, ExpenseStatus statusBefore, ExpenseStatus statusAfter, String comments) {
        this.expense = expense;
        this.approver = approver;
        this.statusBefore = statusBefore;
        this.statusAfter = statusAfter;
        this.comments = comments;
    }
}