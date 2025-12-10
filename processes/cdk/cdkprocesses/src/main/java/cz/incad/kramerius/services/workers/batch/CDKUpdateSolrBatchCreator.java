package cz.incad.kramerius.services.workers.batch;

import cz.incad.kramerius.services.workers.copy.cdk.CDKCopyContext;
import cz.incad.kramerius.services.workers.copy.cdk.model.CDKWorkerIndexedItem;
import cz.incad.kramerius.utils.XMLUtils;
import cz.inovatika.kramerius.services.config.ProcessConfig;
import cz.inovatika.kramerius.services.workers.batch.UpdateSolrBatchCreator;
import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class CDKUpdateSolrBatchCreator extends UpdateSolrBatchCreator {

    private CDKCopyContext context;

    public CDKUpdateSolrBatchCreator(CDKCopyContext context, ProcessConfig processConfig, Element resultElem) {
        super(processConfig, resultElem, null);
        this.context = context;
    }

    public CDKCopyContext getContext() {
        return context;
    }

    @Override
    public Document createBatchForInsert() throws ParserConfigurationException {
        Document batch = super.createBatchForInsert();
        List<Element> docs = XMLUtils.getElementsRecursive(batch.getDocumentElement(), element -> {
            return element.getNodeName().equals("doc");
        });

        for (Element doc : docs) {
            // --- Indexed field modification ---
            List<Element> indexed = XMLUtils.getElements(doc, new XMLUtils.ElementsFilter() {
                @Override
                public boolean acceptElement(Element element) {
                    String attribute = element.getAttribute("name");
                    return "indexed".equals(attribute);
                }
            }).stream().collect(Collectors.toList());

            if (indexed.size() > 0) {
                Instant instant = new Date().toInstant();
                indexed.get(0).setTextContent(DateTimeFormatter.ISO_INSTANT.format(instant));
            }

            //--- License of ancestors; preparing data for cdk.licenses_of_ancestors ---
            List<String> licensesOfAncestors = XMLUtils.getElements(doc, new XMLUtils.ElementsFilter() {

                @Override
                public boolean acceptElement(Element element) {
                    String attribute = element.getAttribute("name");
                    return "licenses_of_ancestors".equals(attribute);
                }
            }).stream().map(Element::getTextContent).collect(Collectors.toList());

            for (String licOfAncestors : licensesOfAncestors) {
                Document document = doc.getOwnerDocument();
                Element cdkLicenses = document.createElement("field");
                cdkLicenses.setAttribute("name", "cdk.licenses_of_ancestors");
                cdkLicenses.setTextContent(this.config.getSourceName() + "_" + licOfAncestors);
                doc.appendChild(cdkLicenses);
            }

            //--- contains_licenses; preparing data for cdk.contains_licenses ---
            List<String> containsLicenses = XMLUtils.getElements(doc, new XMLUtils.ElementsFilter() {

                @Override
                public boolean acceptElement(Element element) {
                    String attribute = element.getAttribute("name");
                    return "contains_licenses".equals(attribute);
                }
            }).stream().map(Element::getTextContent).collect(Collectors.toList());

            for (String licOfAncestors : containsLicenses) {
                Document document = doc.getOwnerDocument();
                Element cdkLicenses = document.createElement("field");
                cdkLicenses.setAttribute("name", "cdk.contains_licenses");
                cdkLicenses.setTextContent(this.config.getSourceName() + "_" + licOfAncestors);
                doc.appendChild(cdkLicenses);
            }

            //--- Licenses; preparing data for cdk.licenses ---
            List<String> licenses = XMLUtils.getElements(doc, new XMLUtils.ElementsFilter() {
                @Override
                public boolean acceptElement(Element element) {
                    String attribute = element.getAttribute("name");
                    return "licenses".equals(attribute);
                }
            }).stream().map(Element::getTextContent).collect(Collectors.toList());

            for (String license : licenses) {
                Document document = doc.getOwnerDocument();
                Element cdkLicenses = document.createElement("field");
                cdkLicenses.setAttribute("name", "cdk.licenses");
                cdkLicenses.setTextContent(this.config.getSourceName() + "_" + license);
                doc.appendChild(cdkLicenses);
            }
            // ----

            // --- has_tiles; count_*
            hasTilesField(doc, false);
            countFields(doc, false);
            hasTextAndAlto(doc,false);
            // ---


            // --- cdk leader & cdk.collection
            Document document = doc.getOwnerDocument();
            Element cdkLeader = document.createElement("field");
            cdkLeader.setAttribute("name", "cdk.leader");
            cdkLeader.setTextContent(config.getSourceName());
            doc.appendChild(cdkLeader);

            Element cdkCollection = document.createElement("field");
            cdkCollection.setAttribute("name", "cdk.collection");
            cdkCollection.setTextContent(config.getSourceName());
            doc.appendChild(cdkCollection);
            // ---

        }
        return batch;
    }

    private void hasTextAndAlto(Element doc, boolean edit) {
        List<Element> hasContent = XMLUtils.getElements(doc, new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                String attribute = element.getAttribute("name");
                return attribute.startsWith("has_text_") || attribute.startsWith("has_alto_");
            }
        }).stream().collect(Collectors.toList());

        for (Element hasContentField : hasContent) {
            String name = "cdk."+hasContentField.getAttribute("name")+"_"+config.getSourceName();
            String value =  hasContentField.getTextContent();

            Document document = doc.getOwnerDocument();
            Element cdkHasContentField = document.createElement("field");
            cdkHasContentField.setAttribute("name", name);
            if (edit) {
                cdkHasContentField.setAttribute("update", "set");
            }
            cdkHasContentField.setTextContent(value);
            doc.appendChild(cdkHasContentField);
        }
    }

    private void countFields(Element doc, boolean edit) {
        List<Element> count_fields = XMLUtils.getElements(doc, new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                String attribute = element.getAttribute("name");
                return attribute.startsWith("count_");
            }
        }).stream().collect(Collectors.toList());

        for (Element count_field : count_fields) {
            String name = "cdk."+count_field.getAttribute("name")+"_"+config.getSourceName();
            String value =  count_field.getTextContent();

            Document document = doc.getOwnerDocument();
            Element cdkCountFields = document.createElement("field");
            cdkCountFields.setAttribute("name", name);
            if (edit) {
                cdkCountFields.setAttribute("update", "set");
            }
            cdkCountFields.setTextContent(value);
            doc.appendChild(cdkCountFields);
        }
    }

    private void hasTilesField(Element doc, boolean edit) {
        List<String> hasTiles = XMLUtils.getElements(doc, new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                String attribute = element.getAttribute("name");
                return "has_tiles".equals(attribute);
            }
        }).stream().map(Element::getTextContent).collect(Collectors.toList());
        if (!hasTiles.isEmpty()) {
            Document document = doc.getOwnerDocument();
            Element cdkHastiles = document.createElement("field");
            cdkHastiles.setAttribute("name", "cdk."+"has_tiles_"+config.getSourceName());
            if (edit) {
                cdkHastiles.setAttribute("update", "set");
            }
            cdkHastiles.setTextContent(hasTiles.getFirst());
            doc.appendChild(cdkHastiles);
        }
    }


    @Override
    protected void fieldModifierInUpdate(Element strElm) {
        String name = strElm.getAttribute("name");

        /** Adding fields */
        List<String> addValues = Arrays.asList(
                "licenses",
                "licenses_of_ancestors",
                "contains_licenses",

                "in_collections",
                "in_collections.direct",

                "titles.search",
                "authors",
                "authors.search",
                "authors.facet",
                "authors.aut.identifiers",
                "authors.aut.facet",

                "cdk.k5.license.translated",
                "cdk.licenses");

        // pridavani poli
        if (addValues.contains(name)) {
            strElm.setAttribute("update", "add-distinct");
        } else {
            strElm.setAttribute("update", "set");
        }

    }


    @Override
    public Document createBatchForUpdate() throws ParserConfigurationException {

        Document batch = super.createBatchForUpdate();


        List<Element> docs = XMLUtils.getElementsRecursive(batch.getDocumentElement(), element -> {
            return element.getNodeName().equals("doc");
        });
        for (Element doc : docs) {

            Element compositeIdElm = XMLUtils.findElement(doc, field -> {
                return field.getAttribute("name").equals("compositeId");
            });
            Element pidElm = XMLUtils.findElement(doc, field -> {
                return field.getAttribute("name").equals("pid");
            });

            // indexed
            Instant instant = new Date().toInstant();
            Element fieldDate = doc.getOwnerDocument().createElement("field");
            fieldDate.setAttribute("name", "indexed");
            fieldDate.setAttribute("update", "set");
            fieldDate.setTextContent(DateTimeFormatter.ISO_INSTANT.format(instant));
            doc.appendChild(fieldDate);


            Element cdkCollection = doc.getOwnerDocument().createElement("field");
            cdkCollection.setAttribute("name", "cdk.collection");
            cdkCollection.setAttribute("update", "add-distinct");
            cdkCollection.setTextContent(config.getSourceName());
            doc.appendChild(cdkCollection);

            hasTilesField(doc, true);
            countFields(doc, true);
            hasTextAndAlto(doc,true);

            List<Pair<String,String>> comparingFields = Arrays.asList(
                    Pair.of("licenses", "cdk.licenses"),
                    Pair.of("contains_licenses", "cdk.contains_licenses"),
                    Pair.of("licenses_of_ancestors", "cdk.licenses_of_ancestors")
            );
            CDKWorkerIndexedItem cdkItem = null;
            if (pidElm != null) {
                cdkItem = this.context.getAlreadyIndexedAsItem(pidElm.getTextContent().trim());
            } else if (compositeIdElm != null) {
                String compositeId = compositeIdElm.getTextContent();
                String[] splitted = compositeId.split("!");
                if (splitted.length > 1) {
                    cdkItem = this.context.getAlreadyIndexedAsItem(splitted[1].trim());
                }
            }
            if (cdkItem != null) {
                String id = cdkItem.getId();
                if (compositeIdElm == null) {
                    Document document = doc.getOwnerDocument();
                    Element cdkCountFields = document.createElement("field");
                    cdkCountFields.setAttribute("name", "compositeId");
                    cdkCountFields.setTextContent(id);
                    doc.appendChild(cdkCountFields);
                }
            }

            Map<String, Object> cdkDoc = cdkItem.getDocument();
            for (Pair<String,String> cpField : comparingFields) {

                String sourceField = cpField.getLeft();
                String specificCDKField = cpField.getRight();
                List<Element> newIndexedField = XMLUtils.getElements(doc, new XMLUtils.ElementsFilter() {
                    @Override
                    public boolean acceptElement(Element element) {
                        String attribute = element.getAttribute("name");
                        return sourceField.equals(attribute);
                    }
                });

                Set<String> newCDKValues = new HashSet<>();
                newCDKValues =  newIndexedField.stream().map(Element::getTextContent).map(cnt-> {
                    return this.config.getSourceName()+"_"+cnt;
                }).collect(Collectors.toSet());


                Set<String> indexedCDKLicenses = cdkDoc.get(specificCDKField) != null ?  new HashSet<String>((List<String>)cdkDoc.get(specificCDKField)) : new HashSet<>();
                indexedCDKLicenses.removeIf(item -> !item.startsWith(this.config.getSourceName() + "_"));
                if (!indexedCDKLicenses.equals(newCDKValues)) {
                    List<String> newList = new ArrayList<String>( cdkDoc.get(specificCDKField)  != null ?  (List<String>)cdkDoc.get(specificCDKField) : new ArrayList<>() );
                    // remove everything what is prefixed
                    newList.removeIf(item -> item.startsWith(this.config.getSourceName() + "_"));

                    // add new indexed values
                    newList.addAll(newCDKValues);


                    for (Element nIF : newIndexedField) {
                        doc.removeChild(nIF);
                    }

                    Set<String> tempSet = new HashSet<>();
                    Document document = doc.getOwnerDocument();
                    if (newList.size() > 0) {
                        newList.stream().forEach(lic-> {

                            Element cdkSpecific = document.createElement("field");
                            cdkSpecific.setAttribute("name", specificCDKField);
                            cdkSpecific.setAttribute("update", "set");
                            cdkSpecific.setTextContent(lic);
                            doc.appendChild(cdkSpecific);

                            Pair<String, String> divided = divideLibraryAndLicense(lic);
                            if (divided != null) {

                                String rv = divided.getRight();
                                if (!tempSet.contains(rv)) {
                                    Element changedField = document.createElement("field");
                                    changedField.setAttribute("name", sourceField);
                                    changedField.setAttribute("update", "set");

                                    changedField.setTextContent(rv);
                                    doc.appendChild(changedField);

                                    tempSet.add(rv);
                                }
                            }
                        });
                    } else {
                        Element cdkSpecific = document.createElement("field");
                        cdkSpecific.setAttribute("name", specificCDKField);
                        cdkSpecific.setAttribute("update", "set");
                        cdkSpecific.setAttribute("null", "true");
                        doc.appendChild(cdkSpecific);

                        Element changedField = document.createElement("field");
                        changedField.setAttribute("name", sourceField);
                        changedField.setAttribute("update", "set");
                        changedField.setAttribute("null", "true");
                        doc.appendChild(changedField);

                    }
                }
            }
        }
        return batch;
    }

    public static Pair<String,String> divideLibraryAndLicense(String cdklicense) {
        if (cdklicense.contains("_")) {
            int index = cdklicense.indexOf("_");
            return Pair.of(cdklicense.substring(0, index), cdklicense.substring(index+1));
        }
        return null;
    }
}

