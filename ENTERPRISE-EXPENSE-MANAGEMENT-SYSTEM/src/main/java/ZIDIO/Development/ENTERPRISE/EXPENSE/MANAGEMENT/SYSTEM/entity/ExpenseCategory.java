package ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "expense_categories")
@Getter
@Setter
@NoArgsConstructor
public class ExpenseCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, unique = true, nullable = false)
    private String name;

    public ExpenseCategory(String name) {
        this.name = name;
    }
}