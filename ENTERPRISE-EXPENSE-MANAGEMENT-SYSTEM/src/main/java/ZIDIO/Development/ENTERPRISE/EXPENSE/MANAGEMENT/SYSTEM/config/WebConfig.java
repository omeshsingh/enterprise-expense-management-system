package ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class WebConfig {

    @Bean
    public CorsFilter corsFilter() { // Bean name matches the one Spring Security might look for
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        // Replace with your frontend URL in production
        config.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:3000")); // Add common dev ports
        config.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type", "X-Requested-With", "Accept", "Origin"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        source.registerCorsConfiguration("/api/**", config); // Apply CORS to /api/** paths
        return new CorsFilter(source);
    }
}