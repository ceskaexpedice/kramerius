package cz.incad.kramerius.rest.apiNew.cdk.v70.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.kramerius.imaging.ImageStreams;
import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.rest.api.k5.client.utils.UsersUtils;
import cz.incad.kramerius.rest.apiNew.client.v70.ClientUserResource;
import cz.incad.kramerius.security.DataMockExpectation;
import cz.incad.kramerius.security.EvaluatingResultState;
import cz.incad.kramerius.security.Right;
import cz.incad.kramerius.security.RightCriteriumContext;
import cz.incad.kramerius.security.RightCriteriumContextFactory;
import cz.incad.kramerius.security.RightCriteriumException;
import cz.incad.kramerius.security.RightsResolver;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.UserManager;
import cz.incad.kramerius.security.impl.DatabaseRightsManager;
import cz.incad.kramerius.security.licenses.License;
import cz.incad.kramerius.security.licenses.LicensesManager;
import cz.incad.kramerius.security.licenses.LicensesManagerException;
import cz.incad.kramerius.security.licenses.lock.ExclusiveLockMaps;
import cz.incad.kramerius.users.UserProfileManager;
import cz.incad.kramerius.utils.IPAddressUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

import static cz.incad.kramerius.rest.apiNew.client.v70.ClientUserResource.*;

/**
 * Provides endpoints for user's endpoint
 * @author happy
 */
public class CDKUsersResource {

    public static final Logger LOGGER  = Logger.getLogger(CDKUsersResource.class.getName());

    @Inject
    UserProfileManager userProfileManager;

    @Inject
    RightsResolver isActionAllowed;

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
    LicensesManager licensesManager;

    @Inject
    ExclusiveLockMaps exclusiveLockMaps;
    
	public Response user() {
        try {
            User user = this.userProvider.get();
            HttpServletRequest httpServletRequest = provider.get();
            // DNNT extension; 
            List<String> emptyLabels = findLabels(user);
            if (user != null) {
            	JSONObject userWithIP = UsersUtils.userToJSON(user,emptyLabels, true);
            	userWithIP.put("remoteAddr", httpServletRequest.getRemoteAddr());
                return Response.ok().entity(userWithIP.toString())
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
                LICENSES_CRITERIA,
                user
        );
        // mock evaluate this criteria
        List<String> evaluatedObjects = new ArrayList<>();
        RightCriteriumContext ctx = this.ctxFactory.create(SpecialObjects.REPOSITORY.getPid(), ImageStreams.IMG_FULL.name(), user, this.provider.get().getRemoteHost(), IPAddressUtils.getRemoteAddress(this.provider.get()), null, this.exclusiveLockMaps);
        Arrays.stream(rightsAsssignedWithLabels).forEach(right -> {
            try {
                EvaluatingResultState result = right.mockEvaluate(ctx, this.databaseRightsManager, DataMockExpectation.EXPECT_DATA_VAUE_EXISTS);
                if (result.equals(EvaluatingResultState.TRUE)) {
                    String name = right.getCriteriumWrapper().getLicense().getName();
                    evaluatedObjects.add(name);
                }
            } catch (RightCriteriumException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        });


        List<String> userLicenses =  evaluatedObjects.stream().filter(label-> {
            return  (Character.isAlphabetic(label.charAt(0)));
        }).collect(Collectors.toList());
        

        
        try {
            List<License> licenses = this.licensesManager.getLicenses();
            licenses.sort(new Comparator<License>() {
                @Override
                public int compare(License o1, License o2) {
                    return Integer.valueOf(o1.getPriority()).compareTo(Integer.valueOf(o2.getPriority()));
                }
            });
            
            List<String> retvals = new ArrayList<>();
            for (License license : licenses) {
                if (userLicenses.contains(license.getName())) {
                    retvals.add(license.getName());
                }
            }

            return retvals;
        } catch (LicensesManagerException e) {
            LOGGER.log(Level.SEVERE,e.getMessage());
            return userLicenses;
        }
    }
}
