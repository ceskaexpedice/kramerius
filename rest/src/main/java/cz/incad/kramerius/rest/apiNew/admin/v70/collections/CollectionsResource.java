package cz.incad.kramerius.rest.apiNew.admin.v70.collections;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.apiNew.admin.v70.AdminApiResource;
import cz.incad.kramerius.rest.apiNew.admin.v70.collections.Collection.ThumbnailbStateEnum;
import cz.incad.kramerius.rest.apiNew.admin.v70.collections.thumbs.ClientIIIFGenerator;
import cz.incad.kramerius.rest.apiNew.admin.v70.collections.thumbs.SimpleIIIFGenerator;
import cz.incad.kramerius.rest.apiNew.admin.v70.collections.thumbs.ThumbsGenerator;
import cz.incad.kramerius.rest.apiNew.exceptions.BadRequestException;
import cz.incad.kramerius.rest.apiNew.exceptions.ForbiddenException;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.security.RightsResolver;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.imgs.ImageMimeType;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;
import cz.incad.kramerius.utils.java.Pair;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.MultipartStream;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileSystemUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.protocol.HTTP;
import org.apache.solr.client.solrj.SolrServerException;
import org.ceskaexpedice.akubra.*;
import org.ceskaexpedice.akubra.processingindex.OwnedAndFosteredChildren;
import org.ceskaexpedice.akubra.processingindex.ProcessingIndexItem;
import org.ceskaexpedice.akubra.processingindex.SizeItemsPair;
import org.ceskaexpedice.akubra.relsext.KnownRelations;
import org.ceskaexpedice.akubra.utils.Dom4jUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


@Path("/admin/v7.0/collections")
public class CollectionsResource extends AdminApiResource {

    // Stream name 
    private static final String COLLECTION_CLIPS = "COLLECTION_CLIPS";
    private static final List<ThumbsGenerator> THUMBS_GENERATOR = new ArrayList<>();

    static {
        THUMBS_GENERATOR.add(new SimpleIIIFGenerator());
        THUMBS_GENERATOR.add(new ClientIIIFGenerator());
    }

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

    @Inject
    Provider<HttpServletRequest> requestProvider;

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
            if (collection.names.isEmpty()) {
                throw new BadRequestException("name can't be empty");
            }
            collection.pid = "uuid:" + UUID.randomUUID().toString();
            Document foxml = foxmlBuilder.buildFoxml(collection, null);
            akubraRepository.ingest(Dom4jUtils.foxmlDocToDigitalObject(foxml, akubraRepository));
            //schedule reindexation - new collection (only object)
            scheduleReindexation(collection.pid, user1.getLoginname(), user1.getLoginname(), "OBJECT", false, "sbírka " + collection.pid);
            return Response.status(Response.Status.CREATED).entity(collection.toJson().toString()).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (DistributedLocksException e) {
            // TODO AK_NEW
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

            User user1 = this.userProvider.get();
            List<String> roles = Arrays.stream(user1.getGroups()).map(Role::getName).collect(Collectors.toList());

            if (!permitCollectionRead(this.rightsResolver, user1, pid) &&
                    !permitCollectionEdit(this.rightsResolver, user1, pid)) {
                throw new ForbiddenException("user '%s' is not allowed to create collections (missing action '%s')", user1.getLoginname(), SecuredActions.A_COLLECTIONS_READ); //403
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

            SizeItemsPair pidsOfObjectsByModel = akubraRepository.pi().getByModel("collection", prefix, Integer.parseInt(rows), Integer.parseInt(page));
            JSONArray collections = new JSONArray();
            for (ProcessingIndexItem item : pidsOfObjectsByModel.items()) {
                try {
                    Collection collection = fetchCollectionFromRepository(item.source(), false, false);
                    collections.put(collection.toJson());
                } catch (RepositoryException e) {
                    //ignoring broken collection and still returning other collections (instead of error response)
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
            JSONObject result = new JSONObject();
            result.put("total_size", pidsOfObjectsByModel.size());
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

            List<ProcessingIndexItem> pids = null;
            if (itemPid != null) {
                checkSupportedObjectPid(itemPid);
                checkObjectExists(itemPid);
                //  not support rows and page
                pids = akubraRepository.pi().getParents(KnownRelations.CONTAINS.toString(), itemPid);
            } else {
                pids = akubraRepository.pi().getByModel("collection", null, Integer.MAX_VALUE, 0).items();
            }
            JSONArray collections = new JSONArray();
            for (ProcessingIndexItem pid : pids) {
                try {
                    Collection collection = fetchCollectionFromRepository(pid.source(), false, false);
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
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @POST
    @Path("{pid}/image/thumb")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(@PathParam("pid") String pid,
                               InputStream mimeTypeStream
    ) {
        try {

            HttpServletRequest req = this.requestProvider.get();

            DiskFileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            List<FileItem> fileItems = upload.parseRequest(req);
            if (fileItems.size() == 1) {
                FileItem fileItem = fileItems.get(0);

                InputStream fileItemStream = fileItem.getInputStream();
                File tmpFile = File.createTempFile("image", "img");

                // Kopírování dat ze vstupního proudu do souboru
                try (OutputStream out = new FileOutputStream(tmpFile)) {
                    IOUtils.copy(fileItemStream, out);
                }
                BufferedImage read = ImageIO.read(new FileInputStream(tmpFile));

                // 127 height
                // calculate scale factor
                int height = read.getHeight();
                int width = read.getWidth();

                double factor = 127d / (double) height;
                double newHeight = ((double) height * factor);
                double newWidth = ((double) width * factor);

                BufferedImage scaled = KrameriusImageSupport.scale(read, (int) newWidth, (int) newHeight);

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ImageIO.write(scaled, "png", bos);

                ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
                akubraRepository.updateManagedDatastream(pid, KnownDatastreams.IMG_THUMB, "image/png", bis);

                Collection nCol = fetchCollectionFromRepository(pid, true, true);
                return Response.ok(nCol.toJson()).build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        } catch (DistributedLocksException e) {
            // TODO AK_NEW
            throw e;
        } catch (RepositoryException | SolrServerException | IOException | FileUploadException e) {
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
            checkObjectExists(pid);
            akubraRepository.doWithWriteLock(pid, () -> {
                Collection current = null;
                try {
                    current = fetchCollectionFromRepository(pid, true, false);
                } catch (IOException | SolrServerException e) {
                    throw new RuntimeException(e);
                }
                Collection updated = current.withUpdatedDataModifiableByClient(extractCollectionFromJson(collectionDefinition));
                if (updated.names.isEmpty()) {
                    throw new BadRequestException("name can't be empty");
                }
                if (!current.equalsInDataModifiableByClient(updated)) {
                    //fetch items in collection first (otherwise eventual consistency of processing index would cause no items in new version of rels-ext)
                    List<ProcessingIndexItem> itemsInCollection = akubraRepository.pi().getChildren(KnownRelations.CONTAINS.toString(), pid);
                    //rebuild and update mods
                    Document document = foxmlBuilder.buildMods(updated);
                    ByteArrayInputStream bis = new ByteArrayInputStream(document.asXML().getBytes(Charset.forName("UTF-8")));
                    akubraRepository.updateXMLDatastream(pid, KnownDatastreams.BIBLIO_MODS, "text/xml", bis);
                    //rebuild and update rels-ext (because of "standalone")
                    List<String> pids = new ArrayList<>();
                    for (ProcessingIndexItem item : itemsInCollection) {
                        pids.add(item.targetPid());
                    }
                    document = foxmlBuilder.buildRelsExt(updated, pids);
                    bis = new ByteArrayInputStream(document.asXML().getBytes(Charset.forName("UTF-8")));
                    akubraRepository.re().update(pid, bis);
                    //schedule reindexation - (only collection object)
                    scheduleReindexation(pid, user1.getLoginname(), user1.getLoginname(), "OBJECT", false, "sbírka " + pid);
                }
                return null;
            });
            return Response.ok().build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (DistributedLocksException e) {
            // TODO AK_NEW
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
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
            //TODO: implement
            throw new RuntimeException("not implemented yet");
        } catch (WebApplicationException e) {
            throw e;
        } catch (DistributedLocksException e) {
            // TODO AK_NEW
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
    public Response addItemToCollection(@PathParam("pid") String collectionPid, @QueryParam("indexation") String indexation, String itemPid) {
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
            checkObjectExists(collectionPid);
            checkObjectExists(itemPid);
            checkCanAddItemToCollection(itemPid, collectionPid);
            //extract relsExt and update by adding new relation
            akubraRepository.doWithWriteLock(collectionPid, () -> {
                Document relsExt = akubraRepository.re().get(collectionPid).asDom4j(true);
                boolean addedNow = foxmlBuilder.appendRelationToRelsExt(collectionPid, relsExt, KnownRelations.CONTAINS.toString(), itemPid);
                if (!addedNow) {
                    throw new ForbiddenException("item %s is already present in collection %s", itemPid, collectionPid);
                }
                //save updated rels-ext
                ByteArrayInputStream bis = new ByteArrayInputStream(relsExt.asXML().getBytes(Charset.forName("UTF-8")));
                akubraRepository.re().update(collectionPid, bis);
                return null;
            });
            //schedule reindexations - 1. newly added item (whole tree and foster trees), 2. no need to re-index collection
            //TODO: mozna optimalizace: pouzit zde indexaci typu COLLECTION_ITEMS (neimplementovana)
            if (StringUtils.isAnyString(indexation) && indexation.trim().toLowerCase().equals("false")) {
                LOGGER.info("Ommiting indexation");
            } else {
                scheduleReindexation(itemPid, user.getLoginname(), user.getLoginname(), "TREE_AND_FOSTER_TREES", false, itemPid);
            }

            //LOGGER.info("addItemToCollection end, Thread " + Thread.currentThread().getName());
            return Response.status(Response.Status.CREATED).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (DistributedLocksException e) {
            // TODO AK_NEW
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
    public Response addItemsToCollection(@PathParam("pid") String collectionPid, @QueryParam("indexation") String indexation, String itemsPidsJsonArrayStr) { //primo JSONArray itemsPids jako parametr nefunguje
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
            akubraRepository.doWithWriteLock(collectionPid, new LockOperation<Object>() {
                @Override
                public Object execute() {
                    Document relsExt = akubraRepository.re().get(collectionPid).asDom4j(true);
                    boolean atLeastOneAdded = false;
                    for (String itemPid : pidsToBeAdded) {
                        boolean addedNow = foxmlBuilder.appendRelationToRelsExt(collectionPid, relsExt, KnownRelations.CONTAINS.toString(), itemPid);
                        if (addedNow) {
                            pidsAdded.add(itemPid);
                            atLeastOneAdded = true;
                        } else {
                            errorsByPid.put(itemPid, "item is already present in collection");
                        }
                    }
                    if (atLeastOneAdded) {
                        //save updated rels-ext
                        ByteArrayInputStream bis = new ByteArrayInputStream(relsExt.asXML().getBytes(Charset.forName("UTF-8")));
                        akubraRepository.re().update(collectionPid, bis);
                        //no need to re-index collection itself
                        if (StringUtils.isAnyString(indexation) && indexation.trim().toLowerCase().equals("false")) {
                            LOGGER.info("Ommiting indexation");
                        } else {
                            for (String itemPid : pidsAdded) {
                                //TODO: mozna optimalizace: pouzit zde indexaci typu COLLECTION_ITEMS (neimplementovana)
                                scheduleReindexation(itemPid, user.getLoginname(), user.getLoginname(), "TREE_AND_FOSTER_TREES", false, itemPid);
                            }
                        }

                    }
                    return null;
                }
            });

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
        } catch (DistributedLocksException e) {
            // TODO AK_NEW
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
        if ("collection".equals(akubraRepository.pi().getModel(itemPid))) {
            String cyclicPath = findCyclicPath(itemPid, collectionPid, String.format("%s --contains--> %s", collectionPid, itemPid));
            if (cyclicPath != null) {
                throw new ForbiddenException("adding item to collection would create cycle: " + cyclicPath);
            }
        }
    }

    private boolean isModelCollection(String collectionPid) {
        return "collection".equals(akubraRepository.pi().getModel(collectionPid));
    }

    private String findCyclicPath(String pid, String pidOfObjectNotAllowedOnPath, String pathSoFar) {
        OwnedAndFosteredChildren children = akubraRepository.pi().getOwnedAndFosteredChildren(pid);
        List<ProcessingIndexItem> fosterChildrenTriplets = children.foster();
        for (ProcessingIndexItem triplet : fosterChildrenTriplets) {
            String path = String.format("%s --%s--> %s ", pathSoFar, triplet.relation(), triplet.targetPid());
            if (pidOfObjectNotAllowedOnPath.equals(triplet.targetPid())) {
                throw new ForbiddenException("adding item to collection would create cycle: " + path);
            } else {
                String cyclicPathFound = findCyclicPath(triplet.targetPid(), pidOfObjectNotAllowedOnPath, path);
                if (cyclicPathFound != null) {
                    return cyclicPathFound;
                }
            }
        }
        return null;
    }

    @PUT
    @Path("{collectionPid}/items/delete_batch_items")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeItemFromCollection(@PathParam("collectionPid") String collectionPid, JSONObject batch) {
        List<String> reindexCollection = new ArrayList<>();
        checkSupportedObjectPid(collectionPid);
        try {
            User user1 = this.userProvider.get();
            // TODO List<String> roles = Arrays.stream(user1.getGroups()).map(Role::getName).collect(Collectors.toList());
            akubraRepository.doWithWriteLock(collectionPid, () -> {
                Document relsExt = akubraRepository.re().get(collectionPid).asDom4j(true);

                for (int i = 0; i < batch.getJSONArray("pids").length(); i++) {
                    String itemPid = batch.getJSONArray("pids").getString(i);

                    checkSupportedObjectPid(itemPid);
                    try {
                        if (!permitCollectionEdit(rightsResolver, user1, collectionPid)) {
                            throw new ForbiddenException(
                                    "user '%s' is not allowed to create collections (missing action '%s')",
                                    user1.getLoginname(), SecuredActions.A_COLLECTIONS_EDIT); // 403
                        }

                        if (!permitAbleToAdd(rightsResolver, user1, itemPid)) {
                            throw new ForbiddenException(
                                    "user '%s' is not allowed to add item %s to collection (missing action '%s')",
                                    user1.getLoginname(), itemPid, SecuredActions.A_ABLE_TOBE_PART_OF_COLLECTION); // 403
                        }
                        checkObjectExists(collectionPid);
                        checkObjectExists(itemPid);
                        checkCanRemoveItemFromCollection(itemPid, collectionPid);
                    } catch (IOException | SolrServerException e) {
                        throw new RuntimeException(e);
                    }
                    // extract relsExt and update by removing relation
                    boolean removed = foxmlBuilder.removeRelationFromRelsExt(collectionPid, relsExt, KnownRelations.CONTAINS, itemPid);
                    if (!removed) {
                        throw new ForbiddenException("item %s is not present in collection %s", itemPid, collectionPid);
                    } else {
                        reindexCollection.add(itemPid);
                    }
                }

                // save updated rels-ext
                ByteArrayInputStream bis = new ByteArrayInputStream(relsExt.asXML().getBytes(Charset.forName("UTF-8")));
                akubraRepository.re().update(collectionPid, bis);
                return null;
            });

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
        } catch (DistributedLocksException e) {
            // TODO AK_NEW
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }

        return Response.status(Response.Status.OK).build();

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
            akubraRepository.doWithWriteLock(collectionPid, new LockOperation<Object>() {
                @Override
                public Object execute() {
                    try {
                        checkCanRemoveItemFromCollection(itemPid, collectionPid);
                    } catch (IOException | SolrServerException e) {
                        throw new RuntimeException(e);
                    }
                    //extract relsExt and update by removing relation
                    Document relsExt = akubraRepository.re().get(collectionPid).asDom4j(true);
                    boolean removed = foxmlBuilder.removeRelationFromRelsExt(collectionPid, relsExt, KnownRelations.CONTAINS, itemPid);
                    if (!removed) {
                        throw new ForbiddenException("item %s is not present in collection %s", itemPid, collectionPid);
                    }
                    //save updated rels-ext
                    ByteArrayInputStream bis = new ByteArrayInputStream(relsExt.asXML().getBytes(Charset.forName("UTF-8")));
                    akubraRepository.re().update(collectionPid, bis);
                    return null;
                }
            });
            //schedule reindexations - 1. item that was removed (whole tree and foster trees), 2. no need to re-index collection
            //TODO: mozna optimalizace: pouzit zde indexaci typu COLLECTION_ITEMS (neimplementovana)
            scheduleReindexation(itemPid, user1.getLoginname(), user1.getLoginname(), "TREE_AND_FOSTER_TREES", false, itemPid);
            return Response.status(Response.Status.OK).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (DistributedLocksException e) {
            // TODO AK_NEW
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    private void checkCanRemoveItemFromCollection(String itemPid, String collectionPid) throws SolrServerException, RepositoryException, IOException {
        //pid of object that item is to be removed from must belong to collection
        if (!"collection".equals(akubraRepository.pi().getModel(collectionPid))) {
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
            OwnedAndFosteredChildren childrenTpls = akubraRepository.pi().getOwnedAndFosteredChildren(pid);
            List<String> childrenPids = new ArrayList<>();
            for (ProcessingIndexItem ownChildTpl : childrenTpls.own()) {
                String childPid = ownChildTpl.targetPid();
                childrenPids.add(childPid);
            }
            for (ProcessingIndexItem fosterChildTpl : childrenTpls.foster()) {
                String childPid = fosterChildTpl.targetPid();
                childrenPids.add(childPid);
            }
            //delete collection object form repository (not managed datastreams, since those for IMG_THUMB are referenced from other objects - pages)
            akubraRepository.doWithWriteLock(pid, () -> {
                akubraRepository.delete(pid, false, true);
                akubraRepository.pi().commit();
                ;
                return null;
            });
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
        } catch (DistributedLocksException e) {
            // TODO AK_NEW
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }


    @POST
    @Path("{pid}/delete_clip_item")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeClipItem(@PathParam("pid") String collectionPid, String itemJsonObj) {
        try {
            JSONObject json = new JSONObject(itemJsonObj);
            CutItem clipItem = CutItem.fromJSONObject(json);
            if (clipItem == null) {
                throw new BadRequestException("badREquest");
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

            return akubraRepository.doWithWriteLock(collectionPid, () -> {
                try {
                    JSONArray jsonArray = new JSONArray();
                    if (akubraRepository.datastreamExists(collectionPid, COLLECTION_CLIPS)) {
                        try (InputStream latestVersionOfDatastream = akubraRepository.getDatastreamContent(collectionPid, COLLECTION_CLIPS).asInputStream()) {
                            jsonArray = new JSONArray(IOUtils.toString(latestVersionOfDatastream, "UTF-8"));
                        }
                    }

                    int index = -1;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        CutItem rawCutItem = CutItem.fromJSONObject(jsonArray.getJSONObject(i));
                        if (clipItem.equals(rawCutItem)) {
                            index = i;
                            break;
                        }
                    }

                    if (index > -1) {
                        if (clipItem.getUrl() != null) {
                            String thumbName = clipItem.getThumbnailmd5();
                            if (akubraRepository.datastreamExists(collectionPid, thumbName)) {
                                akubraRepository.deleteDatastream(collectionPid, thumbName);
                            }

                        }

                        jsonArray.remove(index);

                        JSONArray finalJsonArray = jsonArray;
                        ByteArrayInputStream bis = new ByteArrayInputStream(finalJsonArray.toString().getBytes(Charset.forName("UTF-8")));
                        akubraRepository.updateManagedDatastream(collectionPid, COLLECTION_CLIPS, "application/json", bis);
                        Collection collection = fetchCollectionFromRepository(collectionPid, true, true);
                        return Response.ok(collection.toJson()).build();

                    } else {
                        return Response.status(Status.BAD_REQUEST).build();
                    }
                } catch (IOException | SolrServerException | NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
            });

        } catch (WebApplicationException e) {
            throw e;
        } catch (DistributedLocksException e) {
            // TODO AK_NEW
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }


    @PUT
    @Path("{collectionPid}/delete_batch_clipitems")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeClipItemsBatch(@PathParam("collectionPid") String collectionPid, String stringBatch) {
        try {
            JSONObject batch = new JSONObject(stringBatch);
            JSONArray batchArray = batch.optJSONArray("clipitems");

            checkSupportedObjectPid(collectionPid);
            if (!isModelCollection(collectionPid)) {
                throw new ForbiddenException("not a collection: " + collectionPid);
            }
            User user = this.userProvider.get();
            if (!permitCollectionEdit(this.rightsResolver, user, collectionPid)) {
                throw new ForbiddenException("user '%s' is not allowed to modify collection (missing action '%s')", user.getLoginname(), SecuredActions.A_COLLECTIONS_EDIT); //403
            }
            if (batchArray != null && batchArray.length() > 0) {
                return akubraRepository.doWithWriteLock(collectionPid, () -> {
                    try {
                        boolean cuttingsModified = false;
                        Set<String> thumbsToDelete = new LinkedHashSet<>();
                        JSONArray fetchedJSONArray = new JSONArray();
                        if (akubraRepository.datastreamExists(collectionPid, COLLECTION_CLIPS)) {
                            try (InputStream latestVersionOfDatastream = akubraRepository.getDatastreamContent(collectionPid, COLLECTION_CLIPS).asInputStream()) {
                                fetchedJSONArray = new JSONArray(IOUtils.toString(latestVersionOfDatastream, "UTF-8"));
                            }
                        }

                        for (int i = 0; i < batchArray.length(); i++) {
                            CutItem toDelete = CutItem.fromJSONObject(batchArray.getJSONObject(i));
                            if (toDelete == null) {
                                throw new BadRequestException("badREquest");
                            }

                            int index = -1;
                            for (int j = 0; j < fetchedJSONArray.length(); j++) {
                                CutItem rawCutItem = CutItem.fromJSONObject(fetchedJSONArray.getJSONObject(j));
                                if (toDelete.equals(rawCutItem)) {
                                    index = j;
                                    cuttingsModified = true;
                                    break;
                                }
                            }

                            if (index > -1) {
                                if (toDelete.getUrl() != null) {
                                    String thumbName = toDelete.getThumbnailmd5();
                                    thumbsToDelete.add(thumbName);
                                }
                                fetchedJSONArray.remove(index);

                            } else {
                                return Response.status(Status.BAD_REQUEST).build();
                            }
                        }
                        if (cuttingsModified) {
                            JSONArray finalFetchedJSONArray = fetchedJSONArray;
                            ByteArrayInputStream bis = new ByteArrayInputStream(finalFetchedJSONArray.toString().getBytes(Charset.forName("UTF-8")));
                            akubraRepository.updateManagedDatastream(collectionPid, COLLECTION_CLIPS, "application/json", bis);
                            for (String thumbName : thumbsToDelete) {
                                if (akubraRepository.datastreamExists(collectionPid, thumbName)) {
                                    akubraRepository.deleteDatastream(collectionPid, thumbName);
                                }
                            }
                        }
                        Collection collection = fetchCollectionFromRepository(collectionPid, true, true);
                        return Response.ok(collection.toJson()).build();
                    } catch (IOException | SolrServerException | NoSuchAlgorithmException e) {
                        throw new RuntimeException(e);
                    }
                });
            } else {
                throw new BadRequestException("badREquest");
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (DistributedLocksException e) {
            // TODO AK_NEW
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @POST
    @Path("{pid}/add_clip_item")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addClipItem(@PathParam("pid") String collectionPid, String itemJsonObj) {
        try {
            JSONObject json = new JSONObject(itemJsonObj);
            CutItem clipItem = CutItem.fromJSONObject(json);
            if (clipItem == null) {
                throw new BadRequestException("badREquest");
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

            akubraRepository.doWithWriteLock(collectionPid, () -> {
                try {
                    JSONArray jsonArray = new JSONArray();
                    if (akubraRepository.datastreamExists(collectionPid, COLLECTION_CLIPS)) {
                        try (InputStream latestVersionOfDatastream = akubraRepository.getDatastreamContent(collectionPid, COLLECTION_CLIPS).asInputStream()) {
                            jsonArray = new JSONArray(IOUtils.toString(latestVersionOfDatastream, "UTF-8"));
                        }
                    }

                    if (clipItem.getUrl() != null) {
                        String url = clipItem.getUrl();
                        String thumbName = clipItem.getThumbnailmd5();

                        THUMBS_GENERATOR.forEach(gen -> {
                            if (gen.acceptUrl(url)) {
                                try {
                                    BufferedImage thumb = gen.generateThumbnail(url);
                                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                    ImageIO.write(thumb, "png", bos);

                                    ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
                                    akubraRepository.updateManagedDatastream(collectionPid, thumbName, "image/png", bis);
                                } catch (IOException e) {
                                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                                }
                            }
                        });
                    }
                    jsonArray.put(json);

                    JSONArray finalJsonArray = jsonArray;
                    ByteArrayInputStream bis = new ByteArrayInputStream(finalJsonArray.toString().getBytes(Charset.forName("UTF-8")));
                    akubraRepository.updateManagedDatastream(collectionPid, COLLECTION_CLIPS, "application/json", bis);
                    return null;
                } catch (IOException | NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
            });

            Collection collection = fetchCollectionFromRepository(collectionPid, true, true);
            return Response.ok(collection.toJson()).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (DistributedLocksException e) {
            // TODO AK_NEW
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    private static LocalDateTime convertToLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    // Udelat castecne ulozene na CDK
    private Collection fetchCollectionFromRepository(String pid, boolean withContent, boolean withItems) throws
            IOException, RepositoryException, SolrServerException {


        Collection collection = new Collection();
        collection.pid = pid;
        //timestamps from Foxml properties

        ObjectProperties objectProperties = akubraRepository.getProperties(pid);
        if (objectProperties != null) {
            collection.created = convertToLocalDateTime(objectProperties.getPropertyCreated());
            collection.modified = convertToLocalDateTime(objectProperties.getPropertyLastModified());
        }

        //data from MODS
        Document mods = akubraRepository.getDatastreamContent(pid, KnownDatastreams.BIBLIO_MODS).asDom4j(false);
        collection.nameUndefined = Dom4jUtils.stringOrNullFromFirstElementByXpath(mods.getRootElement(), "//mods/titleInfo[not(@lang)]/title");

        Iso639Converter converter = new Iso639Converter();
        // all other languages
        List<Element> titlesByXPath = Dom4jUtils.elementsByXpath(mods.getRootElement(), "//mods/titleInfo[@lang]");
        for (Element titleInfo : titlesByXPath) {
            Attribute lang = titleInfo.attribute("lang");
            String title = Dom4jUtils.stringOrNullFromFirstElementByXpath(titleInfo, "title");
            collection.names.put(lang.getValue(), title);

            if (converter.isConvertable(lang.getValue())) {
                List<String> converted = converter.convert(lang.getValue());
                converted.forEach(cKey -> {
                    collection.names.put(cKey, title);
                });
            }
        }


        collection.descriptionUndefined = Dom4jUtils.stringOrNullFromFirstElementByXpath(mods.getRootElement(), "//mods/abstract[not(@lang)]");

        List<Element> descriptionsByXPath = Dom4jUtils.elementsByXpath(mods.getRootElement(), "//mods/abstract[@lang]");
        for (Element desc : descriptionsByXPath) {
            Attribute lang = desc.attribute("lang");
            String d = desc.getTextTrim();
            collection.descriptions.put(lang.getValue(), d);

            if (converter.isConvertable(lang.getValue())) {
                List<String> converted = converter.convert(lang.getValue());
                converted.forEach(cKey -> {
                    collection.descriptions.put(cKey, d);

                });
            }
        }

        if (withContent) {
            String contentHtmlCzEscapedNoLang = Dom4jUtils.stringOrNullFromFirstElementByXpath(mods.getRootElement(), "//mods/note[not(@lang)]");
            if (contentHtmlCzEscapedNoLang != null) {
                collection.contentUndefined = StringEscapeUtils.unescapeHtml(contentHtmlCzEscapedNoLang);
            }

            List<Element> notesByXPath = Dom4jUtils.elementsByXpath(mods.getRootElement(), "//mods/note[@lang]");
            for (Element note : notesByXPath) {
                Attribute lang = note.attribute("lang");
                String d = note.getTextTrim();
                String escaped = StringEscapeUtils.unescapeHtml(d);
                collection.contents.put(lang.getValue(), escaped);

                if (converter.isConvertable(lang.getValue())) {
                    List<String> converted = converter.convert(lang.getValue());
                    converted.forEach(cKey -> {
                        collection.contents.put(cKey, escaped);
                    });
                }
            }
        }
        List<Element> subjectXPath = Dom4jUtils.elementsByXpath(mods.getRootElement(), "//mods/subject[@lang]");
        for (Element s : subjectXPath) {
            Attribute langAttr = s.attribute("lang");
            List<Element> elms = s.elements();
            List<String> texts = s.elements().stream().map(Element::getTextTrim).collect(Collectors.toList());
            if (!collection.keywords.containsKey(langAttr.getStringValue())) {
                collection.keywords.put(langAttr.getStringValue(), new ArrayList<>());
            }
            collection.keywords.get(langAttr.getStringValue()).addAll(texts);
        }


        Element authorsXPath = Dom4jUtils.firstElementByXpath(mods.getRootElement(), "//mods/name[@type='personal']");
        if (authorsXPath != null) {
            String author = authorsXPath.elements().stream().map(Element::getTextTrim).collect(Collectors.joining(" "));
            if (StringUtils.isAnyString(author)) {
                collection.author = author;
            }
        }

        //data from RELS-EXT
        Document relsExt = akubraRepository.re().get(pid).asDom4j(false);
        collection.standalone = Boolean.valueOf(Dom4jUtils.stringOrNullFromFirstElementByXpath(relsExt.getRootElement(), "//standalone"));

        List<ProcessingIndexItem> items = akubraRepository.pi().getChildren(KnownRelations.CONTAINS.toString(), pid);
        List<String> pids = new ArrayList<>();
        for (ProcessingIndexItem item : items) {
            pids.add(item.targetPid());
        }
        if (withItems) {
            collection.items = pids;
        }

        List<String> streams = akubraRepository.getDatastreamNames(pid);
        if (streams.contains(KnownDatastreams.IMG_THUMB)) {
            collection.thumbnailInfo = ThumbnailbStateEnum.thumb;
        } else if (items.size() > 0) {
            collection.thumbnailInfo = ThumbnailbStateEnum.content;
        } else {
            collection.thumbnailInfo = ThumbnailbStateEnum.none;
        }


        if (akubraRepository.datastreamExists(pid, COLLECTION_CLIPS)) {

            try (InputStream latestVersionOfDatastream = akubraRepository.getDatastreamContent(pid, COLLECTION_CLIPS).asInputStream()) {
                JSONArray jsonArray = new JSONArray(IOUtils.toString(latestVersionOfDatastream, "UTF-8"));
                collection.clippingItems = CutItem.fromJSONArray(jsonArray);
                collection.clippingItems.forEach(cl -> {
                    try {
                        cl.initGeneratedThumbnail(akubraRepository, pid);
                    } catch (NoSuchAlgorithmException | IOException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    }
                });
            }
        } else {
            collection.clippingItems = new ArrayList<>();
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
        if (pidPaths.length > 0) {
            for (ObjectPidsPath objectPidsPath : pidPaths) {
                boolean permited = user != null ? rightsResolver.isActionAllowed(user, SecuredActions.A_ABLE_TOBE_PART_OF_COLLECTION.getFormalName(), pid, null, objectPidsPath).flag() : false;
                if (permited) return permited;
            }
            return false;
        } else {
            boolean permited = user != null ? rightsResolver.isActionAllowed(user, SecuredActions.A_ABLE_TOBE_PART_OF_COLLECTION.getFormalName(), pid, null, ObjectPidsPath.REPOSITORY_PATH).flag() : false;
            if (permited) return permited;
        }
        return false;
    }
}
