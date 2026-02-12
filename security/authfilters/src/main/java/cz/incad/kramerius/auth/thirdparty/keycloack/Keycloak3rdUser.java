package cz.incad.kramerius.auth.thirdparty.keycloack;

import cz.incad.kramerius.auth.thirdparty.impl.AbstractThirdPartyUser;
import cz.incad.kramerius.security.DefaultRoles;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.UserManager;
import cz.incad.kramerius.security.impl.UserImpl;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Keycloak3rdUser extends AbstractThirdPartyUser {
    
    public static final Logger LOGGER = Logger.getLogger(Keycloak3rdUser.class.getName());
    
    private String hash;

    private Map<String,String> sessionAttributes = new HashMap<>();

    public Keycloak3rdUser(String h) {
        this.hash = h;
    }

    @Override
    public String getCalculatedName() {
        return this.hash;
    }

    @Override
    public String getProperty(String key) {
        return sessionAttributes.get(key);
    }

    public void setProperty(String key, String value) {
        this.sessionAttributes.put(key, value);
    }

    @Override
    public Set<String> getPropertyKeys() {
        return this.sessionAttributes.keySet();
    }


    @Override
    public User toUser(UserManager userManager) {
        List<String> associatedRoles = getRoles();
        if (!associatedRoles.contains(DefaultRoles.COMMON_USERS.getName())) {
            associatedRoles.add(DefaultRoles.COMMON_USERS.getName());
        }
        if (!associatedRoles.contains(DefaultRoles.AUTHENTICATED_USERS.getName())) {
            associatedRoles.add(DefaultRoles.AUTHENTICATED_USERS.getName());
        }
        LOGGER.fine(String.format("Associated roles %s",associatedRoles.toString()));

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
        LOGGER.fine(String.format("Returning wrapped user %s (%s)",user.getLoginname(), Arrays.stream(user.getGroups()).map(Role::getName).collect(Collectors.joining(","))));
        
        return user;
    }
}
