package ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor; // For custom executor
import java.util.concurrent.Executor; // Use java.util.concurrent.Executor


@Configuration
@EnableAsync // Enables support for @Async methods
public class AsyncConfig {

    // Define a custom executor bean for more control (optional but recommended)
    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);   // Start with a few threads
        executor.setMaxPoolSize(10);  // Allow scaling up
        executor.setQueueCapacity(25); // Buffer tasks if threads are busy
        executor.setThreadNamePrefix("AppAsync-"); // Prefix threads for easier logging/debugging
        executor.initialize(); // Initialize the executor
        return executor;
    }

    // Spring Boot will use this custom 'asyncExecutor' by default for @Async methods
    // because it finds an Executor bean. If you had multiple Executor beans,
    // you might need to specify which one to use via @Async("beanName").
}