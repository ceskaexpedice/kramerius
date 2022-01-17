package cz.incad.kramerius.statistics.accesslogs.utils;

import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SElemUtils {

    public static String selem(String type, String attrVal, Document solrDoc) {
        Element dcElm = XMLUtils.findElement(solrDoc.getDocumentElement(), new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                String nodeName = element.getNodeName();
                String attr = element.getAttribute("name");
                if (nodeName.equals(type) && StringUtils.isAnyString(attr) && attr.equals(attrVal)) return true;
                return false;
            }
        });
        return dcElm != null ? dcElm.getTextContent() : null;
    }
}
