package cz.incad.kramerius.security.impl.http.shibrules.shibs;

import javax.servlet.http.HttpServletRequest;

import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.impl.UserImpl;

public interface ShibbolethContext {

    public void associateFirstName(String firstName);
    
    public void associateLastName(String lastName);
    
    public void associateRole(String rname);
    
    public boolean isRoleAssociated(String rname);

    public HttpServletRequest getHttpServletRequest();
    
//    User user = ctx.getUser();
//    if (userField.equals(FIRSTNAME)) {
//        ((UserImpl) user).setFirstName(this.value.getValue(ctx.getHttpServletRequest()));
//    } else if (userField.equals(SURNAME)) {
//        ((UserImpl) user).setSurname(this.value.getValue(ctx.getHttpServletRequest()));
//    } else throw new IllegalStateException("illegal key '"+userField);
}
