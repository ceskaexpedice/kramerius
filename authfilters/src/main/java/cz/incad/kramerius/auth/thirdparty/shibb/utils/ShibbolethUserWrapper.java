package cz.incad.kramerius.auth.thirdparty.shibb.utils;

import java.util.*;

import cz.incad.kramerius.auth.thirdparty.UsersWrapper;
import cz.incad.kramerius.auth.thirdparty.impl.AbstractUsersWrapper;
import cz.incad.kramerius.security.utils.UserUtils;

public class ShibbolethUserWrapper extends AbstractUsersWrapper {

    private String calculatedName;
    private String firstName;
    private String lastName;

    private Map<String, String> sessionAttributes = new HashMap<>();

    private List<String> roles = new ArrayList<String>();
    
    public ShibbolethUserWrapper(String calculatedName) {
        super();
        this.calculatedName = calculatedName;
    }

    @Override
    public String getCalculatedName() {
        return this.calculatedName;
    }

    public void setFirstName(String fname) {
        this.firstName = fname;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setRoles(List<String> rls) {
        this.roles = rls;
    }
    
    public List<String> getRoles() {
        return roles;
    }


    public void addSessionAttribute(String key, String value) {
        this.sessionAttributes.put(key, value);
    }

    public void removeSessionAttribute(String key) {
        this.sessionAttributes.remove(key);
    }


    @Override
    public String getProperty(String key) {
        if (key.equals(UserUtils.FIRST_NAME_KEY)) {
            return this.firstName;
        } else if (key.equals(UserUtils.LAST_NAME_KEY)) {
            return this.lastName;
        } else  return this.sessionAttributes.get(key);
    }

    @Override
    public Set<String> getPropertyKeys() {
        Set<String> set = new HashSet<>(this.sessionAttributes.keySet());
        if (this.firstName != null) set.add(UserUtils.FIRST_NAME_KEY);
        if (this.lastName != null)  set.add(UserUtils.LAST_NAME_KEY);
        return set;
    }
}
