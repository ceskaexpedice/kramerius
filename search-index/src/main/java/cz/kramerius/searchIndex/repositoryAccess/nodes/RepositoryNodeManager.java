package cz.kramerius.searchIndex.repositoryAccess.nodes;

import cz.incad.kramerius.resourceindex.ResourceIndexException;
import cz.kramerius.searchIndex.indexer.conversions.extraction.AuthorsExtractor;
import cz.kramerius.searchIndex.indexer.conversions.extraction.LanguagesExtractor;
import cz.kramerius.searchIndex.indexer.conversions.extraction.TitlesExtractor;
import cz.kramerius.searchIndex.repositoryAccess.KrameriusRepositoryAccessAdapter;
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

    public RepositoryNodeManager(KrameriusRepositoryAccessAdapter krameriusRepositoryAccessAdapter) {
        this.krameriusRepositoryAccessAdapter = krameriusRepositoryAccessAdapter;
    }

    public RepositoryNode getKrameriusNode(String pid) {
        if (nodesByPid.containsKey(pid)) {
            return nodesByPid.get(pid);
        } else {
            RepositoryNode node = buildKrameriusNodeFromRepository(pid);
            if (node != null) {
                this.nodesByPid.put(pid, node);
            }
            return node;
        }
    }

    private RepositoryNode buildKrameriusNodeFromRepository(String pid) {
        try {
            //System.out.println("building node for " + pid);
            Pair<String, List<String>> parents = krameriusRepositoryAccessAdapter.getPidsOfParents(pid);
            //process parents first
            RepositoryNode ownParent = parents.getFirst() == null ? null : getKrameriusNode(parents.getFirst());
            List<RepositoryNode> fosterParents = new ArrayList<>();
            for (String fosterParent : parents.getSecond()) {
                fosterParents.add(getKrameriusNode(fosterParent));
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
            Title myTitle = extractTitleFromMods(model, modsDoc);
            List<String> myLanguages = extractLanguagesFromMods(model, modsDoc);
            List<String> languages = mergeLanguages(ownParent, fosterParents, myLanguages);
            List<String> myAuthors = extractAuthorsFromMods(model, modsDoc);
            List<String> authors = mergeAuthors(ownParent, fosterParents, myAuthors);

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
            Title rootTitle = ownParent == null ? myTitle : ownParent.getRootTitle();

            String ownParentPid = ownParent == null ? null : ownParent.getPid();
            String ownParentModel = ownParent == null ? null : ownParent.getModel();
            Title ownParentTitle = ownParent == null ? null : ownParent.getTitle();
            Integer positionInOwnParent = extractPositionInParent(pid, ownParent);

            return new RepositoryNode(
                    pid, model, myTitle,
                    pidPath, modelPath,
                    rootPid, rootModel, rootTitle,
                    ownParentPid, ownParentModel, ownParentTitle, positionInOwnParent,
                    fosterParentsPids, fosterParentsOfTypeCollectionPids, anyAncestorsOfTypeCollectionPids,
                    ownChildren, fosterChildren,
                    languages, authors
            );

        } catch (IOException | ResourceIndexException e) {
            e.printStackTrace();
            return null;
        }
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

    private List<String> mergeAuthors(RepositoryNode ownParent, List<RepositoryNode> fosterParents, List<String> myAuthors) {
        //fill set
        Set<String> set = new HashSet<>();
        if (!myAuthors.isEmpty()) { //prefer this object' authors
            set.addAll(myAuthors);
        } else if (ownParent != null) { //but use parent's if there aren't any authors of this object
            set.addAll(ownParent.getAuthors());
        }
        for (RepositoryNode fosterParent : fosterParents) { //also use authors of all foster parents (typically articles)
            set.addAll(fosterParent.getAuthors());
        }
        //return list
        List<String> list = new ArrayList<>();
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

    private List<String> extractAuthorsFromMods(String model, Document modsDoc) throws IOException {
        AuthorsExtractor extractor = new AuthorsExtractor();
        List<String> languages = extractor.extractAuthors(modsDoc.getRootElement(), model);
        return languages;
    }


}
