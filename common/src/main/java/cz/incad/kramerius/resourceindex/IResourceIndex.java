/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.incad.kramerius.resourceindex;

import java.util.ArrayList;
import org.w3c.dom.Document;

/**
 *
 * @author Alberto
 */
public interface IResourceIndex { 

    public Document getFedoraObjectsFromModelExt(String model, int limit, int offset, String orderby, String orderDir) throws Exception;
    public ArrayList<String> getFedoraPidsFromModel(String model, int limit, int offset) throws Exception;
    public Document getFedoraModels() throws Exception;
    public ArrayList<String> getParentsPids(String pid) throws Exception;
    public ArrayList<String> getPidPaths(String pid) throws Exception;
    public ArrayList<String> getObjectsInCollection(String collection, int limit, int offset) throws Exception;
    public boolean existsPid(String pid) throws Exception;
    public Document getVirtualCollections() throws Exception;

}
