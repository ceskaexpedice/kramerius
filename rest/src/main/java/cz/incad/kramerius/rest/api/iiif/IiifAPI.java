package cz.incad.kramerius.rest.api.iiif;

import com.fasterxml.jackson.core.JsonProcessingException;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.rest.api.k5.client.SolrMemoization;
import cz.incad.kramerius.rest.api.k5.client.item.exceptions.PIDNotFound;
import cz.incad.kramerius.rest.api.k5.client.item.utils.IIIFUtils;
import cz.incad.kramerius.rest.api.k5.client.item.utils.ItemResourceUtils;
import cz.incad.kramerius.rest.api.k5.client.utils.PIDSupport;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.RESTHelper;
import de.digitalcollections.iiif.presentation.model.api.v2.Canvas;
import de.digitalcollections.iiif.presentation.model.api.v2.IiifResource;
import de.digitalcollections.iiif.presentation.model.api.v2.Image;
import de.digitalcollections.iiif.presentation.model.api.v2.ImageResource;
import de.digitalcollections.iiif.presentation.model.api.v2.Manifest;
import de.digitalcollections.iiif.presentation.model.api.v2.PropertyValue;
import de.digitalcollections.iiif.presentation.model.api.v2.Sequence;
import de.digitalcollections.iiif.presentation.model.api.v2.Service;
import de.digitalcollections.iiif.presentation.model.impl.jackson.v2.IiifPresentationApiObjectMapper;
import de.digitalcollections.iiif.presentation.model.impl.v2.CanvasImpl;
import de.digitalcollections.iiif.presentation.model.impl.v2.ImageImpl;
import de.digitalcollections.iiif.presentation.model.impl.v2.ImageResourceImpl;
import de.digitalcollections.iiif.presentation.model.impl.v2.ManifestImpl;
import de.digitalcollections.iiif.presentation.model.impl.v2.PropertyValueSimpleImpl;
import de.digitalcollections.iiif.presentation.model.impl.v2.SequenceImpl;
import de.digitalcollections.iiif.presentation.model.impl.v2.ServiceImpl;

import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * IiiPresentationApi
 *
 * @author Martin Rumanek
 */
@Path("/")
public class IiifAPI {

    private SolrMemoization solrMemoization;

    private FedoraAccess fedoraAccess;

    private SolrAccess solrAccess;

    private Provider<HttpServletRequest> requestProvider;

    private URI iiifUri;

    @Inject
    public IiifAPI(SolrMemoization solrMemoization, @Named("cachedFedoraAccess") FedoraAccess fedoraAccess,
                   SolrAccess solrAccess, Provider<HttpServletRequest> requestProvider) {
        this.solrMemoization = solrMemoization;
        this.fedoraAccess = fedoraAccess;
        this.solrAccess = solrAccess;
        this.requestProvider = requestProvider;

        try {
            this.iiifUri = new URI(ApplicationURL.applicationURL(this.requestProvider.get()) + "/iiif-presentation/");
        } catch (URISyntaxException e) {
            throw new GenericApplicationException(e.getMessage());
        }
    }

    @GET
    @Path("{pid}/manifest")
    @Produces({MediaType.APPLICATION_JSON + ";charset=utf-8"})
    public Response manifest(@PathParam("pid") String pid) {
        checkPid(pid);
        try {
            DocumentDto document = getIiifDocument(pid);
            PropertyValue titleLabel = new PropertyValueSimpleImpl(document.getTitle());
            Manifest manifest = new ManifestImpl(UriBuilder.fromUri(iiifUri).path(getClass(), "manifest").build(pid),
                    titleLabel);
            List<String> fieldList = new ArrayList<String>();
            List<Canvas> canvases = new ArrayList<Canvas>();
            List<String> children = ItemResourceUtils.solrChildrenPids(pid, fieldList, solrAccess, solrMemoization);

            for (String p : children) {
                String repPid = p.replace("/", "");
                if (repPid.equals(pid)) {
                    continue;
                }

                DocumentDto page = getIiifDocument(repPid);
                if (!"page".equals(page.getModel())) continue;

                String id = ApplicationURL.applicationURL(this.requestProvider.get()) + "/canvas/" + repPid;
                Pair<Integer, Integer> resolution = getResolution(repPid);
                if (resolution != null) {
                    Canvas canvas = new CanvasImpl(id, new PropertyValueSimpleImpl(page.getTitle()), resolution.getLeft(),
                            resolution.getRight());

                    ImageResource resource = new ImageResourceImpl();
                    String resourceId = ApplicationURL.applicationURL(this.requestProvider.get()) + "/iiif/"
                            + repPid + "/full/full/0/default.jpg";
                    resource.setType("dctypes:Image");
                    resource.setId(resourceId);
                    resource.setHeight(resolution.getLeft());
                    resource.setWidth(resolution.getRight());
                    resource.setFormat("image/jpeg");

                    Service service = new ServiceImpl();
                    service.setContext("http://iiif.io/api/image/2/context.json");
                    service.setId(
                            ApplicationURL.applicationURL(this.requestProvider.get()) + "/iiif/" + repPid);
                    service.setProfile("http://iiif.io/api/image/2/level1.json");

                    resource.setService(service);
                    Image image = new ImageImpl();
                    image.setOn(new URI(id));
                    image.setResource(resource);
                    canvas.setImages(Collections.singletonList(image));
                    canvases.add(canvas);
                }
            }

            // no pages - 500 ?
            if (canvases.isEmpty()) {
                throw new GenericApplicationException("cannot create manifest for pid '" + pid + "'");
            }

            Sequence sequence = new SequenceImpl();
            sequence.setCanvases(canvases);
            manifest.setSequences(Collections.singletonList(sequence));

            return Response.ok().entity(toJSON(manifest)).build();

        } catch (IOException e) {
            throw new GenericApplicationException(e.getMessage());
        } catch (URISyntaxException e) {
            throw new GenericApplicationException(e.getMessage());
        } catch (XPathExpressionException e) {
            throw new GenericApplicationException(e.getMessage());
        }
    }

    private String toJSON(IiifResource resource) throws JsonProcessingException {
        IiifPresentationApiObjectMapper objectMapper = new IiifPresentationApiObjectMapper();
        return objectMapper.writeValueAsString(resource);
    }

    private Pair<Integer, Integer> getResolution(String pid) throws XPathExpressionException, IOException {
        String iiifEndpoint = IIIFUtils.iiifImageEndpoint(pid, this.fedoraAccess);
        return iiifEndpoint != null ? resolution(iiifEndpoint) : null;
    }

    private Pair<Integer, Integer> resolution(String url) throws IOException {
        URLConnection con = RESTHelper.openConnection(url + "/info.json", "", "");
        InputStream inputStream = con.getInputStream();
        String json = org.apache.commons.io.IOUtils.toString(inputStream, Charset.defaultCharset());
        final org.json.JSONObject jsonObject = new org.json.JSONObject(json);

        final Integer width = jsonObject.getInt("width");
        final Integer height = jsonObject.getInt("height");

        return new Pair<Integer, Integer>() {
            @Override
            public Integer getLeft() {
                return height;
            }

            @Override
            public Integer getRight() {
                return width;
            }

            @Override
            public Integer setValue(Integer value) {
                return null;
            }
        };
    }

    private DocumentDto getIiifDocument(String pid) throws IOException {
        Element indexDoc = this.solrMemoization.getRememberedIndexedDoc(pid);
        if (indexDoc == null) {
            indexDoc = this.solrMemoization.askForIndexDocument(pid);
        }
        return new DocumentDto(pid, indexDoc);
    }

    private void checkPid(String pid) throws PIDNotFound {
        try {
            if (PIDSupport.isComposedPID(pid)) {
                String p = PIDSupport.first(pid);
                this.fedoraAccess.getRelsExt(p);
            } else {
                this.fedoraAccess.getRelsExt(pid);
            }
        } catch (IOException e) {
            throw new PIDNotFound("pid not found");
        } catch (Exception e) {
            throw new PIDNotFound("error while parsing pid (" + pid + ")");
        }
    }

}
