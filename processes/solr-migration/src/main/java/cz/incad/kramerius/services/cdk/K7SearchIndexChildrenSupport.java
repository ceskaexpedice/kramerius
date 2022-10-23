package cz.incad.kramerius.services.cdk;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.sun.jersey.api.client.Client;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.repository.KrameriusRepositoryApi.FosterRelationsMapping;
import cz.incad.kramerius.repository.KrameriusRepositoryApi.OwnRelationsMapping;
import cz.incad.kramerius.repository.RepositoryApi;
import cz.incad.kramerius.repository.RepositoryApi.Triplet;
import cz.incad.kramerius.service.MigrateSolrIndexException;
import cz.incad.kramerius.services.IterationUtils;
import cz.incad.kramerius.services.IterationUtils.IterationCallback;
import cz.incad.kramerius.services.IterationUtils.IterationContext;
import cz.incad.kramerius.services.IterationUtils.IterationEndCallback;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

//TODO: Move to common
public class K7SearchIndexChildrenSupport {

	// TODO: Remove
	public static void ownChildrenAndFosterChildren(SolrAccess solrAccess, String objectPid, String source, List<RepositoryApi.Triplet> ownChildrenTriplets, List<RepositoryApi.Triplet> fosterChildrenTriplets) throws IOException, ParserConfigurationException, MigrateSolrIndexException, SAXException, InterruptedException, BrokenBarrierException {
		org.w3c.dom.Document solrDataByPid = solrAccess.getSolrDataByPid(objectPid);
	    List<Element> parentModel = XMLUtils.getElementsRecursive(solrDataByPid.getDocumentElement(), (elm) -> {
	        String name = elm.getAttribute("name");
	        return (elm.getNodeName().equals("str") && name.equals("model"));
	    });
	
	    // use composite id in solr cloud
	    String id = KConfiguration.getInstance().getConfiguration().getString("kramerius.api.id", "compositeId");
	    IterationUtils.IterationContext ownParentContext = new IterationUtils.IterationContext(id, 100, Arrays.asList("pid","own_parent","model", "foster_parents.pids"));
	    // pridat source
	    String ownparentQuery =  "own_parent.pid:" +URLEncoder.encode("\""+objectPid+"\"", "UTF-8");
	    if (ownparentQuery != null) {
	    	ownparentQuery = ownparentQuery + URLEncoder.encode(" AND cdk.collection:"+source,"UTF-8");
	    }

	    IterationUtils.queryPaginationIteration(solrAccess, ownparentQuery, (results,iterationToken)-> {
	        List<Map<String, Object>> collectedDocuments = K7SearchIndexChildrenSupport.resultsToMap(results);
	        collectedDocuments.stream().filter(hmap -> {
	            return (hmap.containsKey("pid") && !hmap.get("pid").equals(objectPid) && hmap.containsKey("model"));
	        }).forEach(hmap -> {
	            Object model = hmap.get("model");
	            OwnRelationsMapping mapping = OwnRelationsMapping.find(model.toString());
	            String relationName = mapping.relation().toString();
	            ownChildrenTriplets.add(new RepositoryApi.Triplet(objectPid, relationName, hmap.get("pid").toString()));
	        });
	    }, ()->{}, ownParentContext, "rels_ext_index.sort ASC");

	    
	    //queryPaginationIteration(Client client, String address, String masterQuery,IterationCallback callback, IterationEndCallback endCallback, IterationContext context) throws MigrateSolrIndexException, IOException, SAXException, ParserConfigurationException, BrokenBarrierException, InterruptedException {
	    //cursorIteration(SolrAccess solrAccess,  String masterQuery, IterationCallback callback, IterationEndCallback endCallback, IterationContext context) throws ParserConfigurationException, MigrateSolrIndexException, SAXException, IOException, InterruptedException, BrokenBarrierException {

	    
	    IterationUtils.cursorIteration(solrAccess, ownparentQuery, (results,iterationToken)-> {
	        List<Map<String, Object>> collectedDocuments = K7SearchIndexChildrenSupport.resultsToMap(results);
	        collectedDocuments.stream().filter(hmap -> {
	            return (hmap.containsKey("pid") && !hmap.get("pid").equals(objectPid) && hmap.containsKey("model"));
	        }).forEach(hmap -> {
	            Object model = hmap.get("model");
	            OwnRelationsMapping mapping = OwnRelationsMapping.find(model.toString());
	            String relationName = mapping.relation().toString();
	            ownChildrenTriplets.add(new RepositoryApi.Triplet(objectPid, relationName, hmap.get("pid").toString()));
	        });
	    }, ()->{}, ownParentContext);
	
	    if (parentModel != null) {
	    	
	        IterationUtils.IterationContext fosterContext = new IterationUtils.IterationContext(id, 100, Arrays.asList("pid","own_parent","model", "foster_parents.pids"));
	
	        String fosterQuery = "foster_parents.pids:"+ URLEncoder.encode("\""+objectPid+"\"", "UTF-8");;
	        IterationUtils.cursorIteration(solrAccess, fosterQuery, (results,iterationToken)-> {
	            List<Map<String, Object>> collectedDocuments = K7SearchIndexChildrenSupport.resultsToMap(results);
	            collectedDocuments.stream().filter(hmap -> {
	                return (hmap.containsKey("pid") && !hmap.get("pid").equals(objectPid) && hmap.containsKey("model"));
	            }).forEach(hmap -> {
	                Object model = hmap.get("model");
	                // pokud je parent article nebo internal part, pak
	                FosterRelationsMapping mapping = FosterRelationsMapping.find(model.toString());
	                fosterChildrenTriplets.add(new RepositoryApi.Triplet(objectPid, mapping.relation(parentModel.get(0).getTextContent()).toString(), hmap.get("pid").toString()));
	            });
	
	        }, ()->{}, fosterContext);
	    }
	}

	public static List<Map<String, Object>> resultsToMap(Element results) {
	
	    return XMLUtils.getElementsRecursive(results, (elm) -> {
	            return elm.getNodeName().equals("doc");
	        }).stream().map(doc -> {
	            Map<String, Object> hashMapDocument = new HashMap<>();
	            XMLUtils.getElements(doc, (field) -> {
	                String name = field.getAttribute("name");
	                if (name.equals("foster_parents.pids")) {
	                    List<String> values = XMLUtils.getElements(field).stream().map(arrayElm -> {
	                        return arrayElm.getTextContent();
	                    }).collect(Collectors.toList());
	                    hashMapDocument.put("foster_parents.pids", values);
	                } else {
	                    hashMapDocument.put(name, field.getTextContent());
	                }
	                return false;
	            });
	            return hashMapDocument;
	    }).collect(Collectors.toList());
	}

}
