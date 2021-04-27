package cz.incad.kramerius.rest.apiNew.admin.v10.collections;

import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.repository.KrameriusRepositoryApi;
import cz.incad.kramerius.repository.RepositoryApi;
import cz.incad.kramerius.rest.apiNew.admin.v10.AdminApiResource;
import cz.incad.kramerius.rest.apiNew.admin.v10.AuthenticatedUser;
import cz.incad.kramerius.rest.apiNew.admin.v10.ClientAuthHeaders;
import cz.incad.kramerius.rest.apiNew.admin.v10.ProcessSchedulingHelper;
import cz.incad.kramerius.rest.apiNew.exceptions.BadRequestException;
import cz.incad.kramerius.rest.apiNew.exceptions.ForbiddenException;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.utils.Dom4jUtils;
import cz.incad.kramerius.utils.java.Pair;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.dom4j.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/admin/v1.0/collections")
public class CollectionsResource extends AdminApiResource {

    public static final Logger LOGGER = Logger.getLogger(CollectionsResource.class.getName());

    //TODO: prejmenovat role podle spravy uctu
    private static final String ROLE_CREATE_COLLECTION = "kramerius_admin";
    private static final String ROLE_LIST_COLLECTIONS = "kramerius_admin";
    private static final String ROLE_READ_COLLECTION = "kramerius_admin";
    private static final String ROLE_EDIT_COLLECTION = "kramerius_admin";
    private static final String ROLE_DELETE_COLLECTION = "kramerius_admin";

    @Inject
    private FoxmlBuilder foxmlBuilder;

    @Inject
    ProcessSchedulingHelper processSchedulingHelper;

    /**
     * Creates new collection and assigns a pid to it.
     *
     * @param collectionDefinition collection object (JSON) with attributes name:string, description:string, content:string, standalone: boolean
     * @return collection object in JSON with pid assign
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createCollection(JSONObject collectionDefinition) {
        try {
            //authentication
            AuthenticatedUser user = getAuthenticatedUserByOauth();
            String role = ROLE_CREATE_COLLECTION;
            if (!user.getRoles().contains(role)) {
                throw new ForbiddenException("user '%s' is not allowed to create collections (missing role '%s')", user.getName(), role); //403
            }
            Collection collection = extractCollectionFromJson(collectionDefinition);
            if ((collection.nameCz == null || collection.nameCz.isEmpty()) && (collection.nameEn == null || collection.nameEn.isEmpty())) {
                throw new BadRequestException("name can't be empty");
            }
            collection.pid = "uuid:" + UUID.randomUUID().toString();
            Document foxml = foxmlBuilder.buildFoxml(collection, null);
            krameriusRepositoryApi.getLowLevelApi().ingestObject(foxml);
            //schedule reindexation - new collection (only object)
            scheduleReindexation(collection.pid, user, "OBJECT", UUID.randomUUID().toString(), false, "sbírka " + collection.pid);
            return Response.status(Response.Status.CREATED).entity(collection.toJson().toString()).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    /**
     * Returs a collection identified by pid.
     *
     * @param pid
     * @return
     */
    @GET
    @Path("{pid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCollection(@PathParam("pid") String pid) {
        try {
            checkSupportedObjectPid(pid);
            //authentication
            AuthenticatedUser user = getAuthenticatedUserByOauth();
            String role = ROLE_READ_COLLECTION;
            if (!user.getRoles().contains(role)) {
                throw new ForbiddenException("user '%s' is not allowed to read collections (missing role '%s')", user.getName(), role); //403
            }
            checkObjectExists(pid);
            Collection collection = fetchCollectionFromRepository(pid, true, true);
            return Response.ok(collection.toJson()).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    /**
     * Returns all collections or collections that directly contain given item.
     *
     * @param itemPid pid of an item that all returned collections directly contain
     * @return
     */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCollections(@QueryParam("withItem") String itemPid) {
        try {
            //authentication & authorization by external provider of identities & rights
            AuthenticatedUser user = getAuthenticatedUserByOauth();
            String role = ROLE_LIST_COLLECTIONS;
            if (!user.getRoles().contains(role)) {
                throw new ForbiddenException("user '%s' is not allowed to list collections (missing role '%s')", user.getName(), role); //403
            }
            //authentication with JSESSIONID cookie and authorization against (global) SecuredAction - i.e. schema used Kramerius legacy APIs and servlets
            //checkCurrentUserByJsessionidIsAllowedToPerformGlobalSecuredAction(SecuredActions.VIRTUALCOLLECTION_MANAGE);

            List<String> pids = null;
            if (itemPid != null) {
                checkSupportedObjectPid(itemPid);
                checkObjectExists(itemPid);
                pids = krameriusRepositoryApi.getPidsOfCollectionsContainingItem(itemPid);
            } else {
                pids = krameriusRepositoryApi.getLowLevelApi().getPidsOfObjectsByModel("collection");
            }
            JSONArray collections = new JSONArray();
            for (String pid : pids) {
                Collection collection = fetchCollectionFromRepository(pid, false, false);
                collections.put(collection.toJson());
            }
            JSONObject result = new JSONObject();
            result.put("total_size", pids.size());
            result.put("collections", collections);
            return Response.ok(result.toString()).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    /**
     * Updates collections metadata, but not items that collection directly contains.
     *
     * @param pid
     * @param collectionDefinition collection object (JSON) with attributes name:string, description:string, content:string, standalone: boolean
     * @return
     */
    @PUT
    @Path("{pid}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateCollection(@PathParam("pid") String pid, JSONObject collectionDefinition) {
        try {
            checkSupportedObjectPid(pid);
            //authentication
            AuthenticatedUser user = getAuthenticatedUserByOauth();
            String role = ROLE_EDIT_COLLECTION;
            if (!user.getRoles().contains(role)) {
                throw new ForbiddenException("user '%s' is not allowed to edit collections (missing role '%s')", user.getName(), role); //403
            }
            checkObjectExists(pid);
            Collection current = fetchCollectionFromRepository(pid, true, false);

            Collection updated = current.withUpdatedDataModifiableByClient(extractCollectionFromJson(collectionDefinition));
            if ((updated.nameCz == null || updated.nameCz.isEmpty()) && (updated.nameEn == null || updated.nameEn.isEmpty())) {
                throw new BadRequestException("name can't be empty");
            }
            if (!current.equalsInDataModifiableByClient(updated)) {
                //fetch items in collection first (otherwise eventual consistency of processing index would cause no items in new version of rels-ext)
                List<String> itemsInCollection = krameriusRepositoryApi.getPidsOfItemsInCollection(pid);
                //rebuild and update mods
                krameriusRepositoryApi.updateMods(pid, foxmlBuilder.buildMods(updated));
                //rebuild and update rels-ext (because of "standalone")
                krameriusRepositoryApi.updateRelsExt(pid, foxmlBuilder.buildRelsExt(updated, itemsInCollection));
                //schedule reindexation - l (only object)
                scheduleReindexation(pid, user, "OBJECT", UUID.randomUUID().toString(), false, "sbírka " + pid);
            }
            return Response.ok().build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    private void scheduleReindexation(String objectPid, AuthenticatedUser user, String indexationType, String batchToken, boolean ignoreInconsistentObjects, String title) {
        String newProcessAuthToken = UUID.randomUUID().toString();
        List<String> paramsList = new ArrayList<>();
        //Kramerius
        paramsList.addAll(processSchedulingHelper.processParamsKrameriusAdminApiCredentials(extractClientAuthHeaders()));
        //Solr
        paramsList.addAll(processSchedulingHelper.processParamsSolr());
        //indexation params
        paramsList.add(indexationType);
        paramsList.add(objectPid);
        paramsList.add(Boolean.toString(ignoreInconsistentObjects));
        paramsList.add(title);
        processSchedulingHelper.scheduleProcess("new_indexer_index_object", paramsList, user.getId(), user.getName(), batchToken, newProcessAuthToken);
    }

    /**
     * Sets items that the collection directly contains. I.e. removes all existing items and adds all items from method's data.
     *
     * @param pid
     * @param pidsOfItems array of pids to be added to the collection
     * @return
     */
    @PUT
    @Path("{pid}/items")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response setItemsInCollection(@PathParam("pid") String pid, JSONArray pidsOfItems) {
        try {
            checkSupportedObjectPid(pid);
            //TODO: implement
            throw new RuntimeException("not implemented yet");
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    /**
     * Adds single item to collection.
     *
     * @param collectionPid
     * @param itemPid
     * @return
     */
    @POST
    @Path("{pid}/items")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response addItemToCollection(@PathParam("pid") String collectionPid, String itemPid) {
        //TODO: maybe JSONArray insted of single String, to be able to add multiple items at once.
        try {
            checkSupportedObjectPid(collectionPid);
            checkSupportedObjectPid(itemPid);
            //authentication
            AuthenticatedUser user = getAuthenticatedUserByOauth();
            String role = ROLE_EDIT_COLLECTION;
            if (!user.getRoles().contains(role)) {
                throw new ForbiddenException("user '%s' is not allowed to edit collections (missing role '%s')", user.getName(), role); //403
            }
            checkObjectExists(collectionPid);
            checkObjectExists(itemPid);
            Document relsExt = krameriusRepositoryApi.getRelsExt(collectionPid, true);
            foxmlBuilder.appendRelationToRelsExt(collectionPid, relsExt, KrameriusRepositoryApi.KnownRelations.CONTAINS, itemPid);
            krameriusRepositoryApi.updateRelsExt(collectionPid, relsExt);
            //schedule reindexations - 1. newly added item (whole tree and foster trees), 2. no need to re-index collection
            String batchToken = UUID.randomUUID().toString();
            //TODO: namísto TREE_AND_FOSTER_TREES nový typ indexace, co bude řešit jen sbírky
            scheduleReindexation(itemPid, user, "TREE_AND_FOSTER_TREES", batchToken, true, itemPid);
            //scheduleReindexation(collectionPid, user, "OBJECT", batchToken, false, "sbírka " + collectionPid);
            return Response.status(Response.Status.CREATED).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    /**
     * Removes single item from collection.
     *
     * @param collectionPid
     * @param itemPid
     * @return
     */
    @DELETE
    @Path("{collectionPid}/items/{itemPid}")
    public Response removeItemFromCollection(@PathParam("collectionPid") String collectionPid, @PathParam("itemPid") String itemPid) {
        try {
            checkSupportedObjectPid(collectionPid);
            checkSupportedObjectPid(itemPid);
            //authentication
            AuthenticatedUser user = getAuthenticatedUserByOauth();
            String role = ROLE_EDIT_COLLECTION;
            if (!user.getRoles().contains(role)) {
                throw new ForbiddenException("user '%s' is not allowed to edit collections (missing role '%s')", user.getName(), role); //403
            }
            checkObjectExists(collectionPid);
            checkObjectExists(itemPid);
            Document relsExt = krameriusRepositoryApi.getRelsExt(collectionPid, true);
            foxmlBuilder.removeRelationFromRelsExt(collectionPid, relsExt, KrameriusRepositoryApi.KnownRelations.CONTAINS, itemPid);
            krameriusRepositoryApi.updateRelsExt(collectionPid, relsExt);
            //schedule reindexations - 1. item that was removed (whole tree and foster trees), 2. no need to re-index collection
            String batchToken = UUID.randomUUID().toString();
            //TODO: namísto TREE_AND_FOSTER_TREES nový typ indexace, co bude řešit jen sbírky
            scheduleReindexation(itemPid, user, "TREE_AND_FOSTER_TREES", batchToken, true, itemPid);
            //scheduleReindexation(collectionPid, user, "OBJECT", batchToken, false, "sbírka " + collectionPid);
            return Response.status(Response.Status.OK).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    /**
     * Removes collection.
     *
     * @param pid
     * @return
     */
    @DELETE
    @Path("{pid}")
    public Response deleteCollection(@PathParam("pid") String pid) {
        try {
            checkSupportedObjectPid(pid);
            //authentication
            AuthenticatedUser user = getAuthenticatedUserByOauth();
            String role = ROLE_DELETE_COLLECTION;
            if (!user.getRoles().contains(role)) {
                throw new ForbiddenException("user '%s' is not allowed to delete collections (missing role '%s')", user.getName(), role); //403
            }
            //extract children before deleting collection
            Pair<List<RepositoryApi.Triplet>, List<RepositoryApi.Triplet>> childrenTpls = krameriusRepositoryApi.getChildren(pid);
            List<String> childrenPids = new ArrayList<>();
            for (RepositoryApi.Triplet ownChildTpl : childrenTpls.getFirst()) {
                String childPid = ownChildTpl.target;
                System.out.println(childPid);
                childrenPids.add(childPid);
            }
            for (RepositoryApi.Triplet fosterChildTpl : childrenTpls.getSecond()) {
                String childPid = fosterChildTpl.target;
                System.out.println(childPid);
                childrenPids.add(childPid);
            }
            //delete collection object form repository
            krameriusRepositoryApi.getLowLevelApi().deleteObject(pid);
            //schedule reindexations - 1. deleted collection (only object) , 2. all children (both own and foster, their wholes tree and foster trees), 3. no need to reindex collections owning this one
            String batchToken = UUID.randomUUID().toString();
            scheduleReindexation(pid, user, "OBJECT", batchToken, false, "sbírka " + pid);
            for (String childPid : childrenPids) {
                //TODO: namísto TREE_AND_FOSTER_TREES nový typ indexace, co bude řešit jen sbírky
                scheduleReindexation(childPid, user, "TREE_AND_FOSTER_TREES", batchToken, true, childPid);
            }
            return Response.ok().build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    private Collection fetchCollectionFromRepository(String pid, boolean withContent, boolean withItems) throws IOException, RepositoryException, SolrServerException {
        Collection collection = new Collection();
        collection.pid = pid;
        //timestamps from Foxml properties
        collection.created = krameriusRepositoryApi.getLowLevelApi().getPropertyCreated(pid);
        collection.modified = krameriusRepositoryApi.getLowLevelApi().getPropertyLastModified(pid);
        //data from MODS
        Document mods = krameriusRepositoryApi.getMods(pid, false);
        collection.nameCz = Dom4jUtils.stringOrNullFromFirstElementByXpath(mods.getRootElement(), "//mods/titleInfo[@lang='cze']/title");
        collection.nameEn = Dom4jUtils.stringOrNullFromFirstElementByXpath(mods.getRootElement(), "//mods/titleInfo[@lang='eng']/title");
        if (collection.nameCz == null) { //fallback for older data without @lang
            collection.nameCz = Dom4jUtils.stringOrNullFromFirstElementByXpath(mods.getRootElement(), "//mods/titleInfo[not(@lang)]/title");
        }
        collection.descriptionCz = Dom4jUtils.stringOrNullFromFirstElementByXpath(mods.getRootElement(), "//mods/abstract[@lang='cze']");
        collection.descriptionEn = Dom4jUtils.stringOrNullFromFirstElementByXpath(mods.getRootElement(), "//mods/abstract[@lang='eng']");
        if (collection.descriptionCz == null) {//fallback for older data without @lang
            collection.descriptionCz = Dom4jUtils.stringOrNullFromFirstElementByXpath(mods.getRootElement(), "//mods/abstract[not(@lang)]");
        }
        if (withContent) {
            String contentHtmlEscapedCz = Dom4jUtils.stringOrNullFromFirstElementByXpath(mods.getRootElement(), "//mods/note[@lang='cze']");
            if (contentHtmlEscapedCz != null) {
                collection.contentCz = StringEscapeUtils.unescapeHtml(contentHtmlEscapedCz);
            }
            String contentHtmlEscapedEn = Dom4jUtils.stringOrNullFromFirstElementByXpath(mods.getRootElement(), "//mods/note[@lang='eng']");
            if (contentHtmlEscapedEn != null) {
                collection.contentEn = StringEscapeUtils.unescapeHtml(contentHtmlEscapedEn);
            }
            if (collection.contentCz == null) { //fallback for older data without @lang
                String contentHtmlCzEscapedNoLang = Dom4jUtils.stringOrNullFromFirstElementByXpath(mods.getRootElement(), "//mods/note[not(@lang)]");
                if (contentHtmlCzEscapedNoLang != null) {
                    collection.contentCz = StringEscapeUtils.unescapeHtml(contentHtmlCzEscapedNoLang);
                }
            }
        }
        //data from RELS-EXT
        Document relsExt = krameriusRepositoryApi.getRelsExt(pid, false);
        collection.standalone = Boolean.valueOf(Dom4jUtils.stringOrNullFromFirstElementByXpath(relsExt.getRootElement(), "//standalone"));
        //data from Processing index
        if (withItems) {
            collection.items = krameriusRepositoryApi.getPidsOfItemsInCollection(pid);
        }
        return collection;
    }

    private Collection extractCollectionFromJson(JSONObject collectionDefinition) {
        try {
            return new Collection(collectionDefinition);
        } catch (JSONException e) {
            throw new BadRequestException("error parsing json: " + e);
        }
    }

}
