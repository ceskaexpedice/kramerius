package cz.incad.kramerius.services.workers.update_dep;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.services.Worker;
import cz.incad.kramerius.services.iterators.IterationItem;
import cz.incad.kramerius.services.utils.SolrUtils;
import cz.incad.kramerius.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.ParserConfigurationException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.logging.Level;
import java.util.logging.Logger;

//TODO: Delete
public class UpdateWorker extends Worker {

    static Logger LOGGER = Logger.getLogger(UpdateWorker.class.getName());

    private List<Element> updateElements = new ArrayList<>();

    public UpdateWorker(Element workerElm, Client client, List<IterationItem> pids) {
        super(workerElm, client, pids);
        Element destinationElm = XMLUtils.findElement(workerElm, "destination");
        if (destinationElm != null) {
            Element updateFieldElement = XMLUtils.findElement(destinationElm, "update.dest.field");
            this.updateElements = XMLUtils.getElements(updateFieldElement);
        }
    }

    @Override
    public void run() {
        try {
            LOGGER.info("["+Thread.currentThread().getName()+"] processing list of pids "+this.pidsToBeProcessed.size());
            int batches = this.pidsToBeProcessed.size() / batchSize + (this.pidsToBeProcessed.size() % batchSize == 0 ? 0 :1);
            LOGGER.info("["+Thread.currentThread().getName()+"] creating  "+batches+" update batches ");
            for (int i=0;i<batches;i++) {
                int from = i*batchSize;
                int to = from + batchSize;
                try {
                    // create big batch - contains all subprocessed pids
                    List<String> subpids = pidsToBeProcessed.subList(from, Math.min(to,pidsToBeProcessed.size() ));
                    // testovat pritomnost v mzk - pokud ano, kandidat na update
                    List<Document> batchDocuments = new ArrayList<>();
                    // Add operation
                    Document addDocument = XMLUtils.crateDocument("add");


                    for (String pid :  subpids) {
                        Element docElement = addDocument.createElement("doc");

                        // doc
                        addDocument.getDocumentElement().appendChild(docElement);

                        //append field

                        Element field = addDocument.createElement("field");
                        field.setAttribute("name", "PID");
                        field.setTextContent(pid);
                        docElement.appendChild(field);

                        this.updateElements.stream().forEach(f->{
                            Node node = null;
                            synchronized (f.getOwnerDocument()) {
                                node = f.cloneNode(true);
                                addDocument.adoptNode(node);
                            }
                            docElement.appendChild(node);
                        });

                    }
                    batchDocuments.add(addDocument);
                    for (Document  batch : batchDocuments) {
                        String s = SolrUtils.sendToDest(this.destinationUrl, this.client, batch);
                        LOGGER.info("Response "+s);
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



}
