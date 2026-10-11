package cz.incad.kramerius.cdk.index1;

import java.util.List;

public interface ProcessingIndexService {
    List<String> getCDKCollections();

    List<String> getCDKReferences(String collectionPid);

    String getParentCollection(String collectionPid);

    boolean isCDKCollection(String collectionPid);
}