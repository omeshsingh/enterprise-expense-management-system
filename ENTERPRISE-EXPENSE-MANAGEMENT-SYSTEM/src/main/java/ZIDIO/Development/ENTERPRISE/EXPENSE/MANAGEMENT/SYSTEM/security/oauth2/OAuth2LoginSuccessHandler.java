package ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.security.oauth2;

import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.config.AppProperties; // Assuming you have this for frontend URL
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.entity.User;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.repository.UserRepository;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.security.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder; // For building redirect URI if needed

import java.io.IOException;
import java.io.PrintWriter; // For writing HTML response

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository; // Assuming CustomOidcUserService already saved/updated user
    private final AppProperties appProperties; // You'll need to create this to store frontend URL

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
        String email = oidcUser.getEmail();

        // CustomOidcUserService should have ensured the user exists in your DB
        // and has correct roles. We fetch them to get our internal User object details.
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    // This case should ideally be handled by CustomOidcUserService creating the user.
                    // If it can happen, you might want to log an error or handle it.
                    // For now, we assume user exists.
                    log.warn("User not found in DB after OAuth2 success, CustomOidcUserService might have an issue. Email: {}", email);
                    // You could create a minimal User object here if absolutely necessary,
                    // but it's better if CustomOidcUserService guarantees user creation.
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setUsername(email); // Or some other logic for username
                    newUser.setFirstName(oidcUser.getGivenName());
                    newUser.setLastName(oidcUser.getFamilyName());
                    newUser.setEnabled(true);
                    // Assign default roles if needed
                    return userRepository.save(newUser);
                });


        String token = jwtService.generateToken(user); // Generate token using your UserDetails implementation

        // Instead of redirecting, send back HTML that posts a message to the opener window
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        // IMPORTANT: Get your frontend origin for postMessage security
        String frontendOrigin = appProperties.getOauth2().getAuthorizedRedirectUris().get(0);
        // Assuming the first redirect URI is your main frontend app origin
        // Or hardcode it for dev: String frontendOrigin = "http://localhost:5173";

        out.println("<html>");
        out.println("<head><title>Authentication Success</title></head>");
        out.println("<body>");
        out.println("<script type='text/javascript'>");
        // Post the message to the window that opened this popup
        out.println("if (window.opener) {");
        out.println("  window.opener.postMessage({ type: 'oauth2_success', token: '" + token + "', userId: '" + user.getId() + "', username: '" + user.getUsername() + "' }, '" + frontendOrigin + "');");
        out.println("  window.close();"); // Close the popup
        out.println("} else {");
        out.println("  document.body.innerHTML = 'Authentication successful. Please close this window and return to the application. If you were not redirected, copy this token: " + token + "';"); // Fallback message
        out.println("}");
        out.println("</script>");
        out.println("<p>Authentication successful. Closing this window...</p>"); // Fallback message if script fails
        out.println("</body></html>");
        out.flush();

        // clearAuthenticationAttributes(request); // No longer needed as we are not doing a server-side redirect
    }

    // If you were using determineTargetUrl before, it's not directly used for this popup method.
    // protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
    //     // ... your existing logic to build the redirect URL with token ...
    // }
}