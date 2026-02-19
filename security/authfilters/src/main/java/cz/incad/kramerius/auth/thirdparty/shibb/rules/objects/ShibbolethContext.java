package cz.incad.kramerius.auth.thirdparty.shibb.rules.objects;

import jakarta.servlet.http.HttpServletRequest;

import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.impl.UserImpl;

public interface ShibbolethContext {

    void associateFirstName(String firstName);
    
    void associateLastName(String lastName);

    void associateSessionAttribute(String key, String value);

    void associateRole(String rname);
    
    boolean isRoleAssociated(String rname);

    HttpServletRequest getHttpServletRequest();
    
}
