package cz.incad.kramerius.rest.apiNew.client.v70;

import org.json.JSONObject;

import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class ClientKeycloakConfig {
    
    private String realm;
    private String authServer;
    private String resource;
    private String secret;
    
    
    public ClientKeycloakConfig(String realm, String authServer, String resource, String secret) {
        super();
        this.realm = realm;
        this.authServer = authServer;
        this.resource = resource;
        this.secret = secret;
    }
    
    public String getRealm() {
        return realm;
    }
    
    public String getAuthServer() {
        return authServer;
    }
    
    public String getResource() {
        return resource;
    }
    
    public String getSecret() {
        return secret;
    }

    public String loginKeycloak(String redirectUrl, String type) {
        
        if (type == null || !StringUtils.isAnyString(type)) {
            type = KConfiguration.getInstance().getConfiguration().getString("default.eduid.type","all");;
        }
        
        StringBuilder builder = new StringBuilder();
        builder.append(this.authServer);
        if (!builder.toString().endsWith("/")) builder.append("/");
        builder.append("realms/");
        builder.append(this.realm);
        builder.append("/protocol/openid-connect/auth?client_id=").append(this.resource);
        if (StringUtils.isAnyString(redirectUrl)) {
            builder.append("&redirect_uri="+redirectUrl);
        }
        builder.append("&response_type=code");
        
        if (type!= null) {
            builder.append("#"+type);
        }
        
        return builder.toString();
    }

    public String logoutKeycloak(String redirectUrl) {
        StringBuilder builder = new StringBuilder();
        builder.append(this.authServer);
        if (!builder.toString().endsWith("/")) builder.append("/");
        builder.append("realms/");
        builder.append(this.realm);
        builder.append("/protocol/openid-connect/logout");
        if (StringUtils.isAnyString(redirectUrl)) {
            builder.append("?redirect_uri="+redirectUrl);
        }
        return builder.toString();
    }

    public String token(String code) {
        StringBuilder builder = new StringBuilder();
        builder.append(this.authServer);
        if (!builder.toString().endsWith("/")) builder.append("/");
        builder.append("realms/");
        builder.append(this.realm);
        builder.append("/protocol/openid-connect/token");
        return builder.toString();
    }
    
    public static ClientKeycloakConfig load(JSONObject conf) {
        String realm = conf.optString("realm");
        String authServer = conf.optString("auth-server-url");
        String resource = conf.optString("resource");
        String secret = null;
        JSONObject credentials = conf.optJSONObject("credentials");
        if (credentials != null) {
            secret = credentials.optString("secret");
        }
        return new ClientKeycloakConfig(realm, authServer, resource, secret);
    }
    
}
