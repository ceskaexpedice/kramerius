package cz.incad.kramerius.rest.apiNew.client.v60.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Element;

import com.google.inject.Inject;

import cz.incad.kramerius.rest.apiNew.client.v60.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v60.libs.OneInstance;
import cz.incad.kramerius.rest.apiNew.client.v60.libs.PhysicalLocationMap;
import cz.incad.kramerius.rest.apiNew.client.v60.libs.properties.DefaultPropertiesInstances;
import cz.incad.kramerius.utils.XMLUtils;

public class DefaultFilter implements ProxyFilter{
	
	private Instances libraries;
	
	@Inject
	public DefaultFilter(Instances libraries) {
		super();
		this.libraries = libraries;
	}

	@Override
	public String newFilter() {
		if (this.libraries.isAnyDisabled()) {
			return filter();
		} else return null;
	}

	
	private String filter() {
		if (this.libraries.isAnyDisabled()) {
			List<String> eInsts = libraries.enabledInstances().stream().map(OneInstance::getName).collect(Collectors.toList());
			//List<String> eInsts = libraries.enabledInstances().stream().map(OneInstance::getName)::getName).;
			String enabled = eInsts.stream().collect(Collectors.joining(" OR "));
			return "cdk.collection:("+enabled+")";
		} else {
			return null;
		}
	}

	@Override
	public String enhancedFilter(String f) {
		if (this.libraries.isAnyDisabled()) {
			return f+" AND "+filter();
		} else return f;
	}
	
	
	
    @Override
    public void filterValue(Element rawDoc) {
        // physical locations
        Element physicalLocationElm = XMLUtils.findElement(rawDoc, new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                String name = element.getAttribute("name");
                if (name != null && name.equals("physical_locations.facet")) {
                    return true;
                }
                return false;
            }
        });
        
        List<String> acronymsByPhysicalLocations =  physicalLocationElm != null ?  acronymsSortedByPhysicalLocation(XMLUtils.getElements(physicalLocationElm), libraries) : new ArrayList<>();

        List<String> disabledInstance = libraries.disabledInstances().stream().map(OneInstance::getName).collect(Collectors.toList());
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

    private List<String> acronymsSortedByPhysicalLocation(List<Element> locations, Instances instances) {
        PhysicalLocationMap physMap = new PhysicalLocationMap();
        List<String> textLocations = locations.stream().map(Element::getTextContent).map(String::trim).collect(Collectors.toList());
        List<String> acronymsByPhysicalLocations  = textLocations.stream().map(sigla-> {
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
        return acronymsByPhysicalLocations;
    }

    private List<String> acronymsSortedByPhysicalLocation(JSONArray locations, Instances instances) {
        if (locations != null) {
            PhysicalLocationMap physMap = new PhysicalLocationMap();

            List<String> textLocations = new ArrayList<>();
            for (int i = 0; i < locations.length(); i++) {  textLocations.add(locations.getString(i)); }

            List<String> acronymsByPhysicalLocations  = textLocations.stream().map(sigla-> {
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
            return acronymsByPhysicalLocations;
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public void filterValue(JSONObject rawDoc) {
        List<String> dInsts = libraries.disabledInstances().stream().map(OneInstance::getName).collect(Collectors.toList());
        if (rawDoc.has("cdk.collection")) {
            
            List<String> acronymsSortedByPhysicalLocation = acronymsSortedByPhysicalLocation(rawDoc.optJSONArray("physical_locations.facet"), libraries);
            
            JSONArray col = rawDoc.getJSONArray("cdk.collection");
            JSONArray nCol = new JSONArray();
            if (!acronymsSortedByPhysicalLocation.isEmpty()) {
                String leader = null;
                rawDoc.put("cdk.collection.sorted", true);
                
                List<String> collectionFromIndex = new ArrayList<>();
                for (int i = 0; i < col.length(); i++) { collectionFromIndex.add(col.getString(i)); }
                
                while(!acronymsSortedByPhysicalLocation.isEmpty()) {
                    String acronym = acronymsSortedByPhysicalLocation.remove(0);
                    if (leader  == null) leader = acronym;
                    nCol.put(acronym);
                    collectionFromIndex.remove(acronym);
                }

                for (int i = 0; i < collectionFromIndex.size(); i++) { 
                    if (!dInsts.contains(collectionFromIndex.get(i))) nCol.put(collectionFromIndex.get(i)); 
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

	
	
}
