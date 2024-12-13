/*
 * Copyright (C) 2016 Pavel Stastny
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

package cz.incad.kramerius.fedora.om;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.w3c.dom.Document;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

/**
 * This interface represents basic repository item;
 * @author pavels
 */
public interface RepositoryObject {


    /**
     * Get path within repository
     * @return
     */
    public String getPath();

    /**
     * Return list of streams
     * @return
     * @throws RepositoryException
     */
    public List<RepositoryDatastream> getStreams() throws RepositoryException;


    /**
     * Create new XML stream
     * @param streamId Stream id
     * @param mimeType Mimetype of the stream
     * @param input Binary content
     * @return
     * @throws RepositoryException
     */
    public RepositoryDatastream createStream(String streamId, String mimeType, InputStream input) throws RepositoryException;

    /**
     * Create new managed stream
     * @param streamId Stream id
     * @param mimeType Mimetype of the stream
     * @param input Binary content
     * @return
     * @throws RepositoryException
     */
    public RepositoryDatastream createManagedStream(String streamId, String mimeType, InputStream input) throws RepositoryException;


    /**
     * Delete stream
     * @param streamId
     * @throws RepositoryException
     */
    public void deleteStream(String streamId)  throws RepositoryException;

    // update properties by sparql

    /**
     * Update sparql properties
     * @param sparql
     * @throws RepositoryException
     */
    public void updateSPARQL(String sparql) throws RepositoryException;

    /**
     * Create redirect stream
     * @param streamId Stream id
     * @param url url
     * @return
     * @throws RepositoryException
     */
    public RepositoryDatastream createRedirectedStream(String streamId, String url, String mimeType) throws RepositoryException;

    /**
     * Return stream of the object
     * @param streamId Stream id
     * @return
     * @throws RepositoryException
     */
    public RepositoryDatastream getStream(String streamId) throws RepositoryException;

    /**
     * Returns true if the stream exists
     * @param streamId
     * @return
     * @throws RepositoryException
     */
    public boolean streamExists(String streamId) throws RepositoryException;

    /**
     * Returns last modified flag
     * @return
     * @throws RepositoryException
     */
    public Date getLastModified() throws RepositoryException;

    /**
     * REturns metadata document
     * @return
     * @throws RepositoryException
     */
    public Document getMetadata() throws RepositoryException;

    /**
     * Returns foxml representation
     * @return
     * @throws RepositoryException
     */
    public InputStream getFoxml() throws RepositoryException;


    /**
     * Add relation
     * @param relation Type of relation
     * @param namespace Namespace
     * @param targetRelation Target
     * @throws RepositoryException
     */
    public void addRelation(String relation, String namespace, String targetRelation) throws RepositoryException;

    /**
     * Add literal
     * @param relation Type of relation
     * @param namespace Namespace
     * @param value Literal value
     * @throws RepositoryException
     */
    public void addLiteral(String relation, String namespace, String value) throws RepositoryException;

    /**
     * Remove relation
     * @param relation Type of relation
     * @param namespace Namespace
     * @param targetRelation Target
     * @throws RepositoryException
     */
    public void removeRelation(String relation, String namespace, String targetRelation) throws RepositoryException;

    /**
     * Remove all relations by relation type and namespace
     * @param relation Type of relation
     * @param namespace Namespace
     * @throws RepositoryException
     */
    public void removeRelationsByNameAndNamespace(String relation, String namespace) throws RepositoryException;

    /**
     * Remove all relations by namespace
     * @param namespace Namespace
     * @throws RepositoryException
     */
    public void removeRelationsByNamespace(String namespace) throws RepositoryException;

    /**
     * Remove all literal
     * @param relation Relation type
     * @param namespace Namespace
     * @param value Literal value
     * @throws RepositoryException
     */
    public void removeLiteral(String relation, String namespace, String value) throws RepositoryException;

    /**
     * Returns true if relation identified by relation type, namespace and target exists
     * @param relation Type of relation
     * @param namespace Namespace
     * @param targetRelation Target relation
     * @return
     * @throws RepositoryException
     */
    public boolean relationExists(String relation, String namespace, String targetRelation) throws RepositoryException;

    /**
     * Returns true if relations identified by relationType and namespace exists
     * @param relation Relation type
     * @param namespace Namespace
     * @return
     * @throws RepositoryException
     */
    public boolean relationsExists(String relation, String namespace) throws RepositoryException;

    /**
     * Returns true if literal exists
     * @param relation Relation type
     * @param namespace Namespace
     * @param value Value
     * @return
     * @throws RepositoryException
     */
    public boolean  literalExists(String relation, String namespace, String value) throws RepositoryException;

    /**
     * Returns all relations identified by namespace
     * @param namespace Namespace
     * @return
     * @throws RepositoryException
     */
    public List<Triple<String, String, String>> getRelations(String namespace) throws RepositoryException;

    /**
     * Returns all literals identified by namespace
     * @param namespace
     * @return
     * @throws RepositoryException
     */
    public List<Triple<String, String, String>>  getLiterals(String namespace) throws RepositoryException;


    /**
     * Remove all relations; from RELS-EXT and properties
     * @throws RepositoryException
     */
    public void removeRelationsAndRelsExt() throws RepositoryException;

    /**
     * Returns fullpath
     * @return
     * @throws RepositoryException
     */
    public String getFullPath() throws RepositoryException;

    /**
     * Method is able to rebuild processing index for current object
     * @throws RepositoryException
     */
    public void rebuildProcessingIndex() throws RepositoryException;

   
}
