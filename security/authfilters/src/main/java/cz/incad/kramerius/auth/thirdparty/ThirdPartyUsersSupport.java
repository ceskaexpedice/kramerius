package cz.incad.kramerius.auth.thirdparty;

import javax.servlet.http.HttpServletRequest;

public interface ThirdPartyUsersSupport {

    String calculateUserName(HttpServletRequest request);

    HttpServletRequest updateRequest(HttpServletRequest req);

    String storeUserPropertiesToSession(HttpServletRequest req, String userName) throws Exception;

    void disconnectUser(String userName);

    String getUserPassword(String userName);
}

