/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.incad.kramerius.resourceindex;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

/**
 *
 * @author Alberto
 */
public interface IResourceIndex { 
    
    public Document getFedoraObjectsFromModelExt(String model, int limit, int offset, String orderby, String orderDir) throws Exception;

    public List<String> getFedoraPidsFromModel(String model, int limit, int offset) throws Exception;

    public Document getFedoraModels() throws Exception;
    
    public List<String> getParentsPids(String pid) throws Exception;
    public List<String> getPidPaths(String pid) throws Exception;
    public List<String> getObjectsInCollection(String collection, int limit, int offset) throws Exception;
    public boolean existsPid(String pid) throws Exception;
    
    // Virtual collection
    public Document getVirtualCollections() throws Exception;

}
