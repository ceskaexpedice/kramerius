/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.incad.kramerius.resourceindex;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;

/**
 *
 * @author Alberto
 */
public interface IResourceIndex { 

    /**
     * Returs rendered DOM in SPARQL format;  Used only in XSLT transformation and <b>SHOULD be removed<b> in the future.
     * @param model Model 
     * @param limit Limit
     * @param offset Offset 
     * @param orderby Order by field
     * @param orderDir Order dir field
     * @return Returs rendered DOM in SPARQL format
     * @throws ResourceIndexException
     */
    @Deprecated
    public Document getFedoraObjectsFromModelExt(String model, int limit, int offset, String orderby, String orderDir) throws ResourceIndexException;


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
    
    /**
     * Returns whole pidpaths for given pid
     * @param pid Pid of the exploring object
     * @return
     * @throws ResourceIndexException
     */
    public List<String> getPidPaths(String pid) throws ResourceIndexException;
    
    
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
