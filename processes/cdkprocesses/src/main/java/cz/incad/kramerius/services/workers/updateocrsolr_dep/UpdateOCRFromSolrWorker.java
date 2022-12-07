package cz.incad.kramerius.services.workers.updateocrsolr_dep;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import cz.incad.kramerius.services.iterators.IterationItem;
import cz.incad.kramerius.services.workers.updateocr_dep.UpdateOCRWorker;
import cz.incad.kramerius.utils.BasicAuthenticationClientFilter;
import cz.incad.kramerius.utils.XMLUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.ws.rs.core.MediaType;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UpdateOCRFromSolrWorker extends UpdateOCRWorker {

    public UpdateOCRFromSolrWorker(Element worker, Client client, List<IterationItem> items) {
        super(worker, client, items);
    }

    @Override
    protected List<Pair<String, String>> fetchTextOCR(List<String> subpids) {
        List<Pair<String, String>> retvals = new ArrayList<>();
        try {
            String collected = URLEncoder.encode(subpids.stream().map(pid -> "\"" + pid + "\"").collect(Collectors.joining(" OR ")), "UTF-8");
            String url = this.requestUrl+ String.format("?q=*:*&fq=PID:(%s)&rows=%d&fl="+URLEncoder.encode("text_ocr PID", "UTF-8"), collected, subpids.size());
            WebResource r = client.resource(url);
            String t = r.accept(MediaType.APPLICATION_XML).get(String.class);
            Document parseDocument = XMLUtils.parseDocument(new StringReader(t));

            Element result = XMLUtils.findElement(parseDocument.getDocumentElement(), (elm) -> {
                return elm.getNodeName().equals("result");
            });

            List<Element> docs = XMLUtils.getElements(result, new XMLUtils.ElementsFilter() {
                @Override
                public boolean acceptElement(Element element) {
                    return element.getNodeName().equals("doc");
                }
            });
            if (docs!= null) {
                docs.stream().forEach(doc-> {
                    Element pid = XMLUtils.findElement(doc, new XMLUtils.ElementsFilter() {
                        @Override
                        public boolean acceptElement(Element element) {
                            return element.getNodeName().equals("str") && element.getAttribute("name").equals("PID");
                        }
                    });

                    Element textOcr = XMLUtils.findElement(doc, new XMLUtils.ElementsFilter() {
                        @Override
                        public boolean acceptElement(Element element) {
                            return element.getNodeName().equals("str") && element.getAttribute("name").equals("text_ocr");
                        }
                    });
                    Pair<String,String> val =  (textOcr != null&&pid!=null) ? Pair.of(pid.getTextContent(), textOcr.getTextContent()) : null;
                    retvals.add(val);
                });
            } else return null;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }


        return retvals;
    }
}
