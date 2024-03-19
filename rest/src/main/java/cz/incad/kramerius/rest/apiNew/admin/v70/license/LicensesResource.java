package cz.incad.kramerius.rest.apiNew.admin.v70.license;

import com.google.inject.Inject;
import com.google.inject.Provider;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.rest.api.exceptions.ActionNotAllowed;
import cz.incad.kramerius.rest.api.exceptions.BadRequestException;
import cz.incad.kramerius.rest.api.exceptions.CreateException;
import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.rest.api.replication.exceptions.ObjectNotFound;
import cz.incad.kramerius.security.*;
import cz.incad.kramerius.security.licenses.License;
import cz.incad.kramerius.security.licenses.LicensesManager;
import cz.incad.kramerius.security.licenses.LicensesManagerException;
import cz.incad.kramerius.security.licenses.utils.LicenseTOJSONSupport;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static cz.incad.kramerius.security.licenses.utils.LicenseTOJSONSupport.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/admin/v7.0/licenses")
public class LicensesResource {
    
    public static final Logger LOGGER = Logger.getLogger(LicensesResource.class.getName()); 
    

    @Inject
    LicensesManager licensesManager;

    @Inject
    RightsResolver rightsResolver;

    @Inject
    Provider<User> userProvider;

    /** All licenses */
    @GET
    @Produces({MediaType.APPLICATION_JSON+";charset=utf-8"})
    public Response allLicenses() {
        try {
            List<License> localLicenses = this.licensesManager.getAllLicenses();
            return labelsAsResponse(localLicenses);
        } catch (JSONException | LicensesManagerException e) {
            throw new GenericApplicationException(e.getMessage(), e);
        }
    }

    /** Global licenses */
    @GET
    @Path("global")
    @Produces({MediaType.APPLICATION_JSON+";charset=utf-8"})
    public Response globalLicenses() {
        try {
            List<License> localLicenses = this.licensesManager.getGlobalLicenses();
            return labelsAsResponse(localLicenses);
        } catch (JSONException | LicensesManagerException e) {
            throw new GenericApplicationException(e.getMessage(), e);
        }
    }
    
    /** Local licenses */
    @GET
    @Path("local")
    @Produces({MediaType.APPLICATION_JSON+";charset=utf-8"})
    public Response LocalLicenses() {
        try {
            List<License> localLicenses = this.licensesManager.getLocalLicenses();
            return labelsAsResponse(localLicenses);
        } catch (JSONException | LicensesManagerException e) {
            throw new GenericApplicationException(e.getMessage(), e);
        }
    }
    
    

    @GET
    @Path("local/{id:[0-9]+}")
    @Produces({MediaType.APPLICATION_JSON+";charset=utf-8"})
    public Response oneLicense(@PathParam("id")String id ) {
        try {
            License licenseById = this.licensesManager.getLicenseById(Integer.parseInt(id));
            if (licenseById != null) {
                return Response.ok().entity(LicenseTOJSONSupport.licenseToJSON(licenseById).toString()).build();
            } else {
                throw new ObjectNotFound(String.format("cannot find licenses %s", id));
            }
        } catch (JSONException | LicensesManagerException e) {
            throw new GenericApplicationException(e.getMessage(), e);
        }
    }


    @POST
    @Path("local")
    @Produces({MediaType.APPLICATION_JSON+";charset=utf-8"})
    @Consumes({MediaType.APPLICATION_JSON+";charset=utf-8"})
    public Response insert(JSONObject json) {
        if (permit(this.userProvider.get())) {
            try {
                if (json.has("name")) {
                    License l = licenseFromJSON(json);
                    if (l != null) {
                        if (this.licensesManager.getLicenseByName(l.getName()) == null) {
                            this.licensesManager.addLocalLicense(l);
                            Optional<License> any = this.licensesManager.getLicenses().stream().filter(f -> {
                                return f.getName().equals(l.getName());
                            }).findAny();
                            if (any.get() != null) {
                                return Response.ok().entity(LicenseTOJSONSupport.licenseToJSON(any.get()).toString()).build();
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
    @Path("local/{id:[0-9]+}")
    @Produces({MediaType.APPLICATION_JSON+";charset=utf-8"})
    @Consumes({MediaType.APPLICATION_JSON+";charset=utf-8"})
    public Response update(@PathParam("id")String id, JSONObject jsonObject) {
        if (permit(this.userProvider.get())) {
            try {
                int ident = Integer.parseInt(id);
                if (this.licensesManager.getLicenseById(ident) != null) {
                    License l = LicenseTOJSONSupport.licenseFromJSON(Integer.parseInt(id), jsonObject);
                    if (l != null ) {
                        try {
                            License licenseByName = this.licensesManager.getLicenseByName(l.getName());
                            if (licenseByName != null && l.getId() != licenseByName.getId()) {
                                throw new CreateException(String.format("Licence %s already exists", l.getName()));
                            } else {
                                this.licensesManager.updateLocalLicense(l);
                                License licenseById = this.licensesManager.getLicenseById(l.getId());
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
    @Path("changeOrdering")
    @Produces({MediaType.APPLICATION_JSON+";charset=utf-8"})
    @Consumes({MediaType.APPLICATION_JSON+";charset=utf-8"})
    public Response changeOrdering(JSONObject jsonObject) {
        if (permit(this.userProvider.get())) {
            List<License> lics = new ArrayList<>();
            JSONArray jsonLicenses = jsonObject.getJSONArray("licenses");
            for (int i = 0; i < jsonLicenses.length(); i++) {
                JSONObject jsonLic = jsonLicenses.getJSONObject(i);
                lics.add(LicenseTOJSONSupport.licenseFromJSON(jsonLic));
            }
            try {
                this.licensesManager.changeOrdering(lics);

                List<License> localLicenses = this.licensesManager.getAllLicenses();
                return labelsAsResponse(localLicenses);
            } catch (LicensesManagerException e) {
                throw new GenericApplicationException(e.getMessage(), e);
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
                License license = this.licensesManager.getLicenseById(Integer.parseInt(id));
                if (license != null) {
                    int priority = license.getPriority();
                    if (priority >= 2) {
                        this.licensesManager.moveUp(license);
                        return labelsAsResponse(this.licensesManager.getLocalLicenses());
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
                License license = this.licensesManager.getLicenseById(Integer.parseInt(id));
                if (license != null) {
                    int priority = license.getPriority();
                    // TODO: synchronizzation
                    int minPriority = this.licensesManager.getMinPriority();

                    if (priority < minPriority) {
                        this.licensesManager.moveDown(license);
                        return labelsAsResponse(this.licensesManager.getLocalLicenses());
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


    private Response labelsAsResponse(List<License> licenses) throws LicensesManagerException {
        //List<License> licenses = this.licensesManager.getLicenses();
        licenses.sort(new Comparator<License>() {
            @Override
            public int compare(License o1, License o2) {
                return Integer.valueOf(o1.getPriority()).compareTo(Integer.valueOf(o2.getPriority()));
            }
        });

        JSONArray jsonArray = new JSONArray();
        licenses.stream().map(LicenseTOJSONSupport::licenseToJSON)
                .forEach(jsonArray::put);
        return Response.ok().entity(jsonArray.toString()).build();
    }


    @DELETE
    @Path("local/{id:[0-9]+}")
    @Produces({MediaType.APPLICATION_JSON+";charset=utf-8"})
    public Response delete(@PathParam("id")String id) {
        if (permit(this.userProvider.get())) try {
            int id2 = Integer.parseInt(id);
            License licenseById = this.licensesManager.getLicenseById(id2);
            if (licenseById != null) {
                licensesManager.removeLocalLicense(licenseById);
                License found = licensesManager.getLicenseById(id2);
                if (found == null) {
                    return Response.status(Response.Status.NO_CONTENT).entity(new JSONObject().toString()).build();
                } else {
                    throw new GenericApplicationException(String.format("Cannot delete label %s", licenseById.getName()));
                }
            } else {
                throw new ObjectNotFound("cannot find label '" + id + "'");
            }
        } catch (NumberFormatException | LicensesManagerException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
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
