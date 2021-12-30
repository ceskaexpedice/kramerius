package cz.incad.kramerius.services.workers.updateocr;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.services.Worker;
import cz.incad.kramerius.services.iterators.IterationItem;
import cz.incad.kramerius.services.utils.SolrUtils;
import cz.incad.kramerius.services.workers.replicate.copy.CopyReplicateWorker;
import cz.incad.kramerius.utils.XMLUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.ParserConfigurationException;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.logging.Level;
import java.util.logging.Logger;


//partial update
public abstract class UpdateOCRWorker extends Worker {

    public static  Logger LOGGER = Logger.getLogger(CopyReplicateWorker.class.getName());

    private String user;
    private String pass;

    public UpdateOCRWorker(Element worker, Client client, List<IterationItem> items) {
        super(worker, client, items);

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
                    long start = System.currentTimeMillis();
                    List<Pair<String,String>> list = fetchTextOCR(subpids);
                    //LOGGER.info(String.format("Document from cdk fetched and it took %d", (System.currentTimeMillis() - start)));

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
                        long startBatch = System.currentTimeMillis();
                        SolrUtils.sendToDest(this.destinationUrl, this.client, addDocument);
                        //LOGGER.info(String.format("Batch sent to %s and number of document %d and it took %d", this.destinationUrl, addDocument.getDocumentElement().getChildNodes().getLength(), (System.currentTimeMillis() - startBatch)));
                    } else {
                        LOGGER.info("No add document");
                    }


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

    protected abstract  List<Pair<String, String>> fetchTextOCR(List<String> subpids);

}
