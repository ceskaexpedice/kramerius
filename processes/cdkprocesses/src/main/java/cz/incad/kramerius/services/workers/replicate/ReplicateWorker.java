package cz.incad.kramerius.services.workers.replicate;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.services.Worker;
import cz.incad.kramerius.services.utils.SolrUtils;
import cz.incad.kramerius.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.logging.Level;
import java.util.logging.Logger;

// replicate index
public class ReplicateWorker extends Worker {

    public static Logger LOGGER = Logger.getLogger(ReplicateWorker.class.getName());

    private String fieldList;

    public ReplicateWorker(Element workerElm, Client client, List<String> pids) {
        super(workerElm, client, pids);
        Element requestElm = XMLUtils.findElement(workerElm, "request");
        if (requestElm != null) {
            Element fieldlistElm = XMLUtils.findElement(requestElm, "fieldlist");
            if (fieldlistElm != null) {
                fieldList = fieldlistElm.getTextContent();
            }
        }

        List<Element> elms = new ArrayList<>();
        NodeList childNodes = workerElm.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);


        }
    }


    @Override
    public void run() {
        try {
            LOGGER.info("["+Thread.currentThread().getName()+"] processing list of pids "+this.pidsToBeProcessed.size());
            //int batchSize = configurationBase.getRequests().getBatchSize();
            int batches = this.pidsToBeProcessed.size() / batchSize + (this.pidsToBeProcessed.size() % batchSize == 0 ? 0 :1);
            LOGGER.info("["+Thread.currentThread().getName()+"] creating  "+batches+" migrateBatches ");
            for (int i=0;i<batches;i++) {
                int from = i*batchSize;
                int to = from + batchSize;
                try {
                    List<String> subpids = pidsToBeProcessed.subList(from, Math.min(to,pidsToBeProcessed.size() ));
                    Element response = fetchDocuments( this.client,  subpids);
                    Element resultElem = XMLUtils.findElement(response, (elm) -> {
                        return elm.getNodeName().equals("result");
                    });

                    NodeList childNodes = resultElem.getChildNodes();
                    for (int j = 0; j < childNodes.getLength(); j++) {
                        Node item = childNodes.item(j);

                    }
//                    // insert new document
//                    // or update
//                    List<Document> batchDocuments = BatchUtils.migrateBatches(resultElem, batchSize);
//                    for (Document  batch : batchDocuments) {
//                        SolrUtils.sendToDest(this.destinationUrl, this.client, batch);
//                    }
                } catch (ParserConfigurationException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                } catch (SAXException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                } catch (IOException e) {
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


    public  Element fetchDocuments(Client client,  List<String> pids) throws IOException, SAXException, ParserConfigurationException {
        String reduce = pids.stream().reduce("", (i, v) -> {
            if (!i.equals("")) {
                return i + " OR \"" + v+"\"";
            } else {
                return '"'+v+'"';
            }
        });


        String query =  this.requestEndpoint + "?q=PID:(" + URLEncoder.encode(reduce, "UTF-8") + ")&fl=" + URLEncoder.encode(this.fieldList, "UTF-8")+"&wt=xml";
        return SolrUtils.executeQuery(client, this.requestUrl , query);
    }


}
