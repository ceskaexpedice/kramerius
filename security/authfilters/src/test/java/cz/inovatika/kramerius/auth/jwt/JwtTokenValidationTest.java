package cz.inovatika.kramerius.auth.jwt;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jwt.*;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import java.security.interfaces.RSAPublicKey;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JwtTokenValidationTest {

    private static final String TOKEN =
            "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJXWFE2Y2ZMN1hXTFZIZUF2cXlWQks2Tjdtamx0T0cyd3J6c0EtTDRUWDBjIn0.eyJleHAiOjE3NzMzNTU0MjEsImlhdCI6MTc3MzMxOTQyMywiYXV0aF90aW1lIjoxNzczMzE5NDIxLCJqdGkiOiI5YWZjNGJlYy0yNWVlLTQ0ZjUtOWVhZC0zNmI1MTYyYTYyYjMiLCJpc3MiOiJodHRwczovL2VkdWlkLmlub3ZhdGlrYS5kZXYvcmVhbG1zL2tyYW1lcml1cyIsImF1ZCI6ImFjY291bnQiLCJzdWIiOiIzNWE1MzRjNC1lNDQ0LTQ0NTEtODhmYS0zZjkwNTZlYzlmMjYiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJrcmFtZXJpdXNDbGllbnQiLCJzZXNzaW9uX3N0YXRlIjoiNDJmZTdlYTUtM2IyMi00OTQxLWFiYTUtNTk0OGZiNzkwZWFjIiwiYWNyIjoiMSIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJkbm50X3VzZXJzIiwib2ZmbGluZV9hY2Nlc3MiLCJzcGVjaWFsLW5lZWRzIiwiZGVmYXVsdC1yb2xlcy1rcmFtZXJpdXMiLCJ1bWFfYXV0aG9yaXphdGlvbiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC0yZmEiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsIm1hbmFnZS1hY2NvdW50LWJhc2ljLWF1dGgiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6InByb2ZpbGUgZW1haWwiLCJzaWQiOiI0MmZlN2VhNS0zYjIyLTQ5NDEtYWJhNS01OTQ4ZmI3OTBlYWMiLCJlZHVQZXJzb25FbnRpdGxlbWVudCI6InVybjptYWNlOmRpcjplbnRpdGxlbWVudDpjb21tb24tbGliLXRlcm1zIiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJlZHVQZXJzb25TY29wZWRBZmZpbGlhdGlvbiI6Im1lbWJlckBpbm92YXRpa2EuZGV2IyNlbXBsb3llZUBpbm92YXRpa2EuZGV2IiwiYWZmaWxpYXRpb24iOiJtZW1iZXJAaW5vdmF0aWthLmRldiMjZW1wbG95ZWVAaW5vdmF0aWthLmRldiIsImRpc3BsYXlOYW1lIjoiUGV0ciBQb2RzZWRuaWsiLCJlZHVQZXJzb25QcmluY2lwYWxOYW1lIjoicGVwb0Bpbm92YXRpa2EuZGV2IiwiZWR1UGVyc29uVW5pcXVlSWQiOiIyMjIyQGlub3ZhdGlrYS5kZXYiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJwZXBvIn0.e2jTWY8q3IQdgobJznCeV2a9aLrwT9lFTF_jtU93yajEAVv0uF4mXSW1LmvyrFYWkbynU33yA4mjPTNLdMJVCU-_iRgaNMWV1RMuN6lnm3KVL2_hxvo9IBhFNJsluuEjzwvDzdDTL3PylYCcDrZAbyeqPltlG1Dr88E1IVDXwVw63X9BpyI8-z5JVbcCCa7n_jIbKyvf03J8WfWAG2WN5WxCWkhV8rrNuh_nHdDZTrvneum-qeaDAdawdk0vEXUG-MvJhlULGd4DwOj3baxUqS5DYtGgl923n-YqbBV9GKQyZW_yzHo5zBlkZmsWPKJQrMiU4QbwQSKYGGo4_yRbKw'";

    private static final String JWKS_JSON = """
            {
              "keys": [
                {
                  "kid": "WXQ6cfL7XWLVHeAvqyVBK6N7mjltOG2wrzsA-L4TX0c",
                  "kty": "RSA",
                  "alg": "RS256",
                  "use": "sig",
                  "n": "ivI2Pzb1AFVbnZQxH6gubqa5rTT0hkf-1mJMpIyuXSRGLwUX3GCtufvWpEu2H2NPvsrmjp0rLgx9kdq2PaXriTHvO7VU_c6MtileIh4AMm2cdlkPttEMb4VK0rtF7jWR5jHtU6yBt4CT3YRSVmowFS9S3KE-qowyrRGb8Ng4cs0dcoJcy-6Bb1nR9mbN-h57YvizRfYIciCYDlsu5EL4gqmlwdwzuZbxrf6NPGWe9m9Oz5b0BRSZ_YUMJFXxV5pP4kSugKsLXsMn95GC_BV4_eCeKrhWrD-pusNivySOdkkYlES01gukMfC7IO4bfvEpMXb0xG9tHY93QRP90AMzDQ",
                  "e": "AQAB",
                  "x5c": [
                    "MIICoTCCAYkCBgGS57p82DANBgkqhkiG9w0BAQsFADAUMRIwEAYDVQQDDAlrcmFtZXJpdXMwHhcNMjQxMTAxMTIzNTExWhcNMzQxMTAxMTIzNjUxWjAUMRIwEAYDVQQDDAlrcmFtZXJpdXMwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCK8jY/NvUAVVudlDEfqC5uprmtNPSGR/7WYkykjK5dJEYvBRfcYK25+9akS7YfY0++yuaOnSsuDH2R2rY9peuJMe87tVT9zoy2KV4iHgAybZx2WQ+20QxvhUrSu0XuNZHmMe1TrIG3gJPdhFJWajAVL1LcoT6qjDKtEZvw2DhyzR1yglzL7oFvWdH2Zs36Hnti+LNF9ghyIJgOWy7kQviCqaXB3DO5lvGt/o08ZZ72b07PlvQFFJn9hQwkVfFXmk/iRK6Aqwtewyf3kYL8FXj94J4quFasP6m6w2K/JI52SRiURLTWC6Qx8Lsg7ht+8SkxdvTEb20dj3dBE/3QAzMNAgMBAAEwDQYJKoZIhvcNAQELBQADggEBAFzesWMnBKInf7WqrdzfxsKYZNZDMwg+ZVejRNkNsVdYOcowvU7oX4IivXW/FTo/v7K6QFQtrpbj4kZOiiBgVargDQeV5w33uqq2lU+zObhfL1KwOqHgAmi91RYeg5GY6Odor6PuwYC5hK+C1lT4yMNAPc9Idq69CO2O2N3GB0fMhR6UywNbUY7KdfCQ38DnxNig/uMpsumE+ajadQdUARYHixKNcfy7jZvghzStIj7ez4d4z/QtG2727n+1T3TcqKNRMWS6d9kMmcGnUahN5YrCFoLqfnJUbseTCZcp8JoIEtDWz57YCa4MRvxMrmhzM37LRNaQ/DAGqRM8LroIVxQ="
                  ],
                  "x5t": "YUxQSWIsxMCrGR2_F6pNlTJ0L1o",
                  "x5t#S256": "rhrz8NCrnRC2XQWsV5ziQwX9Fov0HC6s6StDRRbAgvo"
                },
                {
                  "kid": "s0IWYhRD_maJ83cXuqPMsRbNQVjCEZFptQuhcmphm8g",
                  "kty": "RSA",
                  "alg": "RSA-OAEP",
                  "use": "enc",
                  "n": "wttusSgsWzPxAZhfACRevSJ46PJZfxBxhiJeJKMvy8lWSAYPdVyUeG_MkX_KtBpXSOnykwfp9VZE-FnBSbhatj0vXyvZCLLCyDXOpVUP54hDEyTgrUZUdZ4q_pNrMW3XXQ4l2O6I4b1AXJvrF3u3NzBHayWdZOpKi7HNFuCWGpNQ-pM1BquCTTTlbQXNUZXJoiYQOaL737eYFRc3wxBBIY0Vq-9N8S0jXaI2ZUoY3pAZPp9h_SZdOtmqZzi74AqXKH3r1OUpqAZ3Z2sNfQKt7AigHbCsG0SU57wExZXtudVPhoBwKehKaZlLL8Ej0II7qz4LGvTSN3xyTwAor1vFSQ",
                  "e": "AQAB",
                  "x5c": [
                    "MIICoTCCAYkCBgGS57p+ADANBgkqhkiG9w0BAQsFADAUMRIwEAYDVQQDDAlrcmFtZXJpdXMwHhcNMjQxMTAxMTIzNTExWhcNMzQxMTAxMTIzNjUxWjAUMRIwEAYDVQQDDAlrcmFtZXJpdXMwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDC226xKCxbM/EBmF8AJF69Injo8ll/EHGGIl4koy/LyVZIBg91XJR4b8yRf8q0GldI6fKTB+n1VkT4WcFJuFq2PS9fK9kIssLINc6lVQ/niEMTJOCtRlR1nir+k2sxbdddDiXY7ojhvUBcm+sXe7c3MEdrJZ1k6kqLsc0W4JYak1D6kzUGq4JNNOVtBc1RlcmiJhA5ovvft5gVFzfDEEEhjRWr703xLSNdojZlShjekBk+n2H9Jl062apnOLvgCpcofevU5SmoBndnaw19Aq3sCKAdsKwbRJTnvATFle251U+GgHAp6EppmUsvwSPQgjurPgsa9NI3fHJPACivW8VJAgMBAAEwDQYJKoZIhvcNAQELBQADggEBAE+5YESsv/VXg92kF+3K9dlqLuG/Vu52lhUwv2QeEQkByJ/xFWVX+kYOug32Dvo2Eq6qmTDTdNbC11ki8vixwkCo48dAi4tPr7Pglo+FDFj1u7LyGVWPqQNNbRxDcsfI9dMVMALqNxDDVm1VufdQivmIwQCL51cjiWH3gQzr+OQmvx8SjwbTzY6nCXQrdq0iit1eTtMMESlM/sDWr0hJXbNvmIludizO9SK3Oo+9/uglzUiz3chvPMFJQXtCD5tcROC+Tm3ZiTWgErappEoWQ2fnRANBhBEe7M7pekPuoRM8sRkhqI73K1WyhoU7xeUyEL8SNNSHmwAGrPDMB2hQrqQ="
                  ],
                  "x5t": "XDX8P00GRkvFaX2UklOeaFHAdjI",
                  "x5t#S256": "qyNujKMN9ZRsZ9pkYhFuiJ9e4CVJCJEAU-S4Al08svw"
                }
              ]
            }
            """;

    @Test
    public void verifyKeycloakToken() throws Exception {
        SignedJWT jwt = SignedJWT.parse(TOKEN);
        // Parse JWKS
        JWKSet jwkSet = JWKSet.parse(JWKS_JSON);
        // Find correct key by kid
        JWK jwk = jwkSet.getKeyByKeyId(jwt.getHeader().getKeyID());
        RSAKey rsaKey = (RSAKey) jwk;
        RSAPublicKey publicKey = rsaKey.toRSAPublicKey();

        JWSVerifier verifier = new RSASSAVerifier(publicKey);
        boolean verified = jwt.verify(verifier);

        // Verify signature
        assertTrue(verified);
        // Validate claims
        JWTClaimsSet claims = jwt.getJWTClaimsSet();
        assertEquals("https://eduid.inovatika.dev/realms/kramerius", claims.getIssuer());
        assertEquals("pepo", claims.getStringClaim("preferred_username"));
        assertTrue(claims.getExpirationTime().before(new java.util.Date()));

        Map realmAccess = (Map) claims.getClaim("realm_access");
        List<String> roles = (List<String>) realmAccess.get("roles");
        System.out.println(roles);
    }

}