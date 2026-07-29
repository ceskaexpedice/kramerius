package cz.inovatika.kramerius.services.config;

import cz.incad.kramerius.utils.ProcessTokenSupport;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.inovatika.kramerius.services.iterators.factories.SolrIteratorFactory;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class EffectiveMigrationConfigRenderer {

    private static final Logger LOGGER = Logger.getLogger(EffectiveMigrationConfigRenderer.class.getName());

    private EffectiveMigrationConfigRenderer() {
    }

    public static String render(String configuration, CloseableHttpClient client)
            throws ParserConfigurationException, IOException, SAXException, TransformerException {
        Document document = XMLUtils.parseDocument(new ByteArrayInputStream(configuration.getBytes(StandardCharsets.UTF_8)));
        Element root = document.getDocumentElement();

        Element timestampElm = XMLUtils.findElement(root, "timestamp");
        Element iterationElm = XMLUtils.findElement(root, "iteration");
        if (timestampElm == null || iterationElm == null) {
            return configuration;
        }

        String sourceName = text(root, "source-name");
        String timestampUrl = MigrationConfigParser.normalizeTimestampUrl(timestampElm.getTextContent(), sourceName);
        if (!StringUtils.isAnyString(timestampUrl)) {
            return serialize(document);
        }
        timestampElm.setTextContent(timestampUrl);

        JSONObject timestamp = fetchTimestamp(client, timestampUrl);
        if (timestamp == null || !timestamp.has("date")) {
            return serialize(document);
        }

        String timestampField = text(iterationElm, "timestamp_field");
        if (!StringUtils.isAnyString(timestampField)) {
            timestampField = SolrIteratorFactory.DEFAULT_TIMESTAMP_FIELD;
        }

        Element fqueryElm = XMLUtils.findElement(iterationElm, "fquery");
        if (fqueryElm == null) {
            fqueryElm = document.createElement("fquery");
            iterationElm.appendChild(fqueryElm);
        }
        fqueryElm.setTextContent(appendTimestampFilter(fqueryElm.getTextContent(), timestampField, timestamp.getString("date")));
        return serialize(document);
    }

    public static String appendTimestampFilter(String filterQuery, String timestampField, String date) {
        String timestampFilter = timestampField + ":[" + date + " TO NOW]";
        if (StringUtils.isAnyString(filterQuery)) {
            return filterQuery + " AND " + timestampFilter;
        }
        return timestampFilter;
    }

    private static JSONObject fetchTimestamp(CloseableHttpClient httpClient, String timestampUrl) throws IOException {
        LOGGER.info(String.format("[%s] url %s", Thread.currentThread().getName(), timestampUrl));
        HttpGet request = new HttpGet(timestampUrl);
        request.setHeader("Accept", "application/json");
        ProcessTokenSupport.setBearerToken(request, httpClient);

        return httpClient.execute(request, response -> {
            int status = response.getCode();
            String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            if (status == 200) {
                return new JSONObject(body);
            }
            if (status == 404) {
                return null;
            }
            throw new IOException(String.format("Unexpected response status: %d. Body: %s", status, body));
        });
    }

    private static String text(Element parent, String childName) {
        Element child = XMLUtils.findElement(parent, childName);
        return child != null ? child.getTextContent() : null;
    }

    private static String serialize(Document document) throws TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(document), new StreamResult(writer));
        return writer.toString();
    }
}
