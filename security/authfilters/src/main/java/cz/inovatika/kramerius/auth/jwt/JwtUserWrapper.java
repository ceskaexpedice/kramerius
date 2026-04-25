package cz.inovatika.kramerius.auth.jwt;

import cz.incad.kramerius.auth.thirdparty.impl.AbstractThirdPartyUser;
import cz.incad.kramerius.security.DefaultRoles;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.UserManager;
import cz.incad.kramerius.security.impl.UserImpl;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * JwtUserWrapper
 *
 * @author ppodsednik
 */
public class JwtUserWrapper extends AbstractThirdPartyUser {

    private static final Logger LOGGER = Logger.getLogger(JwtUserWrapper.class.getName());

    private String username;
    private Map<String, String> sessionAttributes = new HashMap<>();

    public JwtUserWrapper(String username) {
        this.username = username;
    }

    @Override
    public String getCalculatedName() {
        return username;
    }

    @Override
    public String getProperty(String key) {
        return sessionAttributes.get(key);
    }

    public void setProperty(String key, String value) {
        sessionAttributes.put(key, value);
    }

    @Override
    public Set<String> getPropertyKeys() {
        return sessionAttributes.keySet();
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
        LOGGER.fine(String.format("Associated roles %s", associatedRoles));
        User user = super.toUser(userManager);
        if (user instanceof UserImpl) {
            Role[] allRoles = userManager.findAllRoles("");
            Role[] roles = Arrays.stream(allRoles).filter(role -> associatedRoles.contains(role.getName())).toArray(Role[]::new);
            ((UserImpl) user).setGroups(roles);
        }
        LOGGER.fine(String.format(
                "Returning wrapped user %s (%s)",
                user.getLoginname(),
                Arrays.stream(user.getGroups()).map(Role::getName).collect(Collectors.joining(","))
        ));
        return user;
    }

}