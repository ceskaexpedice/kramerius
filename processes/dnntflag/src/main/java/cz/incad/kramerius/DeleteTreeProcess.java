package cz.incad.kramerius;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;

import com.google.inject.name.Names;
import cz.incad.kramerius.ProcessHelper.PidsOfDescendantsProducer;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.impl.SolrAccessImplNewIndex;
import cz.incad.kramerius.processes.WarningException;
import cz.incad.kramerius.processes.starter.ProcessStarter;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;
import cz.kramerius.searchIndex.indexer.SolrConfig;
import cz.kramerius.searchIndex.indexer.SolrIndexAccess;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.RepositoryException;
import org.ceskaexpedice.akubra.processingindex.ConflictingOwnedAndFosteredParents;
import org.ceskaexpedice.akubra.processingindex.OwnedAndFosteredChildren;
import org.ceskaexpedice.akubra.processingindex.OwnedAndFosteredParents;
import org.ceskaexpedice.akubra.processingindex.ProcessingIndexItem;
import org.ceskaexpedice.akubra.utils.Dom4jUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Deklarace procesu je v shared/common/src/main/java/cz/incad/kramerius/processes/res/lp.st (delete_tree)
 * Nahrazuje cz.incad.kramerius.service.impl.DeleteServiceImpl.main()
 */
public class DeleteTreeProcess {
    //TODO: move from here, but not to module shared (circular dependency because of ResourceIndexImplByKrameriusNewApis)

    public static final Logger LOGGER = Logger.getLogger(DeleteTreeProcess.class.getName());

    private static final String SOLR_FIELD_IN_COLLECTIONS = "in_collections";
    private static final String SOLR_FIELD_IN_COLLECTIONS_DIRECT = "in_collections.direct";

    private static final boolean DRY_RUN = false;

    /**
     * args[0] - authToken
     * args[1] - pid of root object, for example "uuid:df693396-9d3f-4b3b-bf27-3be0aaa2aadf"
     * args[2-...] - optional title of the root object
     */


    public static void main(String[] args) throws IOException, SolrServerException {

        if (args.length < 2) {
            throw new RuntimeException("Not enough arguments.");
        }
        LOGGER.log(Level.INFO, "Process parameters {0}", Arrays.asList(args));

        int argsIndex = 0;
        //token for keeping possible following processes in same batch
        String authToken = args[argsIndex++]; //auth token always first, but still suboptimal solution, best would be if it was outside the scope of this as if ProcessHelper.scheduleProcess() similarly to changing name (ProcessStarter)
        //process params
        String pid = args[argsIndex++];
        String title = ProcessHelper.shortenIfTooLong(ProcessHelper.mergeArraysEnd(args, argsIndex++), 256);
        //String scopeDesc = scope == SetPolicyProcess.Scope.OBJECT ? "jen objekt" : "objekt včetně potomků";
        ProcessStarter.updateName(title != null
                ? String.format("Smazání stromu %s (%s)", title, pid)
                : String.format("Smazání stromu %s", pid)
        );

        boolean ignoreIncosistencies = false;

        if (args.length > 3) {
            ignoreIncosistencies = Boolean.parseBoolean(args[argsIndex++]);
        }


        Injector injector = Guice.createInjector(new SolrModule(), new RepoModule(), new NullStatisticsModule());
        AkubraRepository akubraRepository = injector.getInstance(Key.get(AkubraRepository.class));

        SolrAccess searchIndex = injector.getInstance(Key.get(SolrAccess.class, Names.named("new-index")));
        SolrIndexAccess indexerAccess = new SolrIndexAccess(new SolrConfig());
        // ProcessingIndex processingIndex = new ProcessingIndexImplByKrameriusNewApis(akubraRepository, ProcessUtils.getCoreBaseUrl());
        //check object exists in repository
        // pokud je 
        //boolean existsInProcessingIndex = processingIndex.existsPid(pid);

        if (!akubraRepository.exists(pid) && !ignoreIncosistencies) {
            throw new RuntimeException(String.format("object %s not found in repository", pid));
        }

        try {
            boolean noErrors = deleteTree(pid, true, akubraRepository, akubraRepository, indexerAccess, searchIndex, ignoreIncosistencies);
            if (!noErrors) {
                throw new WarningException("failed to delete some objects");
            }
        }finally {
            akubraRepository.shutdown();
        }
    }

    public static boolean deleteTree(String pid, boolean deletionRoot, AkubraRepository repository, AkubraRepository akubraRepository, SolrIndexAccess indexerAccess, SolrAccess searchIndex, boolean ignoreIncosistencies) throws SolrServerException, IOException {
        LOGGER.info(String.format("deleting own tree of %s", pid));
        boolean someProblem = false;
        //
        boolean skipP = ignoreIncosistencies && !repository.exists(pid);

        String myModel = "";
        if (!skipP) {
            //1. potomci
            OwnedAndFosteredChildren pidsOfChildren = akubraRepository.pi().getOwnedAndFosteredChildren(pid);
            //1.a. smaz vlastni potomky
            for (ProcessingIndexItem ownChild : pidsOfChildren.own()) {
                someProblem &= deleteTree(ownChild.targetPid(), false, repository, akubraRepository, indexerAccess, searchIndex, ignoreIncosistencies);
            }

            //1.b. pokud jsem sbirka a mam nevlastni potomky, odeber celym jejich stromum nalezitost do sbirky (mne) ve vyhledavacim indexu
            myModel = akubraRepository.pi().getModel(pid);
            if ("collection".equals(myModel) && !pidsOfChildren.foster().isEmpty()) {
                LOGGER.info(String.format("object %s is collection and not empty, removing items from the collection", pid));
                for (ProcessingIndexItem fosterChild : pidsOfChildren.foster()) {
                    removeItemsFromCollectionBeforeDeletingCollection(pid, fosterChild.targetPid(), searchIndex, indexerAccess);
                }
            }

            try{

                //2. předci
                OwnedAndFosteredParents pidsOfParents = akubraRepository.pi().getOwnedAndFosteredParents(pid);
                //2.a. pokud jsem deletionRoot, smaz rels-ext vazbu na me z vlastniho rodice (pokud existuje)
                if (deletionRoot && pidsOfParents.own() != null) {
                    deleteRelationFromOwnParent(pid, pidsOfParents.own().source(), repository);
                }
                //2.a. smaz rels-ext vazby na me ze vsech nevlastnich rodicu
                for (ProcessingIndexItem fosterParent : pidsOfParents.foster()) {
                    deleteRelationFromForsterParent(pid, fosterParent.source(), repository);
                }

                //3. pokud mazany objekt ma licenci, aktualizovat predky (rels-ext:containsLicense a solr:contains_licenses)
                updateLicenseFlagsForAncestors(pid, akubraRepository, indexerAccess);

            }catch (RepositoryException re) {

                if (ignoreIncosistencies) {
                    try {
                        ConflictingOwnedAndFosteredParents conflicts = akubraRepository.pi().getConflictingOwnerAndFosteredParents(pid);
                        String header = String.format("Object \"%s\" has multiple own parents via:", pid);
                        String firstRelation = String.format("\n\t - " + conflicts.ownItems().stream().map(ProcessingIndexItem::pid).collect(Collectors.joining(";")));
                        LOGGER.severe(header+firstRelation);

                        for (ProcessingIndexItem own : conflicts.ownItems()) {
                            //2. předci
                            //2.a. pokud jsem deletionRoot, smaz rels-ext vazbu na me z vlastniho rodice (pokud existuje)
                            if (deletionRoot && own.source() != null) {
                                deleteRelationFromOwnParent(pid, own.source(), repository);
                            }
                        }

                        //2.a. smaz rels-ext vazby na me ze vsech nevlastnich rodicu
                        for (ProcessingIndexItem fosterParent : conflicts.foster()) {
                            deleteRelationFromForsterParent(pid, fosterParent.source(), repository);
                        }

                        //3. pokud mazany objekt ma licenci, aktualizovat predky (rels-ext:containsLicense a solr:contains_licenses)
                        updateLicenseFlagsForAncestors(pid, akubraRepository, indexerAccess);
                    } catch(Throwable ex) {
                        if (ignoreIncosistencies) {
                            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                        } else {
                            throw ex;
                        }
                        someProblem =true;
                    }
                } else {
                    throw re;
                }
            } catch (Exception ex) {
                if (ignoreIncosistencies) {
                    LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                } else {
                    throw ex;
                }
                someProblem = true;
            }

        } else {
            LOGGER.warning(String.format("object %s is not found in repository, skipping", pid));
        }

        //4. smaz me z repozitare i vyhledavaciho indexu
        try {
            deleteObject(pid, "collection".equals(myModel), repository, indexerAccess, ignoreIncosistencies);
        } catch(Throwable ex) {
            if (ignoreIncosistencies) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            } else {
                throw ex;
            }
            someProblem = true;
        }
        return !someProblem;
    }

    private static void updateLicenseFlagsForAncestors(String pid, AkubraRepository akubraRepository, SolrIndexAccess indexerAccess) throws IOException {
        boolean readableAndParsable = isReadableAndParsable(pid, akubraRepository, true);
        // TODO: Plan reindex of parents
        if (!readableAndParsable) {
            LOGGER.warning(String.format("object %s is not readable or parsable, skipping updating license flags for ancestors", pid));
            return;
        }
        List<String> licences = LicenseHelper.getLicensesByRelsExt(pid, akubraRepository);
        for (String license : licences) {
            //Z rels-ext vsech (vlastnich) predku se odebere containsLicence=L, pokud tam je.
            //A pokud neexistuje jiny zdroj pro licenci (jiny potomek predka, ktery ma rels-ext:containsLicense kvuli jineho objektu, nez targetPid)
            LOGGER.info("updating RELS-EXT record of all (own) ancestors (without another source of license) of the target object " + pid);
            List<String> pidsOfAncestorsWithoutAnotherSourceOfLicense = LicenseHelper.getPidsOfOwnAncestorsWithoutAnotherSourceOfLicense(pid, akubraRepository, license);
            for (String ancestorPid : pidsOfAncestorsWithoutAnotherSourceOfLicense) {
                if (!DRY_RUN) {
                    LicenseHelper.removeRelsExtRelationAfterNormalization(ancestorPid, LicenseHelper.RELS_EXT_RELATION_CONTAINS_LICENSE, LicenseHelper.RELS_EXT_RELATION_CONTAINS_LICENSE_DEPRECATED, license, akubraRepository);
                }
            }
            //Aktualizuje se index predku, kteri nemaji jiny zdroj licence (odebere se contains_licenses=L) atomic updatem
            LOGGER.info("updating search index of all (own) ancestors without another source of license");
            if (!DRY_RUN) {
                indexerAccess.removeSingleFieldValueFromMultipleObjects(pidsOfAncestorsWithoutAnotherSourceOfLicense, LicenseHelper.SOLR_FIELD_CONTAINS_LICENSES, license, false, false);
            }
        }
    }

    /**
     * no need to delete rels-ext relation, because that is in Collection object that will be soon deleted
     * so we're only synchronizing search index by:
     * - removing 'in_collection.direct:collectionPid' from item in collection and
     * - removing 'in_collection:collectionPid' from everything within tree of item in collection (including item itself)
     * Implemented efficiently with atomic updates over batches of pids
     *
     * @param collectionPid
     * @param itemInCollection
     * @param searchIndex
     * @param indexerAccess
     */
    private static void removeItemsFromCollectionBeforeDeletingCollection(String collectionPid, String itemInCollection, SolrAccess searchIndex, SolrIndexAccess indexerAccess) {
        LOGGER.info(String.format("removing tree from collection: collection %s, tree with root %s", collectionPid, itemInCollection));
        //item itself
        List<String> itemInCollectionOnly = new ArrayList<>();
        itemInCollectionOnly.add(itemInCollection);

        indexerAccess.removeSingleFieldValueFromMultipleObjects(itemInCollectionOnly, SOLR_FIELD_IN_COLLECTIONS, collectionPid, false, false);
        indexerAccess.removeSingleFieldValueFromMultipleObjects(itemInCollectionOnly, SOLR_FIELD_IN_COLLECTIONS_DIRECT, collectionPid, false, false);
        //rest of the tree
        PidsOfDescendantsProducer iterator = new PidsOfDescendantsProducer(itemInCollection, searchIndex, false);
        while (iterator.hasNext()) {
            List<String> pids = iterator.next();
            indexerAccess.removeSingleFieldValueFromMultipleObjects(pids, SOLR_FIELD_IN_COLLECTIONS, collectionPid, false, false);
            LOGGER.info(String.format("removed from collection: %d/%d", iterator.getReturned() + 1, iterator.getTotal() + 1));
        }
    }

    private static void deleteObject(String pid, boolean isCollection, AkubraRepository repository, SolrIndexAccess indexerAccess, boolean ignoreIncosistencies) throws SolrServerException, IOException {
        LOGGER.info(String.format("deleting object %s", pid));
        LOGGER.info(String.format("deleting %s from repository", pid));
        if (!DRY_RUN) {
            String tilesUrl = null;

            AtomicReference<Boolean> readableAndParsableRef = new AtomicReference<>(false);

            if (repository.exists(pid)) {
                readableAndParsableRef.set(isReadableAndParsable(pid, repository, ignoreIncosistencies));
                if (readableAndParsableRef.get()) {
                    try {
                        tilesUrl = repository.re().getTilesUrl(pid);
                    } catch (RepositoryException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    }
                }


                repository.doWithWriteLock(pid, () -> {
                    // managed streams NOT deleted for collections (IMG_THUMB are referenced from other objects - pages)
                    if (readableAndParsableRef.get()) {
                        repository.delete(pid, !isCollection, true);
                    } else {
                        //TODO: if object is corrupted, streams are not accessible -> cannot be deleted
                        repository.delete(pid, false, true);
                    }
                    repository.pi().commit();
                    return null;
                });
            }
            if (tilesUrl != null) {
                boolean deleteFromImageServer = KConfiguration.getInstance().getConfiguration().getBoolean("delete.fromImageServer", false);
                String imageDir = KConfiguration.getInstance().getConfiguration().getString("convert.imageServerDirectory");
                String serverTilesPrefix = KConfiguration.getInstance().getConfiguration().getString("convert.imageServerTilesURLPrefix");
                if (deleteFromImageServer) {
                    deleteFileFromIIP(tilesUrl, serverTilesPrefix, imageDir);
                }
            }
        }
        LOGGER.info(String.format("deleting %s from search index", pid));
        if (!DRY_RUN) {
            ifPDFDeleteVirtualPages(pid,indexerAccess);
            LOGGER.info(String.format("deleting %s from search index", pid));
            indexerAccess.deleteById(pid);
        }
    }

    private static boolean isReadableAndParsable(String pid, AkubraRepository repository, boolean ignoreIncosistencies) {
        boolean readableAndParsable = false;
        try {
            // readable and parsable
            repository.get(pid).asDom(false);
            readableAndParsable = true;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
            if (!ignoreIncosistencies) {
                throw new RuntimeException(e);
            }
        }
        return readableAndParsable;
    }

    private static void ifPDFDeleteVirtualPages(String pid, SolrIndexAccess indexerAccess)throws IOException, SolrServerException{
        SolrDocument doc = indexerAccess.getObjectByPid(pid);
        if (doc != null && doc.containsKey("ds.img_full.mime")) {
            String valString = (String) doc.getFieldValue("ds.img_full.mime");
            LOGGER.info(String.format("ValString: %s", valString));
            if (valString.equals("application/pdf")) {//Also delete virtual pages
                LOGGER.info(String.format("deleting %s from search index by root ID", pid));
                indexerAccess.deleteByParentRootPid(pid);
            }
        }
    }

    public static void deleteFileFromIIP(String tilesUrl, String imageTilesUrlPrefix, String imageDir) throws MalformedURLException {
        int indexOf = tilesUrl.indexOf(imageTilesUrlPrefix);
        if (indexOf >= 0) {
            String endPath = tilesUrl.substring(indexOf + imageTilesUrlPrefix.length());

            List<File> filesToDelete = new ArrayList<>();
            File realPath = new File(new File(imageDir), endPath);
            String compareName = realPath.getName().contains(".") ? realPath.getName().substring(0, realPath.getName().indexOf(".")) : realPath.getName();
            File parentFile = realPath.getParentFile();
            File[] listFiles = parentFile.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    String name = pathname.getName();
                    return (name.contains(compareName));
                }
            });

            if (listFiles != null) {
                Arrays.asList(listFiles).forEach(filesToDelete::add);
            }

            File[] filesArray = filesToDelete.toArray(new File[filesToDelete.size()]);
            for (int i = 0, ll = filesArray.length; i < ll; i++) {
                File deletingFile = filesArray[i];
                if (deletingFile.exists() && deletingFile.isFile()) {
                    File parentFolder = deletingFile.getParentFile();
                    if (parentFile.listFiles() != null && parentFile.listFiles().length == 1) {
                        filesToDelete.add(parentFolder);
                    }
                }
            }
            LOGGER.info("Deleting files: " + filesToDelete.stream().map(File::getAbsolutePath).collect(Collectors.joining(", ")));
            if (!filesToDelete.isEmpty()) {
                filesToDelete.forEach(File::delete);
            }
        }
    }


    private static void deleteRelationFromForsterParent(String pid, String fosterParentPid, AkubraRepository repository) {
        LOGGER.info(String.format("removing foster-parent relation %s -> %s", fosterParentPid, pid));
        removeAnyRelsExtRelation(fosterParentPid, pid, repository);
    }

    private static void deleteRelationFromOwnParent(String pid, String ownParentPid, AkubraRepository repository) {
        LOGGER.info(String.format("removing own-parent relationship %s -> %s", ownParentPid, pid));
        removeAnyRelsExtRelation(ownParentPid, pid, repository);
    }


    private static boolean removeAnyRelsExtRelation(String srcPid, String targetPid, AkubraRepository repository) {
        return repository.doWithWriteLock(srcPid, () -> {
            if (!repository.re().exists(srcPid)) {
                throw new RepositoryException("RDF record (datastream RELS-EXT) not found for " + srcPid);
            }
            Document relsExt = repository.re().get(srcPid).asDom4j(true);
            Element rootEl = (Element) Dom4jUtils.buildXpath("/rdf:RDF/rdf:Description").selectSingleNode(relsExt);
            boolean relsExtNeedsToBeUpdated = false;

            //remove relation if found
            List<Node> relationEls = Dom4jUtils.buildXpath("rel:*/@rdf:resource").selectNodes(rootEl);
            for (Node relationEl : relationEls) {
                String content = relationEl.getText();
                String relationElementName = relationEl.getParent().getName();
                if (content.equals("info:fedora/" + targetPid)) {
                    LOGGER.info(String.format("removing relation '%s %s' from RELS-EXT of %s", relationElementName, targetPid, srcPid));
                    relationEl.detach();
                    relsExtNeedsToBeUpdated = true;
                }
            }

            //update RELS-EXT in repository if there was a change
            if (relsExtNeedsToBeUpdated) {
                if (!DRY_RUN) {
                    ByteArrayInputStream bis = new ByteArrayInputStream(relsExt.asXML().getBytes(Charset.forName("UTF-8")));
                    repository.re().update(srcPid, bis);
                }
                LOGGER.info(String.format("RELS-EXT of %s has been updated", srcPid));
            }
            return relsExtNeedsToBeUpdated;
        });
    }
}
