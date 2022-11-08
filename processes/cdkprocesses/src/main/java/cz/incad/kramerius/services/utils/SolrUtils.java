package cz.incad.kramerius.services.utils;

import com.sun.jersey.api.client.*;
import cz.incad.kramerius.utils.BasicAuthenticationClientFilter;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.XMLUtils;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.ws.rs.core.MediaType;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SolrUtils {



    public static final Logger LOGGER = Logger.getLogger(SolrUtils.class.getName());

    private SolrUtils() {}

    public static String sendToDest(String destSolr, Client client, Document batchDoc) {
        try {
            StringWriter writer = new StringWriter();
            XMLUtils.print(batchDoc, writer);

            WebResource r = client.resource(destSolr);
            ClientResponse resp = r.accept(MediaType.TEXT_XML).type(MediaType.TEXT_XML).entity(writer.toString(), MediaType.TEXT_XML).post(ClientResponse.class);
            if (resp.getStatus() != ClientResponse.Status.OK.getStatusCode()) {

                StringWriter stringWriter = new StringWriter();
                XMLUtils.print(batchDoc,stringWriter);
                LOGGER.warning("Problematic batch: ");
                LOGGER.warning(stringWriter.toString());

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                InputStream entityInputStream = resp.getEntityInputStream();
                IOUtils.copyStreams(entityInputStream, bos);
                return new String(bos.toByteArray(), "UTF-8");
            } else {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                InputStream entityInputStream = resp.getEntityInputStream();
                IOUtils.copyStreams(entityInputStream, bos);
                return new String(bos.toByteArray(), "UTF-8");
            }
        } catch (UniformInterfaceException | ClientHandlerException  | TransformerException | IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public static void printToConsole(Document batchDoc)  {
        try {
            StringWriter writer = new StringWriter();
            XMLUtils.print(batchDoc, writer);
            System.out.println(writer.toString());
        } catch (UniformInterfaceException | ClientHandlerException | TransformerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }


    public static void commit(Client client, String destServer)  {
        try {
            String destSolr = destServer + "?commit=true";
            WebResource r = client.resource(destSolr);
            Document document = XMLUtils.crateDocument("add");
            StringWriter strWriter = new StringWriter();
            XMLUtils.print(document, strWriter);
            String t = r.accept(MediaType.TEXT_XML).type(MediaType.TEXT_XML).entity(strWriter.toString(), MediaType.TEXT_XML).post(String.class);
        } catch (UniformInterfaceException | ClientHandlerException | ParserConfigurationException | TransformerException e) {
            throw new RuntimeException(e);
        }
    }



    public static List<String> findAllPids(Element elm) {
        Element result = XMLUtils.findElement(elm, new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                String nodeName = element.getNodeName();
                return nodeName.equals("result");
            }
        });
        if (result != null) {
            List<Element> elements = XMLUtils.getElements(result, new XMLUtils.ElementsFilter() {
                @Override
                public boolean acceptElement(Element element) {
                    String nodeName = element.getNodeName();
                    return nodeName.equals("doc");
                }
            });

            return elements.stream().map(item -> {
                        Element str = XMLUtils.findElement(item, new XMLUtils.ElementsFilter() {
                                    @Override
                                    public boolean acceptElement(Element element) {
                                        return element.getNodeName().equals("str");
                                    }
                                }
                        );
                        return str.getTextContent();
                    }
            ).collect(Collectors.toList());

        } else return new ArrayList<>();
    }


    public static Element executeQuery(Client client, String url, String query, String user, String pass) throws ParserConfigurationException, SAXException, IOException {
    	String u = url+(url.endsWith("/") ? "" : "/")+ query;
        LOGGER.info(String.format("[" + Thread.currentThread().getName() + "] url %s", u));
    	WebResource r = client.resource(u);

    	if (user != null && pass != null) {
            r.addFilter(new BasicAuthenticationClientFilter(user, pass));
        }

        LOGGER.info(String.format("[" + Thread.currentThread().getName() + "] processing %s", r.getURI().toString()));
        String t = r.accept(MediaType.APPLICATION_XML).get(String.class);
        Document parseDocument = XMLUtils.parseDocument(new StringReader(t));
        Stack<Element> stack = new Stack<>();
        stack.push(parseDocument.getDocumentElement());

        while(!stack.isEmpty()) {
            Element pop = stack.pop();
            if (pop.getNodeName().equals("str")) {
                String textContent = pop.getTextContent();
                if (textContent !=null && textContent.startsWith("uuid:") && textContent.contains("@") && !textContent.contains("/")) {
                    String[] split = textContent.split("@");
                    String formatted = String.format("%s/@%s", split[0], split[1]);
                    pop.setTextContent(formatted);
                }
            }
            XMLUtils.getElements(pop).stream().forEach(stack::push);
        }

        return parseDocument.getDocumentElement();
    }
}
