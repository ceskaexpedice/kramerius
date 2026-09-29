package cz.incad.kramerius.cdk.index;

import java.util.List;

interface CDKProcessingIndexService {

    List<String> getCollections(String collectionPid);

    List<String> getCDKReferences(String collectionPid);

    String getParentCollection(String collectionPid);
}