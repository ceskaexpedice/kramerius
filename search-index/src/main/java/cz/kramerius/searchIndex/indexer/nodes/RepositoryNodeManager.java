package cz.kramerius.searchIndex.indexer.nodes;

import cz.kramerius.searchIndex.indexer.conversions.extraction.*;
import cz.kramerius.shared.AuthorInfo;
import cz.kramerius.shared.DateInfo;
import cz.kramerius.shared.Title;
import org.apache.commons.lang3.tuple.Pair;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.KnownDatastreams;
import org.ceskaexpedice.akubra.utils.ProcessingIndexUtils;
import org.dom4j.Document;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RepositoryNodeManager {

    private final AkubraRepository akubraRepository;
    private final LRUCache<String, RepositoryNode> nodesByPid = new LRUCache<>(1024);
    private final boolean surviveInconsistentObjects;

    public RepositoryNodeManager(AkubraRepository akubraRepository, boolean surviveInconsistentObjects) {
        this.akubraRepository = akubraRepository;
        this.surviveInconsistentObjects = surviveInconsistentObjects;
    }

    public RepositoryNode getKrameriusNode(String pid) {
        try {
            return getKrameriusNodeWithCycleDetection(pid, new ArrayList<>());
        } catch (RuntimeException e) {
            e.printStackTrace();
            if (surviveInconsistentObjects) {
                //e.printStackTrace();
                System.err.println(e.getMessage());
                return null;
            } else {
                throw e;
            }
        }
    }

    private RepositoryNode getKrameriusNodeWithCycleDetection(String pid, List<String> path) {
        //http://admin.k7-test.mzk.cz/processes/8410 and uuid:51f84b60-5542-11e9-8854-005056827e51
        if (nodesByPid.containsKey(pid)) {
            return nodesByPid.get(pid);
        } else {
            if (path.contains(pid)) {
                throw new RuntimeException("parent cycle detected: " + buildPath(pid, path));
            }
            path.add(pid);
            RepositoryNode node = buildKrameriusNodeFromRepository(pid, path);
            if (node != null) {
                this.nodesByPid.put(pid, node);
            }
            return node;
        }
    }

    private String buildPath(String pid, List<String> path) {
        String result = "";
        boolean found = false;
        for (String item : path) {
            if (item.equals(pid)) {
                found = true;
            }
            if (found) {
                result += item + " -> ";
            }
        }
        result += pid;
        return result;
    }

    private RepositoryNode buildKrameriusNodeFromRepository(String pid, List<String> path) {
        try {
            if (!akubraRepository.objectExists(pid)) {
                return null;
            }
            //System.out.println("building node for " + pid);
            Pair<String, Set<String>> parents = ProcessingIndexUtils.getPidsOfParents(pid, akubraRepository);
            //process parents first
            RepositoryNode ownParent = parents.getLeft() == null ? null : getKrameriusNodeWithCycleDetection(parents.getLeft(), path);
            List<RepositoryNode> fosterParents = new ArrayList<>();
            for (String fosterParent : parents.getRight()) {
                fosterParents.add(getKrameriusNodeWithCycleDetection(fosterParent, path));
            }

            Document relsExtDoc = akubraRepository.getDatastreamContent(pid, KnownDatastreams.RELS_EXT).asDom4j(false);
            //String model = KrameriusRepositoryUtils.extractKrameriusModelName(relsExtDoc);
            String model = ProcessingIndexUtils.getModel(pid, akubraRepository);
            List<String> ownChildren = null;
            List<String> fosterChildren = null;
            if (!"page".equals(model) && !"track".equals(model)) { //just optimization, pages and tracks never have children
                //Pair<List<String>, List<String>> children = KrameriusRepositoryUtils.extractChildren(relsExtDoc);
                Pair<List<String>, List<String>> children = ProcessingIndexUtils.getPidsOfChildren(pid, akubraRepository);
                ownChildren = children.getLeft();
                fosterChildren = children.getRight();
            }
            //System.out.println("own children: " + (ownChildren == null? null : ownChildren.size()));
            Document modsDoc = akubraRepository.getDatastreamContent(pid, KnownDatastreams.BIBLIO_MODS).asDom4j(false);
            if (modsDoc == null) {
                throw new RuntimeException("missing MODS");
            }
            Title title = extractTitleFromMods(model, modsDoc);

            //data from parents
            DateInfo myDateInfo = extractDateInfoFromMods(model, modsDoc, pid);
            DateInfo dateInfo = mergeDateInfos(ownParent, myDateInfo);
            List<String> myLanguages = extractLanguagesFromMods(model, modsDoc);
            List<String> languages = mergeLanguages(ownParent, fosterParents, myLanguages);
            List<AuthorInfo> myPrimaryAuthors = extractPrimaryAuthorsFromMods(model, modsDoc);
            List<AuthorInfo> myOtherAuthors = extractNonPrimaryAuthorsFromMods(model, modsDoc);
            List<AuthorInfo> primaryAuthors = mergePrimaryAuthors(ownParent, fosterParents, myPrimaryAuthors);
            List<AuthorInfo> otherAuthors = mergeOtherAuthors(ownParent, fosterParents, myOtherAuthors);
            List<String> myLicences = extractLicenses(model, relsExtDoc);
            List<String> licencesOfOwnAncestors = getLicensesFromAllAncestors(ownParent, fosterParents);

            //pids of all foster parents
            List<String> fosterParentsPids = toPidList(fosterParents);
            //pids of foster parents that are collections
            List<String> fosterParentsOfTypeCollectionPids = new ArrayList<>();
            for (RepositoryNode fosterParentNode : fosterParents) {
                if (fosterParentNode.getModel().equals("collection")) {
                    fosterParentsOfTypeCollectionPids.add(fosterParentNode.getPid());
                }
            }
            //pids of all ancestor with model:collection
            List<String> anyAncestorsOfTypeCollectionPids = new ArrayList<>();
            anyAncestorsOfTypeCollectionPids.addAll(fosterParentsOfTypeCollectionPids);
            if (ownParent != null) {
                anyAncestorsOfTypeCollectionPids.addAll(ownParent.getPidsOfAnyAncestorsOfTypeCollection());
            }
            for (RepositoryNode fosterParentNode : fosterParents) {
                anyAncestorsOfTypeCollectionPids.addAll(fosterParentNode.getPidsOfAnyAncestorsOfTypeCollection());
            }

            String pidPath = ownParent == null ? pid : ownParent.getPidPath() + "/" + pid;
            String modelPath = ownParent == null ? model : ownParent.getModelPath() + "/" + model;
            List<String> allPathsThroughAllParents = new ArrayList<>();
            if (ownParent == null) { //nema vlastniho rodice, tj. jedina cesta ve vlastim strome: PID
                allPathsThroughAllParents.add(pid);
            } else {
                for (String anyPathToOwnParent : ownParent.getAllPidPathsThroughAllParents()) { //vsechno, co vede na vlastniho rodice, treba skrz sbirky
                    allPathsThroughAllParents.add(anyPathToOwnParent + "/" + pid);
                }
            }
            for (RepositoryNode fosterParent : fosterParents) {//vsechno, co vede na vsechny nevlastni rodice, treba skrz sbirky/hierarchie sbirek
                for (String anyPathToFosterParent : fosterParent.getAllPidPathsThroughAllParents()) {
                    allPathsThroughAllParents.add(anyPathToFosterParent + "/" + pid);
                }
            }
            String rootPid = ownParent == null ? pid : ownParent.getRootPid();
            String rootModel = ownParent == null ? model : ownParent.getRootModel();
            Title rootTitle = ownParent == null ? title : ownParent.getRootTitle();

            String ownParentPid = ownParent == null ? null : ownParent.getPid();
            String ownParentModel = ownParent == null ? null : ownParent.getModel();
            Title ownParentTitle = ownParent == null ? null : ownParent.getTitle();
            Integer positionInOwnParent = extractPositionInParent(pid, ownParent);

            return new RepositoryNode(
                    pid, model, title,
                    pidPath, modelPath, allPathsThroughAllParents,
                    rootPid, rootModel, rootTitle,
                    ownParentPid, ownParentModel, ownParentTitle, positionInOwnParent,
                    fosterParentsPids, fosterParentsOfTypeCollectionPids, anyAncestorsOfTypeCollectionPids,
                    ownChildren, fosterChildren,
                    languages, primaryAuthors, otherAuthors, dateInfo,
                    myLicences, licencesOfOwnAncestors
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private DateInfo mergeDateInfos(RepositoryNode ownParent, DateInfo myDateInfo) {
        //no actual merging, just using this object's dateInfo or if empty, then parent's
        if (myDateInfo != null && !myDateInfo.isEmpty()) {
            return myDateInfo;
        } else if (ownParent != null && ownParent.getDateInfo() != null && !ownParent.getDateInfo().isEmpty()) {
            return new DateInfo(ownParent.getDateInfo());
        }
        return null;
    }

    private List<String> mergeLanguages(RepositoryNode ownParent, List<RepositoryNode> fosterParents, List<String> myLanguages) {
        //fill set
        Set<String> set = new HashSet<>();
        if (ownParent != null) {
            set.addAll(ownParent.getLanguages());
        }
        for (RepositoryNode fosterParent : fosterParents) {
            set.addAll(fosterParent.getLanguages());
        }
        set.addAll(myLanguages);
        //return list
        List<String> list = new ArrayList<>();
        list.addAll(set);
        return list;
    }

    //from both own and foster ancestors
    private List<String> getLicensesFromAllAncestors(RepositoryNode ownParent, List<RepositoryNode> fosterParents) {
        //fill set
        Set<java.lang.String> set = new HashSet<>();
        if (ownParent != null) {
            set.addAll(ownParent.getLicenses());
            set.addAll(ownParent.getLicensesOfAncestors());
        }
        for (RepositoryNode fosterParent : fosterParents) {
            set.addAll(fosterParent.getLicenses());
            set.addAll(fosterParent.getLicensesOfAncestors());
        }
        //return list
        List<java.lang.String> list = new ArrayList<>();
        list.addAll(set);
        return list;
    }

    private List<AuthorInfo> mergePrimaryAuthors(RepositoryNode ownParent, List<RepositoryNode> fosterParents, List<AuthorInfo> myPrimaryAuthors) {
        //fill list
        List<AuthorInfo> authors = new ArrayList<>();
        if (!myPrimaryAuthors.isEmpty()) { //prefer this object' authors
            authors.addAll(myPrimaryAuthors);
        } else if (ownParent != null) { //but use parent's if there aren't any authors of this object
            authors.addAll(ownParent.getPrimaryAuthors());
        }
        for (RepositoryNode fosterParent : fosterParents) { //also use authors of all foster parents (typically articles)
            authors.addAll(fosterParent.getPrimaryAuthors());
        }
        //return list without duplicates
        return authors.stream().distinct().collect(Collectors.toList());
    }

    private List<AuthorInfo> mergeOtherAuthors(RepositoryNode ownParent, List<RepositoryNode> fosterParents, List<AuthorInfo> myOtherAuthors) {
        //fill set
        List<AuthorInfo> authors = new ArrayList<>();
        if (!myOtherAuthors.isEmpty()) { //prefer this object' authors
            authors.addAll(myOtherAuthors);
        } else if (ownParent != null) { //but use parent's if there aren't any authors of this object
            authors.addAll(ownParent.getOtherAuthors());
        }
        for (RepositoryNode fosterParent : fosterParents) { //also use authors of all foster parents (typically articles)
            authors.addAll(fosterParent.getOtherAuthors());
        }
        //return list without duplicates
        return authors.stream().distinct().collect(Collectors.toList());
    }

    private Integer extractPositionInParent(String childPid, RepositoryNode parent) {
        if (parent == null) {
            return null;
        }
        List<String> parentsOwnChildren = parent.getPidsOfOwnChildren();
        if (parentsOwnChildren == null || parentsOwnChildren.isEmpty()) {
            return null;
        }
        int position = parentsOwnChildren.indexOf(childPid);
        return position == -1 ? null : Integer.valueOf(position);
    }

    private List<String> toPidList(List<RepositoryNode> nodes) {
        if (nodes == null) {
            return null;
        } else {
            List<String> result = new ArrayList<>(nodes.size());
            for (RepositoryNode node : nodes) {
                result.add(node.getPid());
            }
            return result;
        }
    }

    private Title extractTitleFromMods(String model, Document modsDoc) throws IOException {
        TitlesExtractor extractor = new TitlesExtractor();
        Title title = extractor.extractPrimaryTitle(modsDoc.getRootElement(), model);
        return title;
    }

    private List<String> extractLanguagesFromMods(String model, Document modsDoc) throws IOException {
        LanguagesExtractor extractor = new LanguagesExtractor();
        List<String> languages = extractor.extractLanguages(modsDoc.getRootElement(), model);
        return languages;
    }

    private List<String> extractLicenses(String model, Document relsExtDoc) {
        LicensesExtractor extractor = new LicensesExtractor();
        List<String> licenses = extractor.extractLicenses(relsExtDoc.getRootElement(), model);
        return licenses;
    }

    private List<AuthorInfo> extractPrimaryAuthorsFromMods(String model, Document modsDoc) throws IOException {
        AuthorsExtractor extractor = new AuthorsExtractor();
        List<AuthorInfo> authors = extractor.extractPrimaryAuthors(modsDoc.getRootElement(), model);
        return authors;
    }

    private List<AuthorInfo> extractNonPrimaryAuthorsFromMods(String model, Document modsDoc) throws IOException {
        AuthorsExtractor extractor = new AuthorsExtractor();
        List<AuthorInfo> authors = extractor.extractNonPrimaryAuthors(modsDoc.getRootElement(), model);
        return authors;
    }

    private DateInfo extractDateInfoFromMods(String model, Document modsDoc, String pid) {
        DateExtractor dateExtractor = new DateExtractor();
        DateInfo dateInfo = dateExtractor.extractDateInfoFromMultipleSources(modsDoc.getRootElement(), pid);
        return dateInfo;
    }

}
