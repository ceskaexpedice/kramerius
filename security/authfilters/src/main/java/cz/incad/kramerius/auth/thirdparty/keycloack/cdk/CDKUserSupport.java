package cz.incad.kramerius.auth.thirdparty.keycloack.cdk;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.keycloak.representations.idm.GroupRepresentation;

import cz.incad.kramerius.auth.thirdparty.impl.AbstractThirdPartyUser;
import cz.incad.kramerius.auth.thirdparty.impl.AbstractThirdPartyUsersSupport;
import cz.incad.kramerius.auth.thirdparty.keycloack.utils.BaseUsersFunctions;
import cz.incad.kramerius.auth.utils.GeneratePasswordUtils;
import cz.incad.kramerius.processes.cdk.KeycloakCDKCache;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.impl.RoleImpl;
import cz.incad.kramerius.security.utils.UserUtils;

public class CDKUserSupport extends AbstractThirdPartyUsersSupport<CDK3rdUser> {

    public static final Logger LOGGER = Logger.getLogger(CDKUserSupport.class.getName());
    
    protected KeycloakCDKCache cache;
    
    public KeycloakCDKCache getCache() {
        return cache;
    }

    public void setCache(KeycloakCDKCache cache) {
        this.cache = cache;
    }

    @Override
    protected String updateExistingUser(String userName, CDK3rdUser kUser) throws Exception {
        User u = this.usersManager.findUserByLoginName(userName);

        UserUtils.associateGroups(u, this.usersManager);
        UserUtils.associateCommonGroup(u, this.usersManager);
        String password = GeneratePasswordUtils.generatePswd();

        User userByLoginName = this.usersManager.findUserByLoginName(userName);
        Role[] groups = userByLoginName.getGroups();

        List<String> fromDb = Arrays.stream(groups).map(Role::getName).collect(Collectors.toList());
        List<String> fromKeycloack = checkRolesExists(kUser).stream().map(Role::getName).collect(Collectors.toList());

        if (!fromKeycloack.isEmpty()) {
            this.usersManager.changeRoles(u, kUser.getRoles());
        }
        return password;

    }

    private List<Role> checkRolesExists(AbstractThirdPartyUser w) {
        List<String> roleString = w.getRoles();
        List<Role> roles = new ArrayList<>();
        roleString.stream().forEach(r-> {
            Role roleByName = this.usersManager.findRoleByName(r);
            if (roleByName != null) {  roles.add(roleByName);  }
            else {
                try {
                    Role nr = new RoleImpl(-1, r,-1);
                    this.usersManager.insertRole(nr);
                    Role nCreated = this.usersManager.findRoleByName(r);
                    if (nCreated != null) { roles.add(nCreated); }
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(),e);
                }
            }
        });
        return roles;
    }

    @Override
    protected boolean checkIfUserExists(String userName) throws Exception {
        User user = this.usersManager.findUserByLoginName(userName);
        return user != null;
    }
    
    @Override
    protected String createNewUser(String user, CDK3rdUser w) throws Exception {
        return BaseUsersFunctions.createNewUser(usersManager, w);
    }

    @Override
    public String calculateUserName(HttpServletRequest req) {
        Map<String, String> map = attributes(req);
        String username = "not_defined";
        if (map.containsKey("edupersonuniqueid")) {
            username = map.get("edupersonuniqueid");
        }
        return "_cdk_"+username;
    }

    @Override
    protected CDK3rdUser createUserWrapper(HttpServletRequest req, String userName) throws Exception {


        Map<String, String> map = attributes(req);
        CDK3rdUser  cdkUser = new CDK3rdUser(userName);
        
        List<GroupRepresentation> resultOfGroup = new ArrayList<>();
        
        List<GroupRepresentation> groups = cache.getGroups();
        groups.stream().forEach(grp-> {
            Map<String, List<String>> attributes = grp.getAttributes();
            map.keySet().forEach(key-> {
                if(attributes.containsKey(key)) {
                    if (matchValue(map.get(key), attributes.get(key))) {
                        LOGGER.info("Found match in attribute:"+key);
                        resultOfGroup.add(grp);
                    }
                }
            });
        });
        
        LOGGER.info("Associated group with user is "+resultOfGroup);
        Set<String> roles = new HashSet<>();
        resultOfGroup.stream().forEach(rg-> {
            roles.addAll(rg.getRealmRoles());
        });
        cdkUser.setRoles(roles.stream().collect(Collectors.toList()));
        
        map.keySet().forEach(key-> {
            cdkUser.setProperty(key, map.get(key));
        });
        
        
        Map<String,List<String>> userAttributes = new HashMap<>();
        map.keySet().forEach(key-> {
            userAttributes.put(key, Arrays.asList(map.get(key)));
        });
        this.cache.registerUser(userName, userName, userName, userAttributes, groups.stream().map(GroupRepresentation::getName).collect(Collectors.toList()));
        
        return cdkUser;
    }

    private Map<String, String> attributes(HttpServletRequest req) {
        Map<String, String> map = new HashMap<>();
        Enumeration headerNames = req.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            Object object = headerNames.nextElement();
            String key = object.toString();
            if (key.startsWith("cdk_")) {
                String mkey = key.substring("cdk_".length());
                Object attribute = req.getHeader(key);
                map.put(mkey.toString(), attribute.toString());
            }
        }
        return map;
    }

    private boolean matchValue(String expectedValue, List<String> groupAttrs) {
        for (String grpAttr : groupAttrs) {
            if (expectedValue.equals(grpAttr)) {
                return true;
            }
        }
        return false;
    }
}
