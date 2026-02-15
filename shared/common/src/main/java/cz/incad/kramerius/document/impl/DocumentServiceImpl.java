/*
 * Copyright (C) 2010 Pavel Stastny
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.kramerius.document.impl;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;

import javax.xml.xpath.XPathExpressionException;

import cz.incad.kramerius.document.model.*;
import cz.incad.kramerius.iiif.IIIFUtils;
import cz.incad.kramerius.security.SecuredAkubraRepository;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.KnownDatastreams;
import org.ceskaexpedice.akubra.RepositoryNamespaces;
import org.ceskaexpedice.akubra.relsext.TreeNodeProcessor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.document.DocumentService;
import cz.incad.kramerius.pdf.OutOfRangeException;
import cz.incad.kramerius.pdf.utils.TitlesUtils;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.utils.DCUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.mods.PageNumbersBuilder;
import cz.incad.kramerius.utils.pid.LexerException;

/**
 * Default document service implementation
 *
 * @author pavels
 */
public class DocumentServiceImpl implements DocumentService {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(DocumentServiceImpl.class.getName());

    private AkubraRepository akubraRepository;
    private Provider<Locale> localeProvider;
    private ResourceBundleService resourceBundleService;
    private SolrAccess solrAccess;
    private boolean useAlto;

    @Inject
    public DocumentServiceImpl(
            SecuredAkubraRepository akubraRepository,
            @Named("new-index") SolrAccess solrAccess,
            Provider<Locale> localeProvider,
            ResourceBundleService resourceBundleService
            ) {
        super();
        this.akubraRepository = akubraRepository;
        this.localeProvider = localeProvider;
        this.resourceBundleService = resourceBundleService;
        this.solrAccess = solrAccess;
        try {
            this.init();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }

        this.useAlto =  KConfiguration.getInstance().getConfiguration().getBoolean("pdfQueue.useAlto", false);

    }

    private void init() throws IOException {

    }

    @Override
    public void setUseAlto(boolean useAlto) {
        this.useAlto = useAlto;
    }

    @Override
    public boolean isUseAlto() {
        return this.useAlto;
    }

    protected void buildRenderingDocumentAsFlat(
            final AkubraDocument renderedDocument, final String pidFrom,
            final int howMany) throws IOException {
        if (pidFrom != null && akubraRepository.datastreamExists(pidFrom, KnownDatastreams.IMG_FULL)) {

            ObjectPidsPath[] path = solrAccess.getPidPaths(pidFrom);
            String[] pathFromLeafToRoot = path[0].getPathFromLeafToRoot();
            // { str, clanek, monografie }
            String parent = null;
            if (pathFromLeafToRoot.length > 1) {
                parent = pathFromLeafToRoot[1];
            } else {
                parent = pidFrom;
            }

            akubraRepository.re().processInTree(parent, new TreeNodeProcessor() {
                private int index = 0;
                private boolean acceptingState = false;

                @Override
                public void process(String pid, int level) {
                    try {
                        if (akubraRepository.datastreamExists(pid, KnownDatastreams.IMG_FULL)) {
                            if (pid.equals(pidFrom) || (pidFrom == null)) {
                                acceptingState = true;
                            }
                            if (acceptingState) {
                                if (index < howMany) {
                                    renderedDocument.addPage(createPage(
                                            renderedDocument, pid));
                                }
                                index += 1;
                                System.out.println("Index: " + index);
                                if (index >= howMany) {
                                    acceptingState = false;
                                }
                            }
                        }
                    } catch (LexerException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    }
                }

                @Override
                public boolean skipBranch(String pid, int level) {
                    return false;
                }

                @Override
                public boolean breakProcessing(String pid, int level) {
                    return index >= howMany;
                }
            });

        } else {
            // find first parent
            String pagePid = akubraRepository.re().getFirstViewablePidInTree(pidFrom);
            String parentPid = pidFrom;
            ObjectPidsPath[] path = solrAccess.getPidPaths(pagePid);
            String[] pathFromLeafToRoot = path[0].getPathFromLeafToRoot();
            if (pathFromLeafToRoot.length > 1) {
                parentPid = pathFromLeafToRoot[1];
            }

            akubraRepository.re().processInTree(parentPid, new TreeNodeProcessor() {

                private int index = 0;

                @Override
                public void process(String pid, int level) {
                    try {
                        if (akubraRepository.datastreamExists(pid, KnownDatastreams.IMG_FULL)) {
                            if (index < howMany) {
                                renderedDocument.addPage(createPage(
                                        renderedDocument, pid));
                            }
                            index += 1;
                        }
                    } catch (LexerException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    }
                }

                @Override
                public boolean breakProcessing(String pid, int level) {
                    return index >= howMany;
                }

                @Override
                public boolean skipBranch(String pid, int level) {
                    return false;
                }
            });
        }
    }

    protected void buildRenderingDocumentAsTree(
            /* org.w3c.dom.Document relsExt, */final AkubraDocument renderedDocument,
            final String pid) throws IOException {

        akubraRepository.re().processInTree(pid, new TreeNodeProcessor() {
            private OutlineItem currOutline = null;

            @Override
            public void process(String pid, int level) {
                try {
                	AbstractPage page = null;

                	if (akubraRepository.datastreamExists(pid, KnownDatastreams.IMG_FULL)) {
                    	page = createPage(renderedDocument, pid);
                        renderedDocument.addPage(page);
                        this.currOutline = createOutlineItem(
                                renderedDocument.getOutlineItemRoot(),
                                page.getOutlineDestination(),
                                page.getOutlineTitle(), 1);
                        StringBuffer buffer = new StringBuffer();
                        this.currOutline.debugInformations(buffer, 0);

                    } else {
                    	// no page
                    }
                } catch (DOMException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    throw new RuntimeException(e);
                } catch (LexerException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            }

            private OutlineItem createOutlineItem(OutlineItem parent,
                    String objectId, String biblioModsTitle, int level) {
                OutlineItem item = new OutlineItem();
                item.setDestination(objectId);

                item.setTitle(biblioModsTitle);

                parent.addChild(item);
                item.setParent(parent);
                item.setLevel(level);
                return item;
            }

            @Override
            public boolean breakProcessing(String pid, int level) {
                return false;
            }

            @Override
            public boolean skipBranch(String pid, int level) {
                return false;
            }
        });
    }

	protected AbstractPage createTextPage(final AkubraDocument renderedDocument,
            String pid) throws LexerException, IOException {
		throw new IllegalStateException();
	}

    protected String iiifJson(String tilesUrl) {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            String infoJson = tilesUrl + ( tilesUrl.endsWith("/") ? "" : "/" )+ "/info.json";
            try (final CloseableHttpResponse httpResponse = httpclient.execute(new HttpGet(infoJson))) {

                switch (httpResponse.getStatusLine().getStatusCode()) {
                    case 200:
                        return org.apache.commons.io.IOUtils.toString(httpResponse.getEntity().getContent());
                    case 404:
                        return null;
                    default:
                        return null;
                }

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    protected AbstractPage createPage(final AkubraDocument renderedDocument,
            String pid) throws LexerException, IOException {

        try {
            Document biblioMods = akubraRepository.getDatastreamContent(pid, KnownDatastreams.BIBLIO_MODS).asDom(true);
            String tilesUrl = IIIFUtils.iiifImageEndpoint(pid, akubraRepository);
            //String s = this.iiifJson(tilesUrl);

            String iifResponse =tilesUrl != null ?  iiifJson(tilesUrl) : null;
            Document dc = akubraRepository.getDatastreamContent(pid, KnownDatastreams.BIBLIO_DC).asDom(false);
            String modelName = akubraRepository.re().getModel(pid);
            ResourceBundle resourceBundle = resourceBundleService.getResourceBundle("base", localeProvider.get());

            AbstractPage page = null;


            if (akubraRepository.datastreamExists(pid, KnownDatastreams.IMG_FULL)) {

                ImagePage imagePage = new ImagePage(modelName, pid);
                imagePage.setOutlineDestination(pid);
                imagePage.setBiblioMods(biblioMods);
                imagePage.setDc(dc);
                page = imagePage;

                if (iifResponse != null) {
                    JSONObject iifJson = new JSONObject(iifResponse);

                    if (iifJson.has("service")) {
                        JSONArray service = iifJson.getJSONArray("service");
                        if (service.length() > 0) {
                            JSONObject serviceJson = service.getJSONObject(0);
                            double physicalScale = serviceJson.getDouble("physicalScale");
                            imagePage.setScaleFactor(physicalScale);
                            String physicalUnits = serviceJson.getString("physicalUnits");
                            imagePage.setPhysicalDimensionUnit(physicalUnits);

                            imagePage.setPhysicalDimensionsSet(true);
                        }
                    }

                    if (iifJson.has("width")) {
                        double width = iifJson.getDouble("width");
                        imagePage.setWidth(width); // width in px
                    }
                    if (iifJson.has("height")) {
                        double height = iifJson.getDouble("height");
                        imagePage.setHeight(height); // height in px
                    }

                    if (imagePage.isPhysicalDimensionsSet()) {
                        double realWidth = imagePage.getWidth() * imagePage.getScaleFactor();
                        double realHeight = imagePage.getHeight() * imagePage.getScaleFactor();
                        float divider = 2.54f;

                        String unit = imagePage.getPhysicalDimensionUnit() != null ? imagePage.getPhysicalDimensionUnit().trim() : "cm";
                        switch (unit) {
                            case "cm":
                                divider = 2.54f;
                                break;
                            case "mm":
                                divider = 25.4f;
                                break;
                            default:
                                divider = 2.54f;
                                break;
                        }
                        float pointsPerCm = 72f / divider;
                        imagePage.setPageDimension(new PageDimension((float) (realWidth * pointsPerCm),(float) (realHeight * pointsPerCm)));
                    } else {
                        // Default scale factor

                        float dpi = 300f; // da se nacist z jpegu
                        float scaleFactor = 72f / dpi;
                        imagePage.setScaleFactor(72f / dpi);

                        imagePage.setPageDimension(new PageDimension(
                                (float) (imagePage.getWidth() * scaleFactor),
                                (float) (imagePage.getHeight() * scaleFactor)
                        ));

                        // neni nactene z iiif deskriptoru
                        imagePage.setPhysicalDimensionsSet(false);
                    }
                } else {

                    Dimension rawDim = KrameriusImageSupport.readDimension(pid, FedoraUtils.IMG_FULL_STREAM, akubraRepository, 0);
                    if (rawDim.getWidth() > 0.0)  imagePage.setWidth(rawDim.getWidth());
                    if (rawDim.getHeight() > 0.0)  imagePage.setHeight(rawDim.getHeight());

                    // Default scale factor
                    float dpi = 300f;
                    float scaleFactor = 72f / dpi;

                    imagePage.setPageDimension(new PageDimension(
                            (float) (imagePage.getWidth() * scaleFactor),
                            (float) (imagePage.getHeight() * scaleFactor)
                    ));
                    imagePage.setScaleFactor(72f / dpi);

                    imagePage.setPhysicalDimensionsSet(false);

                }

                // alto for image page
                //boolean useAlto = KConfiguration.getInstance().getConfiguration().getBoolean("pdfQueue.useAlto", false);
                if (isUseAlto() && akubraRepository.datastreamExists(pid, KnownDatastreams.OCR_ALTO)) {
                    Document ocrLAlto = akubraRepository.getDatastreamContent(pid, KnownDatastreams.OCR_ALTO).asDom(true);
                    imagePage.setAltoXML(ocrLAlto);
                }

                Map<String, List<String>> map = new HashMap<String, List<String>>();
                PageNumbersBuilder pageNumbersBuilder = new PageNumbersBuilder();
                pageNumbersBuilder.build(biblioMods, map, modelName);
                List<String> pageNumbers = map
                        .get(PageNumbersBuilder.MODS_PAGENUMBER);
                pageNumbers = pageNumbers != null ? pageNumbers
                        : new ArrayList<String>();
                String pageNumber = pageNumbers.isEmpty() ? "" : pageNumbers
                        .get(0);

                page.setPageNumber(pageNumber);
                // renderedDocument.addPage(page);
                Element part = XMLUtils.findElement(
                        biblioMods.getDocumentElement(), "part",
                        RepositoryNamespaces.BIBILO_MODS_URI);
                String attribute = part != null ? part.getAttribute("type")
                        : null;
                if (attribute != null) {
                    String key = "pdf." + attribute;
                    if (resourceBundle.containsKey(key)) {
                        page.setOutlineTitle(page.getPageNumber() + " "
                                + resourceBundle.getString(key));
                    } else {
                        page.setOutlineTitle(page.getPageNumber());
                        // throw new RuntimeException("");
                    }
                }
                if ((renderedDocument.getUuidTitlePage() == null)
                        && ("TitlePage".equals(attribute))) {
                    renderedDocument.setUuidTitlePage(pid);
                }

                if ((renderedDocument.getUuidFrontCover() == null)
                        && ("FrontCover".equals(attribute))) {
                    renderedDocument.setUuidFrontCover(pid);
                }

                if ((renderedDocument.getUuidBackCover() == null)
                        && ("BackCover".equals(attribute))) {
                    renderedDocument.setUuidBackCover(pid);
                }

                if (renderedDocument.getFirstPage() == null) {
                    renderedDocument.setFirstPage(pid);
                }

            } else {

            		page = new TextPage(modelName,akubraRepository.re().getFirstViewablePidInTree(pid));
                    page.setOutlineDestination(pid);

                    page.setBiblioMods(biblioMods);
                    page.setDc(dc);

                    Map<String, List<String>> map = new HashMap<String, List<String>>();
                    PageNumbersBuilder pageNumbersBuilder = new PageNumbersBuilder();
                    pageNumbersBuilder.build(biblioMods, map, modelName);
                    List<String> pageNumbers = map
                            .get(PageNumbersBuilder.MODS_PAGENUMBER);
                    pageNumbers = pageNumbers != null ? pageNumbers
                            : new ArrayList<String>();
                    String pageNumber = pageNumbers.isEmpty() ? "" : pageNumbers
                            .get(0);

                    page.setPageNumber(pageNumber);
                    // renderedDocument.addPage(page);
                    Element part = XMLUtils.findElement(
                            biblioMods.getDocumentElement(), "part",
                            RepositoryNamespaces.BIBILO_MODS_URI);
                    String attribute = part != null ? part.getAttribute("type")
                            : null;
                    if (attribute != null) {
                        String key = "pdf." + attribute;
                        if (resourceBundle.containsKey(key)) {
                            page.setOutlineTitle(page.getPageNumber() + " "
                                    + resourceBundle.getString(key));
                        } else {
                            page.setOutlineTitle(page.getPageNumber());
                            // throw new RuntimeException("");
                        }
                    }
                    if ((renderedDocument.getUuidTitlePage() == null)
                            && ("TitlePage".equals(attribute))) {
                        renderedDocument.setUuidTitlePage(pid);
                    }

                    if ((renderedDocument.getUuidFrontCover() == null)
                            && ("FrontCover".equals(attribute))) {
                        renderedDocument.setUuidFrontCover(pid);
                    }

                    if ((renderedDocument.getUuidBackCover() == null)
                            && ("BackCover".equals(attribute))) {
                        renderedDocument.setUuidBackCover(pid);
                    }

                    if (renderedDocument.getFirstPage() == null) {
                        renderedDocument.setFirstPage(pid);
                    }
            	}
            return page;
        } catch (XPathExpressionException e) {
            throw new IOException(e);
        }
    }

    @Override
    public AkubraDocument buildDocumentFromSelection(String[] selection,
                                                     float[] rect) throws IOException {

        try {
            final AkubraDocument renderedDocument = new AkubraDocument(
                    "selection", selection[0]);
            if ((rect != null) && (rect.length == 2)) {
                PageDimension dim = new PageDimension(rect[0], rect[1]);
                renderedDocument.setPageDimension(dim);
//                renderedDocument.setWidth(rect[0]);
//                renderedDocument.setHeight(rect[1]);
            }
            for (String pid : selection) {
                renderedDocument.addPage(createPage(renderedDocument, pid));
                Document doc = akubraRepository.getDatastreamContent(pid, KnownDatastreams.BIBLIO_DC).asDom(false);
                renderedDocument.mapDCConent(pid, DCUtils.contentFromDC(doc));
            }
            return renderedDocument;
        } catch (LexerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
//    public PreparedDocument buildDocumentAsFlat(ObjectPidsPath path,
//                                                String pidFrom, int howMany, int[] rect) throws IOException, OutOfRangeException {
//        return buildDocumentAsFlat(path, pidFrom, howMany, rect, true);
//    }

    @Override
    public AkubraDocument buildDocumentAsFlat(ObjectPidsPath path,
                                              String pidFrom, int howMany, float[] rect) throws IOException, OutOfRangeException {

        String leaf = path.getLeaf();
        ResourceBundle resourceBundle = resourceBundleService
                .getResourceBundle("base", localeProvider.get());

        final AkubraDocument renderedDocument = new AkubraDocument(akubraRepository.re().getModel(leaf), pidFrom);
        if ((rect != null) && (rect.length == 2)) {
                PageDimension dim = new PageDimension(rect[0], rect[1]);
                renderedDocument.setPageDimension(dim);
        }
        renderedDocument.setObjectPidsPath(path);
        renderedDocument.setDocumentTitle(TitlesUtils.title(leaf, this.solrAccess, akubraRepository, resourceBundle));
        renderedDocument.setUuidTitlePage(path.getLeaf());
        renderedDocument.setUuidMainTitle(path.getRoot());
        if (path != null) {
            String[] pids = path.getPathFromLeafToRoot();
            for (String pid : pids) {
                Document dcDocument = akubraRepository.getDatastreamContent(pid, KnownDatastreams.BIBLIO_DC).asDom(false);
                renderedDocument.mapDCConent(pid, DCUtils.contentFromDC(dcDocument));
            }
        }
        buildRenderingDocumentAsFlat(renderedDocument, pidFrom, howMany);
        return renderedDocument;
    }

    @Override
    public AkubraDocument buildDocumentAsTree(ObjectPidsPath path,
                                              String pidFrom, float[] rect) throws IOException {
        ResourceBundle resourceBundle = resourceBundleService.getResourceBundle("base", localeProvider.get());

        String leaf = path.getLeaf();
        String modelName = akubraRepository.re().getModel(leaf);

        final AkubraDocument renderedDocument = new AkubraDocument(
                modelName, pidFrom);
        if ((rect != null) && (rect.length == 2)) {
            PageDimension dim = new PageDimension(rect[0], rect[1]);
            renderedDocument.setPageDimension(dim);
//            renderedDocument.setWidth(rect[0]);
//            renderedDocument.setHeight(rect[1]);
        }

        renderedDocument.setDocumentTitle(TitlesUtils.title(leaf,
                this.solrAccess, akubraRepository, resourceBundle));
        renderedDocument.setUuidTitlePage(path.getLeaf());
        renderedDocument.setUuidMainTitle(path.getRoot());

        // TextPage dpage = new TextPage(modelName, path.getRoot());
        // dpage.setOutlineDestination("desc");
        // dpage.setOutlineTitle("Popis");
        // renderedDocument.addPage(dpage);

        // OutlineItem item = new OutlineItem();
        // item.setLevel(1);
        // item.setParent(renderedDocument.getOutlineItemRoot());
        // item.setTitle("Popis"); item.setDestination("desc");
        // renderedDocument.getOutlineItemRoot().addChild(item);

        buildRenderingDocumentAsTree(renderedDocument, pidFrom);

        return renderedDocument;
    }
}
