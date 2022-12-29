/*
 * Copyright (C) 2013 Pavel Stastny
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.kramerius.rest.apiNew.client.v60;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.imaging.ImageStreams;
import cz.incad.kramerius.security.*;
import cz.incad.kramerius.security.impl.DatabaseRightsManager;
import cz.incad.kramerius.security.impl.RoleImpl;
import cz.incad.kramerius.security.impl.UserImpl;
import cz.incad.kramerius.utils.IPAddressUtils;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.sun.jersey.api.client.Client;

import cz.incad.kramerius.rest.api.exceptions.BadRequestException;
import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.rest.api.k5.client.utils.UsersUtils;
import cz.incad.kramerius.rest.api.replication.exceptions.ObjectNotFound;
import cz.incad.kramerius.rest.apiNew.client.v60.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v60.libs.OneInstance;
import cz.incad.kramerius.rest.apiNew.client.v60.redirection.ProxyHandlerException;
import cz.incad.kramerius.rest.apiNew.client.v60.redirection.user.ProxyUserHandler;
import cz.incad.kramerius.security.RightsResolver;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.UserManager;
import cz.incad.kramerius.security.utils.PasswordDigest;
import cz.incad.kramerius.users.UserProfile;
import cz.incad.kramerius.users.UserProfileManager;

//@Path("/v5.0/user")
@Path("/client/v7.0/user")
public class ClientUserResource {

//	public static final boolean DEBUG = true;
	
    public static final Logger LOGGER  = Logger.getLogger(ClientUserResource.class.getName());

    public static final String[] LABELS_CRITERIA = new String[]{
            "cz.incad.kramerius.security.impl.criteria.ReadDNNTLabels",
            "cz.incad.kramerius.security.impl.criteria.ReadDNNTLabelsIPFiltered"
    };

    @Inject
    UserProfileManager userProfileManager;

    @Inject
    RightsResolver rightsResolver;

    @Inject
    Provider<User> userProvider;

    @Inject
    UserManager userManager;

    @Inject
    @Named("new-index")
    SolrAccess solrAccess;

    @Inject
    DatabaseRightsManager databaseRightsManager;

    @Inject
    RightCriteriumContextFactory ctxFactory;

    @Inject
    Provider<HttpServletRequest> provider;
    
    @Inject
    Instances instances;

    @Inject
    @Named("forward-client")
    Provider<Client> clientProvider;

    
    @GET
    @Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
    public Response info(@QueryParam("sessionAttributes") String sessionAttributes) {
        try {
        	Boolean flag  = Boolean.valueOf(sessionAttributes);
        	if (flag == null) {
        		flag = new Boolean(false);
        	}
        	User user = this.userProvider.get();
//        	if (DEBUG) {
//        		UserImpl useri = new UserImpl(1, "Pavel", "Stastny", "pavel.stastny@gmail.com", 0);
//        		useri.setGroups(new Role[] {new RoleImpl("common_users")});
//        		user = useri;
//        	}
    		LOGGER.fine(String.format("Returning principal %s (%s)", user.getLoginname(), user.getGroups() != null ? Arrays.asList(user.getGroups()).stream().map(Role::getName).collect(Collectors.joining(",")): ""));
            if (user != null) {
            	if (user.getId() > -1) {
                	List<String> labels = findLabels(user);
                	return Response.ok().entity(UsersUtils.userToJSON(user,labels,flag).toString())
                            .build();
            	} else {
                	return Response.ok().entity(UsersUtils.userToJSON(user,new ArrayList<>(),flag).toString())
                            .build();
            	}
            } else {
                return Response.ok().entity("{}").build();
            }
        } catch (JSONException e) {
            throw new GenericApplicationException(e.getMessage());
        }
    }

    private List<String> findLabels(User user) {
    	
    	List<String> licenses = new ArrayList<>();
    	
    	Client client = this.clientProvider.get();
    	String remoteAddress = IPAddressUtils.getRemoteAddress(this.provider.get(), KConfiguration.getInstance().getConfiguration());
    	
    	List<OneInstance> userAggegations = new ArrayList<>();
    	List<OneInstance> eInstances = this.instances.enabledInstances();
    	eInstances.forEach(eI -> {
    		if (eI.hasFullAccess()) {
    			userAggegations.add(eI);
    		}
    	});
    	
    	for (OneInstance oneInstance : userAggegations) {
			try {
				ProxyUserHandler proxyUserHandler = oneInstance.createProxyUserHandler(user, client, solrAccess, oneInstance.getName(), remoteAddress);
				Pair<User,List<String>> retval = proxyUserHandler.user();
				if (retval != null) {
					licenses.addAll(retval.getValue());
					Map<String, String> sessionAttributes = retval.getKey().getSessionAttributes();
					sessionAttributes.keySet().forEach(key-> {
						user.addSessionAttribute(oneInstance.getName()+"_"+key, sessionAttributes.get(key));
					});
				
				}
			} catch (ProxyHandlerException e) {
				LOGGER.log(Level.SEVERE,e.getMessage());
			}
    	}
    	
    	return licenses;
    }

    //TODO: Merge with actionsForPids
    @GET
    @Path("actions")
    @Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
    public Response allowedActions(@QueryParam("pid") String pid) {
        User user;
        try {
            user = this.userProvider.get();
            if (pid == null || !StringUtils.isAnyString(pid)) {
                pid = SpecialObjects.REPOSITORY.getPid();
            }
            
            
            ObjectPidsPath[] pidPaths = this.solrAccess.getPidPaths(pid);
            user = this.userProvider.get();
            
            SecuredActions[] values = SecuredActions.values();
            if (!SpecialObjects.REPOSITORY.getPid().equals(pid)) {
                values = Arrays.stream(values).filter(it-> {
                    return !it.isGlobalAction();
                }).toArray(SecuredActions[]::new);
            }

            values = Arrays.stream(values).filter(it-> {
                String formalName = it.getFormalName();
                return formalName.startsWith("a_");
            }).toArray(SecuredActions[]::new);


            JSONObject retobject = new JSONObject();
            Set<String> set = actionsForPid(pid, pidPaths, values);
            JSONArray jsonArray = new JSONArray();
            set.forEach(jsonArray::put);
            retobject.put("actions", jsonArray);

            return Response.ok().entity(retobject.toString()).build();
        } catch (UnsupportedEncodingException e) {
            throw new GenericApplicationException(e.getMessage());
        } catch (JSONException e) {
            throw new GenericApplicationException(e.getMessage());
        } catch (IOException e) {
            throw new GenericApplicationException(e.getMessage());
		}
    }

    private Set<String> actionsForPid(String pid, ObjectPidsPath[] pidPaths, SecuredActions[] values) {
        Set<String> set = new LinkedHashSet<>();
        for (SecuredActions sa : values) {
            for (ObjectPidsPath pth : pidPaths) {
                pth = pth.injectRepository();
                RightsReturnObject actionAllowed = this.rightsResolver.isActionAllowed(userProvider.get(), sa.getFormalName(),pid,null,pth.injectRepository());
                if (actionAllowed.getState() == EvaluatingResultState.TRUE) {
                    set.add(sa.getFormalName());
                }
            }
        }         
        return set;
    }

    
    @POST
    @Path("pids_actions")
    @Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
    @Consumes(MediaType.APPLICATION_JSON)
    public Response pidsActionsGET(JSONObject rawdata) {
        //@QueryParam("pids") String pids
        User user;
        try {
            if (rawdata.has("pids") && (rawdata.get("pids") instanceof JSONArray)) {
                JSONArray jsonArray = rawdata.getJSONArray("pids");
                user = this.userProvider.get();
                JSONObject retobject = new JSONObject();
                for (int i = 0; i < jsonArray.length(); i++) {
                    String pid = jsonArray.getString(i);

                    SecuredActions[] values = actions(rawdata, pid);
                    
                    ObjectPidsPath[] pidPaths = this.solrAccess.getPidPaths(pid);
                    user = this.userProvider.get();

                    Set<String> set = actionsForPid(pid, pidPaths, values);
                    JSONArray retArray = new JSONArray();
                    set.forEach(retArray::put);
                    retobject.put(pid, retArray);
                }

                return Response.ok().entity(retobject.toString()).build();
            } else {
                throw new BadRequestException("expecting 'pids' array");
            }
            
        } catch (UnsupportedEncodingException e) {
            throw new GenericApplicationException(e.getMessage());
        } catch (JSONException e) {
            throw new GenericApplicationException(e.getMessage());
        } catch (IOException e) {
            throw new GenericApplicationException(e.getMessage());
        }
        
    }

    private SecuredActions[] actions(JSONObject rawdata, String pid) {
        SecuredActions[] values = SecuredActions.values();
        if (!SpecialObjects.REPOSITORY.getPid().equals(pid)) {
            values = Arrays.stream(values).filter(it-> {
                return !it.isGlobalAction();
            }).toArray(SecuredActions[]::new);
        }

        values = Arrays.stream(values).filter(it-> {
            String formalName = it.getFormalName();
            return formalName.startsWith("a_");
        }).toArray(SecuredActions[]::new);

        
        if (rawdata.has("actions")) {

            JSONArray actionsArray = rawdata.getJSONArray("actions");
            List<String> splitted = new ArrayList<>();
            for (int i = 0; i < actionsArray.length(); i++) { splitted.add(actionsArray.getString(i)); }
            
            values = Arrays.stream(values).filter(it-> {
                String formalName = it.getFormalName();
                return splitted.contains(formalName);
            }).toArray(SecuredActions[]::new);

        }
        return values;
    }

    @POST
    @Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changePassword(JSONObject rawdata) {
        User user;
        try {
            user = this.userProvider.get();
            if (user != null && user.getId() != -1) {
                if (rawdata.has("pswd")) {
                    String newPswd = PasswordDigest.messageDigest(rawdata
                            .getString("pswd"));
                    this.userManager.saveNewPassword(user.getId(), newPswd);
                    return Response.ok()
                            .entity(UsersUtils.userToJSON(user,false).toString())
                            .build();
                } else {
                    throw new ObjectNotFound("cannot find user " + user.getId());
                }
            } else {
                throw new ObjectNotFound("cannot find user " + user.getId());
            }
        } catch (SQLException e) {
            throw new GenericApplicationException(e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            throw new GenericApplicationException(e.getMessage());
        } catch (UnsupportedEncodingException e) {
            throw new GenericApplicationException(e.getMessage());
        } catch (JSONException e) {
            throw new GenericApplicationException(e.getMessage());
        }
    }

    
    
    @GET
    @Path("logout")
    public Response logout() {
        HttpServletRequest httpServletRequest = this.provider.get();
        httpServletRequest.getSession().invalidate();
        return Response.ok().entity("{}").build();
    }

}
