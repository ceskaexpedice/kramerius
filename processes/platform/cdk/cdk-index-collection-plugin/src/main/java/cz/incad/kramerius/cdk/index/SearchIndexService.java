package cz.incad.kramerius.cdk.index;

import java.util.Collection;
import java.util.List;
import java.util.Set;

interface SearchIndexService {

    SearchDocument get(String pid);

    List<String> getChildren(String pid);

    void updateCollectionsFields(String pid, List<String> inCollections, String inCollectionsDirect);

    List<SearchDocument> getAllDocuments();

    Set<String> getExistingPids(Collection<String> pids);

}