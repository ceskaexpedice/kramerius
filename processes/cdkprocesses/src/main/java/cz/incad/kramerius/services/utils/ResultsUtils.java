package cz.incad.kramerius.services.utils;

import cz.incad.kramerius.utils.XMLUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ResultsUtils {

    private ResultsUtils() {}

    public static List<String> pidFromResult(Element resultElem) {
        List<Element> elements = XMLUtils.getElements(resultElem);
        List<String> pids =  elements.stream().map(doc -> {
            String pid = null;
            List<String> collections = new ArrayList<>();
            Element str = XMLUtils.findElement(doc, "str");
            if (str.getAttribute("name").equals("PID")) {
                pid = str.getTextContent();
            }
            return pid;
        }).filter(pid -> {
            return pid != null;
        }).collect(Collectors.toList());

        return pids;
    }




    public static List<Pair<String, List<String>>> pidAndCollectionFromResult(Element resultElem) {
        List<Element> elements = XMLUtils.getElements(resultElem);
        return elements.stream().map(doc -> {
            String pid = null;
            List<String> collections = new ArrayList<>();
            Element str = XMLUtils.findElement(doc, "str");
            if (str.getAttribute("name").equals("PID")) {
                pid = str.getTextContent();
            }
            Element arr = XMLUtils.findElement(doc, "arr");
            NodeList childNodes = arr.getChildNodes();
            for (int j = 0; j < childNodes.getLength(); j++) {
                Node item = childNodes.item(j);
                if (item.getNodeType() == Node.ELEMENT_NODE) {
                    collections.add(item.getTextContent());
                }

            }
            return pid != null && !collections.isEmpty() ? Pair.of(pid, collections) : null;
        }).filter(pid -> {
            return pid != null;
        }).collect(Collectors.toList());
    }
}
