package cz.incad.kramerius.cdk.index;

import java.util.List;

interface SearchIndexService {

    SearchDocument get(String pid);

    List<String> getChildren(String pid);

    void updateCollectionsFields(String pid, List<String> inCollections, String inCollectionsDirect);

}