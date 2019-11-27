package cz.incad.kramerius.auth.thirdparty.shibb.rules.objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.UserManager;
import cz.incad.kramerius.security.impl.UserImpl;

public class ShibbolethContextImpl  implements ShibbolethContext {

    private UserImpl userImpl;
    private UserManager userManager;
    private HttpServletRequest httpServletRequest;
    

    public ShibbolethContextImpl(UserImpl userImpl, UserManager userManager,
            HttpServletRequest httpServletRequest) {
        super();
        this.userImpl = userImpl;
        this.userManager = userManager;
        this.httpServletRequest = httpServletRequest;
    }

    @Override
    public void associateFirstName(String firstName) {
        this.userImpl.setFirstName(firstName);
    }

    @Override
    public void associateLastName(String lastName) {
        this.userImpl.setSurname(lastName);
    }

    @Override
    public void associateRole(String rname) {
      Role grole = this.userManager.findRoleByName(rname);
      Role[] groups = this.userImpl.getGroups() == null ? new Role[0]:this.userImpl.getGroups();
      List<Role> grpList = new ArrayList(Arrays.asList(groups));
      grpList.add(grole);
      this.userImpl.setGroups(grpList.toArray(new Role[grpList.size()]));
    }

    @Override
    public boolean isRoleAssociated(String rname) {
        Role grole = this.userManager.findRoleByName(rname);
        return grole != null;
    }

    @Override
    public HttpServletRequest getHttpServletRequest() {
        return this.httpServletRequest;
    }
}
