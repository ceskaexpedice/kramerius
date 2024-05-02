package cz.incad.kramerius.rest.apiNew.client.v60.filter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.inject.Named;
import javax.ws.rs.core.UriBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Element;

import com.google.inject.Inject;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.impl.SolrAccessImplNewIndex;
import cz.incad.kramerius.rest.apiNew.client.v60.SearchResource;
import cz.incad.kramerius.rest.apiNew.client.v60.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v60.libs.OneInstance;
import cz.incad.kramerius.rest.apiNew.client.v60.libs.PhysicalLocationMap;
import cz.incad.kramerius.rest.apiNew.client.v60.libs.properties.DefaultPropertiesInstances;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class DefaultFilter implements ProxyFilter{

    
    public static final Logger LOGGER = Logger.getLogger(DefaultFilter.class.getName());
    
    
    private Instances libraries;

    private SolrAccess solrAccess;

    @Inject
    public DefaultFilter(Instances libraries, @Named("new-index") SolrAccess solrAccess) {
        super();
        this.libraries = libraries;
        this.solrAccess = solrAccess;
    }

    @Override
    public String newFilter() {
        if (this.libraries.isAnyDisabled()) {
            return filter();
        } else
            return null;
    }

    private String filter() {
        if (this.libraries.isAnyDisabled()) {
            List<String> eInsts = libraries.enabledInstances().stream().map(OneInstance::getName)
                    .collect(Collectors.toList());
            // List<String> eInsts =
            // libraries.enabledInstances().stream().map(OneInstance::getName)::getName).;
            String enabled = eInsts.stream().collect(Collectors.joining("+OR+"));
            return "cdk.collection:(" + enabled + ")";
        } else {
            return null;
        }
    }

    @Override
    public String enhancedFilter(String f) {
        if (this.libraries.isAnyDisabled()) {
            return f + "+AND+" + filter();
        } else
            return f;
    }	

    

    @Override
    public void filterValue(Element rawDoc) {
        // find all acronyms translated from physical locations
        List<String> acronymsByPhysicalLocations =  acronymsSortedByPhysicalLocation(rawDoc, libraries);
        // list of disabled instances 
        List<String> disabledInstance = libraries.disabledInstances().stream().map(OneInstance::getName).collect(Collectors.toList());
        // cdk leader element
        Element cdkLeaderElement = XMLUtils.findElement(rawDoc, new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                String name = element.getAttribute("name");
                if (name != null && name.equals("cdk.leader")) {
                    return true;
                }
                return false;
            }
        });

        // all cdk libraries element
        Element cdkElement = XMLUtils.findElement(rawDoc, new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                String name = element.getAttribute("name");
                if (name != null && name.equals("cdk.collection")) {
                    return true;
                }
                return false;
        	}
        });

        if (cdkElement != null) {
            
            List<Element> realCollections = XMLUtils.getElements(cdkElement);
            List<String> realCollectionNames = realCollections.stream().map(Element::getTextContent).collect(Collectors.toList());

            // odebrani vsech 
            List<Element> elements = XMLUtils.getElements(cdkElement);
                elements.forEach(e-> {
                    synchronized(rawDoc.getOwnerDocument()) {
                        cdkElement.removeChild(e);
                    }
            });

            if (acronymsByPhysicalLocations != null && acronymsByPhysicalLocations.size() > 0) {
                
                String name = "cdk.collection.sorted";
                Element sortedElement = rawDoc.getOwnerDocument().createElement("bool");
                sortedElement.setAttribute("name", name);
                sortedElement.setTextContent("true");
                synchronized(rawDoc.getOwnerDocument()) {
                    rawDoc.appendChild(sortedElement);
                }

                Map<String, Element> map = new HashMap<>();
                realCollections.stream().forEach(re-> {
                    String acronym = re.getTextContent();
                    map.put(acronym, re);
                });
                
                synchronized(rawDoc.getOwnerDocument()) {
                    
                    acronymsByPhysicalLocations.forEach(sortedAc-> {
                        Element rElem = map.get(sortedAc);
                        if (!disabledInstance.contains(sortedAc)) {
                            cdkElement.appendChild(rElem);
                        }
                        realCollectionNames.remove(sortedAc);
                    });
                    
                    if (!realCollectionNames.isEmpty()) {
                        realCollectionNames.forEach(rc-> {
                            Element rElem = map.get(rc);
                            if (!disabledInstance.contains(rc)) {
                                cdkElement.appendChild(rElem);
                            }
                        });
                    }

                    if (cdkLeaderElement != null) {
                        cdkLeaderElement.setTextContent(acronymsByPhysicalLocations.get(0));
                    }
                    
                }
            } else {
            
                String name = "cdk.collection.sorted";
                Element sortedElement = rawDoc.getOwnerDocument().createElement("bool");
                sortedElement.setAttribute("name", name);
                sortedElement.setTextContent("false");
                synchronized(rawDoc.getOwnerDocument()) {
                    rawDoc.appendChild(sortedElement);
                }
                
                synchronized(rawDoc.getOwnerDocument()) {
                    elements.forEach(e-> {
                        String content = e.getTextContent().trim();
                            if (!disabledInstance.contains(content)) {
                                cdkElement.appendChild(e);
                            }
                    });
                }
            }
        }
	}

    private List<String> acronymsSortedByPhysicalLocation(Element rawDoc, Instances instances) {
        PhysicalLocationMap physMap = new PhysicalLocationMap();

        String model = findSingleElementByName(rawDoc,"model");
        String rootPid = findSingleElementByName(rawDoc,"root.pid");
        List<String> textLocations =  findArrayElementsByName(rawDoc, "physical_locations.facet");

        if (textLocations.size() == 0) {
            List<String> list = KConfiguration.getInstance().getConfiguration().getList("cdk.inferring_physical_locations.model" , Arrays.asList("periodicalitem","monographunit")).stream().map(Object::toString).collect(Collectors.toList());
            if (list.contains(model)) {
                textLocations =  inferPhysicalLocationFromPredecessor(solrAccess, rootPid);
            }
            return new ArrayList<>();
        } 
        
        return  textLocations.stream().map(sigla-> {
            return physMap.findBySigla(sigla);
        }).map(acronym-> {
            if (acronym != null) {
                if (instances.isEnabledInstance(acronym)) {
                    return acronym;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }).filter(acronym -> acronym != null).collect(Collectors.toList());
        
    }

    private String findSingleElementByName(Element rawDoc, String name) {
        Element elm = XMLUtils.findElement(rawDoc, new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                String name = element.getAttribute("name");
                if (name != null && name.equals(name)) {
                    return true;
                }
                return false;
            }
        });
        return elm != null ? elm.getTextContent().trim() : null;
    }


    private List<String> findArrayElementsByName(Element rawDoc, String name) {
        Element elm = XMLUtils.findElement(rawDoc, new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                String name = element.getAttribute("name");
                if (name != null && name.equals(name)) {
                    return true;
                }
                return false;
            }
        });
        if (elm != null) {
            List<Element> elements = XMLUtils.getElements(elm);
            return elements.stream().map(Element::getTextContent).map(String::trim).collect(Collectors.toList());
            
        } else return new ArrayList<>();
    }

    
    
    
    private List<String> acronymsSortedByPhysicalLocation(JSONObject rawDoc, Instances instances) {
        PhysicalLocationMap physMap = new PhysicalLocationMap();

        String model = rawDoc.optString("model");
        String rootPid = rawDoc.optString("root.pid");
        List<String> textLocations = new ArrayList<>();
        
        if (rawDoc.has("physical_locations.facet")) {
            JSONArray locations = rawDoc.optJSONArray("physical_locations.facet");
            for (int i = 0; i < locations.length(); i++) {
                textLocations.add(locations.getString(i));
            }
        } else {
            List<String> list = KConfiguration.getInstance().getConfiguration().getList("cdk.inferring_physical_locations.model" , Arrays.asList("periodicalitem","monographunit")).stream().map(Object::toString).collect(Collectors.toList());
            if (list.contains(model)) {
                textLocations =  inferPhysicalLocationFromPredecessor(solrAccess, rootPid);
            }
        }
        return  textLocations.stream().map(sigla-> {
            return physMap.findBySigla(sigla);
        }).map(acronym-> {
            if (acronym != null) {
                if (instances.isEnabledInstance(acronym)) {
                    return acronym;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }).filter(acronym -> acronym != null).collect(Collectors.toList());
    }

    @Override
    public void filterValue(JSONObject rawDoc) {
        List<String> dInsts = libraries.disabledInstances().stream().map(OneInstance::getName).collect(Collectors.toList());
        if (rawDoc.has("cdk.collection")) {
            
            List<String> acronymsSortedByPhysicalLocation = acronymsSortedByPhysicalLocation(rawDoc, libraries);
            
            JSONArray col = rawDoc.getJSONArray("cdk.collection");
            JSONArray nCol = new JSONArray();
            if (!acronymsSortedByPhysicalLocation.isEmpty()) {
                String leader = null;
                rawDoc.put("cdk.collection.sorted", true);
                
                List<String> processCollectionFromIndex = new ArrayList<>();
                List<String> allCollections = new ArrayList<>();
                for (int i = 0; i < col.length(); i++) { processCollectionFromIndex.add(col.getString(i)); allCollections.add(col.getString(i));}
                
                while(!acronymsSortedByPhysicalLocation.isEmpty()) {
                    String acronym = acronymsSortedByPhysicalLocation.remove(0);
                    if (allCollections.contains(acronym)) {
                        if (leader  == null) leader = acronym;
                        nCol.put(acronym);
                        processCollectionFromIndex.remove(acronym);
                    }
                }

                for (int i = 0; i < processCollectionFromIndex.size(); i++) { 
                    if (!dInsts.contains(processCollectionFromIndex.get(i))) nCol.put(processCollectionFromIndex.get(i)); 
                }
                
                if (leader != null) {
                    rawDoc.put("cdk.leader", leader);
                }
                
            } else {
                rawDoc.put("cdk.collection.sorted", false);

                for (int i = 0; i < col.length(); i++) {
                    String lib = col.getString(i);
                    if (!dInsts.contains(lib)) { nCol.put(lib); }
                }
            }
            rawDoc.put("cdk.collection", nCol);
        }
    }

	@Override
	public String enhanceFacetsTerms() {
		List<String> dInsts = libraries.disabledInstances().stream().map(OneInstance::getName).collect(Collectors.toList());
		if (!dInsts.isEmpty()) {
			String excludedTerms = dInsts.stream().collect(Collectors.joining(","));
			return excludedTerms;
		} else return null;
	}


	
	
	
    private static List<String> inferPhysicalLocationFromPredecessor(SolrAccess sa, String rootPid) {
        try {
            String encoded = URLEncoder.encode(String.format("root.pid:\"%s\"",rootPid), "UTF-8");
            String fl = URLEncoder.encode("pid physical_locations.facet own_pid_path","UTF-8");
            String solrResponseJson = sa.requestWithSelectReturningString( String.format("q=%s+AND+physical_locations.facet:*&fl=%s&rows=4000",encoded,fl), "json");
            JSONObject response = new JSONObject(solrResponseJson);
            
            List<String> physicalLocations = new ArrayList<>();
            JSONArray jsonArray = response.getJSONObject("response").getJSONArray("docs");
            if (jsonArray.length() > 0 ) {
                JSONObject oneDoc = jsonArray.getJSONObject(0);
                JSONArray physJsonArray = oneDoc.getJSONArray("physical_locations.facet");
                for (int i = 0; i < physJsonArray.length(); i++) {
                    physicalLocations.add(physJsonArray.getString(i));
                }
            }
            return physicalLocations;
        } catch (JSONException | IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return new ArrayList<>();
        }
    }
	
}
