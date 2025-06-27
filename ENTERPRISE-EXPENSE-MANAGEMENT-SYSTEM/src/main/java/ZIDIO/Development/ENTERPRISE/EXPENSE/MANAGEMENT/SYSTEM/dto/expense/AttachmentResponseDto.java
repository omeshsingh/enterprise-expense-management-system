package ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.dto.expense;

import lombok.Data;
import java.time.Instant;

@Data
public class AttachmentResponseDto {
    private Long id;
    private String fileName;
    private String fileType;
    // private String downloadUrl; // You might want to construct a download URL
    private Instant uploadedAt;
}