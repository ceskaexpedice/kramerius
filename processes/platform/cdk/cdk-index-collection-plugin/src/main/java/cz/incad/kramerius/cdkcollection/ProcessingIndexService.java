package cz.incad.kramerius.cdkcollection;

import java.util.List;

public interface ProcessingIndexService {
    String CDK_PREFIX = "cdk/";

    List<String> getCollections();

    List<String> getReferences(String collectionPid);

    String getParentCollection(String collectionPid);

    boolean isCollection(String collectionPid);
}