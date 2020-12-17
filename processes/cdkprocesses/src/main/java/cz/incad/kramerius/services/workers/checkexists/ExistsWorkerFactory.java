package cz.incad.kramerius.services.workers.checkexists;

import com.sun.jersey.api.client.*;
import cz.incad.kramerius.services.WorkerFinisher;
import cz.incad.kramerius.services.Worker;
import cz.incad.kramerius.services.WorkerFactory;
import cz.incad.kramerius.services.iterators.IterationItem;
import cz.incad.kramerius.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.ws.rs.core.MediaType;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

public class ExistsWorkerFactory extends WorkerFactory {


    @Override
    public WorkerFinisher createFinisher(Element worker, Client client) {
        return new ExistsFinisher(worker, client);
    }

    @Override
    public Worker createWorker(Element worker, Client client, List<IterationItem> items) {
        Element requestElm = XMLUtils.findElement(worker, "request");
        if (requestElm == null) throw new IllegalStateException("cannot find element request");
        Element localKrameriusElm = XMLUtils.findElement(requestElm, "local.kramerius");
        String localKramerius = localKrameriusElm != null ? localKrameriusElm.getTextContent() : "";
        String kramPoint = localKrameriusElm.getTextContent().endsWith("/") ? localKramerius : localKramerius+"/";

        Element localCollectionsElm = XMLUtils.findElement(worker, "local.collections");
        List<String> localCollectionPids = localCollectionsElm != null ? Arrays.asList(localCollectionsElm.getTextContent().split(",")) : new ArrayList<>();

        List<String> sources = localCollectionPids.stream().map(c -> {
            try {
                Element element = execRequest(client, String.format(kramPoint + "item/%s/streams/DC", c));
                if (element != null) {
                    Element source = XMLUtils.findElement(element, (elm) -> {
                        String localName = elm.getLocalName();
                        String namespaceURI = elm.getNamespaceURI();
                        return (localName.equals("source") && namespaceURI.equals("http://purl.org/dc/elements/1.1/"));
                    });
                    return source.getTextContent();
                } else return "null";
            } catch (ParserConfigurationException | SAXException | IOException  e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());

        Map<String, String> collections = new HashMap<>();
        for (int i = 0; i < localCollectionPids.size(); i++) {
            String collectionPid = localCollectionPids.get(i);
            String url = sources.get(i);
            if (url != null) {
                collections.put(collectionPid, url);
            }

        }
        return new ExistsWorker(worker, client, items, collections);
    }

    public static Element execRequest(Client client, String url) throws ParserConfigurationException, SAXException, IOException, UniformInterfaceException {
        try {
            WebResource r = client.resource(url+(url.endsWith("/") ? "" : "/"));
            String t = r.accept(MediaType.APPLICATION_XML).get(String.class);
            Document parseDocument = XMLUtils.parseDocument(new StringReader(t), true);
            return parseDocument.getDocumentElement();
        } catch (UniformInterfaceException e) {
            ClientResponse response = e.getResponse();
            int status = response.getStatus();
            if (status == 404) return null;
            else throw e;
        }
    }

}
