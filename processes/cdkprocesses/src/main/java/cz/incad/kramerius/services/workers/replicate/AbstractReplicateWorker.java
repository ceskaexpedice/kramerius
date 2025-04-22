package cz.incad.kramerius.services.workers.replicate;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import cz.incad.kramerius.services.utils.KubernetesSolrUtils;
import cz.incad.kramerius.services.utils.ResultsUtils;
import cz.incad.kramerius.services.workers.replicate.records.ExistingConflictRecord;
import cz.incad.kramerius.services.workers.replicate.records.IndexedRecord;
import cz.incad.kramerius.services.workers.replicate.records.ReplicateRecord;
import cz.incad.kramerius.utils.StringUtils;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.sun.jersey.api.client.Client;

import cz.incad.kramerius.services.Worker;
import cz.incad.kramerius.services.WorkerFinisher;
import cz.incad.kramerius.services.iterators.IterationItem;
import cz.incad.kramerius.services.transform.SourceToDestTransform;
import cz.incad.kramerius.utils.XMLUtils;


public abstract class AbstractReplicateWorker extends Worker {

    /**
     * Default field list for copying fields from source library
      */
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

    public AbstractReplicateWorker(String sourceName, String introspectUrl, Element workerElm, Client client, List<IterationItem> items, WorkerFinisher finisher) {
        super(sourceName, introspectUrl, workerElm, client, items, finisher);
    }

    // zjistuje, ziskava vsechny pidy, ktere jsou naindexovane
    protected CDKReplicateContext createReplicateContext(List<IterationItem> subitems, SourceToDestTransform transform)  throws ParserConfigurationException, SAXException, IOException {

        // vsechny pidy
        String reduce = subitems.stream().map(it -> {
            return '"' + it.getPid() + '"';
        }).collect(Collectors.joining(" OR "));
        
        List<String> computedFields = Arrays.asList("cdk.licenses", "cdk.licenses_of_ancestors cdk.contains_licenses");
        String fieldlist = "pid " + collectionField +" cdk.leader cdk.collection "+computedFields.stream().collect(Collectors.joining(" "));
        if (compositeId) {
            fieldlist = fieldlist + " " + " root.pid compositeId";
        }

        String query = "?q=" + "pid" + ":(" + URLEncoder.encode(reduce, "UTF-8")
                + ")&fl=" + URLEncoder.encode(fieldlist, "UTF-8") + "&wt=xml&rows=" + subitems.size();

        String checkUrl = this.checkUrl + (this.checkUrl.endsWith("/") ? "" : "/") + this.checkEndpoint;
        Element resultElem = XMLUtils.findElement(KubernetesSolrUtils.executeQueryJersey(client, checkUrl, query),
                (elm) -> {
                    return elm.getNodeName().equals("result");
                });

        List<Element> docElms = XMLUtils.getElements(resultElem);
        List<Map<String, Object>>  docs = docElms.stream().map(d -> {
            Map<String, Object> map = ResultsUtils.doc(d);
            return map;
        }).collect(Collectors.toList());


        List<IndexedRecord> indexedRecordList = new ArrayList<>();
        // Existing conficts - remove from docElms
        List<ExistingConflictRecord> econflicts = findIndexConflict(docs);
        removePids(econflicts, docs);

        List<String> econflictPids = econflicts.stream().map(ExistingConflictRecord::getPid).collect(Collectors.toList());

        // found indexed & not indexed records
        docElms.stream().forEach(d -> {
            Map<String, Object> map = ResultsUtils.doc(d);

            Element collection = XMLUtils.findElement(d, e -> {
                return e.getAttribute("name").equals(collectionField);
            });

            if (collection != null) {
                map.put(collectionField, collection.getTextContent());
            }

            // computed fields
            computedFields.stream().forEach(it-> computedField(d, map,it));

            IndexedRecord record = new IndexedRecord(map);
            if (!econflictPids.contains(record.getPid())) {
                indexedRecordList.add(record);
            }
        });

        List<String> pidsFromLocalSolr = indexedRecordList.stream().map(m -> {
            return m.getPid();
        }).collect(Collectors.toList());

        List<IterationItem> notindexed = new ArrayList<>();
        subitems.forEach(item -> {
            if (!pidsFromLocalSolr.contains(item.getPid()) && !econflictPids.contains(item.getPid()))
                notindexed.add(item);
        });

        return new CDKReplicateContext(indexedRecordList, econflicts, notindexed);
    }


    private static void removePids(List<? extends ReplicateRecord> conflicts, List<Map<String, Object>> docs) {
        Set<String> pids = conflicts.stream()
                .map(ReplicateRecord::getPid)
                .collect(Collectors.toSet());
        docs.removeIf(doc -> pids.contains(doc.get("pid")));
    }

    private List<ExistingConflictRecord> findIndexConflict(List<Map<String,Object>> docs) {
        Map<String, List<String>> pidToCompositeIds = docs.stream()
                .filter(map -> {
                    String pid = (String) map.get(this.transform.getField(childOfComposite));
                    String compositeId = (String) map.get("compositeId");
                    return StringUtils.isAnyString(pid) && StringUtils.isAnyString(compositeId);
                })
                .collect(Collectors.groupingBy(
                        map -> (String) map.get(this.transform.getField(childOfComposite)),
                        Collectors.mapping(map -> (String) map.get("compositeId"), Collectors.toList())
                ));


        return pidToCompositeIds.entrySet().stream()
                .map(entry -> new ExistingConflictRecord(entry.getKey(),
                        entry.getValue().stream().distinct().collect(Collectors.toList())))
                .filter(ExistingConflictRecord::isConflict)
                .collect(Collectors.toList());
    }


    private void computedField(Element d, Map<String, Object> map, String fieldName) {
        Element cdkLicenses = XMLUtils.findElement(d, e -> {
            return e.getAttribute("name").equals(fieldName);
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

        return KubernetesSolrUtils.executeQueryJersey(client, this.requestUrl, query);
    }
}
