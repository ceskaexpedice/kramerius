package cz.incad.kramerius.rest.api.replication.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.kramerius.imaging.ImageStreams;
import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.rest.api.k5.client.user.ClientUserResource;
import cz.incad.kramerius.rest.api.k5.client.utils.UsersUtils;
import cz.incad.kramerius.security.DataMockExpectation;
import cz.incad.kramerius.security.EvaluatingResultState;
import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.Right;
import cz.incad.kramerius.security.RightCriteriumContext;
import cz.incad.kramerius.security.RightCriteriumContextFactory;
import cz.incad.kramerius.security.RightCriteriumException;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.UserManager;
import cz.incad.kramerius.security.impl.DatabaseRightsManager;
import cz.incad.kramerius.users.UserProfileManager;
import cz.incad.kramerius.utils.IPAddressUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class CDKUsersResource {

    public static final Logger LOGGER  = Logger.getLogger(ClientUserResource.class.getName());

    public static final String[] LABELS_CRITERIA = new String[]{
            "cz.incad.kramerius.security.impl.criteria.ReadDNNTLabels",
            "cz.incad.kramerius.security.impl.criteria.ReadDNNTLabelsIPFiltered"
    };

    @Inject
    UserProfileManager userProfileManager;

    @Inject
    IsActionAllowed isActionAllowed;

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
	
	public Response user() {
        try {
            User user = this.userProvider.get();
            HttpServletRequest httpServletRequest = provider.get();
            // DNNT extension; 
            List<String> emptyLabels = findLabels(user);
            if (user != null) {
            	JSONObject userWithIP = UsersUtils.userToJSON(user,emptyLabels);
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
                SecuredActions.READ.getFormalName(),
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

	
	
}
