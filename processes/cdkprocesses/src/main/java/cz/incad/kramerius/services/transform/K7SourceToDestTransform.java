package cz.incad.kramerius.services.transform;

import cz.incad.kramerius.KrameriusModels;
import cz.incad.kramerius.services.workers.replicate.copy.CopyReplicateConsumer;
import cz.incad.kramerius.services.workers.replicate.copy.CopyReplicateConsumer.ModifyFieldResult;
import cz.incad.kramerius.services.workers.replicate.k7date.DateExtractor;
import cz.incad.kramerius.services.workers.replicate.k7date.DateInfo;
import cz.incad.kramerius.services.workers.replicate.k7date.MyDateTimeUtils;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.UTFSort;
import cz.incad.kramerius.utils.XMLUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class K7SourceToDestTransform extends SourceToDestTransform {

    public static final Logger LOGGER = Logger.getLogger(K7SourceToDestTransform.class.getName());


    private Map<String,List<String>> plainValueFields = new HashMap<>();

    private Map<String,String> restValueFields = new HashMap<>();
    private Map<String, String> sortValueFields = new HashMap<>();
    private Map<String, String> firstValue = new HashMap<>();

    public K7SourceToDestTransform() {
        // Plain copy fields
        plainValueFields.put("PID", Arrays.asList("pid"));
        plainValueFields.put("fedora.model", Arrays.asList("model"));
        plainValueFields.put("created_date", Arrays.asList("created"));
        plainValueFields.put("modified_date", Arrays.asList("modified", "indexed"));
        plainValueFields.put("timestamp", Arrays.asList("indexed"));
        plainValueFields.put("keywords",  Arrays.asList("keywords.search","keywords.facet"));
        plainValueFields.put("geographic_names",  Arrays.asList("geographic_names.search","geographic_names.facet"));
        plainValueFields.put("search_autor",  Arrays.asList("authors","authors.search"));
        plainValueFields.put("facet_autor",  Arrays.asList("authors.facet"));
        
        plainValueFields.put("dc.title",  Arrays.asList("title.search","titles.search"));
        //plainValueFields.put("dc.title",  Arrays.asList("titles.search"));

        plainValueFields.put("keywords",  Arrays.asList("keywords.search", "keywords.facet"));
        plainValueFields.put("root_pid",  Arrays.asList("root.pid"));
        plainValueFields.put("root_model",  Arrays.asList("root.model"));
        plainValueFields.put("root_title",  Arrays.asList("root.title"));
        plainValueFields.put("pid_path",  Arrays.asList("pid_paths"));
        plainValueFields.put("parent_model",  Arrays.asList("own_parent.model"));
        plainValueFields.put("parent_title",  Arrays.asList("own_parent.title"));
        plainValueFields.put("parent_pid",  Arrays.asList("foster_parents.pids"));
        plainValueFields.put("mtd",  Arrays.asList("mtd"));
        plainValueFields.put("ddt",  Arrays.asList("ddt"));
        plainValueFields.put("level",  Arrays.asList("level"));
        plainValueFields.put("mods.physicalLocation",  Arrays.asList("physical_locations.facet"));
        plainValueFields.put("mods.shelfLocator",  Arrays.asList("shelf_locators"));
        plainValueFields.put("dostupnost",  Arrays.asList("accessibility"));
        plainValueFields.put("img_full_mime",  Arrays.asList("ds.img_full.mime"));
        plainValueFields.put("language",  Arrays.asList("languages.facet"));
        plainValueFields.put("datum_str",  Arrays.asList("date.str"));
        plainValueFields.put("dnnt-labels",  Arrays.asList("licenses"));
        plainValueFields.put("contains-dnnt-labels",  Arrays.asList("contains_licenses"));
        plainValueFields.put("text_ocr",  Arrays.asList("text_ocr"));
        
        // all values but first
        restValueFields.put("parent_pid","foster_parents.pids");

        // sort values
        sortValueFields.put("dc.title","title.sort");
        //sortValueFields.put("dc.title","title.search");
        sortValueFields.put("root_title","root.title.sort");

        // first values
        firstValue.put("pid_path","own_pid_path");
        firstValue.put("model_path","own_model_path");
        firstValue.put("rels_ext_index",  "rels_ext_index.sort");
        firstValue.put("parent_pid",  "own_parent.pid");
    }


    @Override
    public void transform(Element sourceDocElm, Document destDocument, Element destDocElem, CopyReplicateConsumer consumer) {
        if (sourceDocElm.getNodeName().equals("doc")) {

            Map<String, List<String>> document = new HashMap<>();

            NodeList childNodes = sourceDocElm.getChildNodes();
            for (int j = 0,lj=childNodes.getLength(); j < lj; j++) {
                Node node = childNodes.item(j);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    List<String> primitiveVals = Arrays.asList("str","int","bool", "date");
                    if (primitiveVals.contains(node.getNodeName())) {
                        Element srcElm = (Element) node;
                        String name = srcElm.getAttribute("name");
                        if (this.plainValueFields.containsKey(name)) {
                            List<String> values = this.plainValueFields.get(name);
                            values.stream().forEach(v-> {
                                if (!document.containsKey(v)) {
                                    document.put(v, new ArrayList<String>());
                                }
                                document.get(v).add(node.getTextContent());
                                field(destDocument, destDocElem, node.getTextContent(), v,consumer);
                            });
                        }
                    } else {
                        arrayValue(null, sourceDocElm, destDocument, destDocElem, node,consumer);
                    }
                }
            }

            // sorting fields
            XMLUtils.getElements(sourceDocElm, new XMLUtils.ElementsFilter() {
                @Override
                public boolean acceptElement(Element element) {
                    String nodeName = element.getNodeName();
                    if (nodeName.equals("str")) {
                        String nameAttr = element.getAttribute("name");
                        if (nameAttr != null) return sortValueFields.containsKey(nameAttr);
                    }
                    return false;
                }
            }).stream().forEach(elm-> {
                String name = elm.getAttribute("name");
                if (elm.getNodeName().equals("str")) {
                    try {
                        UTFSort utf_sort = new UTFSort();
                        //utf_sort.init();
                        String value = utf_sort.translate(elm.getTextContent());
                        String targetName = sortValueFields.get(name);

                        if (!document.containsKey(targetName)) {
                            document.put(targetName, new ArrayList<String>());
                        }
                        document.get(targetName).add(value);

                        field(destDocument, destDocElem, value, targetName,consumer);
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE,e.getMessage(),e);
                    }
                } else {
                    throw new IllegalStateException("only src fields are supported for sorting");
                }
            });

            //first value
            XMLUtils.getElements(sourceDocElm, new XMLUtils.ElementsFilter() {
                @Override
                public boolean acceptElement(Element element) {
                    String nodeName = element.getNodeName();
                    if (nodeName.equals("arr")) {
                        String nameAttr = element.getAttribute("name");
                        if (nameAttr != null) return firstValue.containsKey(nameAttr);
                    }
                    return false;
                }
            }).stream().forEach(elm-> {
                String name = elm.getAttribute("name");
                if (elm.getNodeName().equals("arr")) {
                    List<Element> elements = XMLUtils.getElements(elm);
                    if (!elements.isEmpty()) {
                        String firstVal = elements.get(0).getTextContent();

                        if (!document.containsKey(this.firstValue.get(name))) {
                            document.put(this.firstValue.get(name), new ArrayList<String>());
                        }
                        document.get(this.firstValue.get(name)).add(firstVal);


                        field(destDocument, destDocElem, firstVal, this.firstValue.get(name), consumer);
                    }
                } else {
                    throw new IllegalStateException("only src fields are supported for sorting");
                }
            });

            //rest values
            XMLUtils.getElements(sourceDocElm, new XMLUtils.ElementsFilter() {
                @Override
                public boolean acceptElement(Element element) {
                    String nodeName = element.getNodeName();
                    if (nodeName.equals("arr")) {
                        String nameAttr = element.getAttribute("name");
                        if (nameAttr != null) return restValueFields.containsKey(nameAttr);
                    }
                    return false;
                }
            }).stream().forEach(elm-> {
                String name = elm.getAttribute("name");
                if (elm.getNodeName().equals("arr")) {
                    List<Element> elements = XMLUtils.getElements(elm);

                    if (!elements.isEmpty() && elements.size() > 1) {

                        List<Element> nlist = elements.subList(1, elements.size()-1);
                        nlist.stream().forEach(newElm-> {
                            String val = newElm.getTextContent();
                            if (!document.containsKey(this.restValueFields.get(name))) {
                                document.put(this.restValueFields.get(name), new ArrayList<String>());
                            }
                            document.get(this.restValueFields.get(name)).add(val);
                            field(destDocument, destDocElem, val, this.restValueFields.get(name), consumer);
                        });
                    }
                } else {
                    throw new IllegalStateException("only src fields are supported for sorting");
                }
            });

            //issn, isbn
            Element issnElement = XMLUtils.findElement(sourceDocElm, new XMLUtils.ElementsFilter() {
                @Override
                public boolean acceptElement(Element element) {
                    String nodeName = element.getNodeName();
                    if (nodeName.equals("str")) {
                        String nameAttr = element.getAttribute("name");
                        if (nameAttr != null) return "issn".equals(nameAttr);
                    }
                    return false;
                }
            });
            Element modelPathElement = XMLUtils.findElement(sourceDocElm, new XMLUtils.ElementsFilter() {
                @Override
                public boolean acceptElement(Element element) {
                    String nodeName = element.getNodeName();
                    if (nodeName.equals("arr")) {
                        String nameAttr = element.getAttribute("name");
                        if (nameAttr != null) return "model_path".equals(nameAttr);
                    }
                    return false;
                }
            });

            if (modelPathElement != null && issnElement != null) {
                Optional<Element> first = XMLUtils.getElements(modelPathElement).stream().findFirst();
                if (first.isPresent()) {
                    String textContent = issnElement.getTextContent();
                    if (StringUtils.isAnyString(textContent)) {
                        if (first.get().getTextContent().contains(KrameriusModels.MONOGRAPH.getValue())) {

                            if (!document.containsKey("id_isbn")) {
                                document.put("id_isbn", new ArrayList<String>());
                            }
                            document.get("id_isbn").add(textContent);


                            field(destDocument, destDocElem, textContent, "id_isbn", consumer);
                        } else if(first.get().getTextContent().contains(KrameriusModels.PERIODICAL.getValue())) {

                            if (!document.containsKey("id_issn")) {
                                document.put("id_issn", new ArrayList<String>());
                            }
                            document.get("id_issn").add(textContent);

                            field(destDocument, destDocElem, textContent, "id_issn",consumer);
                        }
                    }
                }
            }

            Element identifiers = XMLUtils.findElement(sourceDocElm, new XMLUtils.ElementsFilter() {
                @Override
                public boolean acceptElement(Element element) {
                    String nodeName = element.getNodeName();
                    if (nodeName.equals("arr")) {
                        String nameAttr = element.getAttribute("name");
                        if (nameAttr != null) return "dc.identifier".equals(nameAttr);
                    }
                    return false;
                }
            });
            if (identifiers != null) {
                List<Element> oneIdent = XMLUtils.getElements(identifiers);
                List<String> stringIdentifiers = oneIdent.stream().map(Element::getTextContent).collect(Collectors.toList());
                stringIdentifiers.stream().forEach(id-> {
                    if (id != null && id.toLowerCase().startsWith("ccnb:")) {

                        if (!document.containsKey("id_ccnb")) {
                            document.put("id_ccnb", new ArrayList<String>());
                        }
                        document.get("id_ccnb").add(id.substring("ccnb:".length()));

                        field(destDocument, destDocElem, id.substring("ccnb:".length()), "id_ccnb", consumer);
                    } else if (id != null && id.toLowerCase().startsWith("urnnbn:")) {

                        if (!document.containsKey("id_urnnbn")) {
                            document.put("id_urnnbn", new ArrayList<String>());
                        }
                        document.get("id_urnnbn").add(id.substring("urnnbn:".length()));


                        field(destDocument, destDocElem, id.substring("urnnbn:".length()), "id_urnnbn", consumer);
                    } else if (id != null && id.toLowerCase().startsWith("issn:")&& !document.keySet().contains("id_issn")) {

                        if (!document.containsKey("id_issn")) {
                            document.put("id_issn", new ArrayList<String>());
                        }
                        document.get("id_issn").add(id.substring("issn:".length()));

                        field(destDocument, destDocElem, id.substring("issn:".length()), "id_issn", consumer);
                    } else if (id != null && id.toLowerCase().startsWith("isbn:")&& !document.keySet().contains("id_isbn")) {

                        if (!document.containsKey("id_isbn")) {
                            document.put("id_isbn", new ArrayList<String>());
                        }
                        document.get("id_isbn").add(id.substring("isbn:".length()));

                        field(destDocument, destDocElem, id.substring("isbn:".length()), "id_isbn", consumer);
                    } else if (id != null && id.toLowerCase().startsWith("barcode:")&& !document.keySet().contains("barcode")) {

                        if (!document.containsKey("id_barcode")) {
                            document.put("id_barcode", new ArrayList<String>());
                        }
                        document.get("id_barcode").add(id.substring("barcode:".length()));

                        field(destDocument, destDocElem, id.substring("barcode:".length()), "id_barcode", consumer);
                    } else if (id != null && id.toLowerCase().startsWith("oclc:")&& !document.keySet().contains("oclc")) {

                        if (!document.containsKey("id_oclc")) {
                            document.put("id_oclc", new ArrayList<String>());
                        }
                        document.get("id_oclc").add(id.substring("oclc:".length()));

                        field(destDocument, destDocElem, id.substring("oclc:".length()), "id_oclc", consumer);
                    } else if (id != null && id.toLowerCase().startsWith("ismn:")&& !document.keySet().contains("ismn")) {

                        if (!document.containsKey("id_ismn")) {
                            document.put("id_ismn", new ArrayList<String>());
                        }
                        document.get("id_ismn").add(id.substring("ismn:".length()));

                        field(destDocument, destDocElem, id.substring("ismn:".length()), "id_ismn", consumer);
                    }
                });
            }


            Element dateStr = XMLUtils.findElement(sourceDocElm, new XMLUtils.ElementsFilter() {
                @Override
                public boolean acceptElement(Element element) {
                    String nodeName = element.getNodeName();
                    if (nodeName.equals("str")) {
                        String nameAttr = element.getAttribute("name");
                        if (nameAttr != null) return "datum_str".equals(nameAttr);
                    }
                    return false;
                }
            });

            if (dateStr != null) {
                List<String> pids = document.get("pid");
                String pid = !pids.isEmpty() ? pids.get(0) : "";
                String textContent = dateStr.getTextContent();
                DateExtractor dateExtractor = new DateExtractor();
                DateInfo dateInfo = dateExtractor.extractFromString(textContent.toString(), pid);
                appendDateFields(destDocument, destDocElem, dateInfo, consumer );
            }

            
            // Issue 42 / https://github.com/ceskaexpedice/ceska-digitalni-knihovna/issues/42
            if (document.containsKey("model")) {
                String model = document.get("model").get(0);
                if (model.equals("page")) {
                    Element details = XMLUtils.findElement(sourceDocElm, new XMLUtils.ElementsFilter() {
                        @Override
                        public boolean acceptElement(Element element) {
                            String nodeName = element.getNodeName();
                            if (nodeName.equals("arr")) {
                                String nameAttr = element.getAttribute("name");
                                if (nameAttr != null) return "details".equals(nameAttr);
                            }
                            return false;
                        }
                    });
                    
                    if (details != null) {
                        List<Element> elements = XMLUtils.getElements(details);
                        if (elements != null && elements.size() > 0) {
                            String textContent = elements.get(0).getTextContent();
                            String[] split = textContent.split("##");
                            if (split.length> 1) {
                                field(destDocument, destDocElem,split[1], "page.type",consumer);
                            }
                        }
                    }
                }
            }
            
            if (document.containsKey("accessibility") && document.containsKey("model")) {
                String accessibility = document.get("accessibility").get(0);
                String model = document.get("model").get(0);
                
                translateAccessibilityToLicense(model, accessibility, destDocument, destDocElem, consumer);
            }
        }
    }

    private void translateAccessibilityToLicense(String model, String accessibility, Document destDocument, Element destDocElem, CopyReplicateConsumer consumer) {
        
        String license = "public".equals(accessibility) ? "public" : "onsite";
        
        List<String> markContainsLicenses = Arrays.asList("monographunit","periodicalvolume");

        List<String> licenses = Arrays.asList("monograph","monographunit", "periodicalvolume","map","sheetmusic");
        List<String> licenses_of_ancestors = Arrays.asList("page","supplement","periodicalitem");
        if (licenses.contains(model)) {
            field(destDocument, destDocElem,license, "licenses",consumer);
        } else if (licenses_of_ancestors.contains(model)) {
            field(destDocument, destDocElem,license, "licenses_of_ancestors",consumer);
        }
        
        if (markContainsLicenses.contains(model)) {
            field(destDocument, destDocElem,license, "cdk.k5.marked_for_contains_licenses",consumer);
        }
        field(destDocument, destDocElem,license, "cdk.k5.license.translated",consumer);
    }


    private void field(Document destDocument, Element destDocElem, String value, String targetName, CopyReplicateConsumer consumer) {
        Element strElm = destDocument.createElement("field");
        strElm.setAttribute("name", targetName);
        String content = StringEscapeUtils.escapeXml(value);
        // add to context to process
        strElm.setTextContent(content);
        ModifyFieldResult result = ModifyFieldResult.none;
        if (consumer != null) {
        	result = consumer.modifyField(strElm);
        }
        if (!result.equals(ModifyFieldResult.delete)) {
            destDocElem.appendChild(strElm);
        }
    }


    public void arrayValue(String pid, Element sourceDocElement, Document feedDoc, Element feedDocElement, Node node, CopyReplicateConsumer consumer) {
        String attributeName = ((Element) node).getAttribute("name");
        NodeList childNodes = node.getChildNodes();
        for (int i = 0,ll=childNodes.getLength(); i < ll; i++) {
            Node n = childNodes.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {

                if (this.plainValueFields.containsKey(attributeName)) {
                    List<String> values = this.plainValueFields.get(attributeName);
                    values.stream().forEach(v-> {
                        field(feedDoc, feedDocElement, n.getTextContent(),v, consumer);
                    });
                }
            }
        }
    }


    private void appendDateFields(Document feedDoc, Element feedDocElement, DateInfo dateInfo, CopyReplicateConsumer consumer) {
        //min-max dates
        if (dateInfo.dateMin != null) {
            field(feedDoc, feedDocElement,MyDateTimeUtils.formatForSolr(dateInfo.dateMin),"date.min", consumer);
        }
        if (dateInfo.dateMax != null) {
            field(feedDoc, feedDocElement,MyDateTimeUtils.formatForSolr(dateInfo.dateMin),"date.max", consumer);
        }
        if (dateInfo.isInstant()) { //instant
            if (dateInfo.instantYear != null) {
                field(feedDoc, feedDocElement, dateInfo.instantYear.toString(),"date_range_start.year", consumer);
                field(feedDoc, feedDocElement, dateInfo.instantYear.toString(), "date_range_end.year", consumer);
            }
            if (dateInfo.instantMonth != null) {

                field(feedDoc, feedDocElement,  dateInfo.instantMonth.toString(),"date_range_start.month", consumer);
                field(feedDoc, feedDocElement,  dateInfo.instantMonth.toString(), "date_range_end.month", consumer);
            }
            if (dateInfo.instantDay != null) {

                field(feedDoc, feedDocElement,  dateInfo.instantDay.toString(), "date_range_start.day", consumer);
                field(feedDoc, feedDocElement,  dateInfo.instantDay.toString(), "date_range_end.day", consumer);
            }
        } else { //range
            //range start
            if (dateInfo.rangeStartYear != null) {
                field(feedDoc, feedDocElement,  dateInfo.rangeStartYear.toString(), "date_range_start.year", consumer);
            }
            if (dateInfo.rangeStartMonth != null) {
                field(feedDoc, feedDocElement,  dateInfo.rangeStartMonth.toString(), "date_range_start.month", consumer);

            }
            if (dateInfo.rangeStartDay != null) {
                field(feedDoc, feedDocElement,  dateInfo.rangeStartMonth.toString(), "date_range_start.day", consumer);

            }
            //range end
            if (dateInfo.rangeEndYear != null) {
                field(feedDoc, feedDocElement,  dateInfo.rangeEndYear.toString(), "date_range_end.year", consumer);
                //addSolrField(solrInput, "date_range_end.year", dateInfo.rangeEndYear.toString());
            }
            if (dateInfo.rangeEndMonth != null) {
                field(feedDoc, feedDocElement,  dateInfo.rangeEndMonth.toString(), "date_range_end.month", consumer);
                //addSolrField(solrInput, "date_range_end.month", dateInfo.rangeEndMonth.toString());
            }
            if (dateInfo.rangeEndDay != null) {
                field(feedDoc, feedDocElement,  dateInfo.rangeEndDay.toString(), "date_range_end.day", consumer);
                //addSolrField(solrInput, "date_range_end.day", dateInfo.rangeEndDay.toString());
            }
        }
    }

    @Override
    public String getField(String fieldId) {
        if (this.plainValueFields.containsKey(fieldId)) {
            return this.plainValueFields.get(fieldId).get(0);
        } else return null;
    }
}
