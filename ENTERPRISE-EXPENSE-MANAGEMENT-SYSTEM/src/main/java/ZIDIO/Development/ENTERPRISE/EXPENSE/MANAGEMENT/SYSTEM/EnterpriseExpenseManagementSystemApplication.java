package ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM;

import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.security.JwtAuthFilter; // Import it
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan
public class EnterpriseExpenseManagementSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(EnterpriseExpenseManagementSystemApplication.class, args);
	}
}