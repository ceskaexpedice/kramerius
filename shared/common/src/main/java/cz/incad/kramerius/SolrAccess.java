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

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Class for access to SOLR
 *
 * @author pavels
 */
public interface SolrAccess {

    /**
     * Returns SOLR data containing document with given pid.
     * Including solr response envelope, i.e. <response><result><doc>...</doc></result></response>, not just <doc>...</doc>
     *
     * @param pid Requested object
     * @return
     * @throws IOException
     */
    public Document getSolrDataByPid(String pid) throws IOException;

    /**
     * Returns SOLR data containing document with given handle
     * Examples of handle: handle/ABA001/12b34a, handle/uuid:8b0b25e0-49b7-11de-a45e-000d606f5dc6
     * Including solr response envelope, i.e. <response><result><doc>...</doc></result></response>, not just <doc>...</doc>
     *
     * @param handle handle as object identifier
     * @return
     * @throws IOException
     */
    public Document getSolrDataByHandle(String handle) throws IOException;

    /**
     * Returns SOLR data containing documents with given parent's pid.
     * Including solr response envelope, i.e. <response><result><doc>...</doc></result></response>, not just <doc>...</doc>
     * <p>
     * TODO: why offset here? Can't imaging anything having so many parents that it is justified.
     *
     * @param parentPid
     * @param offset
     * @return
     * @throws IOException
     */
    public Document getSolrDataByParentPid(String parentPid, String offset) throws IOException;

    /**
     * Returns all paths for given pid
     *
     * @param pid Object's pid
     * @return all pid paths for given pid
     * @throws IOException IO error has been occurred
     */
    public ObjectPidsPath[] getPidPaths(String pid) throws IOException;

    /**
     * Returns all paths from given Solr data (Ended by datastream if datastream is defined)
     *
     * @param datastreamName datastream name  - could be null
     * @param solrDataDoc    Parsed SOLR document, including solr response envelope, i.e. <response><result><doc>...</doc></result></response>, not just <doc>...</doc>
     * @return all pid paths for given pid (and possibly datastream)
     * @throws IOException IO error has been occurred
     */
    public ObjectPidsPath[] getPidPaths(String datastreamName, Document solrDataDoc) throws IOException;


    /**
     * Returns all paths from given Solr data (Ended by datastream if datastream is defined)
     *
     * @param datastreamName       datastream name  - could be null
     * @param solrDocParentElement Solr hit element
     * @return all pid paths for given pid (and possibly datastream)
     * @throws IOException IO error has been occurred
     */
    public ObjectPidsPath[] getPidPaths(String datastreamName, Element solrDocParentElement) throws IOException;


    /**
     * Returns all model's paths
     *
     * @param pid PID of requested object
     * @return all model paths for given pid
     * @throws IOException
     */
    public ObjectModelsPath[] getModelPaths(String pid) throws IOException;

    /**
     * @param solrDataDoc Parsed SOLR document, including solr response envelope, i.e. <response><result><doc>...</doc></result></response>, not just <doc>...</doc>
     * @return all model paths dissected from solr document
     * @throws IOException
     */
    public ObjectModelsPath[] getModelPaths(Document solrDataDoc) throws IOException;

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
     *
     * @param pid Requesting pid
     * @return
     * @throws IOException
     */
    public Map<String, AbstractObjectPath[]> getModelAndPidPaths(String pid) throws IOException;


    /**
     * Returns SOLR data document (in xml) for given SELECT query.
     * Including solr response envelope, i.e. <response><result><doc>...</doc><doc>...</doc></doc></result></response>, not just <result><doc>...</doc><doc>...</doc></result>
     *
     * @param query query as it will be passed to url including all query params except for wt,
     *              for example: start=0&wt=json&fl=pid&q=indexed:%5B2021-02-01T18%5C:18%5C:00.000Z%20TO%20*%5D&rows=10
     *              notice the url encoding of query param content, here demonstrated on "indexed:[2021-02-01T18\:18\:00.000Z TO *]"
     * @return
     * @throws IOException
     */
    public Document requestWithSelectReturningXml(String query) throws IOException;

    /**
     * Returns SOLR data document (in json) for given SELECT query.
     * Including solr response envelope, i.e. {response: {docs: [{...},{...}], ...}} not just {docs: [{...},{...}]}
     *
     * @param query query as it will be passed to url including all query params except for wt,
     *              for example: start=0&wt=json&fl=pid&q=indexed:%5B2021-02-01T18%5C:18%5C:00.000Z%20TO%20*%5D&rows=10
     *              notice the url encoding of query param content, here demonstrated on "indexed:[2021-02-01T18\:18\:00.000Z TO *]"
     * @return
     * @throws IOException
     */
    public JSONObject requestWithSelectReturningJson(String query) throws IOException;

    /**
     * Returns SOLR data document (in xml or json) for given SELECT query.
     * Including solr response envelope, i.e. <response><result><doc>...</doc><doc>...</doc></doc></result></response>, not just <result><doc>...</doc><doc>...</doc></result>
     * or {response: {docs: [{...},{...}], ...}} not just {docs: [{...},{...}]}
     *
     * @param query query as it will be passed to url including all query params except for wt,
     *              for example: start=0&wt=json&fl=pid&q=indexed:%5B2021-02-01T18%5C:18%5C:00.000Z%20TO%20*%5D&rows=10
     *              notice the url encoding of query param content, here demonstrated on "indexed:[2021-02-01T18\:18\:00.000Z TO *]"
     * @param type  value "xml" or "json", this will be passed to query param wt
     * @return
     * @throws IOException
     * @deprecated instead use requestWithSelectReturningJson(), requestWithSelectReturningXml(query), or requestWithSelectReturningString(query,type)
     */
    @Deprecated
    public InputStream requestWithSelectReturningInputStream(String query, String type) throws IOException;

    /**
     * Returns SOLR data document (in xml or json) for given SELECT query.
     * Including solr response envelope, i.e. <response><result><doc>...</doc><doc>...</doc></doc></result></response>, not just <result><doc>...</doc><doc>...</doc></result>
     * or {response: {docs: [{...},{...}], ...}} not just {docs: [{...},{...}]}
     *
     * @param query query as it will be passed to url including all query params except for wt,
     *              for example: start=0&wt=json&fl=pid&q=indexed:%5B2021-02-01T18%5C:18%5C:00.000Z%20TO%20*%5D&rows=10
     *              notice the url encoding of query param content, here demonstrated on "indexed:[2021-02-01T18\:18\:00.000Z TO *]"
     * @param type  value "xml" or "json", this will be passed to query param wt
     * @return
     * @throws IOException
     */
    public String requestWithSelectReturningString(String query, String type) throws IOException;

    /**
     * Returns SOLR data document (in xml or json) for given TERMS query.
     * Including solr response envelope, i.e. <response><result><doc>...</doc></result></response>, not just <doc>...</doc>
     *
     * @param query query as it will be passed to url including all query params except for wt,
     *              for example: start=0&wt=json&fl=pid&q=indexed:%5B2021-02-01T18%5C:18%5C:00.000Z%20TO%20*%5D&rows=10
     *              notice the url encoding of query param content, here demonstrated on "indexed:[2021-02-01T18\:18\:00.000Z TO *]"
     * @param type  value "xml" or "json", this will be passed to query param wt
     * @return
     * @throws IOException
     */
    public InputStream requestWithTerms(String query, String type) throws IOException;

}
