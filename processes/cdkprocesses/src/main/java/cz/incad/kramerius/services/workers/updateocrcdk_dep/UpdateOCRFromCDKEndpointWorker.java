package cz.incad.kramerius.services.workers.updateocrcdk_dep;

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
import java.util.List;
import java.util.stream.Collectors;

public class UpdateOCRFromCDKEndpointWorker extends UpdateOCRWorker {

    private String user;
    private String pass;

    public UpdateOCRFromCDKEndpointWorker(String sourceName, Element worker, Client client, List<IterationItem> items) {
        super(sourceName, worker, client, items);
        Element request = XMLUtils.findElement(workerElm, "request");
        if (request != null) {
            Element userElm = XMLUtils.findElement(request, "cdk.user");
            user = userElm != null ? userElm.getTextContent() : "";

            Element passElm = XMLUtils.findElement(request, "cdk.pass");
            pass = passElm != null  ? passElm.getTextContent() : "";
        }

    }


    public Pair<String, String> textOCR(String pid) {
        try {


            WebResource r = client.resource(this.requestUrl+(this.requestUrl.endsWith("/") ? "" : "/")+ String.format("api/v4.6/cdk/%s/solrxml", pid));
            r.addFilter(new BasicAuthenticationClientFilter(user, pass));

            String t = r.accept(MediaType.APPLICATION_XML).get(String.class);
            Document parseDocument = XMLUtils.parseDocument(new StringReader(t));

            Element result = XMLUtils.findElement(parseDocument.getDocumentElement(), (elm) -> {
                return elm.getNodeName().equals("result");
            });

            //SolrUtils.printToConsole(parseDocument);

            Element doc = XMLUtils.findElement(result, new XMLUtils.ElementsFilter() {
                @Override
                public boolean acceptElement(Element element) {
                    return element.getNodeName().equals("doc");
                }
            });
            if (doc!= null) {
                Element textOcr = XMLUtils.findElement(doc, new XMLUtils.ElementsFilter() {
                    @Override
                    public boolean acceptElement(Element element) {
                        return element.getNodeName().equals("str") && element.getAttribute("name").equals("text_ocr");
                    }
                });
                return (textOcr != null) ? Pair.of(pid, textOcr.getTextContent()) : null;
            } else return null;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }




    public  List<Pair<String,String>> fetchTextOCR(List<String> pids) {
        return pids.stream().map(pid -> {
            return this.textOCR(pid);
        }).filter(it -> it != null).collect(Collectors.toList());

    }

}
