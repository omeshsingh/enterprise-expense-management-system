package ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.controller.admin;

import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.dto.expense.ExpenseCategoryDto;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.service.expense.ExpenseCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class ExpenseCategoryController {

    private final ExpenseCategoryService expenseCategoryService;

    @PostMapping
    public ResponseEntity<ExpenseCategoryDto> createCategory(@Valid @RequestBody ExpenseCategoryDto categoryDto) {
        return new ResponseEntity<>(expenseCategoryService.createCategory(categoryDto), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()") // Changed from permitAll() for consistency; all logged-in users can see
    public ResponseEntity<List<ExpenseCategoryDto>> getAllCategories() {
        return ResponseEntity.ok(expenseCategoryService.getAllCategories());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseCategoryDto> updateCategory(@PathVariable Long id, @Valid @RequestBody ExpenseCategoryDto categoryDto) {
        return ResponseEntity.ok(expenseCategoryService.updateCategory(id, categoryDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        expenseCategoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}