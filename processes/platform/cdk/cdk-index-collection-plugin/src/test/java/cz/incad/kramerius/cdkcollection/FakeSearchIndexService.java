package cz.incad.kramerius.cdkcollection;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

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
    public void findUnlinkedSubtree(String rootPid, List<String> cdkPath, Consumer<List<SearchDocument>> pageConsumer) {
        if (pageConsumer == null) {
            throw new IllegalArgumentException("Page consumer must not be null");
        }
        SearchDocument root = documents.get(rootPid);
        if (root == null || root.getOwnPidPath() == null || root.getOwnPidPath().isEmpty()) {
            return;
        }
        if (cdkPath == null || cdkPath.isEmpty()) {
            throw new IllegalArgumentException("CDK path must not be empty");
        }
        String targetPath = String.join("/", cdkPath) + "/" + root.getOwnPidPath();
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
        if (!result.isEmpty()) {
            pageConsumer.accept(result);
        }
    }

    @Override
    public void findByDirectCollection(String collectionPid, Consumer<List<SearchDocument>> pageConsumer) {
        List<SearchDocument> result = new ArrayList<>();
        for (SearchDocument document : documents.values()) {
            if (document.getInCollectionsDirect().contains(collectionPid)) {
                result.add(document);
            }
        }
        if (!result.isEmpty()) {
            pageConsumer.accept(result);
        }
    }

    @Override
    public void findByPidPathPrefix(String pidPathPrefix, Consumer<List<SearchDocument>> pageConsumer) {
        List<SearchDocument> result = new ArrayList<>();
        for (SearchDocument document : documents.values()) {
            for (String path : document.getPidPaths()) {
                if (path != null
                        && (path.equals(pidPathPrefix)
                        || path.startsWith(pidPathPrefix + "/"))) {
                    result.add(document);
                    break;
                }
            }
        }
        if (!result.isEmpty()) {
            pageConsumer.accept(result);
        }
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
    public void updateDocumentFieldsBatch(List<SearchDocumentUpdate> updates) {
        if (updates == null || updates.isEmpty()) {
            return;
        }
        for (SearchDocumentUpdate update : updates) {
            SearchDocument document = requireDocument(update.getPid());
            document.setPidPaths(update.getPidPaths());
            document.setCollections(update.getInCollections(), update.getInCollectionsDirect());
        }
    }

    @Override
    public void commit() {
        // nothing
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
