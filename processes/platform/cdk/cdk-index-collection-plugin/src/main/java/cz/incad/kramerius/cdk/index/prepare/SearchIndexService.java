package cz.incad.kramerius.cdk.index.prepare;

import java.util.List;

interface SearchIndexService {

    SearchDocument get(String pid);

    List<String> getChildren(String pid);

    void updateCollectionFields(
            String pid,
            Object inCollection,
            Object inCollectionDirect);
}