package cz.incad.kramerius.rest.apiNew.admin.v70.license;

import com.google.inject.Inject;
import com.google.inject.Provider;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.rest.api.exceptions.ActionNotAllowed;
import cz.incad.kramerius.rest.api.exceptions.BadRequestException;
import cz.incad.kramerius.rest.api.exceptions.CreateException;
import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.rest.api.k5.admin.utils.LicenseUtils;
import cz.incad.kramerius.rest.api.replication.exceptions.ObjectNotFound;
import cz.incad.kramerius.security.*;
import cz.incad.kramerius.security.licenses.License;
import cz.incad.kramerius.security.licenses.LicensesManager;
import cz.incad.kramerius.security.licenses.LicensesManagerException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static cz.incad.kramerius.rest.api.k5.admin.utils.LicenseUtils.*;

@Path("/admin/v7.0/licenses")
public class LicensesResource {

    @Inject
    LicensesManager licensesManager;

    @Inject
    RightsResolver rightsResolver;

    @Inject
    Provider<User> userProvider;


    @GET
    @Produces({MediaType.APPLICATION_JSON+";charset=utf-8"})
    public Response licenses() {
        if (permit(this.userProvider.get())) {
            try {
                return labelsAsResponse();
            } catch (JSONException | LicensesManagerException e) {
                throw new GenericApplicationException(e.getMessage(), e);
            }
        } else throw new ActionNotAllowed("action is not allowed");
    }

    @GET
    @Path("{id:[0-9]+}")
    @Produces({MediaType.APPLICATION_JSON+";charset=utf-8"})
    public Response oneLicense(@PathParam("id")String id ) {
        if (permit(this.userProvider.get())) {
            try {
                License licenseById = this.licensesManager.getLabelById(Integer.parseInt(id));
                if (licenseById != null) {
                    return Response.ok().entity(LicenseUtils.licenseToJSON(licenseById).toString()).build();
                } else {
                    throw new ObjectNotFound(String.format("cannot find licenses %s", id));
                }
            } catch (JSONException | LicensesManagerException e) {
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
                if (json.has("name")) {
                    License l = licenseFromJSON(json);
                    if (l != null) {
                        if (this.licensesManager.getLabelByName(l.getName()) == null) {
                            this.licensesManager.addLocalLabel(l);
                            Optional<License> any = this.licensesManager.getLabels().stream().filter(f -> {
                                return f.getName().equals(l.getName());
                            }).findAny();
                            if (any.get() != null) {
                                return Response.ok().entity(LicenseUtils.licenseToJSON(any.get()).toString()).build();
                            } else {
                                return Response.ok().entity(new JSONObject().toString()).build();
                            }
                        } else {
                            throw new BadRequestException(String.format("Licence %s already exists", l.getName()));
                        }
                    } else {
                        throw new BadRequestException("Must contain license as body");
                    }
                } else {
                    throw new BadRequestException("Cannot find name of license");
                }
            } catch (JSONException | LicensesManagerException e) {
                throw new GenericApplicationException(e.getMessage(), e);
            }
        } else throw new ActionNotAllowed("action is not allowed");
    }



    @PUT
    @Path("{id:[0-9]+}")
    @Produces({MediaType.APPLICATION_JSON+";charset=utf-8"})
    @Consumes({MediaType.APPLICATION_JSON+";charset=utf-8"})
    public Response update(@PathParam("id")String id, JSONObject jsonObject) {
        if (permit(this.userProvider.get())) {
            try {
                int ident = Integer.parseInt(id);
                if (this.licensesManager.getLabelById(ident) != null) {
                    License l = LicenseUtils.licenseFromJSON(Integer.parseInt(id), jsonObject);
                    if (l != null ) {
                        try {
                            License licenseByName = this.licensesManager.getLabelByName(l.getName());
                            if (licenseByName != null && l.getId() != licenseByName.getId()) {
                                throw new CreateException(String.format("Licence %s already exists", l.getName()));
                            } else {
                                this.licensesManager.updateLabel(l);
                                License licenseById = this.licensesManager.getLabelById(l.getId());
                                if (licenseById != null) {
                                    return Response.ok().entity(licenseToJSON(licenseById).toString()).build();
                                } else {
                                    return Response.ok().entity(new JSONObject().toString()).build();
                                }
                            }

                        } catch (JSONException | LicensesManagerException e) {
                            throw new GenericApplicationException(e.getMessage(), e);
                        }
                    } else {
                        throw new BadRequestException("invalid payload '"+jsonObject+"'");
                    }
                } else {
                    throw new ObjectNotFound("cannot find license  for '"+jsonObject+"'");
                }

            } catch (JSONException  | LicensesManagerException e1) {
                throw new GenericApplicationException(e1.getMessage(), e1);
            }
        } else throw new ActionNotAllowed("action is not allowed");
    }

    @PUT
    @Path("moveup/{id:[0-9]+}")
    @Produces({MediaType.APPLICATION_JSON+";charset=utf-8"})
    @Consumes({MediaType.APPLICATION_JSON+";charset=utf-8"})
    public Response moveUp(@PathParam("id")String id) {
        if (permit(this.userProvider.get())) {
            try {
                License license = this.licensesManager.getLabelById(Integer.parseInt(id));
                if (license != null) {
                    int priority = license.getPriority();
                    if (priority >= 2) {
                        this.licensesManager.moveUp(license);
                        return labelsAsResponse();
                    } else {
                        throw new BadRequestException(String.format("cannot change priority for label %s", license.toString()));
                    }
                } else {
                    throw  new ObjectNotFound(String.format("cannot find license %d", id));
                }
            } catch (JSONException | LicensesManagerException e1) {
                throw new GenericApplicationException(e1.getMessage(), e1);
            }
        } else throw new ActionNotAllowed("action is not allowed");
    }

    @PUT
    @Path("movedown/{id:[0-9]+}")
    @Produces({MediaType.APPLICATION_JSON+";charset=utf-8"})
    @Consumes({MediaType.APPLICATION_JSON+";charset=utf-8"})
    public Response moveDown(@PathParam("id")String id) {
        if (permit(this.userProvider.get())) {
            try {
                License license = this.licensesManager.getLabelById(Integer.parseInt(id));
                if (license != null) {
                    int priority = license.getPriority();
                    // TODO: synchronizzation
                    int minPriority = this.licensesManager.getMinPriority();

                    if (priority < minPriority) {
                        this.licensesManager.moveDown(license);
                        return labelsAsResponse();
                    } else {
                        throw new BadRequestException(String.format("cannot change priority for label %s", license.toString()));
                    }
                } else {
                    throw  new ObjectNotFound(String.format("cannot find license %d", id));
                }
            } catch (JSONException | LicensesManagerException e1) {
                throw new GenericApplicationException(e1.getMessage(), e1);
            }
        } else throw new ActionNotAllowed("action is not allowed");
    }


    private Response labelsAsResponse() throws LicensesManagerException {
        List<License> licenses = this.licensesManager.getLabels();
        licenses.sort(new Comparator<License>() {
            @Override
            public int compare(License o1, License o2) {
                return Integer.valueOf(o1.getPriority()).compareTo(Integer.valueOf(o2.getPriority()));
            }
        });

        JSONArray jsonArray = new JSONArray();
        licenses.stream().map(LicenseUtils::licenseToJSON)
                .forEach(jsonArray::put);
        return Response.ok().entity(jsonArray.toString()).build();
    }


    @DELETE
    @Path("{id:[0-9]+}")
    @Produces({MediaType.APPLICATION_JSON+";charset=utf-8"})
    public Response delete(@PathParam("id")String id) {
        if (permit(this.userProvider.get())) try {
            int id2 = Integer.parseInt(id);
            License licenseById = this.licensesManager.getLabelById(id2);
            if (licenseById != null) {
                licensesManager.removeLocalLabel(licenseById);
                License found = licensesManager.getLabelById(id2);
                if (found == null) {
                    return Response.status(Response.Status.NO_CONTENT).entity(new JSONObject().toString()).build();
                } else {
                    throw new GenericApplicationException(String.format("Cannot delete label %s", licenseById.getName()));
                }
            } else {
                throw new ObjectNotFound("cannot find label '" + id + "'");
            }
        } catch (NumberFormatException | LicensesManagerException e) {
            throw new GenericApplicationException(e.getMessage());
        }
        else throw new ActionNotAllowed("action is not allowed");
    }

    boolean permit(User user) {
        if (user != null)
            return  this.rightsResolver.isActionAllowed(user, SecuredActions.A_RIGHTS_EDIT.getFormalName(), SpecialObjects.REPOSITORY.getPid(), null , ObjectPidsPath.REPOSITORY_PATH).flag();
        else
            return false;
    }


}
