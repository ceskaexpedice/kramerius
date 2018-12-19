package cz.incad.kramerius;

import com.sun.jersey.api.client.*;
import cz.incad.kramerius.service.MigrateSolrIndexException;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.logging.Level;

public class DNNTBatchUtils {

    private DNNTBatchUtils() {}

    public static Document createBatch(List<String> pids) throws ParserConfigurationException {
        DNNTMode mode = DNNTMode.valueOf(KConfiguration.getInstance().getConfiguration().getString(DNNTFlag.DNNT_MODE, DNNTMode.add.name()));
        DocumentBuilder builder = DocumentBuilderFactory.newInstance(). newDocumentBuilder();
        Document document = builder.newDocument();
        Element rootElm = document.createElement("add");
        document.appendChild(rootElm);
        for (String pid :
                pids) {
            Element doc = doc(document, pid, mode);
            rootElm.appendChild(doc);
        }
        return document;
    }

    private static Element doc(Document document, String pid, DNNTMode flag) {
        Element doc = document.createElement("doc");

        Element fname = document.createElement("field");
        fname.setAttribute("name", "PID");
        fname.setTextContent(pid);

        doc.appendChild(fname);

        Element fdnnt = document.createElement("field");
        fdnnt.setAttribute("name", "dnnt");
        fdnnt.setAttribute("update", flag.toString());
        fdnnt.setTextContent("true");

        doc.appendChild(fdnnt);

        return doc;
    }



}
