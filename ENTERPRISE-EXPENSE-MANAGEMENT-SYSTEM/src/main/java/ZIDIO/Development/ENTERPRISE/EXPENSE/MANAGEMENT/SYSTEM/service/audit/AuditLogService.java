package ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.service.audit;

import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.entity.AuditLog;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogService.class);
    private final AuditLogRepository auditLogRepository;

    @Async // Make audit logging asynchronous so it doesn't slow down the main transaction
    @Transactional(propagation = Propagation.REQUIRES_NEW) // Run in a new transaction
    public void logAction(String username, String action, String entityName, Long entityId, String details) {
        try {
            AuditLog auditLog = new AuditLog(username, action, entityName, entityId, details);
            auditLogRepository.save(auditLog);
            logger.debug("Audit log saved: User='{}', Action='{}', Entity='{}', ID='{}'", username, action, entityName, entityId);
        } catch (Exception e) {
            logger.error("Failed to save audit log: User='{}', Action='{}', Details='{}': {}",
                    username, action, details, e.getMessage(), e);
            // Decide if you want to re-throw or just log. For audit, usually just log.
        }
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(String username, String action, String details) {
        logAction(username, action, null, null, details);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogs(Pageable pageable) {
        // Add filtering capabilities here if needed based on criteria
        return auditLogRepository.findAll(pageable);
    }
}
