/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.incad.kramerius.resourceindex;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cz.incad.kramerius.ObjectPidsPath;
import org.w3c.dom.Document;

/**
 *
 * @author Alberto
 */
public interface IResourceIndex {


    /**
     * Return objects from processing index by model
     * @param model
     * @param limit
     * @param offset
     * @param orderby
     * @param orderDir
     * @return
     * @throws ResourceIndexException
     */
    public List<Map<String,String>> getObjects(String model, int limit, int offset, String orderby, String orderDir) throws ResourceIndexException;


    public List<Map<String,String>> search(String query, int limit, int offset) throws ResourceIndexException;


    /**
     * Returs rendered DOM in SPARQL format;  Used only in XSLT transformation and <b>SHOULD be removed<b> in the future.
     * @return
     * @throws ResourceIndexException
     */
    @Deprecated
    public Document getFedoraModels() throws ResourceIndexException;

    /**
     * Never used; throw it out
     */
    @Deprecated
    public List<String> getFedoraPidsFromModel(String model, int limit, int offset) throws ResourceIndexException;
    
    

    /**
     * Returns all objects associated with given model
     * @param model Model for searching
     * @param limit Limit for results
     * @param offset Offset 
     * @param orderby Ordered by 
     * @param orderDir Order dir
     * @return Sets of objects associated with given model
     * @throws ResourceIndexException 
     */
    public List<String> getObjectsByModel(String model, int limit, int offset, String orderby, String orderDir) throws ResourceIndexException;

    public List<Map<String,String>> getSubjects(String pid) throws ResourceIndexException;

    /**
     * Returns parent pids for given pid
     * @param pid Pid of the exploring object
     * @return
     * @throws ResourceIndexException
     */
    public List<String> getParentsPids(String pid) throws ResourceIndexException;


    public ObjectPidsPath[] getPath(String pid) throws ResourceIndexException;
    /**
     * Returns true if the pid exists in underlaying resource index db
     * @param pid
     * @return
     * @throws ResourceIndexException
     */
    public boolean existsPid(String pid) throws ResourceIndexException;
    
    //  Virtual collection
    /**
     * Returs rendered DOM in SPARQL format;  Used only in XSLT transformation and <b>SHOULD be removed<b> in the future.
     * @return
     * @throws ResourceIndexException
     */
    @Deprecated
    public Document getVirtualCollections() throws ResourceIndexException;

    public List<String> getCollections() throws ResourceIndexException;

    /**
     * Returns all objects in collection
     * @param collection Given collection
     * @param limit Limit 
     * @param offset Offset
     * @return
     * @throws ResourceIndexException
     */
    public List<String> getObjectsInCollection(String collection, int limit, int offset) throws ResourceIndexException;

}
