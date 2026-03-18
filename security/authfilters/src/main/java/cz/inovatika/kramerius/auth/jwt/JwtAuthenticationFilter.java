package cz.inovatika.kramerius.auth.jwt;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;

import cz.incad.kramerius.auth.ClientKeycloakConfig;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

import static cz.incad.kramerius.Constants.WORKING_DIR;

/**
 * JwtAuthenticationFilter
 *
 * @author ppodsednik
 */
public class JwtAuthenticationFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(JwtAuthenticationFilter.class.getName());
    private static final String ACCOUNT_ATTR = JwtAccount.class.getName();
    private static final long CLOCK_SKEW_SECONDS = 60;
    private ConfigurableJWTProcessor<SecurityContext> jwtProcessor;
    private String issuer;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        try {
            String path = WORKING_DIR + "/keycloak.json";
            String str = IOUtils.toString(new FileInputStream(path), "UTF-8");
            ClientKeycloakConfig cnf = ClientKeycloakConfig.load(new JSONObject(str));
            issuer = cnf.issuer();
            String jwksUrl = cnf.jwks();
            DefaultResourceRetriever retriever = new DefaultResourceRetriever(2000, 2000);
            JWKSource<SecurityContext> jwkSource = new RemoteJWKSet<>(new URL(jwksUrl), retriever);
            jwtProcessor = new DefaultJWTProcessor<>();
            jwtProcessor.setJWSKeySelector(new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, jwkSource));
        } catch (Exception e) {
            throw new ServletException("JWT initialization failed", e);
        }
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        try {
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);
                JWTClaimsSet claims = jwtProcessor.process(token, null);
                if (issuer.equals(claims.getIssuer())) {
                    Date exp = claims.getExpirationTime();
                    if (exp == null || System.currentTimeMillis() <= exp.getTime() + CLOCK_SKEW_SECONDS * 1000) {
                        String username = claims.getStringClaim("preferred_username");
                        Set<String> roles = new HashSet<>(extractRoles(claims));
                        JwtAccount account = new JwtAccount(username, roles, claims.getClaims());
                        request.setAttribute(ACCOUNT_ATTR, account);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warning("JWT validation failed: " + e.getMessage());
        }
        chain.doFilter(req, res);
    }

    private List<String> extractRoles(JWTClaimsSet claims) {
        Map<String, Object> realmAccess = (Map<String, Object>) claims.getClaim("realm_access");
        if (realmAccess == null) {
            return List.of();
        }
        List<String> roles = (List<String>) realmAccess.get("roles");
        return roles == null ? List.of() : roles;
    }

    @Override
    public void destroy() {
    }
}