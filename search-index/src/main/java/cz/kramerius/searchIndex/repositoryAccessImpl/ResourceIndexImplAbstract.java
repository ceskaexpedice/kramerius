package cz.kramerius.searchIndex.repositoryAccessImpl;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.resourceindex.ResourceIndexException;
import cz.kramerius.adapters.IResourceIndex;
import cz.kramerius.shared.Pair;
import org.w3c.dom.Document;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ResourceIndexImplAbstract implements IResourceIndex {

    private Object notImplmented() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<Map<String, String>> getObjects(String model, int limit, int offset, String orderby, String orderDir) throws ResourceIndexException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<Map<String, String>> search(String query, int limit, int offset) throws ResourceIndexException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Document getFedoraModels() throws ResourceIndexException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<String> getFedoraPidsFromModel(String model, int limit, int offset) throws ResourceIndexException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<String> getObjectsByModel(String model, int limit, int offset, String orderby, String orderDir) throws ResourceIndexException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<Map<String, String>> getSubjects(String pid) throws ResourceIndexException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<String> getParentsPids(String pid) throws ResourceIndexException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getModel(String pid) throws ResourceIndexException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Pair<String, Set<String>> getPidsOfParents(String pid) throws ResourceIndexException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ObjectPidsPath[] getPaths(String pid) throws ResourceIndexException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Pair<List<String>, List<String>> getPidsOfChildren(String pid) throws ResourceIndexException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean existsPid(String pid) throws ResourceIndexException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Document getVirtualCollections() throws ResourceIndexException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<String> getCollections() throws ResourceIndexException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<String> getObjectsInCollection(String collection, int limit, int offset) throws ResourceIndexException {
        throw new UnsupportedOperationException("Not implemented");
    }
}
