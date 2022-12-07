package cz.incad.kramerius.services.workers.replicate;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.sun.jersey.api.client.Client;

import cz.incad.kramerius.services.Worker;
import cz.incad.kramerius.services.iterators.IterationItem;
import cz.incad.kramerius.services.transform.SourceToDestTransform;
import cz.incad.kramerius.services.utils.SolrUtils;
import cz.incad.kramerius.utils.XMLUtils;

public abstract class AbstractReplicateWorker extends Worker {

	public static final String DEFAULT_FIELDLIST = "PID timestamp fedora.model document_type handle status created_date modified_date parent_model "
			+ "parent_pid parent_pid parent_title root_model root_pid root_title text_ocr pages_count "
			+ "datum_str datum rok datum_begin datum_end datum_page issn mdt ddt dostupnost keywords "
			+ "geographic_names collection sec model_path pid_path rels_ext_index level dc.title title_sort "
			+ "title_sort dc.creator dc.identifier language dc.description details facet_title browse_title browse_autor img_full_mime viewable "
			+ "virtual location range mods.shelfLocator mods.physicalLocation text dnnt dnnt-labels";

	public static final String DEFAULT_PID_FIELD = "PID";
	public static final String COLLECTION_FIELD = "collection";

	// default field list
	protected String fieldList = DEFAULT_FIELDLIST;
	
	protected String onIndexedFieldList = null;
	protected String onUpdateFieldList = null;
	
	
	protected String idIdentifier = DEFAULT_PID_FIELD;
	protected String collectionField = COLLECTION_FIELD;
	protected boolean compositeId = false;
	protected String rootOfComposite = null;
	protected String childOfComposite = null;
	protected String checkUrl = null;
	protected String checkEndpoint = null;
	protected SourceToDestTransform transform;

	public AbstractReplicateWorker(Element workerElm, Client client, List<IterationItem> items) {
		super(workerElm, client, items);
	}

	protected ReplicateContext findPidsAlreadyIndexed(List<String> subpids, SourceToDestTransform transform)
			throws ParserConfigurationException, SAXException, IOException {

		// vsechny pidy 
		String reduce = subpids.stream().map(it -> {
			return '"' + it + '"';
		}).collect(Collectors.joining(" OR "));

		// field list je pid + collection + rootpid
		String fieldlist = this.transform.getField(idIdentifier) + " " + collectionField;
		if (compositeId) {
			fieldlist = fieldlist + " " + this.transform.getField(this.rootOfComposite);
			if (!idIdentifier.equals(childOfComposite)) {
				fieldlist = fieldlist + " " + this.transform.getField(this.childOfComposite);
			}
		}

		String query = "?q=" + this.transform.getField(idIdentifier) + ":(" + URLEncoder.encode(reduce, "UTF-8")
				+ ")&fl=" + URLEncoder.encode(fieldlist, "UTF-8") + "&wt=xml&rows=" + subpids.size();

		String checkUrl = this.checkUrl + (this.checkUrl.endsWith("/") ? "" : "/") + this.checkEndpoint;
		Element resultElem = XMLUtils.findElement(SolrUtils.executeQuery(client, checkUrl, query, this.user, this.pass),
			(elm) -> {
				return elm.getNodeName().equals("result");
		});

		List<Element> docs = XMLUtils.getElements(resultElem);
		List<Map<String, String>> list = new ArrayList<>();
		docs.stream().forEach(d -> {
			Map<String, String> map = new HashMap<>();
			Element pid = XMLUtils.findElement(d, e -> {
				return e.getAttribute("name").equals(this.transform.getField(idIdentifier));
			});
			if (pid != null) {
				map.put(this.transform.getField(idIdentifier), pid.getTextContent());
			}
			Element collection = XMLUtils.findElement(d, e -> {
				return e.getAttribute("name").equals(collectionField);
			});
			if (collection != null) {
				map.put(collectionField, collection.getTextContent());
			}

			if (compositeId) {
				Element compositeRoot = XMLUtils.findElement(d, e -> {
					return e.getAttribute("name").equals(this.transform.getField(rootOfComposite));
				});
				if (compositeRoot != null) {
					map.put(this.transform.getField(rootOfComposite), compositeRoot.getTextContent());
				}

				Element compositeChild = XMLUtils.findElement(d, e -> {
					return e.getAttribute("name").equals(childOfComposite);
				});

				if (compositeChild != null) {
					map.put(this.transform.getField(childOfComposite), compositeChild.getTextContent());
				}
			}
			list.add(map);
		});

		List<String> pidsFromLocalSolr = list.stream().map(m -> {
			return m.get(this.transform.getField(idIdentifier));
		}).collect(Collectors.toList());


		List<String> notindexed = new ArrayList<>();
		subpids.forEach(pid -> {
			if (!pidsFromLocalSolr.contains(pid))
				notindexed.add(pid);
		});

		return new ReplicateContext(list, notindexed);
	}

	
	protected void config(Element workerElm) {
		Element requestElm = XMLUtils.findElement(workerElm, "request");
	    if (requestElm != null) {

	    	// on index 
	        Element onIndexFieldList = XMLUtils.findElement(requestElm, "onindex");
	        if (onIndexFieldList != null) {
	        	Element oIfieldlist = XMLUtils.findElement(onIndexFieldList, "fieldlist");
	        	if (oIfieldlist != null) {
	        		this.onIndexedFieldList = oIfieldlist.getTextContent();
	        	}
	        }
	        
	        //on update
	        Element onUpdateFieldList = XMLUtils.findElement(requestElm, "onupdate");
	        if (onUpdateFieldList != null) {
	        	Element oIfieldlist = XMLUtils.findElement(onUpdateFieldList, "fieldlist");
	        	if (oIfieldlist != null) {
	        		this.onUpdateFieldList = oIfieldlist.getTextContent();
	        	}
	        }
	        
	        
	    	// default field list
	        Element fieldlistElm = XMLUtils.findElement(requestElm, "fieldlist");
	        if (fieldlistElm != null) {
	            fieldList = fieldlistElm.getTextContent();
	        }
	        
	        // Id
	        Element idElm = XMLUtils.findElement(requestElm, "id");
	        if (idElm != null) {
	            idIdentifier = idElm.getTextContent();
	        }
	
	        // transform
	        Element transformFormat = XMLUtils.findElement(requestElm, "trasfrom");
	        if (transformFormat != null) {
	            this.transform = SourceToDestTransform.Format.findTransform(transformFormat.getTextContent());
	        } else {
	            this.transform = SourceToDestTransform.Format.COPY.create();
	        }
	
	        // collection
	        Element collectionElm = XMLUtils.findElement(requestElm, "collection");
	        if (collectionElm != null) {
	            collectionField = collectionElm.getTextContent();
	        }
	
	        // Composite id if there is solr cloud
	        Element compositeIdElm = XMLUtils.findElement(requestElm, "composite.id");
	        if (compositeIdElm != null) {
	            compositeId = Boolean.parseBoolean(compositeIdElm.getTextContent());
	
	            Element compositeRootElm = XMLUtils.findElement(requestElm, "composite.root");
	            if (compositeRootElm != null) {
	                this.rootOfComposite = compositeRootElm.getTextContent();
	            }
	            Element compositeChildElm = XMLUtils.findElement(requestElm, "composite.child");
	            if (compositeChildElm != null) {
	                this.childOfComposite = compositeChildElm.getTextContent();
	            }
	        }
	        // Check url; for checking if the document is already indexed
	        Element checkUrlElm = XMLUtils.findElement(requestElm, "checkUrl");
	        if (checkUrlElm != null) {
	            this.checkUrl = checkUrlElm.getTextContent();
	        }
	
	        // Check url endpoint; for checking if the document is already indexed
	        Element checkEndpointElm = XMLUtils.findElement(requestElm, "checkEndpoint");
	        if (checkEndpointElm != null) {
	            this.checkEndpoint = checkEndpointElm.getTextContent();
	        }
	    }
	}

	public Element fetchDocumentFromRemoteSOLR(Client client, List<String> pids, String fieldlist)
			throws IOException, SAXException, ParserConfigurationException {
	    String reduce = pids.stream().reduce("", (i, v) -> {
	        if (!i.equals("")) {
	            return i + " OR \"" + v+"\"";
	        } else {
	            return '"'+v+'"';
	        }
	    });
	    String query =  "?q="+idIdentifier+":(" + URLEncoder.encode(reduce, "UTF-8") + ")&fl=" + URLEncoder.encode(fieldlist, "UTF-8")+"&wt=xml&rows="+pids.size();
	    return SolrUtils.executeQuery(client, this.requestUrl , query, this.user, this.pass);
	}
}
