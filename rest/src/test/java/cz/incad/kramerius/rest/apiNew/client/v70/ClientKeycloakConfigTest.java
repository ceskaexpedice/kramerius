package cz.incad.kramerius.rest.apiNew.client.v70;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import cz.incad.kramerius.utils.conf.KConfiguration;


public class ClientKeycloakConfigTest {
    
    
    @Test
    public void testConfig() {
        String json ="{\r\n"
                + "  \"realm\": \"kramerius\",\r\n"
                + "  \"auth-server-url\": \"https://k7.inovatika.dev/auth/\",\r\n"
                + "  \"ssl-required\": \"external\",\r\n"
                + "  \"resource\": \"krameriusClient\",\r\n"
                + "  \"verify-token-audience\": false,\r\n"
                + "  \"credentials\": {\r\n"
                + "    \"secret\": \"XXXXXXXXXXXX\"\r\n"
                + "  },\r\n"
                + "  \"confidential-port\": 0,\r\n"
                + "  \"policy-enforcer\": {}\r\n"
                + "}";
        
        ClientKeycloakConfig config = ClientKeycloakConfig.load(new JSONObject(json));
        Assert.assertTrue(config.getAuthServer().equals("https://k7.inovatika.dev/auth/"));
        Assert.assertTrue(config.getRealm().equals("kramerius"));
        Assert.assertTrue(config.getResource().equals("krameriusClient"));
        Assert.assertTrue(config.getSecret().equals("XXXXXXXXXXXX"));

        KConfiguration.getInstance().getConfiguration().setProperty("default.eduid.type","idp");
        
        String url = config.loginKeycloak("http://localhost:4200/keycloak",null);
        Assert.assertEquals("https://k7.inovatika.dev/auth/realms/kramerius/protocol/openid-connect/auth?client_id=krameriusClient&redirect_uri=http://localhost:4200/keycloak&response_type=code#idp", url);

        String urlIdp = config.loginKeycloak("http://localhost:4200/keycloak","idp");
        Assert.assertEquals("https://k7.inovatika.dev/auth/realms/kramerius/protocol/openid-connect/auth?client_id=krameriusClient&redirect_uri=http://localhost:4200/keycloak&response_type=code#idp", urlIdp);

        String urlForm = config.loginKeycloak("http://localhost:4200/keycloak","form");
        Assert.assertEquals("https://k7.inovatika.dev/auth/realms/kramerius/protocol/openid-connect/auth?client_id=krameriusClient&redirect_uri=http://localhost:4200/keycloak&response_type=code#form", urlForm);

    }
    
}
