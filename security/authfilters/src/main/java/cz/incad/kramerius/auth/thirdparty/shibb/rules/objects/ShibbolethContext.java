package cz.incad.kramerius.auth.thirdparty.shibb.rules.objects;

import javax.servlet.http.HttpServletRequest;

import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.impl.UserImpl;

public interface ShibbolethContext {

    public void associateFirstName(String firstName);
    
    public void associateLastName(String lastName);
    
    public void associateRole(String rname);
    
    public boolean isRoleAssociated(String rname);

    public HttpServletRequest getHttpServletRequest();
    
}
