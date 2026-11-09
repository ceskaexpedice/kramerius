package cz.incad.kramerius.rest.apiNew.client.v70;

import cz.incad.kramerius.rest.apiNew.client.v70.utils.RightRuntimeInformations;
import cz.incad.kramerius.rest.apiNew.client.v70.utils.RightRuntimeInformations.RuntimeInformation;
import cz.incad.kramerius.rest.apiNew.admin.v70.collections.CutItem;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.rest.apiNew.utils.ExtractDataInfoUtils;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.StringUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.ceskaexpedice.akubra.KnownDatastreams;
import org.ceskaexpedice.akubra.utils.Dom4jUtils;
import org.dom4j.*;
import org.json.JSONObject;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Shared local item operations for the regular and CDK client resources.
 * Endpoint declarations stay in the concrete resources so JAX-RS registers
 * only those resources.
 */
public abstract class  AbstractItemsResource extends ClientApiResource {

    private static final Logger LOGGER = Logger.getLogger(AbstractItemsResource.class.getName());

    protected Response localFoxml(String pid) {
        try {
            checkSupportedObjectPid(pid);
            checkObjectExists(pid);
            Document foxml = akubraRepository.get(pid).asDom4j(true);
            Document modifiedFoxml = removeSecuredDatastreams(foxml);
            if (modifiedFoxml == null) {
                throw new InternalErrorException("I cannot return the foxml object => Not all protected datastreams could be removed.");
            }
            String model = model(modifiedFoxml);
            if (StringUtils.isAnyString(model) && model.equals("info:fedora/model:collection")) {
                String streamName = "COLLECTION_CLIPS";
                String collectionClipsContent = null;
                if (akubraRepository.datastreamExists(pid, streamName)) {
                    try (InputStream cutters = akubraRepository.getDatastreamContent(pid, streamName).asInputStream()) {
                        if (cutters != null) {
                            byte[] content = replaceLocationByBinaryContent(modifiedFoxml, cutters, streamName);
                            collectionClipsContent = new String(content, java.nio.charset.StandardCharsets.UTF_8);
                        }
                    }
                }
                if (collectionClipsContent != null) {
                    org.json.JSONArray jsonArray = new org.json.JSONArray(collectionClipsContent);
                    List<CutItem> cutItems = CutItem.fromJSONArray(jsonArray);
                    for (CutItem cutItem : cutItems) {
                        try {
                            cutItem.initGeneratedThumbnail(akubraRepository, pid);
                        } catch (NoSuchAlgorithmException | IOException e) {
                            LOGGER.log(Level.SEVERE, e.getMessage(), e);
                        }
                        if (cutItem.containsGeneratedThumbnail()) {
                            String clipThumbName = cutItem.getThumbnailmd5();
                            try (InputStream cutters = akubraRepository.getDatastreamContent(pid, clipThumbName).asInputStream()) {
                                if (cutters != null) replaceLocationByBinaryContent(modifiedFoxml, cutters, clipThumbName);
                            }
                        }
                    }
                }
                if (akubraRepository.datastreamExists(pid, KnownDatastreams.IMG_THUMB)) {
                    try (InputStream imgThumb = akubraRepository.getDatastreamContent(pid, KnownDatastreams.IMG_THUMB).asInputStream()) {
                        if (imgThumb != null) replaceLocationByBinaryContent(modifiedFoxml, imgThumb, KnownDatastreams.IMG_THUMB.toString());
                    }
                }
            }
            return Response.ok().entity(modifiedFoxml.asXML()).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    private byte[] replaceLocationByBinaryContent(Document modifiedFoxml, InputStream content, String streamName) throws IOException {
        String xpath = String.format("/foxml:digitalObject/foxml:datastream[@ID='%s']/foxml:datastreamVersion", streamName);
        Node version = Dom4jUtils.buildXpath(xpath).selectSingleNode(modifiedFoxml.getRootElement());
        byte[] bytes = IOUtils.toByteArray(content);
        if (version != null) {
            for (Node node : Dom4jUtils.buildXpath("//foxml:contentLocation").selectNodes(version)) node.detach();
            Element element = (Element) version;
            element.addElement("binaryContent", element.getNamespaceURI()).setText(new String(Base64.encodeBase64(bytes)));
        }
        return bytes;
    }

    private String model(Document foxml) {
        Element modelNode = (Element) Dom4jUtils.buildXpath("//model:hasModel").selectSingleNode(foxml.getRootElement());
        if (modelNode == null) return null;
        Namespace ns = new Namespace("rdf", Dom4jUtils.NAMESPACE_URIS.get("rdf"));
        Attribute attribute = modelNode.attribute(new QName("resource", ns));
        return attribute == null ? null : attribute.getStringValue();
    }

    private static Document removeSecuredDatastreams(Document foxmlDoc) {
        synchronized (foxmlDoc) {
            List<Node> toRemove = new ArrayList<>();
            for (String dsId : FedoraUtils.getSecuredStreams()) {
                toRemove.addAll(Dom4jUtils.buildXpath(String.format("//foxml:datastream[@ID='%s']", dsId)).selectNodes(foxmlDoc));
            }
            toRemove.forEach(Node::detach);
            return foxmlDoc;
        }
    }

    protected Response localInfo(String pid) throws java.io.IOException {
        checkSupportedObjectPid(pid);
        checkObjectExists(pid);
        JSONObject json = new JSONObject();
        json.put("data", ExtractDataInfoUtils.extractAvailableDataInfo(akubraRepository, pid));
        json.put("image", ExtractDataInfoUtils.extractImageSourceInfo(akubraRepository, pid));
        RuntimeInformation information = RightRuntimeInformations.extractInformations(rightsResolver, solrAccess, pid);
        json.put(RightRuntimeInformations.PROVIDED_BY_LICENSES, information.getProvidingLicensesAsJSONArray());
        json.put(RightRuntimeInformations.ACCESSIBLE_LOCSK, information.getLockAsJSONArray());
        return Response.ok(json).build();
    }

    protected Response localInfoData(String pid) {
        checkSupportedObjectPid(pid);
        checkObjectExists(pid);
        return Response.ok(ExtractDataInfoUtils.extractAvailableDataInfo(akubraRepository, pid)).build();
    }

    protected Response localInfoProvidedByLicenses(String pid) throws java.io.IOException {
        checkSupportedObjectPid(pid);
        checkObjectExists(pid);
        RuntimeInformation information = RightRuntimeInformations.extractInformations(rightsResolver, solrAccess, pid);
        JSONObject json = new JSONObject();
        json.put("licenses", information.getLockAsJSONArray());
        return Response.ok(json).build();
    }

    protected Response localInfoStructure(String pid) {
        checkSupportedObjectPid(pid);
        checkObjectExists(pid);
        return Response.ok(akubraRepository.pi().extractStructureInfo(pid)).build();
    }

    protected Response localInfoImage(String pid) {
        checkSupportedObjectPid(pid);
        checkObjectExists(pid);
        return Response.ok(ExtractDataInfoUtils.extractImageSourceInfo(akubraRepository, pid)).build();
    }
}
