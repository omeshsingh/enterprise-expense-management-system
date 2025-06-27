package ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.service.report;

import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.dto.report.ExpenseByCategoryDto;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.dto.report.ExpenseTrendDto;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.entity.Expense;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.entity.ExpenseStatus;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // Most reporting methods will be read-only
public class ReportingService {

    private static final Logger logger = LoggerFactory.getLogger(ReportingService.class);
    private final ExpenseRepository expenseRepository;

    public List<ExpenseTrendDto> getMonthlyExpenseTrends(LocalDate startDate, LocalDate endDate) {
        logger.info("Fetching monthly expense trends from {} to {}", startDate, endDate);
        // Fetch approved expenses within the date range
        List<Expense> expenses = expenseRepository.findAllByStatusAndExpenseDateBetween(
                ExpenseStatus.APPROVED, startDate, endDate);

        // Group by YearMonth and sum amounts
        Map<YearMonth, BigDecimal> monthlyTotals = expenses.stream()
                .collect(Collectors.groupingBy(
                        expense -> YearMonth.from(expense.getExpenseDate()),
                        Collectors.mapping(Expense::getAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        // Convert to DTO
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        return monthlyTotals.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()) // Sort by month
                .map(entry -> new ExpenseTrendDto(entry.getKey().format(formatter), entry.getValue()))
                .collect(Collectors.toList());
    }

    public List<ExpenseByCategoryDto> getExpenseBreakdownByCategory(LocalDate startDate, LocalDate endDate) {
        logger.info("Fetching expense breakdown by category from {} to {}", startDate, endDate);
        // Fetch approved expenses within the date range
        List<Expense> expenses = expenseRepository.findAllByStatusAndExpenseDateBetween(
                ExpenseStatus.APPROVED, startDate, endDate);

        // Group by category name and sum amounts
        Map<String, BigDecimal> categoryTotals = expenses.stream()
                .collect(Collectors.groupingBy(
                        expense -> expense.getCategory().getName(), // Assuming Category entity is eagerly fetched or fetched appropriately
                        Collectors.mapping(Expense::getAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        return categoryTotals.entrySet().stream()
                .map(entry -> new ExpenseByCategoryDto(entry.getKey(), entry.getValue()))
                .sorted((c1, c2) -> c2.getTotalAmount().compareTo(c1.getTotalAmount())) // Sort by amount desc
                .collect(Collectors.toList());
    }

    // Placeholder for Excel Export
    public byte[] exportExpensesToExcel(LocalDate startDate, LocalDate endDate) {
        logger.info("Exporting expenses to Excel from {} to {}", startDate, endDate);
        List<Expense> expenses = expenseRepository.findAllByStatusAndExpenseDateBetween(
                ExpenseStatus.APPROVED, startDate, endDate);
        // TODO: Implement Excel generation logic using Apache POI
        // For now, return an empty byte array or throw NotImplementedException
        // Example: return ExcelExportUtil.exportExpenses(expenses);
        logger.warn("Excel export not yet implemented.");
        return new byte[0];
    }

    // Placeholder for PDF Export
    public byte[] exportExpenseReportToPdf(LocalDate startDate, LocalDate endDate) {
        logger.info("Exporting expense report to PDF from {} to {}", startDate, endDate);
        // Fetch data for the report (e.g., trends and category breakdown)
        List<ExpenseTrendDto> trends = getMonthlyExpenseTrends(startDate, endDate);
        List<ExpenseByCategoryDto> breakdown = getExpenseBreakdownByCategory(startDate, endDate);
        // TODO: Implement PDF generation logic using iText/OpenPDF
        // Example: return PdfExportUtil.generateReport(trends, breakdown);
        logger.warn("PDF export not yet implemented.");
        return new byte[0];
    }
}