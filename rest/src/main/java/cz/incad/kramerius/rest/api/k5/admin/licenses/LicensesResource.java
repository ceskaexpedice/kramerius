package cz.incad.kramerius.rest.api.k5.admin.licenses;

import com.google.inject.Inject;
import com.google.inject.Provider;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.rest.api.exceptions.ActionNotAllowed;
import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.rest.api.k5.admin.utils.LicenseUtils;
import cz.incad.kramerius.rest.api.replication.exceptions.ObjectNotFound;
import cz.incad.kramerius.security.*;
import cz.incad.kramerius.security.labels.Label;
import cz.incad.kramerius.security.labels.LabelsManager;
import cz.incad.kramerius.security.labels.LabelsManagerException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static cz.incad.kramerius.rest.api.k5.admin.utils.LicenseUtils.*;

@Path("/v5.0/admin/licenses")
public class LicensesResource {

    @Inject
    LabelsManager labelsManager;

    @Inject
    RightsResolver rightsResolver;

    @Inject
    Provider<User> userProvider;


    @GET
    @Produces({MediaType.APPLICATION_JSON+";charset=utf-8"})
    public Response licenses() {
        if (permit(this.userProvider.get())) {
            try {
                List<Label> labels = this.labelsManager.getLabels();
                labels.sort(new Comparator<Label>() {
                    @Override
                    public int compare(Label o1, Label o2) {
                        return Integer.valueOf(o1.getPriority()).compareTo(Integer.valueOf(o2.getPriority()));
                    }
                });
                JSONArray jsonArray = new JSONArray();
                labels.stream().forEach(l-> {
                    JSONObject labelObject = new JSONObject();
                    labelObject.put("id", l.getId());
                    labelObject.put("name", l.getName());
                    labelObject.put("description", l.getDescription());
                    labelObject.put("priority", l.getPriority());
                    jsonArray.put(labelObject);
                });
                return Response.ok().entity(jsonArray.toString()).build();
            } catch (JSONException | LabelsManagerException e) {
                throw new GenericApplicationException(e.getMessage(), e);
            }
        } else throw new ActionNotAllowed("action is not allowed");
    }


    @POST
    @Produces({MediaType.APPLICATION_JSON+";charset=utf-8"})
    @Consumes({MediaType.APPLICATION_JSON+";charset=utf-8"})
    public Response insert(JSONObject json) {
        if (permit(this.userProvider.get())) {
            try {
                Label l = licenseFromJSON(json);
                if (l != null) {
                    this.labelsManager.addLocalLabel(l);
                }
                Optional<Label> any = this.labelsManager.getLabels().stream().filter(f -> {
                    return f.getName().equals(l.getName());
                }).findAny();

                if (any.get() != null) {
                    return Response.ok().entity(LicenseUtils.licenseToJSON(any.get()).toString()).build();
                } else {
                    return Response.ok().entity(new JSONObject().toString()).build();
                }
            } catch (JSONException | LabelsManagerException e) {
                throw new GenericApplicationException(e.getMessage(), e);
            }
        } else throw new ActionNotAllowed("action is not allowed");
    }



    @PUT
    @Produces({MediaType.APPLICATION_JSON+";charset=utf-8"})
    @Consumes({MediaType.APPLICATION_JSON+";charset=utf-8"})
    public Response update(JSONObject jsonObject) {
        if (permit(this.userProvider.get())) {
            try {
                Label l = LicenseUtils.licenseFromJSON(jsonObject);
                if (l != null) {
                    try {
                        this.labelsManager.updateLabel(l);
                        Label labelById = this.labelsManager.getLabelById(l.getId());
                        if (labelById != null) {
                            return Response.ok().entity(licenseToJSON(l).toString()).build();
                        } else {
                            return Response.ok().entity(new JSONObject().toString()).build();
                        }
                    } catch (JSONException | LabelsManagerException e) {
                        throw new GenericApplicationException(e.getMessage(), e);
                    }
                } else {
                    throw new ObjectNotFound("cannot find right for '"+jsonObject+"'");
                }
            } catch (JSONException e1) {
                throw new GenericApplicationException(e1.getMessage(), e1);
            }
        } else throw new ActionNotAllowed("action is not allowed");
    }


    @DELETE
    @Path("params/{id:[0-9]+}")
    @Produces({MediaType.APPLICATION_JSON+";charset=utf-8"})
    public Response deleteParam(@PathParam("id")String id) {
        if (permit(this.userProvider.get())) {
            try {
                int id2 = Integer.parseInt(id);
                Label labelById = this.labelsManager.getLabelById(id2);
                if (labelById != null) {
                    labelsManager.removeLocalLabel(labelById);
                    return Response.ok().entity(new JSONObject().toString()).build();
                } else {
                    throw new ObjectNotFound("cannot find label '"+id+"'");
                }
            } catch (NumberFormatException |  LabelsManagerException e) {
                throw new GenericApplicationException(e.getMessage());
            }
        } else throw new ActionNotAllowed("action is not allowed");
    }

    boolean permit(User user) {
        if (user != null)
            return  this.rightsResolver.isActionAllowed(user, SecuredActions.ADMINISTRATE.getFormalName(), SpecialObjects.REPOSITORY.getPid(), null , ObjectPidsPath.REPOSITORY_PATH).flag();
        else
            return false;
    }


}
