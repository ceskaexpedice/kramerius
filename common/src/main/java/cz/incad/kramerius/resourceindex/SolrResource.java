package cz.incad.kramerius.resourceindex;

import java.util.List;

import org.w3c.dom.Document;

public class SolrResource implements IResourceIndex {

    @Override
    public Document getFedoraObjectsFromModelExt(String model, int limit, int offset, String orderby, String orderDir)
            throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getFedoraPidsFromModel(String model, int limit, int offset) throws Exception {
        // all pids by model; sourceModel = 'monograph' first filter
        return null;
    }

    @Override
    public Document getFedoraModels() throws Exception {
        // TODO Auto-generated method stub
        // facets by models; source model; relation; should i create target model as well ? 
        return null;
    }

    @Override
    public List<String> getParentsPids(String pid) throws Exception {
        return null;
    }

    @Override
    public List<String> getPidPaths(String pid) throws Exception {
        return null;
    }

    @Override
    public List<String> getObjectsInCollection(String collection, int limit, int offset) throws Exception {
        return null;
    }

    @Override
    public boolean existsPid(String pid) throws Exception {
        return false;
    }

    @Override
    public Document getVirtualCollections() throws Exception {
        return null;
    }
}
