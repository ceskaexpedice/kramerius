package cz.incad.kramerius.rest.api.replication.resources;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.json.JSONException;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.rest.api.k5.client.utils.UsersUtils;
import cz.incad.kramerius.security.User;

public class CDKUsersResource {

    @Inject
    Provider<User> userProvider;
	
	public Response user() {
        try {
            User user = this.userProvider.get();
            // DNNT extension; 
            List<String> emptyLabels = new ArrayList<>();
            if (user != null) {
                return Response.ok().entity(UsersUtils.userToJSON(user,emptyLabels).toString())
                        .build();
            } else {
                return Response.ok().entity("{}").build();
            }
        } catch (JSONException e) {
            throw new GenericApplicationException(e.getMessage());
        }
    }

	
	
}
