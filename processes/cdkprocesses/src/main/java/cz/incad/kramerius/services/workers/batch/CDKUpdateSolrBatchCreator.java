package cz.incad.kramerius.services.workers.batch;

import cz.incad.kramerius.utils.XMLUtils;
import cz.inovatika.kramerius.services.config.ProcessConfig;
import cz.inovatika.kramerius.services.workers.batch.BatchConsumer;
import cz.inovatika.kramerius.services.workers.batch.UpdateSolrBatchCreator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.ParserConfigurationException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class CDKUpdateSolrBatchCreator extends UpdateSolrBatchCreator {

    public CDKUpdateSolrBatchCreator(ProcessConfig processConfig, Element resultElem, BatchConsumer consumer) {
        super(processConfig, resultElem, consumer);
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
        }
        return batch;
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
        // From batch utils
        return batch;
    }
}

