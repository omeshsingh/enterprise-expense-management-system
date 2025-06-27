package ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.dto.audit;

import lombok.Data;
import java.time.Instant;

@Data
public class AuditLogDto {
    private Long id;
    private Instant timestamp;
    private String username;
    private String action;
    private String entityName;
    private Long entityId;
    private String details;
}
