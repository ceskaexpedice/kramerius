package cz.incad.kramerius.cdk.index.prepare;

import java.util.List;

interface ProcessingIndexService {

    List<String> getContainedCollections(
            String collectionPid);

    List<String> getContainedCdkReferences(
            String collectionPid);
}