package cz.incad.kramerius.cdk.index;

import java.util.List;

public interface ProcessingIndexService {

    List<String> getCollections(String collectionPid);

    List<String> getCDKReferences(String collectionPid);

    String getParentCollection(String collectionPid);

    boolean isCDKCollection(String collectionPid);

    /**
     * Vrátí všechny aktuálně existující CDK kolekce.
     */
    List<String> getCDKCollections();
}