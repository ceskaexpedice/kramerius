package cz.incad.kramerius.rest.apiNew.admin.v70.collections;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.repository.KrameriusRepositoryApi;
import cz.incad.kramerius.repository.RepositoryApi;
import cz.incad.kramerius.rest.apiNew.admin.v70.AdminApiResource;
import cz.incad.kramerius.rest.apiNew.exceptions.BadRequestException;
import cz.incad.kramerius.rest.apiNew.exceptions.ForbiddenException;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.security.RightsResolver;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.Dom4jUtils;
import cz.incad.kramerius.utils.java.Pair;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.dom4j.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Path("/admin/v7.0/collections")
public class CollectionsResource extends AdminApiResource {

    public static final Logger LOGGER = Logger.getLogger(CollectionsResource.class.getName());

    private static final int MAX_BATCH_SIZE = 100;


    @Inject
    @Named("new-index")
    SolrAccess solrAccess;

    @Inject
    private CollectionsFoxmlBuilder foxmlBuilder;

    @Inject
    Provider<User> userProvider;

    @Inject
    RightsResolver rightsResolver;

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

            User user1 = this.userProvider.get();
            List<String> roles = Arrays.stream(user1.getGroups()).map(Role::getName).collect(Collectors.toList());
            //authorization

            if (!permitCollectionEdit(this.rightsResolver, user1, SpecialObjects.REPOSITORY.getPid())) {
                throw new ForbiddenException("user '%s' is not allowed to create collections (missing action '%s')", user1.getLoginname(), SecuredActions.A_COLLECTIONS_EDIT); //403
            }

            Collection collection = extractCollectionFromJson(collectionDefinition);
            if ((collection.nameCz == null || collection.nameCz.isEmpty()) && (collection.nameEn == null || collection.nameEn.isEmpty())) {
                throw new BadRequestException("name can't be empty");
            }
            collection.pid = "uuid:" + UUID.randomUUID().toString();
            Document foxml = foxmlBuilder.buildFoxml(collection, null);
            krameriusRepositoryApi.getLowLevelApi().ingestObject(foxml, collection.pid);
            //schedule reindexation - new collection (only object)
            scheduleReindexation(collection.pid, user1.getLoginname(), user1.getLoginname(), "OBJECT", false, "sbírka " + collection.pid);
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
            //AuthenticatedUser user = getAuthenticatedUserByOauth();

            User user1 = this.userProvider.get();
            List<String> roles = Arrays.stream(user1.getGroups()).map(Role::getName).collect(Collectors.toList());

            if (!permitCollectionRead(this.rightsResolver, user1, pid) &&
                    !permitCollectionEdit(this.rightsResolver, user1, pid)) {
                throw new ForbiddenException("user '%s' is not allowed to create collections (missing action '%s')", user1.getLoginname(), SecuredActions.A_COLLECTIONS_READ); //403
            }

            synchronized (CollectionsResource.class) {
                checkObjectExists(pid);
                Collection collection = fetchCollectionFromRepository(pid, true, true);
                return Response.ok(collection.toJson()).build();
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @GET
    @Path("/prefix")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCollectionsByPrefix(@QueryParam("rows") String rows, @QueryParam("page") String page, @QueryParam("prefix") String prefix) {
        try {
            User user1 = this.userProvider.get();
            List<String> roles = Arrays.stream(user1.getGroups()).map(Role::getName).collect(Collectors.toList());

            // TODO: check if it is necessary
            if (!permitCollectionRead(this.rightsResolver, user1, SpecialObjects.REPOSITORY.getPid()) &&
                    !permitCollectionEdit(this.rightsResolver, user1, SpecialObjects.REPOSITORY.getPid())) {
                throw new ForbiddenException("user '%s' is not allowed to create collections (missing action '%s')", user1.getLoginname(), SecuredActions.A_COLLECTIONS_READ); //403
            }

            Pair<Long, List<String>> pidsOfObjectsByModel = krameriusRepositoryApi.getLowLevelApi().getPidsOfObjectsByModel("collection", prefix, Integer.parseInt(rows), Integer.parseInt(page));
            JSONArray collections = new JSONArray();
            for (String pid : pidsOfObjectsByModel.getSecond()) {
                try {
                    Collection collection = fetchCollectionFromRepository(pid, false, false);
                    collections.put(collection.toJson());
                } catch (RepositoryException e) {
                    //ignoring broken collection and still returning other collections (instead of error response)
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
            JSONObject result = new JSONObject();
            result.put("total_size", pidsOfObjectsByModel.getFirst());
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
            User user1 = this.userProvider.get();
            List<String> roles = Arrays.stream(user1.getGroups()).map(Role::getName).collect(Collectors.toList());

            // TODO: check if it is necessary
            if (!permitCollectionRead(this.rightsResolver, user1, SpecialObjects.REPOSITORY.getPid()) &&
                    !permitCollectionEdit(this.rightsResolver, user1, SpecialObjects.REPOSITORY.getPid())) {
                throw new ForbiddenException("user '%s' is not allowed to create collections (missing action '%s')", user1.getLoginname(), SecuredActions.A_COLLECTIONS_READ); //403
            }

            synchronized (CollectionsResource.class) {
                List<String> pids = null;
                if (itemPid != null) {
                    checkSupportedObjectPid(itemPid);
                    checkObjectExists(itemPid);
                    //  not support rows and page
                    pids = krameriusRepositoryApi.getPidsOfCollectionsContainingItem(itemPid);
                } else {
                    pids = krameriusRepositoryApi.getLowLevelApi().getPidsOfObjectsByModel("collection");
                }
                JSONArray collections = new JSONArray();
                for (String pid : pids) {
                    try {
                        Collection collection = fetchCollectionFromRepository(pid, false, false);
                        collections.put(collection.toJson());
                    } catch (RepositoryException e) {
                        //ignoring broken collection and still returning other collections (instead of error response)
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    }
                }
                JSONObject result = new JSONObject();
                result.put("total_size", pids.size());
                result.put("collections", collections);
                return Response.ok(result.toString()).build();
            }
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
            User user1 = this.userProvider.get();
            List<String> roles = Arrays.stream(user1.getGroups()).map(Role::getName).collect(Collectors.toList());

            // TODO: check if it is necessary
            if (!permitCollectionEdit(this.rightsResolver, user1, pid)) {
                throw new ForbiddenException("user '%s' is not allowed to create collections (missing action '%s')", user1.getLoginname(), SecuredActions.A_COLLECTIONS_EDIT); //403
            }


            synchronized (CollectionsResource.class) {
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
                    //schedule reindexation - (only collection object)
                    scheduleReindexation(pid, user1.getLoginname(), user1.getLoginname(), "OBJECT", false, "sbírka " + pid);
                }
                return Response.ok().build();
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

   /* private void scheduleReindexation(String objectPid, String userid, String username, String indexationType, String batchToken, boolean ignoreInconsistentObjects, String title) {
        List<String> paramsList = new ArrayList<>();
        paramsList.add(indexationType);
        paramsList.add(objectPid);
        paramsList.add(Boolean.toString(ignoreInconsistentObjects));
        paramsList.add(title);
        processSchedulingHelper.scheduleProcess("new_indexer_index_object", paramsList, userid, username, batchToken);
    }*/

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
            User user1 = this.userProvider.get();
            if (!permitCollectionEdit(this.rightsResolver, user1, pid)) {
                throw new ForbiddenException("user '%s' is not allowed to create collections (missing action '%s')", user1.getLoginname(), SecuredActions.A_COLLECTIONS_EDIT); //403
            }

            for (int i = 0; i < pidsOfItems.length(); i++) {
                String p = pidsOfItems.getString(i);
                if (!permitAbleToAdd(this.rightsResolver, user1, p)) {
                    throw new ForbiddenException("user '%s' is not allowed to add item %s to collection (missing action '%s')", user1.getLoginname(), p, SecuredActions.A_ABLE_TOBE_PART_OF_COLLECTION); //403
                }
            }

            checkSupportedObjectPid(pid);
            synchronized (CollectionsResource.class) {
                //TODO: implement
                throw new RuntimeException("not implemented yet");
            }
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
        try {
            checkSupportedObjectPid(collectionPid);
            checkSupportedObjectPid(itemPid);
            User user = this.userProvider.get();

            if (!permitCollectionEdit(this.rightsResolver, user, collectionPid)) {
                throw new ForbiddenException("user '%s' is not allowed to modify collection (missing action '%s')", user.getLoginname(), SecuredActions.A_COLLECTIONS_EDIT); //403
            }
            if (!permitAbleToAdd(this.rightsResolver, user, itemPid)) {
                throw new ForbiddenException("user '%s' is not allowed to add item %s to collection (missing action '%s')", user.getLoginname(), itemPid, SecuredActions.A_ABLE_TOBE_PART_OF_COLLECTION); //403
            }
            synchronized (CollectionsResource.class) {
                //LOGGER.info("addItemToCollection execute, Thread " + Thread.currentThread().getName());
                checkObjectExists(collectionPid);
                checkObjectExists(itemPid);
                checkCanAddItemToCollection(itemPid, collectionPid);
                //extract relsExt and update by adding new relation
                Document relsExt = krameriusRepositoryApi.getRelsExt(collectionPid, true);
                boolean addedNow = foxmlBuilder.appendRelationToRelsExt(collectionPid, relsExt, KrameriusRepositoryApi.KnownRelations.CONTAINS, itemPid);
                if (!addedNow) {
                    throw new ForbiddenException("item %s is already present in collection %s", itemPid, collectionPid);
                }
                //save updated rels-ext
                krameriusRepositoryApi.updateRelsExt(collectionPid, relsExt);
                //schedule reindexations - 1. newly added item (whole tree and foster trees), 2. no need to re-index collection
                //TODO: mozna optimalizace: pouzit zde indexaci typu COLLECTION_ITEMS (neimplementovana)
                scheduleReindexation(itemPid, user.getLoginname(), user.getLoginname(), "TREE_AND_FOSTER_TREES", false, itemPid);
                //LOGGER.info("addItemToCollection end, Thread " + Thread.currentThread().getName());
                return Response.status(Response.Status.CREATED).build();
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    /**
     * Add multiple items to collection
     *
     * @param collectionPid
     * @param itemsPidsJsonArrayStr
     * @return
     */
    @POST
    @Path("{pid}/items")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addItemsToCollection(@PathParam("pid") String collectionPid, String itemsPidsJsonArrayStr) { //primo JSONArray itemsPids jako parametr nefunguje
        System.out.println(itemsPidsJsonArrayStr);
        try {
            //parse JSON Array on input
            JSONArray itemsPid;
            try {
                itemsPid = new JSONArray(itemsPidsJsonArrayStr);
                if (itemsPid.length() > MAX_BATCH_SIZE) {
                    throw new BadRequestException("too many items in a batch (%d), limit is %d", itemsPid.length(), MAX_BATCH_SIZE);
                }
            } catch (JSONException e) {
                throw new BadRequestException("not a json array: " + itemsPidsJsonArrayStr);
            }

            //check collection
            checkSupportedObjectPid(collectionPid);
            if (!isModelCollection(collectionPid)) {
                throw new ForbiddenException("not a collection: " + collectionPid);
            }
            User user = this.userProvider.get();
            if (!permitCollectionEdit(this.rightsResolver, user, collectionPid)) {
                throw new ForbiddenException("user '%s' is not allowed to modify collection (missing action '%s')", user.getLoginname(), SecuredActions.A_COLLECTIONS_EDIT); //403
            }

            //check each item pid
            List<String> pidsToBeAdded = new ArrayList<>();
            Map<String, String> errorsByPid = new HashedMap<>();
            for (int i = 0; i < itemsPid.length(); i++) {
                System.out.println(itemsPid);
                String itemPid = itemsPid.getString(i);
                if (!isSupporetdObjectPid(itemPid)) {
                    errorsByPid.put(itemPid, "not supported PID format");
                } else if (!objectExists(itemPid)) {
                    errorsByPid.put(itemPid, "object not found in repository");
                } else if (collectionPid.equals(itemPid)) {
                    errorsByPid.put(itemPid, "cannot add collection into itself");
                } else if (!permitAbleToAdd(this.rightsResolver, user, itemPid)) {
                    errorsByPid.put(itemPid, String.format("user '%s' is not allowed to add item %s to collection (missing action '%s')", user.getLoginname(), itemPid, SecuredActions.A_ABLE_TOBE_PART_OF_COLLECTION));
                } else {
                    String cyclicPath = findCyclicPath(itemPid, collectionPid, String.format("%s --contains--> %s", collectionPid, itemPid));
                    if (cyclicPath != null) {
                        errorsByPid.put(itemPid, "adding item to collection would create cycle: " + cyclicPath);
                    } else {
                        pidsToBeAdded.add(itemPid);
                    }
                }
            }

            //add items to rels-ext of collection, schedule reindexation of items that had been added
            List<String> pidsAdded = new ArrayList<>();
            synchronized (CollectionsResource.class) {
                Document relsExt = krameriusRepositoryApi.getRelsExt(collectionPid, true);
                boolean atLeastOneAdded = false;
                for (String itemPid : pidsToBeAdded) {
                    boolean addedNow = foxmlBuilder.appendRelationToRelsExt(collectionPid, relsExt, KrameriusRepositoryApi.KnownRelations.CONTAINS, itemPid);
                    if (addedNow) {
                        pidsAdded.add(itemPid);
                        atLeastOneAdded = true;
                    } else {
                        errorsByPid.put(itemPid, "item is already present in collection");
                    }
                }
                if (atLeastOneAdded) {
                    //save updated rels-ext
                    krameriusRepositoryApi.updateRelsExt(collectionPid, relsExt);
                    //no need to re-index collection itself
                    for (String itemPid : pidsAdded) {
                        //TODO: mozna optimalizace: pouzit zde indexaci typu COLLECTION_ITEMS (neimplementovana)
                        scheduleReindexation(itemPid, user.getLoginname(), user.getLoginname(), "TREE_AND_FOSTER_TREES", false, itemPid);
                    }
                }
            }

            JSONArray ignored = new JSONArray();
            for (String itemPid : errorsByPid.keySet()) {
                JSONObject ignoredPidInfo = new JSONObject();
                ignoredPidInfo.put("pid", itemPid);
                ignoredPidInfo.put("problem", errorsByPid.get(itemPid));
                ignored.put(ignoredPidInfo);
            }
            JSONObject result = new JSONObject();
            result.put("added", pidsAdded.size());
            result.put("ignored", ignored);
            return Response.ok(result.toString()).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    private void checkCanAddItemToCollection(String itemPid, String collectionPid) throws
            SolrServerException, RepositoryException, IOException {
        //pid of object that item is to be added into must belong to collection
        if (!isModelCollection(collectionPid)) {
            throw new ForbiddenException("not a collection: " + collectionPid);
        }
        //cannot add to itself
        if (collectionPid.equals(itemPid)) {
            throw new ForbiddenException("cannot add collection into itself: " + collectionPid);
        }
        //detect cycle
        if ("collection".equals(krameriusRepositoryApi.getModel(itemPid))) {
            String cyclicPath = findCyclicPath(itemPid, collectionPid, String.format("%s --contains--> %s", collectionPid, itemPid));
            if (cyclicPath != null) {
                throw new ForbiddenException("adding item to collection would create cycle: " + cyclicPath);
            }
        }
    }

    private boolean isModelCollection(String collectionPid) throws
            SolrServerException, RepositoryException, IOException {
        return "collection".equals(krameriusRepositoryApi.getModel(collectionPid));
    }

    private String findCyclicPath(String pid, String pidOfObjectNotAllowedOnPath, String pathSoFar) throws SolrServerException, RepositoryException, IOException {
        List<RepositoryApi.Triplet> fosterChildrenTriplets = krameriusRepositoryApi.getChildren(pid).getSecond();
        for (RepositoryApi.Triplet triplet : fosterChildrenTriplets) {
            String path = String.format("%s --%s--> %s ", pathSoFar, triplet.relation, triplet.target);
            if (pidOfObjectNotAllowedOnPath.equals(triplet.target)) {
                throw new ForbiddenException("adding item to collection would create cycle: " + path);
            } else {
                String cyclicPathFound = findCyclicPath(triplet.target, pidOfObjectNotAllowedOnPath, path);
                if (cyclicPathFound != null) {
                    return cyclicPathFound;
                }
            }
        }
        return null;
    }


//    @PUT
//    @Path("{pid}/children_order")
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response setChildrenOrder(@PathParam("pid") String pid, JSONObject newChildrenOrder) {

    @PUT
    @Path("{collectionPid}/items/delete_batch_items")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeItemFromCollection(@PathParam("collectionPid") String collectionPid, JSONObject batch) {
        synchronized (CollectionsResource.class) {
            List<String> reindexCollection = new ArrayList<>();
            checkSupportedObjectPid(collectionPid);
            try {

                User user1 = this.userProvider.get();
                List<String> roles = Arrays.stream(user1.getGroups()).map(Role::getName).collect(Collectors.toList());
                Document relsExt = krameriusRepositoryApi.getRelsExt(collectionPid, true);

                for (int i = 0; i < batch.getJSONArray("pids").length(); i++) {
                    String itemPid = batch.getJSONArray("pids").getString(i);

                    checkSupportedObjectPid(itemPid);
                    if (!permitCollectionEdit(this.rightsResolver, user1, collectionPid)) {
                        throw new ForbiddenException(
                                "user '%s' is not allowed to create collections (missing action '%s')",
                                user1.getLoginname(), SecuredActions.A_COLLECTIONS_EDIT); // 403
                    }

                    if (!permitAbleToAdd(this.rightsResolver, user1, itemPid)) {
                        throw new ForbiddenException(
                                "user '%s' is not allowed to add item %s to collection (missing action '%s')",
                                user1.getLoginname(), itemPid, SecuredActions.A_ABLE_TOBE_PART_OF_COLLECTION); // 403
                    }
                    checkObjectExists(collectionPid);
                    checkObjectExists(itemPid);
                    checkCanRemoveItemFromCollection(itemPid, collectionPid);
                    // extract relsExt and update by removing relation
                    boolean removed = foxmlBuilder.removeRelationFromRelsExt(collectionPid, relsExt,
                            KrameriusRepositoryApi.KnownRelations.CONTAINS, itemPid);
                    if (!removed) {
                        throw new ForbiddenException("item %s is not present in collection %s", itemPid, collectionPid);
                    } else {
                        reindexCollection.add(itemPid);
                    }
                }

                // save updated rels-ext
                krameriusRepositoryApi.updateRelsExt(collectionPid, relsExt);

                reindexCollection.forEach(itemPid -> {
                    // schedule reindexations - 1. item that was removed (whole tree and foster
                    // trees), 2. no need to re-index collection
                    // TODO: mozna optimalizace: pouzit zde indexaci typu COLLECTION_ITEMS
                    // (neimplementovana)
                    scheduleReindexation(itemPid, user1.getLoginname(), user1.getLoginname(), "TREE_AND_FOSTER_TREES",
                            false, itemPid);

                });

            } catch (WebApplicationException e) {
                throw e;
            } catch (Throwable e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                throw new InternalErrorException(e.getMessage());
            }

            return Response.status(Response.Status.OK).build();

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
    public Response removeItemFromCollection(@PathParam("collectionPid") String collectionPid,
                                             @PathParam("itemPid") String itemPid) {
        synchronized (CollectionsResource.class) {
            try {
                checkSupportedObjectPid(collectionPid);
                checkSupportedObjectPid(itemPid);
                //authentication
                //AuthenticatedUser user = getAuthenticatedUserByOauth();

                User user1 = this.userProvider.get();
                List<String> roles = Arrays.stream(user1.getGroups()).map(Role::getName).collect(Collectors.toList());

                if (!permitCollectionEdit(this.rightsResolver, user1, collectionPid)) {
                    throw new ForbiddenException("user '%s' is not allowed to create collections (missing action '%s')", user1.getLoginname(), SecuredActions.A_COLLECTIONS_EDIT); //403
                }

                if (!permitAbleToAdd(this.rightsResolver, user1, itemPid)) {
                    throw new ForbiddenException("user '%s' is not allowed to add item %s to collection (missing action '%s')", user1.getLoginname(), itemPid, SecuredActions.A_ABLE_TOBE_PART_OF_COLLECTION); //403
                }
                checkObjectExists(collectionPid);
                checkObjectExists(itemPid);
                checkCanRemoveItemFromCollection(itemPid, collectionPid);
                //extract relsExt and update by removing relation
                Document relsExt = krameriusRepositoryApi.getRelsExt(collectionPid, true);
                boolean removed = foxmlBuilder.removeRelationFromRelsExt(collectionPid, relsExt, KrameriusRepositoryApi.KnownRelations.CONTAINS, itemPid);
                if (!removed) {
                    throw new ForbiddenException("item %s is not present in collection %s", itemPid, collectionPid);
                }
                //save updated rels-ext
                krameriusRepositoryApi.updateRelsExt(collectionPid, relsExt);
                //schedule reindexations - 1. item that was removed (whole tree and foster trees), 2. no need to re-index collection
                //TODO: mozna optimalizace: pouzit zde indexaci typu COLLECTION_ITEMS (neimplementovana)
                scheduleReindexation(itemPid, user1.getLoginname(), user1.getLoginname(), "TREE_AND_FOSTER_TREES", false, itemPid);
                return Response.status(Response.Status.OK).build();
            } catch (WebApplicationException e) {
                throw e;
            } catch (Throwable e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                throw new InternalErrorException(e.getMessage());
            }
        }
    }

    private void checkCanRemoveItemFromCollection(String itemPid, String collectionPid) throws SolrServerException, RepositoryException, IOException {
        //pid of object that item is to be removed from must belong to collection
        if (!"collection".equals(krameriusRepositoryApi.getModel(collectionPid))) {
            throw new ForbiddenException("not a collection: " + collectionPid);
        }
        //cannot remove collection from itself
        if (collectionPid.equals(itemPid)) {
            throw new ForbiddenException("collection and item pids are the same: " + collectionPid);
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
            User user1 = this.userProvider.get();
            List<String> roles = Arrays.stream(user1.getGroups()).map(Role::getName).collect(Collectors.toList());

            if (!permitCollectionEdit(this.rightsResolver, user1, SpecialObjects.REPOSITORY.getPid()) &&
                    !permitDelete(this.rightsResolver, user1, SpecialObjects.REPOSITORY.getPid())) {
                throw new ForbiddenException("user '%s' is not allowed to create collections (missing action '%s')", user1.getLoginname(), SecuredActions.A_COLLECTIONS_EDIT); //403
            }

            //extract children before deleting collection
            Pair<List<RepositoryApi.Triplet>, List<RepositoryApi.Triplet>> childrenTpls = krameriusRepositoryApi.getChildren(pid);
            List<String> childrenPids = new ArrayList<>();
            for (RepositoryApi.Triplet ownChildTpl : childrenTpls.getFirst()) {
                String childPid = ownChildTpl.target;
                childrenPids.add(childPid);
            }
            for (RepositoryApi.Triplet fosterChildTpl : childrenTpls.getSecond()) {
                String childPid = fosterChildTpl.target;
                childrenPids.add(childPid);
            }
            //delete collection object form repository (not managed datastreams, since those for IMG_THUMB are referenced from other objects - pages)
            krameriusRepositoryApi.getLowLevelApi().deleteObject(pid, false);
            //schedule reindexations - 1. deleted collection (only object) , 2. all children (both own and foster, their wholes tree and foster trees), 3. no need to reindex collections owning this one
            String batchToken = UUID.randomUUID().toString();
            scheduleReindexationInBatch(pid, user1.getLoginname(), user1.getLoginname(), "OBJECT", batchToken, false, "sbírka " + pid);
           
            for (String childPid : childrenPids) {
                //TODO: mozna optimalizace: pouzit zde indexaci typu COLLECTION_ITEMS (neimplementovana)
                scheduleReindexationInBatch(childPid, user1.getLoginname(), user1.getLoginname(), "TREE_AND_FOSTER_TREES", batchToken, true, childPid);
            }
            return Response.ok().build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    private Collection fetchCollectionFromRepository(String pid, boolean withContent, boolean withItems) throws
            IOException, RepositoryException, SolrServerException {
        Collection collection = new Collection();
        collection.pid = pid;
        //timestamps from Foxml properties
        if (!krameriusRepositoryApi.getLowLevelApi().objectExists(pid)) {
            // index + akubra synchronization problem
        }

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

    public boolean permitCollectionEdit(RightsResolver rightsResolver, User user, String collectionPid) throws IOException {
        ObjectPidsPath[] pidPaths = this.solrAccess.getPidPaths(collectionPid);
        if (pidPaths.length > 0) {
            for (ObjectPidsPath objectPidsPath : pidPaths) {
                boolean permited = user != null ? rightsResolver.isActionAllowed(user, SecuredActions.A_COLLECTIONS_EDIT.getFormalName(), collectionPid, null, objectPidsPath).flag() : false;
                if (permited) return permited;
            }
        } else {
            boolean permited = user != null ? rightsResolver.isActionAllowed(user, SecuredActions.A_COLLECTIONS_EDIT.getFormalName(), collectionPid, null, ObjectPidsPath.REPOSITORY_PATH).flag() : false;
            if (permited) return permited;
        }
        return false;
    }

    public boolean permitDelete(RightsResolver rightsResolver, User user, String collectionPid) throws IOException {
        ObjectPidsPath[] pidPaths = this.solrAccess.getPidPaths(collectionPid);
        for (ObjectPidsPath objectPidsPath : pidPaths) {
            boolean permited = user != null ? rightsResolver.isActionAllowed(user, SecuredActions.A_DELETE.getFormalName(), collectionPid, null, objectPidsPath).flag() : false;
            if (permited) return permited;
        }
        return false;
    }

    public boolean permitCollectionRead(RightsResolver rightsResolver, User user, String collectionPid) throws IOException {
        // jeste neni vytvorena - nema cestu nahoru
        ObjectPidsPath[] pidPaths = this.solrAccess.getPidPaths(collectionPid);
        if (pidPaths.length > 0) {
            for (ObjectPidsPath objectPidsPath : pidPaths) {
                boolean permited = user != null ? rightsResolver.isActionAllowed(user, SecuredActions.A_COLLECTIONS_READ.getFormalName(), collectionPid, null, objectPidsPath).flag() : false;
                if (permited) return permited;
            }
        } else {
            boolean permited = user != null ? rightsResolver.isActionAllowed(user, SecuredActions.A_COLLECTIONS_READ.getFormalName(), collectionPid, null, ObjectPidsPath.REPOSITORY_PATH).flag() : false;
            if (permited) return permited;
        }
        return false;
    }

    public boolean permitAbleToAdd(RightsResolver rightsResolver, User user, String pid) throws IOException {
        ObjectPidsPath[] pidPaths = this.solrAccess.getPidPaths(pid);
        for (ObjectPidsPath objectPidsPath : pidPaths) {
            boolean permited = user != null ? rightsResolver.isActionAllowed(user, SecuredActions.A_ABLE_TOBE_PART_OF_COLLECTION.getFormalName(), pid, null, objectPidsPath).flag() : false;
            if (permited) return permited;
        }
        return false;

    }
}
