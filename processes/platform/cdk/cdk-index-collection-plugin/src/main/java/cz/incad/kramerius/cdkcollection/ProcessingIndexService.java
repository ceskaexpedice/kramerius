package cz.incad.kramerius.cdkcollection;

import java.util.List;

public interface ProcessingIndexService {

    List<String> getCollections();

    List<String> getReferences(String collectionPid);

    List<String> getParentCollections(String collectionPid);

}