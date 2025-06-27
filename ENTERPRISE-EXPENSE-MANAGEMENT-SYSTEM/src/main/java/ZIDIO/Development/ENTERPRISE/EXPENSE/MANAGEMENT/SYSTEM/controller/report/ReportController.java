package ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.controller.report;

import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.dto.report.ExpenseByCategoryDto;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.dto.report.ExpenseTrendDto;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.service.report.ReportingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')") // Secure all reporting endpoints
public class ReportController {

    private final ReportingService reportingService;

    @GetMapping("/trends/monthly")
    public ResponseEntity<List<ExpenseTrendDto>> getMonthlyExpenseTrends(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<ExpenseTrendDto> trends = reportingService.getMonthlyExpenseTrends(startDate, endDate);
        return ResponseEntity.ok(trends);
    }

    @GetMapping("/breakdown/category")
    public ResponseEntity<List<ExpenseByCategoryDto>> getExpenseBreakdownByCategory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<ExpenseByCategoryDto> breakdown = reportingService.getExpenseBreakdownByCategory(startDate, endDate);
        return ResponseEntity.ok(breakdown);
    }

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportExpensesToExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        byte[] excelData = reportingService.exportExpensesToExcel(startDate, endDate);
        // TODO: Implement actual Excel generation
        if (excelData.length == 0) { // Placeholder check
            return ResponseEntity.noContent().build(); // Or a 501 Not Implemented
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM); // Common for Excel
        headers.setContentDispositionFormData("attachment", "expenses_report_" + startDate + "_to_" + endDate + ".xlsx");
        return ResponseEntity.ok().headers(headers).body(excelData);
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportExpenseReportToPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        byte[] pdfData = reportingService.exportExpenseReportToPdf(startDate, endDate);
        // TODO: Implement actual PDF generation
        if (pdfData.length == 0) { // Placeholder check
            return ResponseEntity.noContent().build(); // Or a 501 Not Implemented
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "expense_summary_report_" + startDate + "_to_" + endDate + ".pdf");
        return ResponseEntity.ok().headers(headers).body(pdfData);
    }
}
