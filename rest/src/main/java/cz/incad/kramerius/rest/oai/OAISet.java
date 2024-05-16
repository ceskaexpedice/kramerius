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
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.core.UriBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.apiNew.ConfigManager;
import cz.incad.kramerius.rest.apiNew.client.v60.filter.ProxyFilter;
import cz.incad.kramerius.solr.SolrFieldsMapping;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.XMLUtils;

public class OAISet {
    
    public static final String DEFAULT_SET_KEYWORD = "DEFAULT";
    
    private String host;
    private String setSpec;
    private String setName;
    private String setDescription;
    private String filterQuery;

    private Map<String, String> additionalsInfo = new HashMap<>();
    
    public OAISet(
            String host, 
            String setSpec, 
            String setName, 
            String setDescription, 
            String filterQuery) {
        this(host, setSpec, setName, setDescription, filterQuery, new HashMap<>());
    }

    public OAISet(
            String host, 
            String setSpec, 
            String setName, 
            String setDescription, 
            String filterQuery, Map<String, String> map) {
        this.setSpec = setSpec;
        this.setName = setName;
        this.setDescription = setDescription;
        this.filterQuery = filterQuery;
        this.host = host;
        this.additionalsInfo = map;
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
    
    public Map<String, String> getAdditionalsInfo() {
        return additionalsInfo;
    }
    
    public void storeInitConfig(ConfigManager confMapanger) {
    }
    
    public boolean isMyResumptionToken(String resumptionToken) {
        String spec = OAITools.specFromResumptionToken(resumptionToken);
        return spec.equals(getSetSpec());
    }
    
    public void initToConfig(ConfigManager configManager) {

        String filter = String.format("oai.set.%s.filter", this.setSpec);
        String name = String.format("oai.set.%s.name", this.setSpec);
        String desc = String.format("oai.set.%s.description", this.setSpec);
        
        String property = configManager.getProperty(filter);
        if (property == null) {
            configManager.setProperty(filter, this.getFilterQuery());
            configManager.setProperty(name, this.getSetName());
            configManager.setProperty(desc, this.getSetDescription());
        }
    }
    
    
    public int numberOfDoc(ProxyFilter proxyFilter, SolrAccess solrAccess) throws IOException, ParserConfigurationException, SAXException {
        String query = String.format("q=%s&fl=pid&rows=%d&sort=pid+asc", this.filterQuery,  0);
        if (proxyFilter.newFilter() != null) {
            query = query + String.format("&fq=%s",  URLEncoder.encode(proxyFilter.newFilter(), "UTF-8"));
        }
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
            return Integer.parseInt(number);
        }
        
        return -1;
    }
    
    public OAIResults findRecords(ProxyFilter proxyFilter, SolrAccess solrAccess,String cursor, String metadataPrefix, int rows) throws IOException, ParserConfigurationException, SAXException {
        String fq = proxyFilter.newFilter();
        String query =  String.format("q=%s&cursorMark=%s&fl=pid+cdk.leaders+cdk.collection+cdk.leader&rows=%d&sort=compositeId+asc", this.filterQuery, cursor, rows);
        if (fq != null) {
            String encodedFq =  URLEncoder.encode(fq, "UTF-8");
            query = query + String.format("&fq=%s", encodedFq);
        }
        
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
                
                Element pidElm = XMLUtils.findElement(doc, new XMLUtils.ElementsFilter() {
                    @Override
                    public boolean acceptElement(Element element) { 
                        String name = element.getAttribute("name");
                        return name.equals("pid");
                        
                    }
                });
                
                
                List<Element> collections  = XMLUtils.getElements(doc, new XMLUtils.ElementsFilter() {
                    @Override
                    public boolean acceptElement(Element element) { 
                        String name = element.getAttribute("name");
                        return name.equals("cdk.collection");
                        
                    }
                });
                List<String> cdkCollections = collections.stream().map(Element::getTextContent).collect(Collectors.toList());
                String oaiIdentifier =  OAITools.oaiIdentfier(host, pidElm.getTextContent());
                OAIRecord oaiRecord = new OAIRecord(pidElm.getTextContent(), oaiIdentifier);
                oaiRecord.setCdkCollections(cdkCollections);
                return oaiRecord;
            }).collect(Collectors.toList());
            
            String nextCursor = records.size() == rows && solrNextCursor != null ? solrNextCursor+":"+this.setSpec+":"+metadataPrefix : null; 
            OAIResults results = new OAIResults(Integer.parseInt(number), nextCursor, metadataPrefix, records);
            
            return results;
        } else {
            return null;
        }
    }
}
