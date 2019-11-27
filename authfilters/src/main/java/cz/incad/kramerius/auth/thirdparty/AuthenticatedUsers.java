package cz.incad.kramerius.auth.thirdparty;

import javax.servlet.http.HttpServletRequest;

public interface AuthenticatedUsers {

    public String calculateUserName(HttpServletRequest request);

    public HttpServletRequest updateRequest(HttpServletRequest req);

    public String storeUserPropertiesToSession(HttpServletRequest req, String userName) throws Exception;

    public void disconnectUser(String userName);

    public  String getUserPassword(String userName);
}

