package cz.incad.kramerius.cdkcollection;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FakeSearchIndexService implements SearchIndexService {

    private final Map<String, SearchDocument> documents = new LinkedHashMap<>();
    private final Map<String, List<String>> children = new LinkedHashMap<>();
    private final Map<String, String> parents = new LinkedHashMap<>();

    public void addDocument(SearchDocument document) {
        documents.put(document.getPid(), document);
    }

    public void setParent(String childPid, String parentPid) {
        parents.put(childPid, parentPid);
        List<String> childList = children.computeIfAbsent(parentPid, key -> new ArrayList<>());
        if (!childList.contains(childPid)) {
            childList.add(childPid);
        }
    }

    @Override
    public SearchDocument get(String pid) {
        return documents.get(pid);
    }

    public List<String> getChildren(String pid) {
        List<String> result = children.get(pid);
        return result == null ? new ArrayList<>() : new ArrayList<>(result);
    }

    @Override
    public List<SearchDocument> findUnlinkedSubtree(String rootPid, List<String> cdkPath) {
        SearchDocument root = documents.get(rootPid);
        if (root == null || root.getOwnPidPath() == null || root.getOwnPidPath().isEmpty()) {
            return new ArrayList<>();
        }
        if (cdkPath == null || cdkPath.isEmpty()) {
            throw new IllegalArgumentException("CDK path must not be empty");
        }
        String targetPath = String.join("/", cdkPath) + "/" + rootPid;
        List<SearchDocument> result = new ArrayList<>();
        Deque<String> pending = new ArrayDeque<>();
        Set<String> visited = new LinkedHashSet<>();
        pending.add(rootPid);
        while (!pending.isEmpty()) {
            String pid = pending.removeFirst();
            if (!visited.add(pid)) {
                continue;
            }
            SearchDocument document = documents.get(pid);
            if (document == null) {
                continue;
            }
            if (!hasTargetCDKPath(document, targetPath)) {
                result.add(document);
            }
            pending.addAll(getChildren(pid));
        }

        return result;
    }

    @Override
    public List<SearchDocument> findByDirectCollection(String collectionPid) {
        List<SearchDocument> result = new ArrayList<>();
        for (SearchDocument document : documents.values()) {
            if (document.getInCollectionsDirect().contains(collectionPid)) {
                result.add(document);
            }
        }
        return result;
    }

    @Override
    public List<SearchDocument> findByPidPathPrefix(String pidPathPrefix) {
        List<SearchDocument> result = new ArrayList<>();
        for (SearchDocument document : documents.values()) {
            for (String path : document.getPidPaths()) {
                if (path != null && (path.equals(pidPathPrefix) || path.startsWith(pidPathPrefix + "/"))) {
                    result.add(document);
                    break;
                }
            }
        }
        return result;
    }

    private boolean hasTargetCDKPath(SearchDocument document, String targetPath) {
        for (String path : document.getPidPaths()) {
            if (path != null && (path.equals(targetPath) || path.startsWith(targetPath + "/"))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void updateCollectionsFields(String pid, List<String> inCollections, List<String> inCollectionsDirect) {
        requireDocument(pid).setCollections(inCollections, inCollectionsDirect);
    }

    @Override
    public void updatePidPaths(String pid, List<String> pidPaths) {
        requireDocument(pid).setPidPaths(pidPaths);
    }

    private SearchDocument requireDocument(String pid) {
        SearchDocument document = documents.get(pid);
        if (document == null) {
            throw new IllegalArgumentException("Unknown search document: " + pid);
        }
        return document;
    }

    @Override
    public List<String> getDirectCollectionValues() {
        Set<String> values = new LinkedHashSet<>();
        for (SearchDocument document : documents.values()) {
            values.addAll(document.getInCollectionsDirect());
        }
        return new ArrayList<>(values);
    }

}
