package ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.security; // Correct package

import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.repository.UserRepository; // Import UserRepository
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.security.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
// Remove UserDetailsService import if not used elsewhere in this class
// import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException; // Import this
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);
    private final JwtService jwtService;
    private final UserRepository userRepository; // Using UserRepository

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            final String authHeader = request.getHeader("Authorization");
            final String jwt;
            final String username;

            if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            jwt = authHeader.substring(7);
            username = jwtService.extractUsername(jwt);

            if (StringUtils.hasText(username) && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Fetch UserDetails directly from UserRepository
                // Your User entity implements UserDetails
                UserDetails userDetails = this.userRepository.findByUsername(username)
                        .orElse(null); // Or .orElseThrow(...) if you want to be stricter

                if (userDetails != null) { // Check if user was found
                    if (jwtService.isTokenValid(jwt, userDetails)) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null, // Credentials not needed after token validation
                                userDetails.getAuthorities()
                        );
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        logger.debug("User '{}' authenticated successfully via JWT.", username);
                    } else {
                        logger.warn("Invalid JWT token for user '{}'.", username);
                    }
                } else {
                    logger.warn("User '{}' not found in database based on JWT.", username);
                }
            }
        } catch (Exception e) {
            // Catching a broader Exception here to ensure filter chain always proceeds.
            // Log the error appropriately.
            logger.error("Error during JWT authentication filter processing.", e);
        }

        filterChain.doFilter(request, response);
    }
}