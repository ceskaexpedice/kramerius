/*
 * Copyright (C) 2010 Pavel Stastny
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.kramerius;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.w3c.dom.Document;

/**
 * Class for access to SOLR
 * 
 * @author pavels
 */
public interface SolrAccess {

    /**
     * Returns SOLR data document for given pid
     * 
     * @param pid Requested object
     * @return parsed SOLR document
     * @throws IOException IO error has been occurred
     */
    public Document getSolrDataDocument(String pid) throws IOException;

    /**
     * Returns SOLR data document for given handle
     * 
     * @param handle Handle as object identification
     * @return parsed SOLR document
     * @throws IOException IO error has been occurred
     */
    public Document getSolrDataDocumentByHandle(String handle) throws IOException;

    public Document getSolrDataDocmentsByParentPid(String parentPid, String offset) throws IOException;
    
    
    /**
     * Returns all paths for given pid
     * 
     * @param pid Object's pid
     * @return all paths for given pid
     * @throws IOException IO error has been occurred
     */
    public ObjectPidsPath[] getPath(String pid) throws IOException;

    /**
     * Returns all paths disected from given SOLR data
     * 
     * @param datastreamName datastream name
     * @param solrDataDoc Parsed SOLR document
     * @return disected path
     * @throws IOException IO error has been occurred
     */
    public ObjectPidsPath[] getPath(String datastreamName, Document solrDataDoc) throws IOException;

    /**
     * Returns all model's paths
     * 
     * @param pid PID of requested object
     * @return all model's paths
     * @throws IOException IO error gas been occurred
     */
    public ObjectModelsPath[] getPathOfModels(String pid) throws IOException;

    /**
     * Wrapper allows to return ObjectPidPaths and ObjectModelsPath in one response
     * Example:
     * <pre>
     * <code>
     *  Map<String,Object> solrData = getObjects("uuid:xxx");
     *
     *  ObjectModelsPath[] objectsPaths = (ObjectModelsPath[])solrData.get(ObjectModelsPath.class.getName());
     *  ObjectPidsPath[] objectsPaths = (ObjectPidsPath[])solrData.get(ObjectPidsPath.class.getName());
     *  
     * </code>
     * </pre>
     * @param pid Requesting pid
     * @return
     * @throws IOException
     */
    public Map<String, AbstractObjectPath[]> getPaths(String pid) throws IOException;

    
    public Document request(String req) throws IOException;
    
    public InputStream request(String req, String type) throws IOException;
    
    public InputStream terms(String req, String type) throws IOException;
    
}
