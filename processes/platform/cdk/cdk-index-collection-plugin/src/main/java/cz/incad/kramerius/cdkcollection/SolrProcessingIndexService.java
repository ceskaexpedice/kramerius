package cz.incad.kramerius.cdkcollection;

import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.processingindex.ProcessingIndexItem;
import org.ceskaexpedice.akubra.relsext.KnownRelations;

import java.util.ArrayList;
import java.util.List;

public class SolrProcessingIndexService implements ProcessingIndexService {
    private final AkubraRepository akubraRepository;

    public SolrProcessingIndexService(AkubraRepository akubraRepository) {
        this.akubraRepository = akubraRepository;
    }

    @Override
    public List<String> getCollections() {
        List<String> collections = new ArrayList<>();
        List<ProcessingIndexItem> page = akubraRepository.pi().getByModel("collection", true, 0, Integer.MAX_VALUE);
        for (ProcessingIndexItem item : page) {
            collections.add(item.source());
        }
        return collections;
    }

    @Override
    public List<String> getReferences(String collectionPid) {
        List<String> collections = getCollections();
        List<String> references = new ArrayList<>();
        List<ProcessingIndexItem> children = akubraRepository.pi().getChildren(
                KnownRelations.CONTAINS.toString(), collectionPid);
        for (ProcessingIndexItem item : children) {
            if(collections.contains(item.targetPid())){
                continue;
            }
            references.add(item.targetPid());
        }
        return references;
    }

    @Override
    public List<String> getParentCollections(String collectionPid) {
        List<String> parentsSt = new ArrayList<>();
        List<ProcessingIndexItem> parents = akubraRepository.pi().getParents(KnownRelations.CONTAINS.toString(), collectionPid);
        for (ProcessingIndexItem parent : parents) {
            parentsSt.add(parent.source());
        }
        return parentsSt;
    }

    @Override
    public boolean isCollection(String collectionPid) {
        return getCollections().contains(collectionPid);
    }
}
