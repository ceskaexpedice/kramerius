package cz.incad.kramerius.cdk;

import cz.incad.kramerius.utils.XMLUtils;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CDKUtils {

    public static final String CDK_LEADER_NAME = "cdk.leader";
    public static final String CDK_SOURCES_NAME = "cdk.sources";

    private CDKUtils() {}


    public static final String findCDKLeader(Element solrDocument) {
        Element leaderElement = XMLUtils.findElement(solrDocument, (element) -> {
            if (element.hasAttribute("name") && element.getAttribute("name").equals(CDK_LEADER_NAME)) {
                return true;
            } else return false;
        });
        if (leaderElement != null) {
            return leaderElement.getTextContent();
        }
        return null;
    }


    // find leader and find other sources

    public static List<String> findSources(Element solrDocument) {
        Element foundElement = XMLUtils.findElement(solrDocument, (element) -> {
            if (element.hasAttribute("name") && element.getAttribute("name").equals("cdk.collection")) {
                return true;
            } else return false;
        });

        if (foundElement  != null && foundElement.getNodeName().equals("arr")) {

            List<String> collections = XMLUtils.getElements(foundElement, (elm) -> {
                return elm.getNodeName().equals("str");
            }).stream().map(Element::getTextContent).collect(Collectors.toList());

            List<String> filteredCollections = collections.stream().collect(Collectors.toList());
            return filteredCollections;
        } else return new ArrayList<>();
    }

}
