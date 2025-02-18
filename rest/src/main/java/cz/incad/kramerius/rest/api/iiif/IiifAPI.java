package cz.incad.kramerius.rest.api.iiif;

import com.fasterxml.jackson.core.JsonProcessingException;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.rest.api.k5.client.SolrMemoization;
import cz.incad.kramerius.rest.api.k5.client.item.exceptions.PIDNotFound;
import cz.incad.kramerius.rest.api.k5.client.item.utils.ItemResourceUtils;
import cz.incad.kramerius.rest.api.k5.client.utils.PIDSupport;
import cz.incad.kramerius.rest.utils.IIIFUtils;
import cz.incad.kramerius.utils.ApplicationURL;
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
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hc.client5.http.async.methods.AbstractBinResponseConsumer;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.async.methods.SimpleResponseConsumer;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.nio.AsyncRequestProducer;
import org.apache.hc.core5.http.nio.support.AsyncRequestBuilder;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.util.Timeout;
import org.apache.hc.client5.http.async.HttpAsyncClient;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.json.JSONObject;
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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import cz.incad.kramerius.rest.api.k5.client.item.utils.ItemResourceUtils;


/**
 * IiiPresentationApi
 *
 * @author Martin Rumanek
 */
@Path("/")
public class IiifAPI {

    private static final Logger LOGGER = Logger.getLogger(IiifAPI.class.getName());

    @Inject
    private SolrMemoization solrMemoization;

    private AkubraRepository akubraRepository;

    private SolrAccess solrAccess;

    private Provider<HttpServletRequest> requestProvider;

    private URI iiifUri;

    private HttpAsyncClient asyncClient;

    @Inject
    public IiifAPI(SolrMemoization solrMemoization,
                   // TODO AK_NEW @Named("cachedFedoraAccess") FedoraAccess fedoraAccess,
                   AkubraRepository akubraRepository,
                   @Named("new-index") SolrAccess solrAccess, Provider<HttpServletRequest> requestProvider, HttpAsyncClient asyncClient) {
        this.solrMemoization = solrMemoization;
        this.akubraRepository = akubraRepository;
        this.solrAccess = solrAccess;
        this.requestProvider = requestProvider;
        this.asyncClient = asyncClient;

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

            Map<String, Pair<Integer, Integer>> resolutions = getResolutions(children);

            for (String p : children) {
                String repPid = p.replace("/", "");
                if (repPid.equals(pid)) {
                    continue;
                }

                DocumentDto page = getIiifDocument(repPid);
                if (!"page".equals(page.getModel())) continue;

                String id = ApplicationURL.applicationURL(this.requestProvider.get()) + "/canvas/" + repPid;
                Pair<Integer, Integer> resolution = resolutions.get(p);
                if (resolution != null) {
                    Canvas canvas = new CanvasImpl(id, new PropertyValueSimpleImpl(page.getTitle()), resolution.getLeft(),
                            resolution.getRight());

                    ImageResource resource = new ImageResourceImpl();
                    String resourceId = ApplicationURL.applicationURL(this.requestProvider.get()).toString() + "/iiif/"
                            + repPid + "/full/full/0/default.jpg";
                    resource.setType("dctypes:Image");
                    resource.setId(resourceId);
                    resource.setHeight(resolution.getLeft());
                    resource.setWidth(resolution.getRight());
                    resource.setFormat("image/jpeg");

                    Service service = new ServiceImpl();
                    service.setContext("http://iiif.io/api/image/2/context.json");
                    service.setId(
                            ApplicationURL.applicationURL(this.requestProvider.get()).toString() + "/iiif/" + repPid);
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
        } catch (InterruptedException e) {
            throw new GenericApplicationException(e.getMessage());
        }
    }


    private String toJSON(IiifResource resource) throws JsonProcessingException {
        IiifPresentationApiObjectMapper objectMapper = new IiifPresentationApiObjectMapper();
        return objectMapper.writeValueAsString(resource);
    }

    private Map<String, Pair<Integer, Integer>> getResolutions(List<String> children) throws IOException, InterruptedException {
        final Map<String, Pair<Integer, Integer>> resolutions = new ConcurrentHashMap<>();

        List<CompletableFuture<Void>> futures = children.stream()
                .map(pid -> CompletableFuture.runAsync(() -> {
                    String iiifEndpoint = null;
                    try {
                        iiifEndpoint = IIIFUtils.iiifImageEndpoint(pid, akubraRepository);
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage());
                    }
                    if (iiifEndpoint != null) {
                        AsyncRequestProducer producer = AsyncRequestBuilder.get(iiifEndpoint + "/info.json").build();
                        SimpleResponseConsumer consumer = SimpleResponseConsumer.create();

                        try {
                            Future<SimpleHttpResponse> responseFuture = asyncClient.execute(producer, consumer, null, null, null);
                            SimpleHttpResponse response = responseFuture.get(); // wait for the request

                            String json = response.getBodyText();
                            JSONObject jsonObject = new JSONObject(json);

                            Pair<Integer, Integer> resolution = new Pair<Integer, Integer>() {
                                @Override
                                public Integer getLeft() {
                                    return jsonObject.getInt("height");
                                }

                                @Override
                                public Integer getRight() {
                                    return jsonObject.getInt("width");
                                }

                                @Override
                                public Integer setValue(Integer value) {
                                    return null;
                                }
                            };
                            resolutions.put(pid, resolution);
                        } catch (Exception e) {
                            LOGGER.log(Level.SEVERE, e.getMessage());
                        }
                    }
                }))
                .collect(Collectors.toList());

        // Wait for all futures to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        return resolutions;
    }

    private DocumentDto getIiifDocument(String pid) throws IOException {
        Element indexDoc = this.solrMemoization.getRememberedIndexedDoc(pid);
        if (indexDoc == null) {
            indexDoc = this.solrMemoization.askForIndexDocument(pid);
        }
        DocumentDto document = new DocumentDto(pid, indexDoc);
        return document;
    }

    protected void checkPid(String pid) throws PIDNotFound {
        try {
            if (PIDSupport.isComposedPID(pid)) {
                String p = PIDSupport.first(pid);
                // TODO AK_NEW this.fedoraAccess.getRelsExt(p);
            } else {
                // TODO AK_NEW this.fedoraAccess.getRelsExt(pid);
            }
        } catch (Exception e) {
            throw new PIDNotFound("error while parsing pid (" + pid + ")");
        }
    }
}
