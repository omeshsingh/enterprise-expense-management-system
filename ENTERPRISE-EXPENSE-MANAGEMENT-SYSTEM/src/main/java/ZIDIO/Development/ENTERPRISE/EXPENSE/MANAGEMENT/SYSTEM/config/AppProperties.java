package ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppProperties {
    private final Auth auth = new Auth();
    private final OAuth2 oauth2 = new OAuth2();

    // static inner classes for Auth and OAuth2 properties
    @Getter
    @Setter
    public static class Auth {
        private String tokenSecret; // Your JWT secret
        private long tokenExpirationMsec;
    }

    @Getter
    @Setter
    public static class OAuth2 {
        // This list should contain the URIs your frontend app runs on,
        // where Google is allowed to redirect TO your backend,
        // and then your backend can use the first one as the target for postMessage
        private List<String> authorizedRedirectUris = new ArrayList<>();
    }
}