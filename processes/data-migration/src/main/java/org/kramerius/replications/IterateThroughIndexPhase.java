package org.kramerius.replications;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.XMLUtils;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.ws.rs.core.MediaType;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class IterateThroughIndexPhase extends  AbstractPhase {

    public static final String POSTFIX = "api/v5.0/search?q=*:*";

    @Override
    public void start(String url, String userName, String pswd, String replicationCollections, String replicateImages) throws PhaseException {
        try {
            StringBuilder builder = new StringBuilder("{'pids':[");
            boolean first = true;
            for (String pid:pids(url)) {
                if (!pid.contains("@")) {
                    if (!first) { builder.append(','); }
                    builder.append('\'').append(pid).append('\'');
                    first = false;
                }
            }
            builder.append("]}");

            FileUtils.write(createIterateFile(), builder.toString(), Charset.forName("UTF-8"));
        } catch (IOException e) {
            throw new PhaseException(this,e);
        } catch (ParserConfigurationException e) {
            throw new PhaseException(this,e);
        } catch (SAXException e) {
            throw new PhaseException(this,e);
        }

    }

    public static List<String> pids(String surl) throws PhaseException, IOException, ParserConfigurationException, SAXException {
        List<String> retvals = new ArrayList<>();
        int rows = 1000;
        int numberOfDocs = 0;
        int offset = 0;
        Client c = Client.create();
        do {
            String url = url(surl, rows, offset);
            WebResource r = c.resource(url);
            String t = r.accept(MediaType.APPLICATION_JSON).get(String.class);
            Document document = XMLUtils.parseDocument(new StringReader(t));

            numberOfDocs = Math.max(numFound(document),numberOfDocs);
            List<String> pids = XMLUtils.getElementsRecursive(document.getDocumentElement(), (elm) -> {
                if (elm.getNodeName().equals("str")) {
                    return elm.getAttribute("name").equals("PID");
                } else return false;
            }).stream().map((elm)->elm.getTextContent()).collect(Collectors.toList());
            retvals.addAll(pids);
            offset += rows;
        } while(offset < numberOfDocs);
        return  retvals;
    }

    private static String url(String surl, int rows, int offset) {
        return surl + (surl.endsWith("/") ? "" : "/") + POSTFIX+"&rows="+rows+"&start="+offset+"&wt=xml";
    }

    private static int numFound(Document doc) {
        Element elm = XMLUtils.findElement(doc.getDocumentElement(), (element) -> {
            if (element.getNodeName().equals("result")) {
                return true;
            } else return false;
        });

        return elm != null ? Integer.parseInt(elm.getAttribute("numFound")) : 0;
    }



    @Override
    public void restart(String previousProcessUUID, File previousProcessRoot, boolean phaseCompleted, String url, String userName, String pswd, String replicationCollections, String replicateImages) throws PhaseException {
        throw new UnsupportedOperationException("This is unsupported in iterate phase");
    }
}
