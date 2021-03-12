package cz.kramerius.searchIndex.repositoryAccess.nodes;

import cz.incad.kramerius.resourceindex.ResourceIndexException;
import cz.kramerius.searchIndex.indexer.conversions.extraction.AuthorsExtractor;
import cz.kramerius.searchIndex.indexer.conversions.extraction.DateExtractor;
import cz.kramerius.searchIndex.indexer.conversions.extraction.LanguagesExtractor;
import cz.kramerius.searchIndex.indexer.conversions.extraction.TitlesExtractor;
import cz.kramerius.searchIndex.repositoryAccess.KrameriusRepositoryAccessAdapter;
import cz.kramerius.shared.AuthorInfo;
import cz.kramerius.shared.DateInfo;
import cz.kramerius.shared.Pair;
import cz.kramerius.shared.Title;
import org.dom4j.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RepositoryNodeManager {

    private final KrameriusRepositoryAccessAdapter krameriusRepositoryAccessAdapter;
    private final LRUCache<String, RepositoryNode> nodesByPid = new LRUCache<>(1024);
    private final boolean surviveInconsistentObjects;

    public RepositoryNodeManager(KrameriusRepositoryAccessAdapter krameriusRepositoryAccessAdapter, boolean surviveInconsistentObjects) {
        this.krameriusRepositoryAccessAdapter = krameriusRepositoryAccessAdapter;
        this.surviveInconsistentObjects = surviveInconsistentObjects;
    }

    public RepositoryNode getKrameriusNode(String pid) {
        try {
            return getKrameriusNodeWithCycleDetection(pid, new ArrayList<>());
        } catch (RuntimeException e) {
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
            if (!krameriusRepositoryAccessAdapter.isObjectAvailable(pid)) {
                return null;
            }
            //System.out.println("building node for " + pid);
            Pair<String, List<String>> parents = krameriusRepositoryAccessAdapter.getPidsOfParents(pid);
            //process parents first
            RepositoryNode ownParent = parents.getFirst() == null ? null : getKrameriusNodeWithCycleDetection(parents.getFirst(), path);
            List<RepositoryNode> fosterParents = new ArrayList<>();
            for (String fosterParent : parents.getSecond()) {
                fosterParents.add(getKrameriusNodeWithCycleDetection(fosterParent, path));
            }

            /*for (String parentPid : pidsOfParents) {
                RepositoryNode parentNode = getKrameriusNode(parentPid);
                if (parentNode != null) {
                    if (parentNode.getPidsOfOwnChildren().contains(pid)) {
                        if (ownParent != null) {
                            throw new IllegalStateException(String.format("duplicate parents for %s: %s and %s", this, ownParent.getPid(), parentPid));
                        } else {
                            ownParent = parentNode;
                            //System.out.println(String.format("%s is own parent of %s", parentPid, pid));
                        }
                    } else {
                        fosterParents.add(parentNode);
                        //System.out.println(String.format("%s is foster parent of %s", parentPid, pid));
                    }
                }
            }*/

            //Document relsExtDoc = krameriusRepositoryAccessAdapter.getRelsExt(pid, true);
            //String model = KrameriusRepositoryUtils.extractKrameriusModelName(relsExtDoc);
            String model = krameriusRepositoryAccessAdapter.getModel(pid);
            List<String> ownChildren = null;
            List<String> fosterChildren = null;
            if (!"page".equals(model) && !"track".equals(model)) { //just optimization, pages and tracks never have children
                //Pair<List<String>, List<String>> children = KrameriusRepositoryUtils.extractChildren(relsExtDoc);
                Pair<List<String>, List<String>> children = krameriusRepositoryAccessAdapter.getPidsOfChildren(pid);
                ownChildren = children.getFirst();
                fosterChildren = children.getSecond();
            }
            //System.out.println("own children: " + (ownChildren == null? null : ownChildren.size()));

            Document modsDoc = krameriusRepositoryAccessAdapter.getMods(pid, false);
            if (modsDoc == null) {
                throw new RuntimeException("missing MODS");
            }
            Title title = extractTitleFromMods(model, modsDoc);

            //data from parents
            DateInfo myDateInfo = extractDateInfoFromMods(model, modsDoc);
            DateInfo dateInfo = mergeDateInfos(ownParent, myDateInfo);
            List<String> myLanguages = extractLanguagesFromMods(model, modsDoc);
            List<String> languages = mergeLanguages(ownParent, fosterParents, myLanguages);
            List<AuthorInfo> myAuthors = extractAuthorsFromMods(model, modsDoc);
            List<AuthorInfo> authors = mergeAuthors(ownParent, fosterParents, myAuthors);

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

            String rootPid = ownParent == null ? pid : ownParent.getRootPid();
            String rootModel = ownParent == null ? model : ownParent.getRootModel();
            Title rootTitle = ownParent == null ? title : ownParent.getRootTitle();

            String ownParentPid = ownParent == null ? null : ownParent.getPid();
            String ownParentModel = ownParent == null ? null : ownParent.getModel();
            Title ownParentTitle = ownParent == null ? null : ownParent.getTitle();
            Integer positionInOwnParent = extractPositionInParent(pid, ownParent);

            return new RepositoryNode(
                    pid, model, title,
                    pidPath, modelPath,
                    rootPid, rootModel, rootTitle,
                    ownParentPid, ownParentModel, ownParentTitle, positionInOwnParent,
                    fosterParentsPids, fosterParentsOfTypeCollectionPids, anyAncestorsOfTypeCollectionPids,
                    ownChildren, fosterChildren,
                    languages, authors, dateInfo
            );
        } catch (IOException | ResourceIndexException e) {
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

    private List<String> mergeLanguages(RepositoryNode
                                                ownParent, List<RepositoryNode> fosterParents, List<String> myLanguages) {
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

    private List<AuthorInfo> mergeAuthors(RepositoryNode ownParent, List<RepositoryNode> fosterParents, List<AuthorInfo> myAuthors) {
        //fill set
        Set<AuthorInfo> set = new HashSet<>();
        if (!myAuthors.isEmpty()) { //prefer this object' authors
            set.addAll(myAuthors);
        } else if (ownParent != null) { //but use parent's if there aren't any authors of this object
            set.addAll(ownParent.getAuthors());
        }
        for (RepositoryNode fosterParent : fosterParents) { //also use authors of all foster parents (typically articles)
            set.addAll(fosterParent.getAuthors());
        }
        //return list
        List<AuthorInfo> list = new ArrayList<>();
        list.addAll(set);
        return list;
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

    private List<AuthorInfo> extractAuthorsFromMods(String model, Document modsDoc) throws IOException {
        AuthorsExtractor extractor = new AuthorsExtractor();
        List<AuthorInfo> authors = extractor.extractAuthors(modsDoc.getRootElement(), model);
        return authors;
    }

    private DateInfo extractDateInfoFromMods(String model, Document modsDoc) {
        DateExtractor dateExtractor = new DateExtractor();
        DateInfo dateInfo = dateExtractor.extractDateInfoFromMultipleSources(modsDoc.getRootElement());
        return dateInfo;
    }

}
