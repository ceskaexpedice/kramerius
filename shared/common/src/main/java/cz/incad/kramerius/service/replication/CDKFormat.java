package cz.incad.kramerius.service.replication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.service.ReplicateException;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.PIDParser;

/**
 * CDK format
 * 
 * @author pavels
 */
public class CDKFormat implements ReplicationFormat {

    @Inject
    Provider<HttpServletRequest> requestProvider;

    public static final String[] DATA_STREAMS = { FedoraUtils.IMG_FULL_STREAM, /*
                                                                                * FedoraUtils
                                                                                * .
                                                                                * IMG_THUMB_STREAM
                                                                                * ,
                                                                                */
    FedoraUtils.IMG_PREVIEW_STREAM, 
    
    
    // media files
    FedoraUtils.OGG_STREAM, FedoraUtils.MP3_STREAM, FedoraUtils.WAV_STREAM 
    };

    @Override
    public byte[] formatFoxmlData(byte[] input, Object... params)
            throws ReplicateException {
        try {
            Document document = XMLUtils.parseDocument(
                    new ByteArrayInputStream(input), true);
            Element docElement = document.getDocumentElement();
            if (docElement.getLocalName().equals("digitalObject")) {

                List<Element> datastreamsElements = XMLUtils.getElements(
                        docElement, new XMLUtils.ElementsFilter() {

                            @Override
                            public boolean acceptElement(Element elm) {
                                String elmName = elm.getLocalName();
                                String idName = elm.getAttribute("ID");
                                boolean idContains = Arrays
                                        .asList(DATA_STREAMS).contains(idName);
                                return elmName.equals("datastream")
                                        && idContains
                                        && elm.hasAttribute("CONTROL_GROUP")
                                        && elm.getAttribute("CONTROL_GROUP")
                                                .equals("M");
                            }
                        });

                for (Element datStreamElm : datastreamsElements) {
                    dataStreamVersions(document, datStreamElm);
                }

                List<Element> relsExt = XMLUtils.getElements(docElement,
                        new XMLUtils.ElementsFilter() {

                            @Override
                            public boolean acceptElement(Element elm) {
                                String elmName = elm.getLocalName();
                                String idName = elm.getAttribute("ID");
                                return elmName.equals("datastream")
                                        && idName
                                                .equals(FedoraUtils.RELS_EXT_STREAM);
                            }
                        });

                if (!relsExt.isEmpty()) {
                    original(document, relsExt.get(0));
                }

                // remove virtual collections
                boolean omitvc = KConfiguration.getInstance().getConfiguration().getBoolean("cdk.omitvc",true);
                if (!omitvc) {
                    removeVirtualCollections(document, relsExt.get(0));
                }
                
                if (params != null && params.length > 0) {
                    String vcname = params[0].toString();
                    virtualCollectionName(vcname, document, relsExt.get(0));
                }

                // change IIP point
                changeIIPPoint(document, relsExt.get(0));

            } else {
                throw new ReplicateException("Not valid FOXML");
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            XMLUtils.print(document, bos);
            return bos.toByteArray();
        } catch (ParserConfigurationException e) {
            throw new ReplicateException(e);
        } catch (SAXException e) {
            throw new ReplicateException(e);
        } catch (IOException e) {
            throw new ReplicateException(e);
        } catch (TransformerException e) {
            throw new ReplicateException(e);
        } catch (DOMException e) {
            throw new ReplicateException(e);
        } catch (URISyntaxException e) {
            throw new ReplicateException(e);
        }
    }

    private void changeIIPPoint(Document document, Element element)
            throws DOMException, MalformedURLException, URISyntaxException {
        Element descElement = XMLUtils.findElement(element, "Description",
                FedoraNamespaces.RDF_NAMESPACE_URI);
        List<Element> tiles = XMLUtils.getElements(descElement);
        for (Element iip : tiles) {
            if (iip.getNamespaceURI() != null) {
                if (iip.getNamespaceURI()
                        .equals(FedoraNamespaces.KRAMERIUS_URI)
                        && iip.getLocalName().equals("tiles-url")) {
                    if (iip.getTextContent().contains("DeepZoom")) {
                        iip.setTextContent(deepZoomURI(document).toURI()
                                .toString());
                    } else {
                        iip.setTextContent(zoomifyURI(document).toURI()
                                .toString());
                    }
                }
            }
        }

    }

    private URL zoomifyURI(Document doc) throws MalformedURLException {
        HttpServletRequest req = this.requestProvider.get();
        String pid = doc.getDocumentElement().getAttribute("PID");
        String is = ApplicationURL.applicationURL(req) + "/zoomify/" + pid;
        return new URL(is);
    }

    private URL deepZoomURI(Document doc) throws MalformedURLException {
        HttpServletRequest req = this.requestProvider.get();
        String pid = doc.getDocumentElement().getAttribute("PID");
        String is = ApplicationURL.applicationURL(req) + "/deepZoom/" + pid;
        return new URL(is);
    }

    private void virtualCollectionName(String vcname, Document document,
            Element element) {
        Element descElement = XMLUtils.findElement(element, "Description",
                FedoraNamespaces.RDF_NAMESPACE_URI);
        Element elm = document.createElementNS(
                FedoraNamespaces.RDF_NAMESPACE_URI, "isMemberOfCollection");
        if (!vcname.startsWith(PIDParser.INFO_FEDORA_PREFIX)) {
            elm.setAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "resource",
                    PIDParser.INFO_FEDORA_PREFIX + vcname);
        } else {
            elm.setAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "resource",
                    vcname);
        }
        document.adoptNode(elm);
        descElement.appendChild(elm);
    }

    private void removeVirtualCollections(Document document, Element element) {
        Element descElement = XMLUtils.findElement(element, "Description",
                FedoraNamespaces.RDF_NAMESPACE_URI);
        List<Element> delems = XMLUtils.getElements(descElement);
        for (Element del : delems) {
            if (del.getNamespaceURI() != null) {
                if (del.getNamespaceURI().equals(
                        FedoraNamespaces.RDF_NAMESPACE_URI)
                        && del.getLocalName().equals("isMemberOfCollection")) {
                    descElement.removeChild(del);
                }
            }
        }
    }

    private void original(Document document, Element element)
            throws DOMException, MalformedURLException, URISyntaxException {
        Element original = document.createElementNS(
                FedoraNamespaces.KRAMERIUS_URI, "replicatedFrom");
        document.adoptNode(original);
        original.setTextContent(makeHANDLE(document).toURI().toString());
        Element descElement = XMLUtils.findElement(element, "Description",
                FedoraNamespaces.RDF_NAMESPACE_URI);
        descElement.appendChild(original);
    }

    private URL makeHANDLE(Document doc) throws MalformedURLException {
        HttpServletRequest req = this.requestProvider.get();
        String pid = doc.getDocumentElement().getAttribute("PID");
        String imgServ = ApplicationURL.applicationURL(req) + "/handle/" + pid;
        return new URL(imgServ);
    }

    private void dataStreamVersions(Document document, Element dataStreamElm)
            throws ReplicateException {
        String idAttr = dataStreamElm.getAttribute("ID");
        List<Element> versions = XMLUtils.getElements(dataStreamElm,
                new XMLUtils.ElementsFilter() {

                    @Override
                    public boolean acceptElement(Element element) {
                        String locName = element.getLocalName();
                        return locName.endsWith("datastreamVersion");
                    }
                });

        for (Element version : versions) {
            Element found = XMLUtils.findElement(version, "binaryContent",
                    version.getNamespaceURI());

            if (found != null) {
                try {
                    URL url = makeURL(document, found, idAttr);

                    changeDataStream(document, dataStreamElm, version, url);
                } catch (MalformedURLException e) {
                    throw new ReplicateException(e);
                } catch (IOException e) {
                    throw new ReplicateException(e);
                } catch (DOMException e) {
                    throw new ReplicateException(e);
                } catch (URISyntaxException e) {
                    throw new ReplicateException(e);
                }
            }
        }
    }

    private URL makeURL(Document doc, Element found, String idAttr)
            throws MalformedURLException {
        HttpServletRequest req = this.requestProvider.get();
        String pid = doc.getDocumentElement().getAttribute("PID");

        String imgServ = ApplicationURL.applicationURL(req) + "/img?pid=" + pid
                + "&stream=" + idAttr + "&action=GETRAW";
        return new URL(imgServ);
    }

    /**
     * @param version
     * @throws IOException
     * @throws URISyntaxException
     * @throws DOMException
     */
    private void changeDataStream(Document document, Element datastream,
            Element version, URL url) throws IOException, DOMException,
            URISyntaxException {
        InputStream is = null;
        try {
            Element digestElm = XMLUtils.findElement(version, "contentDigest",
                    version.getNamespaceURI());
            if (digestElm != null) {
                version.removeChild(XMLUtils.findElement(version,
                        "contentDigest", version.getNamespaceURI()));
            }

            Element location = document.createElementNS(
                    version.getNamespaceURI(), "contentLocation");
            location.setAttribute("REF", url.toURI().toString());
            location.setAttribute("TYPE", "URL");

            version.removeChild(XMLUtils.findElement(version, "binaryContent",
                    version.getNamespaceURI()));

            document.adoptNode(location);
            version.appendChild(location);

            datastream.setAttribute("CONTROL_GROUP", "E");

        } finally {
            IOUtils.tryClose(is);
        }
    }

    @Override
    public byte[] formatFoxmlData(byte[] input) throws ReplicateException {
        return this.formatFoxmlData(input, null, new Object[0]);
    }

}
