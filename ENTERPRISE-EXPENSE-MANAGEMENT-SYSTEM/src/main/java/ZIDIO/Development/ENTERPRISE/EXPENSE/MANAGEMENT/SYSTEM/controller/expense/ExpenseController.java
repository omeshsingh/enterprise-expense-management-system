package ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.controller.expense; // Ensure this matches your package

import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.dto.expense.ExpenseRequestDto;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.dto.expense.ExpenseResponseDto;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.entity.Attachment; // Assuming you have an Attachment entity
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.repository.AttachmentRepository; // If fetching attachment directly
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.service.attachment.AttachmentService;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.service.expense.ExpenseService;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.service.storage.FileStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator; // For manual validation
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException; // For cleaner error responses

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private static final Logger logger = LoggerFactory.getLogger(ExpenseController.class);

    private final ExpenseService expenseService;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper; // For manual JSON deserialization
    private final Validator validator; // For manual validation of DTO
    private final AttachmentService attachmentService; // Example for download - consider a service for this

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'ADMIN')")
    public ResponseEntity<ExpenseResponseDto> createExpense(
            @RequestPart("expense") String expenseRequestJson,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        try {
            ExpenseRequestDto expenseRequestDto = objectMapper.readValue(expenseRequestJson, ExpenseRequestDto.class);

            // Manual validation
            Set<ConstraintViolation<ExpenseRequestDto>> violations = validator.validate(expenseRequestDto);
            if (!violations.isEmpty()) {
                String errorMessages = violations.stream()
                        .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                        .collect(Collectors.joining(", "));
                logger.warn("Validation failed for createExpense DTO: {}", errorMessages);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Validation failed: " + errorMessages);
            }

            ExpenseResponseDto createdExpense = expenseService.createExpense(expenseRequestDto, files);
            return new ResponseEntity<>(createdExpense, HttpStatus.CREATED);
        } catch (IOException e) {
            logger.error("Error deserializing expense JSON or processing files for createExpense", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid expense data format or file processing error.", e);
        }
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'ADMIN')")
    public ResponseEntity<Page<ExpenseResponseDto>> getCurrentUserExpenses(
            @PageableDefault(size = 10, sort = "expenseDate") Pageable pageable) {
        return ResponseEntity.ok(expenseService.getExpensesForCurrentUser(pageable));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<ExpenseResponseDto>> getAllExpenses(
            @PageableDefault(size = 10, sort = "expenseDate") Pageable pageable) {
        return ResponseEntity.ok(expenseService.getAllExpenses(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'ADMIN')")
    public ResponseEntity<ExpenseResponseDto> getExpenseById(@PathVariable Long id) {
        return ResponseEntity.ok(expenseService.getExpenseById(id));
    }

    @PutMapping(value = "/{id}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public ResponseEntity<ExpenseResponseDto> updateExpense(
            @PathVariable Long id,
            @RequestPart("expense") String expenseRequestJson,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        try {
            ExpenseRequestDto expenseRequestDto = objectMapper.readValue(expenseRequestJson, ExpenseRequestDto.class);

            // Manual validation
            Set<ConstraintViolation<ExpenseRequestDto>> violations = validator.validate(expenseRequestDto);
            if (!violations.isEmpty()) {
                String errorMessages = violations.stream()
                        .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                        .collect(Collectors.joining(", "));
                logger.warn("Validation failed for updateExpense DTO: {}", errorMessages);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Validation failed: " + errorMessages);
            }

            ExpenseResponseDto updatedExpense = expenseService.updateExpense(id, expenseRequestDto, files);
            return ResponseEntity.ok(updatedExpense);
        } catch (IOException e) {
            logger.error("Error deserializing expense JSON or processing files for updateExpense", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid expense data format or file processing error.", e);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }

    // Endpoint to download an attachment
    // CRITICAL: Add proper authorization to this endpoint!
    // E.g., ensure the requesting user owns the expense associated with the attachment or is an Admin.
//    @GetMapping("/attachments/{attachmentId}/download")
//    @PreAuthorize("isAuthenticated()") // Add more specific authorization in service or here
//    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long attachmentId, HttpServletRequest request) {
//        // You should ideally have an AttachmentService for this.
//        // For this example, using AttachmentRepository directly (not best practice for complex logic).
//        Attachment attachment = attachmentRepository.findById(attachmentId)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attachment not found with ID: " + attachmentId));
//
//        // TODO: Add robust authorization here:
//        // Check if the current authenticated user has permission to download this attachment
//        // (e.g., they own the expense, or they are an admin/manager with rights to this expense).
//        // if (!isUserAuthorizedForAttachment(attachment)) {
//        //     throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to download this file.");
//        // }
//
//        Resource resource = fileStorageService.loadFileAsResource(attachment.getFilePath());
//        String contentType = "application/octet-stream"; // Default
//
//        try {
//            String detectedContentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
//            if (detectedContentType != null) {
//                contentType = detectedContentType;
//            }
//        } catch (IOException ex) {
//            logger.info("Could not determine file type for download, using default.", ex);
//        }
//
//        logger.info("Downloading file: {} with Content-Type: {}", resource.getFilename(), contentType);
//
//        return ResponseEntity.ok()
//                .contentType(MediaType.parseMediaType(contentType))
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
//                .body(resource);
//    }

    @GetMapping("/attachments/{attachmentId}/download")
    @PreAuthorize("isAuthenticated()") // Authorization is handled in AttachmentService
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long attachmentId, HttpServletRequest request) {
        Attachment attachment = attachmentService.getAttachmentByIdAndVerifyAccess(attachmentId);
        Resource resource = attachmentService.loadAttachmentAsResource(attachment);

        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            // log error but continue
            System.err.println("Could not determine file type for attachment ID: " + attachmentId);
        }

        // Default content type if it could not be determined
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        // Encode filename for Content-Disposition header to handle special characters
        String encodedFilename = URLEncoder.encode(attachment.getFileName(), StandardCharsets.UTF_8).replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename) // RFC 5987
                // .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getFileName() + "\"") // Simpler, but might have issues with some chars
                .body(resource);
    }

    // Placeholder for authorization logic - implement this robustly!
    // private boolean isUserAuthorizedForAttachment(Attachment attachment) {
    //     // Get current authenticated user
    //     // User currentUser = ... ;
    //     // Check if currentUser.getId().equals(attachment.getExpense().getUser().getId())
    //     // Or if currentUser has ROLE_ADMIN, etc.
    //     return true; // Replace with actual logic
    // }
}