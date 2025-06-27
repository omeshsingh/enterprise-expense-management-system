package ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Instant timestamp;

    @Column(nullable = false, length = 100)
    private String username; // Username of the user who performed the action

    @Column(nullable = false, length = 100)
    private String action; // e.g., "USER_REGISTERED", "EXPENSE_CREATED", "EXPENSE_APPROVED"

    @Column(name = "entity_name", length = 100) // Optional: Name of the entity affected
    private String entityName;

    @Column(name = "entity_id") // Optional: ID of the entity affected
    private Long entityId;

    @Lob // For potentially large details, or use TEXT type in DB
    @Column(columnDefinition = "TEXT")
    private String details; // e.g., JSON string of changes, or descriptive text

    public AuditLog(String username, String action, String entityName, Long entityId, String details) {
        this.username = username;
        this.action = action;
        this.entityName = entityName;
        this.entityId = entityId;
        this.details = details;
    }

    public AuditLog(String username, String action, String details) {
        this.username = username;
        this.action = action;
        this.details = details;
    }
}
