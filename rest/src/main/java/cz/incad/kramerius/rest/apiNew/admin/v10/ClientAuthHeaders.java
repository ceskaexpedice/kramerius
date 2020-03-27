package cz.incad.kramerius.rest.apiNew.admin.v10;

import cz.incad.kramerius.rest.api.exceptions.ProxyAuthenticationRequiredException;
import cz.incad.kramerius.utils.StringUtils;

import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;

public class ClientAuthHeaders {

    public static final String AUTH_HEADER_CLIENT = "client";
    public static final String AUTH_HEADER_UID = "uid";
    public static final String AUTH_HEADER_ACCESS_TOKEN = "access-token";

    private final String client;
    private final String uid;
    private final String accessToken;

    public ClientAuthHeaders(String client, String uid, String accessToken) {
        this.client = client;
        this.uid = uid;
        this.accessToken = accessToken;
    }

    public String getClient() {
        return client;
    }

    public String getUid() {
        return uid;
    }

    public String getAccessToken() {
        return accessToken;
    }

    @Override
    public String toString() {
        return "ClientAuthHeaders{" +
                "client='" + client + '\'' +
                ", uid='" + uid + '\'' +
                ", accessToken='" + accessToken + '\'' +
                '}';
    }

    public static ClientAuthHeaders extract(Provider<HttpServletRequest> requestProvider) throws ProxyAuthenticationRequiredException {
        String client = requestProvider.get().getHeader(AUTH_HEADER_CLIENT);
        String uid = requestProvider.get().getHeader(AUTH_HEADER_UID);
        String accessToken = requestProvider.get().getHeader(AUTH_HEADER_ACCESS_TOKEN);
        if (!StringUtils.isAnyString(accessToken) || !StringUtils.isAnyString(uid) || !StringUtils.isAnyString(client)) {
            throw new ProxyAuthenticationRequiredException("missing one of headaers '%s', '%s', or '%s'", AUTH_HEADER_CLIENT, AUTH_HEADER_UID, AUTH_HEADER_ACCESS_TOKEN);
        }
        return new ClientAuthHeaders(client, uid, accessToken);
    }


}
