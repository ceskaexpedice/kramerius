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
package cz.incad.kramerius.rest.apiNew.client.v70;

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

import cz.incad.kramerius.AbstractObjectPath;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.imaging.ImageStreams;
import cz.incad.kramerius.security.*;
import cz.incad.kramerius.security.impl.DatabaseRightsManager;
import cz.incad.kramerius.utils.IPAddressUtils;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.rest.api.k5.client.utils.UsersUtils;
import cz.incad.kramerius.rest.api.replication.exceptions.ObjectNotFound;
import cz.incad.kramerius.rest.apiNew.exceptions.BadRequestException;
import cz.incad.kramerius.security.utils.PasswordDigest;
import cz.incad.kramerius.security.utils.SortingRightsUtils;
import cz.incad.kramerius.users.UserProfileManager;

//@Path("/v5.0/user")
@Path("/client/v7.0/user")
public class ClientUserResource {

    public static final Logger LOGGER  = Logger.getLogger(ClientUserResource.class.getName());

    public static final String[] LABELS_CRITERIA = new String[]{
            "cz.incad.kramerius.security.impl.criteria.ReadDNNTLabels",
            "cz.incad.kramerius.security.impl.criteria.ReadDNNTLabelsIPFiltered"
    };

    @Inject
    UserProfileManager userProfileManager;


    @Inject
    Provider<User> userProvider;

    @Inject
    UserManager userManager;


    @Inject
    DatabaseRightsManager databaseRightsManager;


    @Inject
    RightCriteriumContextFactory ctxFactory;


    @Inject
    Provider<HttpServletRequest> provider;

    @Inject
    @Named("new-index")
    SolrAccess solrAccess;
    
    @Inject
    RightsResolver rightsResolver;


    @GET
    @Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
    public Response info(@QueryParam("sessionAttributes") String sessionAttributes) {
        try {
        	
        	Boolean flag  = Boolean.valueOf(sessionAttributes);
        	if (flag == null) {
        		flag = new Boolean(false);
        	}
        	
            User user = this.userProvider.get();
            // DNNT extension
            List<String> labels = findLabels(user);
            if (user != null) {
                return Response.ok().entity(UsersUtils.userToJSON(user,labels,flag).toString())
                        .build();
            } else {
                return Response.ok().entity("{}").build();
            }
        } catch (JSONException e) {
            throw new GenericApplicationException(e.getMessage());
        }
    }

    private List<String> findLabels(User user) {
        // find all rights associated with current user and labels criteria
        Right[] rightsAsssignedWithLabels = this.databaseRightsManager.findAllRightByCriteriumNames(
                SecuredActions.A_READ.getFormalName(),
                LABELS_CRITERIA,
                user
        );
        // mock evaluate this criteria
        List<String> evaluatedObjects = new ArrayList<>();
        RightCriteriumContext ctx = this.ctxFactory.create(SpecialObjects.REPOSITORY.getPid(), ImageStreams.IMG_FULL.name(), user, this.provider.get().getRemoteHost(), IPAddressUtils.getRemoteAddress(this.provider.get(), KConfiguration.getInstance().getConfiguration()), null);
        Arrays.stream(rightsAsssignedWithLabels).forEach(right -> {
            try {
                EvaluatingResultState result = right.mockEvaluate(ctx, this.databaseRightsManager, DataMockExpectation.EXPECT_DATA_VAUE_EXISTS);
                if (result.equals(EvaluatingResultState.TRUE)) {
                    String name = right.getCriteriumWrapper().getLabel().getName();
                    evaluatedObjects.add(name);
                }
            } catch (RightCriteriumException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        });

        return evaluatedObjects.stream().filter(label-> {
            return  (Character.isAlphabetic(label.charAt(0)));
        }).collect(Collectors.toList());
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
