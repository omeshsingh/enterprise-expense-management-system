package ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.service.expense;

import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.dto.expense.ExpenseCategoryDto;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.entity.ExpenseCategory;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.exception.ApiException;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.repository.ExpenseCategoryRepository;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.repository.ExpenseRepository; // For checking usage
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.service.audit.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseCategoryService {


    private final AuditLogService auditLogService;
    private final ExpenseCategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository; // Inject if checking usage

    @Transactional
    public ExpenseCategoryDto createCategory(ExpenseCategoryDto categoryDto) {
        if (categoryRepository.existsByName(categoryDto.getName())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Category with name '" + categoryDto.getName() + "' already exists.");
        }
        ExpenseCategory category = new ExpenseCategory();
        category.setName(categoryDto.getName());
        ExpenseCategory savedCategory = categoryRepository.save(category);
        return convertToDto(savedCategory);
    }

    @Transactional(readOnly = true)
    public List<ExpenseCategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ExpenseCategoryDto getCategoryById(Long id) {
        ExpenseCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Category not found with ID: " + id));
        return convertToDto(category);
    }

    @Transactional
    public ExpenseCategoryDto updateCategory(Long id, ExpenseCategoryDto categoryDto) {
        ExpenseCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Category not found with ID: " + id));

        categoryRepository.findByName(categoryDto.getName()).ifPresent(existingCategory -> {
            if (!existingCategory.getId().equals(id)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Another category with name '" + categoryDto.getName() + "' already exists.");
            }
        });

        category.setName(categoryDto.getName());
        ExpenseCategory updatedCategory = categoryRepository.save(category);

        // ... after savedCategory = categoryRepository.save(category);
// String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName(); // Or get from a helper
// auditLogService.logAction(currentUsername, "CATEGORY_CREATED", "ExpenseCategory", savedCategory.getId(), "Name: " + savedCategory.getName());

        return convertToDto(updatedCategory);
    }

    @Transactional
    public void deleteCategory(Long id) {
        ExpenseCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Category not found with ID: " + id));

        // Optional: Check if the category is in use by any expenses before deleting
        // You'll need a method like `existsByCategoryId` in ExpenseRepository
        // if (expenseRepository.existsByCategoryId(id)) {
        //     throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot delete category as it is currently in use by expenses.");
        // }
        categoryRepository.delete(category);
    }

    private ExpenseCategoryDto convertToDto(ExpenseCategory category) {
        ExpenseCategoryDto dto = new ExpenseCategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        return dto;
    }
}