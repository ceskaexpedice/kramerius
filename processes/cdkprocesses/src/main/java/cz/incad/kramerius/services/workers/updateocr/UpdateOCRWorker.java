package cz.incad.kramerius.services.workers.updateocr;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import cz.incad.kramerius.services.Worker;
import cz.incad.kramerius.services.utils.SolrUtils;
import cz.incad.kramerius.services.workers.replicate.ReplicateWorker;
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
import java.util.concurrent.BrokenBarrierException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


//partial update
public class UpdateOCRWorker extends Worker {

    public static  Logger LOGGER = Logger.getLogger(ReplicateWorker.class.getName());

    private String user;
    private String pass;

    public UpdateOCRWorker(Element worker, Client client, List<String> pids) {
        super(worker, client, pids);

        Element request = XMLUtils.findElement(workerElm, "request");
        if (request != null) {
            Element userElm = XMLUtils.findElement(request, "cdk.user");
            user = userElm != null ? userElm.getTextContent() : "";

            Element passElm = XMLUtils.findElement(request, "cdk.pass");
            pass = passElm != null  ? passElm.getTextContent() : "";
        }
    }

    @Override
    public void run() {
        try {
            LOGGER.info("["+Thread.currentThread().getName()+"] processing list of pids "+this.pidsToBeProcessed.size());
            int batches = this.pidsToBeProcessed.size() / batchSize + (this.pidsToBeProcessed.size() % batchSize == 0 ? 0 :1);
            LOGGER.info("["+Thread.currentThread().getName()+"] creating  "+batches+" update ocr batches ");
            for (int i=0;i<batches;i++) {
                int from = i*batchSize;
                int to = from + batchSize;
                try {
                    List<String> subpids = pidsToBeProcessed.subList(from, Math.min(to,pidsToBeProcessed.size() ));
                    List<Pair<String,String>> list = fetchSolrDocumentFromCDKResource(subpids);

                    Document addDocument = XMLUtils.crateDocument("add");
                    list.stream().forEach(p->{
                        Element docElement = addDocument.createElement("doc");

                        Element pid = addDocument.createElement("field");
                        pid.setAttribute("name", "PID");
                        pid.setTextContent(p.getLeft());
                        docElement.appendChild(pid);

                        Element textOCr = addDocument.createElement("field");
                        textOCr.setAttribute("name", "text_ocr");
                        textOCr.setAttribute("update", "set");

                        textOCr.setTextContent(p.getRight());
                        docElement.appendChild(textOCr);
                        addDocument.getDocumentElement().appendChild(docElement);
                    });

                    if (addDocument.getDocumentElement().getChildNodes().getLength() > 0) {
                        SolrUtils.printToConsole(addDocument);
                    }

                    //SolrUtils.sendToDest(configurationBase, this.client, addDocument);

                } catch (ParserConfigurationException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                }
            }
        } finally {
            try {
                this.barrier.await();
            } catch (InterruptedException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(),e);
            } catch (BrokenBarrierException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(),e);
            }
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
            Element doc = XMLUtils.findElement(result, new XMLUtils.ElementsFilter() {
                @Override
                public boolean acceptElement(Element element) {
                    return element.getNodeName().equals("doc");
                }
            });
            Element textOcr = XMLUtils.findElement(doc, new XMLUtils.ElementsFilter() {
                @Override
                public boolean acceptElement(Element element) {
                    return element.getNodeName().equals("str") && element.getAttribute("name").equals("text_ocr");
                }
            });
            return (textOcr != null) ? Pair.of(pid, textOcr.getTextContent()) : null;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }




    public  List<Pair<String,String>> fetchSolrDocumentFromCDKResource( List<String> pids) {
        return pids.stream().map(pid -> {
            return this.textOCR(pid);
        }).filter(it -> it != null).collect(Collectors.toList());

    }
}
