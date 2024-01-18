/*
 * Copyright (C) Jan 10, 2024 Pavel Stastny
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
package cz.incad.kramerius.rest.oai;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.core.UriBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.apiNew.client.v70.SearchResource;
import cz.incad.kramerius.solr.SolrFieldsMapping;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.XMLUtils;

public class OAISet {
    
    private String host;
    
    private String setSpec;
    private String setName;
    private String setDescription;
    private String filterQuery;


    public OAISet(String host, String setSpec, String setName, String setDescription, String filterQuery) {
        super();
        this.setSpec = setSpec;
        this.setName = setName;
        this.setDescription = setDescription;
        this.filterQuery = filterQuery;
        this.host = host;
    }
    
    protected OAISet(String host) {
        super();
        this.host= host;
    }

    public String getHost() {
        return host;
    }
    
    public void setHost(String host) {
        this.host = host;
    }
    
    public String getSetSpec() {
        return setSpec;
    }
    
    public void setSetSpec(String setSpec) {
        this.setSpec = setSpec;
    }
    
    public String getSetName() {
        return setName;
    }
    
    public void setSetName(String setName) {
        this.setName = setName;
    }
    
    public String getSetDescription() {
        return setDescription;
    }
    public void setSetDescription(String setDescription) {
        this.setDescription = setDescription;
    }
    
    public String getFilterQuery() {
        return filterQuery;
    }
    
    public void setFilterQuery(String filterQuery) {
        this.filterQuery = filterQuery;
    }
    
    public boolean isMyResumptionToken(String resumptionToken) {
        String spec = OAITools.specFromResumptionToken(resumptionToken);
        return spec.equals(getSetSpec());
    }
    

    public OAIResults findRecords(SolrAccess solrAccess,String cursor, String metadataPrefix, int rows) throws IOException, ParserConfigurationException, SAXException {

        String query = String.format("q=%s&cursorMark=%s&fl=pid&rows=%d&sort=pid+asc", this.filterQuery, cursor, rows);
        String solrResponseXml = solrAccess.requestWithSelectReturningString(query, "xml");
        Document document = XMLUtils.parseDocument(new StringReader(solrResponseXml));
        
        Element result = XMLUtils.findElement(document.getDocumentElement(), new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                return element.getNodeName().equals("result");
            }
        });
        
        if (result != null) {

            String number = result.getAttribute("numFound");
            
            String solrNextCursor = null;
            Element cursorElement = XMLUtils.findElement(document.getDocumentElement(), (element) -> {
                if (element.getNodeName().equals("str") && element.getAttribute("name").equals("nextCursorMark")) {
                    return true;
                } else return false;
            });
                
            if (cursorElement != null) {
                solrNextCursor = cursorElement.getTextContent();
            }

            List<Element> docs = XMLUtils.getElements(result, new XMLUtils.ElementsFilter() {
                @Override
                public boolean acceptElement(Element element) {
                    return element.getNodeName().equals("doc");
                }
            });

            List<OAIRecord> records = docs.stream().map(doc-> {
                Element pidElm = XMLUtils.findElement(doc, "str");
                String oaiIdentifier =  OAITools.oaiIdentfier(host, pidElm.getTextContent());
                return new OAIRecord(pidElm.getTextContent(), oaiIdentifier);
            }).collect(Collectors.toList());
            
            String nextCursor = records.size() == rows && solrNextCursor != null ? solrNextCursor+":"+this.setSpec+":"+metadataPrefix : null; 
            OAIResults results = new OAIResults(Integer.parseInt(number), nextCursor, metadataPrefix, records);
            return results;
        } else {
            return null;
        }
    }
}
