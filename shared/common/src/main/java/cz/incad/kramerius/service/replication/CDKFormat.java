package cz.incad.kramerius.service.replication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

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

    // GET/HEAD {pid}/image                                 - obsah IMG_FULL konkrétního objektu
    // GET      {pid}/image/thumb                           - IMG_THUMB objektu nebo potomka
    // GET      {pid}/image/preview                         - IMG_PREVIEW objektu nebo potomka
    // GET      {pid}/image/zoomify/ImageProperties.xml     - ImageProperties.xml pro zoomify
    // GET      {pid}/image/zoomify/{tileGroup}/{tile}.jpg  - dlaždice zoomify
    // GET/HEAD {pid}/audio/mp3
    // GET/HEAD {pid}/audio/ogg
    // GET/HEAD {pid}/audio/wav



    public static final Map<String, String> IMG_STREAMS_TO_BINARY = new HashMap<>();
    static {
        IMG_STREAMS_TO_BINARY.put(FedoraUtils.IMG_THUMB_STREAM,"%s/image/thumb");
    }


    public static final Map<String, String> IMG_STREAMS_TO_REFERENCE = new HashMap<>();
    static  {
        IMG_STREAMS_TO_REFERENCE.put(FedoraUtils.IMG_FULL_STREAM,"%s/image");
        IMG_STREAMS_TO_REFERENCE.put(FedoraUtils.IMG_PREVIEW_STREAM,"%s/image/preview");
    }

    public static final Map<String, String> AUDIO_STREAMS_TO_REFERENCE = new HashMap<>();
    static {
        AUDIO_STREAMS_TO_REFERENCE.put(FedoraUtils.OGG_STREAM, "%s/audio/ogg");
        AUDIO_STREAMS_TO_REFERENCE.put(FedoraUtils.MP3_STREAM, "%s/audio/mp3");
        AUDIO_STREAMS_TO_REFERENCE.put(FedoraUtils.WAV_STREAM, "%s/audio/wav");
    }


    @Override
    public byte[] formatFoxmlData(byte[] input, Object... params)
            throws ReplicateException {
        try {
            Document document = XMLUtils.parseDocument(
                    new ByteArrayInputStream(input), true);
            Element docElement = document.getDocumentElement();
            if (docElement.getLocalName().equals("digitalObject")) {

                // Streams that should be referenced
                List<Element> imgReferenceStream = XMLUtils.getElements(
                        docElement, new XMLUtils.ElementsFilter() {
                            @Override
                            public boolean acceptElement(Element elm) {
                                String elmName = elm.getLocalName();
                                String idName = elm.getAttribute("ID");
                                boolean idContains = new ArrayList<String>(IMG_STREAMS_TO_REFERENCE.keySet()).contains(idName);
                                return elmName.equals("datastream")
                                        && idContains
                                        && elm.hasAttribute("CONTROL_GROUP");
                                        //&& elm.getAttribute("CONTROL_GROUP")
                                        //        .equals("M");
                            }
                        });

                for (Element datStreamElm : imgReferenceStream) {
                    List<Element> versions = versions(datStreamElm);
                    String idAttr = datStreamElm.getAttribute("ID");
                    for (Element version: versions) {
                        URL url = makeUrl(document,  idAttr, IMG_STREAMS_TO_REFERENCE);
                        ReplicationUtils.referenceForStream(document, datStreamElm, version, url);
                    }
                }

                // Streams that should be referenced
                List<Element> audioStreams = XMLUtils.getElements(
                        docElement, new XMLUtils.ElementsFilter() {
                            @Override
                            public boolean acceptElement(Element elm) {
                                String elmName = elm.getLocalName();
                                String idName = elm.getAttribute("ID");
                                boolean idContains = new ArrayList<String>(AUDIO_STREAMS_TO_REFERENCE.keySet()).contains(idName);
                                return elmName.equals("datastream")
                                        && idContains
                                        && elm.hasAttribute("CONTROL_GROUP");
                                //&& elm.getAttribute("CONTROL_GROUP")
                                //        .equals("M");
                            }
                        });

                for (Element datStreamElm : audioStreams) {
                    List<Element> versions = versions(datStreamElm);
                    String idAttr = datStreamElm.getAttribute("ID");
                    for (Element version: versions) {
                        URL url = makeUrl(document,  idAttr, AUDIO_STREAMS_TO_REFERENCE);
                        ReplicationUtils.referenceForStream(document, datStreamElm, version, url);
                    }
                }

                // stream should be binary
                List<Element> binaryStreams = XMLUtils.getElements(
                        docElement, new XMLUtils.ElementsFilter() {
                            @Override
                            public boolean acceptElement(Element elm) {
                                String elmName = elm.getLocalName();
                                String idName = elm.getAttribute("ID");
                                boolean idContains = new ArrayList<String>(IMG_STREAMS_TO_BINARY.keySet()).contains(idName);
                                return elmName.equals("datastream")
                                        && idContains
                                        && elm.hasAttribute("CONTROL_GROUP")
                                        && !elm.getAttribute("CONTROL_GROUP")
                                        .equals("M");
                            }
                        });
                for (Element datStreamElm : binaryStreams) {
                    List<Element> versions = versions(datStreamElm);
                    String idAttr = datStreamElm.getAttribute("ID");
                    for (Element version: versions) {
                        URL url = makeUrl(document,  idAttr, IMG_STREAMS_TO_BINARY);
                        ReplicationUtils.binaryContentForStream(document, datStreamElm, version, url);
                    }
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
                
                if (params != null && params.length > 0 && params[0] != null) {
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

    private List<Element> versions(Element datStreamElm) {
        return XMLUtils.getElements(datStreamElm,
                                new XMLUtils.ElementsFilter() {
                                    @Override
                                    public boolean acceptElement(Element element) {
                                        String locName = element.getLocalName();
                                        return locName.endsWith("datastreamVersion");
                                    }
                                });
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

                        iip.setTextContent(zoomifyURI(document).toURI()
                                .toString());
                }
            }
        }

    }

    private URL zoomifyURI(Document doc) throws MalformedURLException {
        HttpServletRequest req = this.requestProvider.get();
        String pid = doc.getDocumentElement().getAttribute("PID");
        String url = applicationURLAndCheckHttps(req);
        String is = url + String.format("/api/client/v7.0/%s/image/zoomify/",pid);
        return new URL(is);
    }

    private String applicationURLAndCheckHttps(HttpServletRequest req) {
        boolean forceHttps = KConfiguration.getInstance().getConfiguration().getBoolean("cdk.makeurls.forcehttps", true);
        String url = ApplicationURL.applicationURL(req);
        if (forceHttps && url != null && url.toLowerCase().startsWith("http://")) {
            url = "https://"+url.substring("http://".length());
        }
        return url;
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
        String imgServ = applicationURLAndCheckHttps(req) + "/handle/" + pid;
        return new URL(imgServ);
    }

//    private void makeReferencedDatastreams(Document document, Element dataStreamElm)
//            throws ReplicateException {
//        String idAttr = dataStreamElm.getAttribute("ID");
//        List<Element> versions = versions(dataStreamElm);
//
//        for (Element version : versions) {
//            try {
//                URL url = makeURL(document,  idAttr);
//                changeDataStream(document, dataStreamElm, version, url);
//            } catch (MalformedURLException e) {
//                throw new ReplicateException(e);
//            } catch (IOException e) {
//                throw new ReplicateException(e);
//            } catch (DOMException e) {
//                throw new ReplicateException(e);
//            } catch (URISyntaxException e) {
//                throw new ReplicateException(e);
//            }
//        }
//    }


    //api/client/v7.0/items/uuid:b1f0b7d2-7313-417f-8f9d-012844808ea2/image/zoomify/ImageProperties.xml
    private URL makeUrl(Document doc, String idAttr, Map<String,String> refmap) throws MalformedURLException {
        HttpServletRequest req = this.requestProvider.get();
        String pid = doc.getDocumentElement().getAttribute("PID");
        String appUrl =applicationURLAndCheckHttps(req);

        appUrl = appUrl + (appUrl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" ;
        appUrl = appUrl + String.format(refmap.get(idAttr), pid);

        return new URL(appUrl);
    }





    @Override
    public byte[] formatFoxmlData(byte[] input) throws ReplicateException {
        return this.formatFoxmlData(input, null, new Object[0]);
    }

}
