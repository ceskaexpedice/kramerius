package cz.incad.kramerius.auth.thirdparty.shibb.utils;

import java.util.*;
import java.util.stream.Collectors;

import cz.incad.kramerius.auth.thirdparty.UsersWrapper;
import cz.incad.kramerius.auth.thirdparty.impl.AbstractUsersWrapper;
import cz.incad.kramerius.security.DefaultRoles;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.UserManager;
import cz.incad.kramerius.security.impl.UserImpl;
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

    @Override
    public User toUser(UserManager userManager) {
        List<String> associatedRoles = getRoles();
        if (!associatedRoles.contains(DefaultRoles.COMMON_USERS.getName())) {
            associatedRoles.add(DefaultRoles.COMMON_USERS.getName());
        }

        User user = super.toUser(userManager);
        // TODO: Change it
        if (user instanceof UserImpl) {
            Role[] allRoles = userManager.findAllRoles("");

            Role[] roles = Arrays.stream(allRoles).filter(role -> {
                String rName = role.getName();
                return associatedRoles.contains(rName);
            }).toArray(Role[]::new);

            ((UserImpl) user).setGroups(roles);

        }
        return user;
    }
}
