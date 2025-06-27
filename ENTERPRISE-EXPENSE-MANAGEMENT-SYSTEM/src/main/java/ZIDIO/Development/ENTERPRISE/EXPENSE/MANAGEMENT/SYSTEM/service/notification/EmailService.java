package ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.service.notification;

import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.entity.Expense;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async; // For async sending
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    // Simple text email example
    @Async // Mark method to be run asynchronously
    public void sendSimpleMessage(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            // Set 'from' if needed, otherwise uses default from properties
            // message.setFrom("noreply@yourapp.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            logger.info("Simple email sent successfully to {}", to);
        } catch (MailException exception) {
            logger.error("Error sending simple email to {}: {}", to, exception.getMessage());
        }
    }

    // Example: Notification to manager about new submission
    @Async
    public void sendExpenseSubmissionNotification(Expense expense, User manager) {
        if (manager == null || manager.getEmail() == null) {
            logger.warn("Cannot send submission notification for expense ID {}: Manager or manager email is null.", expense.getId());
            return;
        }
        String subject = "New Expense Submitted for Approval (ID: " + expense.getId() + ")";
        String text = String.format(
                "Hello %s,\n\nA new expense requires your approval:\n\n" +
                        "Submitted by: %s %s (%s)\n" +
                        "Amount: %.2f\n" +
                        "Description: %s\n" +
                        "Date: %s\n\n" +
                        "Please log in to the system to review and approve/reject this expense.\n\n" +
                        "Thank you,\nExpense Management System",
                manager.getFirstName() != null ? manager.getFirstName() : manager.getUsername(),
                expense.getUser().getFirstName(), expense.getUser().getLastName(), expense.getUser().getEmail(),
                expense.getAmount(),
                expense.getDescription(),
                expense.getExpenseDate()
        );
        sendSimpleMessage(manager.getEmail(), subject, text);
    }

    // Example: Notification to employee about status change
    @Async
    public void sendExpenseStatusUpdateNotification(Expense expense, String comments) {
        User employee = expense.getUser();
        if (employee == null || employee.getEmail() == null) {
            logger.warn("Cannot send status update notification for expense ID {}: Employee or employee email is null.", expense.getId());
            return;
        }
        String subject = "Update on Your Expense Report (ID: " + expense.getId() + ")";
        String statusText = expense.getStatus().toString(); // APPROVED, REJECTED, etc.
        String text = String.format(
                "Hello %s,\n\nYour expense report (ID: %d) status has been updated to: %s\n\n" +
                        "Description: %s\n" +
                        "Amount: %.2f\n",
                employee.getFirstName() != null ? employee.getFirstName() : employee.getUsername(),
                expense.getId(),
                statusText,
                expense.getDescription(),
                expense.getAmount()
        );

        if (comments != null && !comments.isBlank()) {
            text += String.format("\nComments: %s\n", comments);
        }

        text += "\nYou can view the details by logging into the system.\n\n" +
                "Thank you,\nExpense Management System";

        sendSimpleMessage(employee.getEmail(), subject, text);
    }

    // You can add more methods for HTML emails using MimeMessageHelper if needed
    @Async
    public void sendHtmlMessage(String to, String subject, String htmlBody) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8"); // true = multipart
            // Set 'from' if needed
            // helper.setFrom("noreply@yourapp.com");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = isHtml
            // Add attachments if needed: helper.addAttachment(...)
            mailSender.send(message);
            logger.info("HTML email sent successfully to {}", to);
        } catch (MessagingException | MailException e) {
            logger.error("Error sending HTML email to {}: {}", to, e.getMessage());
        }
    }
}
