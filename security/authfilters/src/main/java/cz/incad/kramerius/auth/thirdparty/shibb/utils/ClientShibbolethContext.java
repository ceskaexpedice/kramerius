package cz.incad.kramerius.auth.thirdparty.shibb.utils;

import javax.servlet.http.HttpServletRequest;

import cz.incad.kramerius.auth.thirdparty.shibb.rules.objects.ShibbolethContext;

public  class ClientShibbolethContext implements ShibbolethContext {

    private HttpServletRequest request;
    private Shibboleth3rdUser user;
    
    public ClientShibbolethContext(HttpServletRequest request, Shibboleth3rdUser uwrap) {
        super();
        this.request = request;
        this.user = uwrap;
    }

    @Override
    public void associateFirstName(String firstName) {
        this.user.setFirstName(firstName);
        
    }

    @Override
    public void associateLastName(String lastName) {
        this.user.setLastName(lastName);
    }



    @Override
    public void associateSessionAttribute(String key, String value) {
        this.user.addSessionAttribute(key, value);
    }



    @Override
    public void associateRole(String rname) {
        this.user.getRoles().add(rname);
    }

    @Override
    public boolean isRoleAssociated(String rname) {
        return this.user.getRoles().contains(rname);
    }

    @Override
    public HttpServletRequest getHttpServletRequest() {
        return this.request;
    }
}
