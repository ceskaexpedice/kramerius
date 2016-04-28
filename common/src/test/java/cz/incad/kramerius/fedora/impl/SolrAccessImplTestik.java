package cz.incad.kramerius.fedora.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.impl.SolrAccessImpl;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.solr.SolrUtils;

public class SolrAccessImplTestik {
    
    public static void ahoj(SolrAccess solrAccess, String parentPid) throws IOException {
        int offset = 0;
        int numFound = Integer.MAX_VALUE;
        List<Element> foundElements = new ArrayList<Element>();
        while(offset < numFound) {
            Document resultsDocs = solrAccess.getSolrDataDocmentsByParentPid(parentPid, ""+offset);
            Element result = XMLUtils.findElement(resultsDocs.getDocumentElement(), "result");
            if (result != null) {
                String snumFound = result.getAttribute("numFound");
                numFound = Integer.parseInt(snumFound);
                
                List<Element> elements = XMLUtils.getElements(result, new XMLUtils.ElementsFilter() {
                        @Override
                        public boolean acceptElement(Element element) {
                            return (element.getNodeName().equals("doc"));
                        }
                    });
                foundElements.addAll(elements);
                offset += elements.size();
            }
        }
        
        for (Element doc : foundElements) {
            try {
                String disectPid = SolrUtils.disectPid(doc);
                System.out.println("Disected pid "+disectPid);
            } catch (XPathExpressionException e) {
                e.printStackTrace();
            }
        }

    }
    
    public static void main(String[] args) throws IOException, TransformerException {
//        String pid = "uuid:0823498e-bd85-4a98-b649-42ee5d43f5d8/@225";
//        String[] substring= "uuid:0823498e-bd85-4a98-b649-42ee5d43f5d8/@225";
        SolrAccess solrAcc = new SolrAccessImpl();
        ahoj(solrAcc, "uuid:0823498e-bd85-4a98-b649-42ee5d43f5d8");
                
//        Element result = XMLUtils.findElement(doc.getDocumentElement(), "result");
//        if (result != null) {
//            Element d = XMLUtils.findElement(result, "doc");
//            System.out.println(d.getTextContent());
//        }
    }
}
