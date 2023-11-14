package cz.incad.kramerius.services.workers.replicate;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
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
            + "virtual location range mods.shelfLocator mods.physicalLocation text dnnt dnnt-labels contains-dnnt-labels";

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

    public AbstractReplicateWorker(String sourceName, Element workerElm, Client client, List<IterationItem> items) {
        super(sourceName, workerElm, client, items);
    }

    // zjistuje, ziskava vsechny pidy, ktere jsou naindexovane 
    protected ReplicateContext findPidsAlreadyIndexed(List<String> subpids, SourceToDestTransform transform)
            throws ParserConfigurationException, SAXException, IOException {

        // vsechny pidy
        String reduce = subpids.stream().map(it -> {
            return '"' + it + '"';
        }).collect(Collectors.joining(" OR "));
        

        List<String> computedFields = Arrays.asList("cdk.licenses", "cdk.licenses_of_ancestors cdk.contains_licenses");
        String fieldlist = "pid " + collectionField +" cdk.leader cdk.collection "+computedFields.stream().collect(Collectors.joining(" "));
        if (compositeId) {
            fieldlist = fieldlist + " " + " root.pid compositeId";
        }

        String query = "?q=" + "pid" + ":(" + URLEncoder.encode(reduce, "UTF-8")
                + ")&fl=" + URLEncoder.encode(fieldlist, "UTF-8") + "&wt=xml&rows=" + subpids.size();

        String checkUrl = this.checkUrl + (this.checkUrl.endsWith("/") ? "" : "/") + this.checkEndpoint;
        Element resultElem = XMLUtils.findElement(SolrUtils.executeQuery(client, checkUrl, query, this.user, this.pass),
                (elm) -> {
                    return elm.getNodeName().equals("result");
                });

        List<Element> docs = XMLUtils.getElements(resultElem);

        List<Map<String, Object>> list = new ArrayList<>();
        docs.stream().forEach(d -> {
            List<String> simpleFields = Arrays.asList("str","date","int");
            Map<String, Object> map = new HashMap<>();
            List<Element> fields = XMLUtils.getElements(d);
            fields.stream().forEach(f-> {
               if (simpleFields.contains(f.getNodeName())) {
                   String name = f.getAttribute("name");
                   map.put(name, f.getTextContent());
               } else {
                   String name = f.getAttribute("name");
                   List<Element> elements = XMLUtils.getElements(f);
                   List<String> contents = elements.stream().map(Element::getTextContent).collect(Collectors.toList());
                   map.put(name, contents);
               }
            });
            
            
//            Element pid = XMLUtils.findElement(d, e -> {
//                return e.getAttribute("name").equals("pid");
//            });
//            
//            if (pid != null) {
//                map.put("pid", pid.getTextContent());
//            }

            Element collection = XMLUtils.findElement(d, e -> {
                return e.getAttribute("name").equals(collectionField);
            });
            
            if (collection != null) {
                map.put(collectionField, collection.getTextContent());
            }

//            Element cdkLeader = XMLUtils.findElement(d, e -> {
//                return e.getAttribute("name").equals("cdk.leader");
//            });
//
//            if (cdkLeader != null) {
//                map.put("cdk.leader", cdkLeader.getTextContent());
//            }
            
            // computed fields 
            computedFields.stream().forEach(it-> computedField(d, map,it));
            
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
            return m.get("pid").toString();
        }).collect(Collectors.toList());

        List<String> notindexed = new ArrayList<>();
        subpids.forEach(pid -> {
            if (!pidsFromLocalSolr.contains(pid))
                notindexed.add(pid);
        });

        return new ReplicateContext(list, notindexed);
    }

    private void computedField(Element d, Map<String, Object> map, String fieldName) {
        Element cdkLicenses = XMLUtils.findElement(d, e -> {
            return e.getAttribute("name").equals(fieldName);
            //return e.getAttribute("name").equals("cdk.licenses");
        });
        
        if (cdkLicenses != null) {
            List<String> licenses = XMLUtils.getElements(cdkLicenses).stream().map(Element::getTextContent).collect(Collectors.toList());
            map.put(fieldName, licenses);
        }
    }

    protected void config(Element workerElm) {
        Element destinationElm = XMLUtils.findElement(workerElm, "destination");
        if (destinationElm != null) {
            // on index
            Element onIndexFieldList = XMLUtils.findElement(destinationElm, "onindex");
            if (onIndexFieldList != null) {
                Element oIfieldlist = XMLUtils.findElement(onIndexFieldList, "fieldlist");
                if (oIfieldlist != null) {
                    this.onIndexedFieldList = oIfieldlist.getTextContent();
                }
            }

            // on update
            Element onUpdateFieldList = XMLUtils.findElement(destinationElm, "onupdate");
            if (onUpdateFieldList != null) {
                Element oIfieldlist = XMLUtils.findElement(onUpdateFieldList, "fieldlist");
                if (oIfieldlist != null) {
                    this.onUpdateFieldList = oIfieldlist.getTextContent();
                }
            }

        }
        
        Element requestElm = XMLUtils.findElement(workerElm, "request");
        if (requestElm != null) {

//            // on index
//            Element onIndexFieldList = XMLUtils.findElement(requestElm, "onindex");
//            if (onIndexFieldList != null) {
//                Element oIfieldlist = XMLUtils.findElement(onIndexFieldList, "fieldlist");
//                if (oIfieldlist != null) {
//                    this.onIndexedFieldList = oIfieldlist.getTextContent();
//                }
//            }
//
//            // on update
//            Element onUpdateFieldList = XMLUtils.findElement(destination, "onupdate");
//            if (onUpdateFieldList != null) {
//                Element oIfieldlist = XMLUtils.findElement(onUpdateFieldList, "fieldlist");
//                if (oIfieldlist != null) {
//                    this.onUpdateFieldList = oIfieldlist.getTextContent();
//                }
//            }
            
            

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
                return i + " OR \"" + v + "\"";
            } else {
                return '"' + v + '"';
            }
        });
        String query = "?q=" + idIdentifier + ":(" + URLEncoder.encode(reduce, "UTF-8") + ")&fl="
                + URLEncoder.encode(fieldlist, "UTF-8") + "&wt=xml&rows=" + pids.size();
        return SolrUtils.executeQuery(client, this.requestUrl, query, this.user, this.pass);
    }
}
