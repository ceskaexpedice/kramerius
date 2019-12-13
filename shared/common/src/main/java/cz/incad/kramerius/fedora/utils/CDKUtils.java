package cz.incad.kramerius.fedora.utils;

import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CDKUtils {

    private CDKUtils() {}

    public static List<String> findSources(Element solrDocument) {
        Element foundElement = XMLUtils.findElement(solrDocument, (element) -> {
            if (element.hasAttribute("name") && element.getAttribute("name").equals("collection")) {
                return true;
            } else return false;
        });

        if (foundElement  != null && foundElement.getNodeName().equals("arr")) {
            List list = KConfiguration.getInstance().getConfiguration().getList("cdk.collections.sources");
            List hidden = KConfiguration.getInstance().getConfiguration().getList("cdk.collections.hidden");

            List<String> collections = XMLUtils.getElements(foundElement, (elm) -> {
                return elm.getNodeName().equals("str");
            }).stream().map(Element::getTextContent).collect(Collectors.toList());


            List<String> filteredCollections = collections.stream().filter(it-> list.contains(it)).collect(Collectors.toList());
            return filteredCollections;
        } else return new ArrayList<>();
    }

}
