package ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.repository;

import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    // Add methods for querying audit logs if needed, e.g., by username, action, date range
    Page<AuditLog> findByUsername(String username, Pageable pageable);
    Page<AuditLog> findByAction(String action, Pageable pageable);
    // Add more specific finders as your querying needs evolve
}
