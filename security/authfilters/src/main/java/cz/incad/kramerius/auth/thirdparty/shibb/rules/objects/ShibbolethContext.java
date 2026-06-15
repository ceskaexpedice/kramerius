package cz.incad.kramerius.auth.thirdparty.shibb.rules.objects;

import jakarta.servlet.http.HttpServletRequest;

public interface ShibbolethContext {

    void associateFirstName(String firstName);
    
    void associateLastName(String lastName);

    void associateSessionAttribute(String key, String value);

    void associateRole(String rname);
    
    boolean isRoleAssociated(String rname);

    HttpServletRequest getHttpServletRequest();
    
}
