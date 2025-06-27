package ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.repository;

import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findByExpenseId(Long expenseId);
    Optional<Attachment> findByExpenseIdAndId(Long expenseId, Long attachmentId);
    // Optional<Attachment> findByFilePath(String filePath); // Only if file paths are guaranteed unique across all
}