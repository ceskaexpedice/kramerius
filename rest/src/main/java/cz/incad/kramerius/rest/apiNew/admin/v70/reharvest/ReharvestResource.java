package cz.incad.kramerius.rest.apiNew.admin.v70.reharvest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;


import java.time.Instant;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.base.Functions;
import com.google.common.collect.Lists;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.OneInstance;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.DeleteTriggerSupport;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.item.ProxyItemHandler;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.utils.IntrospectUtils;
import cz.incad.kramerius.utils.IPAddressUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import com.google.inject.Provider;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.security.RightsResolver;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.inject.Inject;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.apiNew.exceptions.BadRequestException;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.XMLUtils;

import static cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.ReharvestItem.*;

@Path("/admin/v7.0/reharvest")
public class ReharvestResource {

    public Logger LOGGER = Logger.getLogger(ReharvestResource.class.getName());

    @Inject
    private ReharvestManager reharvestManager;

    @Inject
    @Named("new-index")
    private SolrAccess solrAccess;

    @Inject
    DeleteTriggerSupport deleteTriggerSupport;

    @javax.inject.Inject
    @Named("forward-client")
    private CloseableHttpClient apacheClient;

    @Inject
    private Instances libraries;

    @Inject
    private RightsResolver rightsResolver;

    @Inject
    private Provider<User> userProvider;


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHarvests(@QueryParam("page") String page, @QueryParam("rows") String rows, @QueryParam("filters") String filterString) {
        List<String> filters = new ArrayList<>();
        if (StringUtils.isAnyString(filterString)) {
            Arrays.asList(filterString.split(";")).stream().forEach(filter -> {
                filters.add(filter);
            });
        }


        int iPage = StringUtils.isAnyString(page) ? Integer.parseInt(page) : 0;
        int iRows = StringUtils.isAnyString(rows) ? Integer.parseInt(rows) : 20;
        String str = this.reharvestManager.searchItems(iPage * iRows, iRows, filters);

        return Response.ok(str).build();
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHarvest(@PathParam("id") String id) {
        if (permit()) {
            ReharvestItem item = this.reharvestManager.getItemById(id);
            if (item != null) {
                return Response.ok(item.toJSON().toString()).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } else {
            return Response.ok(Response.Status.FORBIDDEN).build();
        }
    }

    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("id") String id) {
        if (permit()) {
            ReharvestItem item = this.reharvestManager.getItemById(id);
            if (item != null) {
                this.reharvestManager.deregister(id);
                return Response.ok(item.toJSON().toString()).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } else {
            return Response.ok(Response.Status.FORBIDDEN).build();
        }

    }

    @GET
    @Path("resolveconflicts/{pids}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response resolveConflict(@PathParam("pids") String pids) {

        String[] conflictIdPidsArray = pids.split(",");
        Arrays.sort(conflictIdPidsArray);
        String joined = String.join(",", conflictIdPidsArray).trim();
        UUID conflictUUID = UUID.nameUUIDFromBytes(joined.getBytes(StandardCharsets.UTF_8));
        String conflictId = conflictUUID.toString();

        List<ReharvestItem> alreadySolvingConflict = this.reharvestManager.getItemByConflictId(conflictId);
        if (alreadySolvingConflict.isEmpty() || alreadySolvingConflict.stream().noneMatch(item -> "waiting".equalsIgnoreCase(item.getState()))) {
            String[] pidArray = pids.split(",");
            for (String pid : pidArray) {

                ReharvestItem deleteRoot = new ReharvestItem(UUID.randomUUID().toString());
                deleteRoot.setTypeOfReharvest(ReharvestItem.TypeOfReharvset.delete_root);
                deleteRoot.setName("Conflict - reharvest/delete invalid root");
                deleteRoot.setRootPid(pid);
                deleteRoot.setPid(pid);
                deleteRoot.setState("waiting_for_approve");
                deleteRoot.setConflictId(conflictId);
                try {
                    this.reharvestManager.register(deleteRoot);
                } catch (AlreadyRegistedPidsException e) {
                    throw new BadRequestException();
                }
            }

            List<ReharvestItem> rootItems = new ArrayList<>();
            List<ReharvestItem> childItems = new ArrayList<>();

            List<String> topLevelModels = Lists.transform(KConfiguration.getInstance().getConfiguration().getList("fedora.topLevelModels"), Functions.toStringFunction());
            Arrays.stream(pidArray).forEach(pid -> {
                try {
                    Set<String> models = new HashSet<>();
                    Set<String> rootPids = new HashSet<>();
                    Set<String> pidPaths = new HashSet<>();
                    Set<String> libs = new HashSet<>();
                    JSONObject introspectResult = IntrospectUtils.introspectSolr(this.apacheClient, this.libraries, pid);
                    for (Object keyo : introspectResult.keySet()) {
                        String key = keyo.toString();

                        JSONObject response = introspectResult.getJSONObject(key).optJSONObject("response");
                        int numFound = response != null ? response.getInt("numFound") : 0;
                        if (numFound > 0) {

                            libs.add(key);

                            JSONArray docs = response.getJSONArray("docs");
                            JSONObject doc = docs.getJSONObject(0);
                            String model = doc.optString("model");
                            if (StringUtils.isAnyString(model)) {
                                models.add(model);
                            }
                            String rootPid = doc.optString("root.pid");
                            if (StringUtils.isAnyString(rootPid)) {
                                rootPids.add(rootPid);
                            }

                            JSONArray oPidPaths = doc.optJSONArray("pid_paths");
                            if (oPidPaths != null && oPidPaths.length() > 0) {
                                pidPaths.add(oPidPaths.getString(0));
                            }
                        }
                    }

                    if (models.size() == 1) {
                        // ok
                        String model = models.iterator().next();

                        ReharvestItem rItem = new ReharvestItem(UUID.randomUUID().toString());
                        String rPid = rootPids.iterator().next();
                        rItem.setRootPid(rPid);
                        rItem.setLibraries(new ArrayList<>(libs));
                        rItem.setTypeOfReharvest(topLevelModels.contains(model) ? TypeOfReharvset.root : TypeOfReharvset.children);
                        rItem.setName(topLevelModels.contains(model) ? "Conflict - reharvest/root" : "Conflict - reharvest/children");
                        rItem.setConflictId(conflictId);
                        rItem.setState("waiting_for_approve");
                        rItem.setPid(rPid);
                        rItem.setOwnPidPath(pidPaths.iterator().next());

                        if (topLevelModels.contains(model)) {
                            rootItems.add(rItem);
                        } else {
                            childItems.add(rItem);
                        }

                        // root items first
                        rootItems.forEach(item -> {
                            try {
                                this.reharvestManager.register(item, false);
                            } catch (AlreadyRegistedPidsException e) {
                                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                            }
                        });

                        // child items second
                        childItems.forEach(item -> {
                            try {
                                this.reharvestManager.register(item, false);
                            } catch (AlreadyRegistedPidsException e) {
                                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                            }
                        });
                    } else {
                        // live conflict
                        throw new BadRequestException("Live conflict");
                    }

                } catch (UnsupportedEncodingException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            });
            return Response.ok().build();
        } else {
            // bad request ??
            throw new BadRequestException("Already registered");
        }
    }


    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response register(String json, @QueryParam("registerIfAlreadyExists") String registerIfAlreadyExists) {
        if (permit()) {
            try {
                boolean registrationFlag = false;
                if (StringUtils.isAnyString(registerIfAlreadyExists)) {
                    registrationFlag = Boolean.parseBoolean(registerIfAlreadyExists);
                }

                JSONObject jsonObj = new JSONObject(json);
                if (!jsonObj.has(ID_KEYWORD)) {
                    jsonObj.put(ID_KEYWORD, UUID.randomUUID().toString());
                }
                if (!jsonObj.has(PID_KEYWORD)) {
                    throw new BadRequestException(" Request must contain pid ");
                }


                if (!jsonObj.has(OWN_PID_PATH) || !jsonObj.has(ROOT_PID)) {
                    Document solrDataByPid = this.solrAccess.getSolrDataByPid(jsonObj.getString(PID_KEYWORD));
                    Element rootPid = XMLUtils.findElement(solrDataByPid.getDocumentElement(), new XMLUtils.ElementsFilter() {
                        @Override
                        public boolean acceptElement(Element element) {
                            if (element.getNodeName().equals("str")) {
                                String fieldName = element.getAttribute("name");
                                return fieldName.equals("root.pid");
                            }
                            return false;
                        }
                    });
                    Element ownPidPath = XMLUtils.findElement(solrDataByPid.getDocumentElement(), new XMLUtils.ElementsFilter() {
                        @Override
                        public boolean acceptElement(Element element) {
                            if (element.getNodeName().equals("str")) {
                                String fieldName = element.getAttribute("name");
                                return fieldName.equals("own_pid_path");
                            }
                            return false;
                        }
                    });

                    Element cdkCollection = XMLUtils.findElement(solrDataByPid.getDocumentElement(), new XMLUtils.ElementsFilter() {
                        @Override
                        public boolean acceptElement(Element element) {
                            if (element.getNodeName().equals("arr")) {
                                String fieldName = element.getAttribute("name");
                                return fieldName.equals("cdk.collection");
                            }
                            return false;
                        }
                    });


                    if (rootPid != null && !jsonObj.has(ROOT_PID)) {
                        jsonObj.put(ROOT_PID, rootPid.getTextContent());
                    }
                    if (ownPidPath != null && !jsonObj.has(OWN_PID_PATH)) {
                        jsonObj.put(OWN_PID_PATH, ownPidPath.getTextContent());
                    }

                    if (cdkCollection != null && !jsonObj.has(LIBRARIES_KEYWORD)) {
                        List<String> collections = XMLUtils.getElements(cdkCollection).stream().map(Element::getTextContent).collect(Collectors.toList());
                        JSONArray jsonArr = new JSONArray();
                        collections.forEach(jsonArr::put);
                        jsonObj.put(LIBRARIES_KEYWORD, jsonArr);
                    }
                }

                // nasel to v indexu (pro polozky, ktere nejsou v indexu je potreba novy typ harvestu
                ReharvestItem item = ReharvestItem.fromJSON(jsonObj);
                ReharvestItem alreadyRegistredItem = this.reharvestManager.getOpenItemByPid(item.getPid());
                if (alreadyRegistredItem != null) {
                    return Response.status(Response.Status.CONFLICT).build();
                } else {
                    switch (item.getTypeOfReharvest()) {

                        case delete_pid:
                        case only_pid:
                            if (item != null && StringUtils.isAnyString(item.getPid())) {
                                item.setTimestamp(Instant.now());
                                this.reharvestManager.register(item, registrationFlag);
                                return Response.ok(item.toJSON().toString()).build();
                            } else {
                                JSONObject errorObject = new JSONObject();
                                errorObject.put("error", "No pid");
                                return Response.status(Response.Status.BAD_REQUEST).entity(errorObject.toString()).type(MediaType.APPLICATION_JSON_TYPE).build();
                            }

                        case delete_tree:
                        case children:
                            if (item != null) {
                                item.setTimestamp(Instant.now());
                                this.reharvestManager.register(item, registrationFlag);
                                return Response.ok(item.toJSON().toString()).build();
                            } else {
                                JSONObject errorObject = new JSONObject();
                                errorObject.put("error", "No root pid or own_pid_path");
                                return Response.status(Response.Status.BAD_REQUEST).entity(errorObject.toString()).type(MediaType.APPLICATION_JSON_TYPE).build();
                            }


                        case delete_root:
                        case root:
                        case new_root:
                            if (item != null && StringUtils.isAnyString(item.getRootPid())) {
                                item.setTimestamp(Instant.now());
                                this.reharvestManager.register(item, registrationFlag);
                                return Response.ok(item.toJSON().toString()).build();
                            } else {
                                JSONObject errorObject = new JSONObject();
                                errorObject.put("error", "No root pid");
                                return Response.status(Response.Status.BAD_REQUEST).entity(errorObject.toString()).type(MediaType.APPLICATION_JSON_TYPE).build();
                            }

                        default:
                            throw new IllegalStateException(String.format("Uknown type of reharvest %s", item.getTypeOfReharvest()));
                    }
                }
            } catch (JSONException | ParseException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            } catch (AlreadyRegistedPidsException e) {
                throw new BadRequestException(" Request contains already registered pids " + e.getPids());
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } else {
            return Response.ok(Response.Status.FORBIDDEN).build();
        }
    }


    @GET
    @Path("top")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTopHarvest(@QueryParam("state") String state) {
        if (permit()) {
            ReharvestItem topItem = this.reharvestManager.getTopItem(state);
            if (topItem != null) {
                return Response.ok(topItem.toJSON().toString()).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } else {
            return Response.ok(Response.Status.FORBIDDEN).build();
        }
    }


    @GET
    @Path("open")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOpenHarvest(@QueryParam("pid") String pid) {
        if (permit()) {
            LOGGER.info("Returning open harvest");
            if (StringUtils.isAnyString(pid)) {
                ReharvestItem topItem = this.reharvestManager.getOpenItemByPid(pid);
                if (topItem != null) {
                    return Response.ok(topItem.toJSON().toString()).build();
                } else {
                    LOGGER.info("Open harvest not found");
                    return Response.status(Response.Status.NOT_FOUND).build();
                }
            } else {
                LOGGER.info("Open harvest Bad request");
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        } else {
            return Response.ok(Response.Status.FORBIDDEN).build();
        }
    }

    @PUT
    @Path("{id}/state")
    @Produces(MediaType.APPLICATION_JSON)
    public Response changeState(@PathParam("id") String id, @QueryParam("state") String state) {
        if (permit()) {
            try {
                ReharvestItem itemById = reharvestManager.getItemById(id);
                if (itemById != null) {
                    itemById.setState(state);
                    this.reharvestManager.update(itemById);
                    return Response.ok(itemById.toJSON().toString()).build();
                } else {
                    return Response.status(Response.Status.NOT_FOUND).build();
                }
            } catch (UnsupportedEncodingException | JSONException | ParseException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } else {
            return Response.ok(Response.Status.FORBIDDEN).build();
        }
    }

    @PUT
    @Path("{id}/pod")
    @Produces(MediaType.APPLICATION_JSON)
    public Response changePod(@PathParam("id") String id, @QueryParam("pod") String pod) {
        if (permit()) {
            try {
                ReharvestItem itemById = reharvestManager.getItemById(id);
                if (itemById != null) {
                    itemById.setPodname(pod);
                    this.reharvestManager.update(itemById);
                    return Response.ok(itemById.toJSON().toString()).build();
                } else {
                    return Response.status(Response.Status.NOT_FOUND).build();
                }
            } catch (UnsupportedEncodingException | JSONException | ParseException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }

        } else {
            return Response.ok(Response.Status.FORBIDDEN).build();
        }
    }

    boolean permit() {
        User user = this.userProvider.get();
        if (user != null)
            return this.rightsResolver.isActionAllowed(user, SecuredActions.A_ADMIN_READ.getFormalName(), SpecialObjects.REPOSITORY.getPid(), null, ObjectPidsPath.REPOSITORY_PATH).flag();
        else return false;
    }


    @GET
    @Path("deleteTrigger")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteTrigger(@QueryParam("pid") String pid) {
        deleteTriggerSupport.executeDeleteTrigger(pid);
        JSONObject retval = new JSONObject();
        retval.put("message", "planned");
        return Response.ok(retval.toString()).build();
    }

}
