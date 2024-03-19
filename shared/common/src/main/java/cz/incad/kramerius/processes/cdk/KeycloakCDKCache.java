package cz.incad.kramerius.processes.cdk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.GroupsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import cz.incad.kramerius.utils.conf.KConfiguration;

public class KeycloakCDKCache {
    
    public static Logger LOGGER = Logger.getLogger(KeycloakCDKCache.class.getName());
    
    private int interval = 1000;
    private Timer timer;
    private List<GroupRepresentation> groups;
    private List<UserRepresentation> users;
    
    public void init() {
        LOGGER.info("Initializing KeycloakCDKCache");
        this.timer = new Timer(KeycloakCDKCache.class.getName()+"-thread",true);
        this.groups = new ArrayList<>();
        this.users = new ArrayList<>();
        String sinterval  = KConfiguration.getInstance().getProperty("cdk.keycloak.groups","10000");
        this.interval =  Integer.parseInt(sinterval);

    }
    
    public void setGroups(List<GroupRepresentation> groups) {
        LOGGER.info("Recognized groups for cdk:"+groups.stream().map(GroupRepresentation::getName).collect(Collectors.toList()));
        this.groups = groups;
        
    }
    
    public List<GroupRepresentation> getGroups() {
        return groups;
    }
    
    public void registerUser(String calculatedUserName, String firstName, String lastName,Map<String,List<String>> attributes, List<String> groups) {
        UserRepresentation repre = new UserRepresentation();
        repre.setUsername(calculatedUserName);
        repre.setFirstName(firstName);
        repre.setLastName(lastName);
        repre.setAttributes(attributes);
        repre.setGroups(groups);
        repre.setEnabled(true);
        repre.setEmailVerified(true);
        synchronized (this.users) {
            this.users.add(repre);
        }
    }
    
    
    public void shutdown() {
        LOGGER.info("canceling KeycloakCache scheduler");
        this.timer.cancel();
    }
    
    public void scheduleNextTask() {
        this.timer.purge();
        KeycloakCDKCacheTask schedulerTsk = new KeycloakCDKCacheTask(this);
        this.timer.schedule(schedulerTsk, this.interval);
    }

    public static Keycloak keycloak(ResteasyClientBuilder newBuilder) {
        
        String url = KConfiguration.getInstance().getConfiguration().getString("cdk.keycloak.url");
        String master = KConfiguration.getInstance().getConfiguration().getString("cdk.keycloak.realm.master","master");
        String adminClient = KConfiguration.getInstance().getConfiguration().getString("cdk.keycloak.adminclient","admin-cli");
        String adminUser = KConfiguration.getInstance().getConfiguration().getString("cdk.keycloak.adminuser");
        String adminPass = KConfiguration.getInstance().getConfiguration().getString("cdk.keycloak.adminpass");
        
        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(url)
                .grantType(OAuth2Constants.PASSWORD)
                .realm(master)
                .clientId(adminClient)
                .username(adminUser)
                .password(adminPass)
                .resteasyClient(
                    newBuilder.connectionPoolSize(10).build()
                ).build();
        return keycloak;
    }
    
    public static class KeycloakCDKCacheTask extends TimerTask {

        private static final String CDK = "cdk";

        KeycloakCDKCache cache;
        
        public KeycloakCDKCacheTask(KeycloakCDKCache cache) {
            super();
            this.cache = cache;
        }

        @Override
        public void run() {
            boolean channelEnabled  =  KConfiguration.getInstance().getConfiguration().getBoolean("cdk.secured.channel");
            if (channelEnabled) {
                Keycloak keycloak =  null;
                Boolean users = KConfiguration.getInstance().getConfiguration().getBoolean("cdk.keycloak.realm.createusers",true);
                String kramerius = KConfiguration.getInstance().getConfiguration().getString("cdk.keycloak.realm.kramerius","kramerius");
                String url = KConfiguration.getInstance().getConfiguration().getString("cdk.keycloak.url");
                try {
                    if (url !=  null) {
                        ResteasyClientBuilder newBuilder = (ResteasyClientBuilder) ResteasyClientBuilder.newBuilder();
                        keycloak = keycloak(newBuilder);
                        
                         
                        RealmResource realmResource = keycloak.realm(kramerius);
                        
                        // referesh groups in cache
                        refreshGroups(realmResource);
                        
                        // refresh registerUsers
                        if (users) {
                            refreshOrRegisterUsers(realmResource);
                        }
                        
                    } else {
                        LOGGER.warning("Missing attributes: 'cdk.keycloak.url', 'cdk.keycloak.adminuser', 'cdk.keycloak.adminpass' in configuration");
                    }
                } finally {
                    if (keycloak != null) keycloak.close();
                    cache.scheduleNextTask();
                }
            }
        }

        private void refreshOrRegisterUsers(RealmResource realmResource) {
            List<UserRepresentation> usersToStore = new ArrayList<>();
            synchronized(this.cache.users) {
                this.cache.users.stream().forEach(u-> { usersToStore.add(u); });
                this.cache.users.clear();
            }
  
            UsersResource users = realmResource.users();
            usersToStore.forEach(u-> {
                List<UserRepresentation> searchRes = users.search(u.getUsername());
                if (searchRes.isEmpty()) {
                    Response created = users.create(u);
                    if (created.getStatus() == 201) {
                        
                        MultivaluedMap<String,Object> headers = created.getMetadata();;
                        List<Object> list = headers.get("Location");
                        if (!list.isEmpty()) {
                            String location = list.get(0).toString();
                            int indexOf = location.indexOf("users/");
                            String id = location.substring(indexOf+"users/".length());
                            associateWithGroups(users, u, id);
                        }
                    }
                } else {
                    UserRepresentation found = searchRes.get(0);
                    String id = found.getId();
                    associateWithGroups(users, u, id);
                }
            });
        }

        private void associateWithGroups(UsersResource users, UserRepresentation u, String id) {
            UserResource userResource = users.get(id);
            List<GroupRepresentation> groups = userResource.groups();
            List<String> storedGroups = groups.stream().map(GroupRepresentation::getName).collect(Collectors.toList());
            List<String> mappingGroups = new ArrayList<>(u.getGroups());
            
            while(!storedGroups.isEmpty()) {
                String removed = storedGroups.remove(0);
                if (!mappingGroups.contains(removed)) {
                    List<GroupRepresentation> filtered = cache.getGroups().stream().filter(g-> { return g.getName().equals(removed);}).collect(Collectors.toList());
                    if (!filtered.isEmpty()) {
                        userResource.leaveGroup(filtered.get(0).getId());
                    }
                } else {
                    mappingGroups.remove(removed);
                }
            }
            
            if (!mappingGroups.isEmpty()) {
                mappingGroups.stream().forEach(ng-> {
                    List<GroupRepresentation> filtered = cache.getGroups().stream().filter(g-> { return g.getName().equals(ng);}).collect(Collectors.toList());
                    if (!filtered.isEmpty()) {
                        userResource.joinGroup(filtered.get(0).getId());
                    }
                });
            }
        }
        
        private void refreshGroups(RealmResource realmResource) {
            GroupsResource groupsResource = realmResource.groups();
            List<GroupRepresentation> cdkGroups = groupsResource.groups(CDK, 0, 100, false);
            cache.setGroups(cdkGroups);
        }
    }
    
    
}
