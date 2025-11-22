package cz.inovatika.kramerius.services.workers.copy;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.utils.XMLUtils;
import cz.inovatika.kramerius.services.config.ProcessConfig;
import cz.inovatika.kramerius.services.iterators.IterationItem;
import cz.inovatika.kramerius.services.iterators.utils.KubernetesSolrUtils;
import cz.inovatika.kramerius.services.workers.Worker;
import cz.inovatika.kramerius.services.workers.WorkerFinisher;
import cz.inovatika.kramerius.services.workers.WorkerIndexedItem;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;


public abstract class CopyWorker<T extends WorkerIndexedItem, C extends CopyWorkerContext<T>> extends Worker<C> {

    public CopyWorker(ProcessConfig processConfig, Client client, List<IterationItem> items, WorkerFinisher finisher) {
        super(processConfig, client, items, finisher);
    }

    protected Element fetchDocumentFromRemoteSOLR(Client client, List<String> pids, String fieldlist)
            throws IOException, SAXException, ParserConfigurationException {
        String idIdentifier = this.config.getRequestConfig().getIdIdentifier() != null ?  this.config.getRequestConfig().getIdIdentifier() :  this.processConfig.getIteratorConfig().getIdField();

        String requestUrl = this.config.getRequestConfig().getUrl();
        String requestEndpoint =  this.config.getRequestConfig().getEndpoint();
        String reduce = pids.stream().reduce("", (i, v) -> {
            if (!i.isEmpty()) {
                return i + " OR \"" + v + "\"";
            } else {
                return '"' + v + '"';
            }
        });
        String query = "?q=" + idIdentifier + ":(" + URLEncoder.encode(reduce, StandardCharsets.UTF_8) + ")&fl="
                + URLEncoder.encode(fieldlist, StandardCharsets.UTF_8) + "&wt=xml&rows=" + pids.size();
        LOGGER.info(String.format("Requesting uri %s, %s",requestUrl.endsWith("/") ? requestUrl + requestEndpoint : requestUrl +"/"+ requestEndpoint, query));
        return KubernetesSolrUtils.executeQueryJersey(client,requestUrl.endsWith("/") ? requestUrl + requestEndpoint : requestUrl +"/"+ requestEndpoint , query);
    }

    protected List<Element> solrResult(String checkUrlC, String checkEndpoint, String query) {
        String checkUrl = checkUrlC + (checkUrlC.endsWith("/") ? "" : "/") + checkEndpoint;
        Element resultElem = XMLUtils.findElement(KubernetesSolrUtils.executeQueryJersey(client, checkUrl, query),
                (elm) -> {
                    return elm.getNodeName().equals("result");
                });
        return XMLUtils.getElements(resultElem);
    }

    protected List<String> getIndexedIdentifiers(CopyWorkerContext<WorkerIndexedItem> simpleCopyContext) {
        return simpleCopyContext.getAlreadyIndexed().stream().map(WorkerIndexedItem::getId).collect(Collectors.toList());
    }

    protected List<String> getNotIndexedIdentifiers(CopyWorkerContext<WorkerIndexedItem> simpleCopyContext) {
        return simpleCopyContext.getNotIndexed().stream().map(IterationItem::getId).collect(Collectors.toList());
    }

}
