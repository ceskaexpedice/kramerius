package cz.incad.kramerius.document.impl;

import com.google.inject.Provider;
import com.lowagie.text.DocumentException;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.document.model.AbstractPage;
import cz.incad.kramerius.document.model.ImagePage;
import cz.incad.kramerius.document.model.AkubraDocument;
import cz.incad.kramerius.pdf.impl.SimplePDFServiceImpl;
import cz.incad.kramerius.security.SecuredAkubraRepository;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.utils.pid.LexerException;
import org.apache.commons.io.IOUtils;
import org.ceskaexpedice.akubra.DatastreamContentWrapper;
import org.ceskaexpedice.akubra.DatastreamMetadata;
import org.ceskaexpedice.akubra.KnownDatastreams;
import org.ceskaexpedice.akubra.impl.DatastreamContentWrapperImpl;
import org.ceskaexpedice.akubra.impl.utils.relsext.RelsExtInternalSaxUtils;
import org.ceskaexpedice.akubra.relsext.RelsExtHelper;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.easymock.EasyMock.createMockBuilder;

public class DocumentServiceImplTest {

    private static final String IIIF_JSON_WITHOUT_PHYSICAL_SCALE = "{\"tiles\":[{\"scaleFactors\":[1,2,4],\"width\":256,\"height\":256}],\"protocol\":\"http://iiif.io/api/image\",\"sizes\":[{\"width\":154,\"height\":206},{\"width\":308,\"height\":413},{\"width\":617,\"height\":827}],\"profile\":[\"http://iiif.io/api/image/2/level1.json\",{\"formats\":[\"jpg\"],\"maxHeight\":30000,\"qualities\":[\"native\",\"color\",\"gray\"],\"supports\":[\"regionByPct\",\"regionSquare\",\"sizeByForcedWh\",\"sizeByWh\",\"sizeAboveFull\",\"rotationBy90s\",\"mirroring\"],\"maxWidth\":30000}],\"width\":1235,\"@id\":\"http://lmda-k7.silvarium.cz/search/iiif/uuid:b7dcc0f3-6a33-11f1-896b-001b63bd97ba\",\"@context\":\"http://iiif.io/api/image/2/context.json\",\"height\":1654}";

    @Test
    public void testCreatePage() throws IOException, LexerException, DocumentException {
        AkubraDocument kramDoc = new AkubraDocument("monograph","uuid:xx");

        SecuredAkubraRepository akubraMock = EasyMock.createMock(SecuredAkubraRepository.class);
        SolrAccess solrMock = EasyMock.createMock(SolrAccess.class);
        Provider<Locale> localeProviderMock = EasyMock.createMock(Provider.class);
        ResourceBundleService resourceBundleMock = EasyMock.createMock(ResourceBundleService.class);
        RelsExtHelper relsExtHelperMock = EasyMock.createMock(RelsExtHelper.class);

        DocumentServiceImpl docServiceMock = createMockBuilder(DocumentServiceImpl.class)
                .withConstructor(
                        SecuredAkubraRepository.class,
                        SolrAccess.class,
                        Provider.class,
                        ResourceBundleService.class
                )
                .withArgs(
                        akubraMock,
                        solrMock,
                        localeProviderMock,
                        resourceBundleMock
                )
                .addMockedMethod("iiifJson")
                .addMockedMethod("isUseAlto")
                .createMock();

        EasyMock.expect(docServiceMock.iiifJson("https://imageserver.mzk.cz/NDK/2026/01/b3a01ed3-d731-11f0-b3b2-5acb2ee39cf4/uc_b3a01ed3-d731-11f0-b3b2-5acb2ee39cf4_0001")).andAnswer(()-> {
            InputStream is = DocumentServiceImpl.class.getResourceAsStream("iiif2.json");
            String json = IOUtils.toString(is, StandardCharsets.UTF_8);
            return json;
        }).anyTimes();

        EasyMock.expect(akubraMock.re()).andReturn(relsExtHelperMock).anyTimes();

        EasyMock.expect(relsExtHelperMock.getTilesUrl("uuid:xxxx")).andAnswer(()-> {
            InputStream is = DocumentServiceImpl.class.getResourceAsStream("foxml.xml");
            String tilesUrl = RelsExtInternalSaxUtils.getTilesUrl(is);
            return tilesUrl;
        }).anyTimes();

        EasyMock.expect(relsExtHelperMock.getModel("uuid:xxxx")).andAnswer(()-> {
            InputStream is = DocumentServiceImpl.class.getResourceAsStream("rels-ext.xml");
            return RelsExtInternalSaxUtils.getModel(is);
        }).anyTimes();

        EasyMock.expect(resourceBundleMock.getResourceBundle("base", Locale.getDefault())).andAnswer(()-> {
            return ResourceBundle.getBundle("base", Locale.getDefault());
        }).anyTimes();

        EasyMock.expect(akubraMock.datastreamExists("uuid:xxxx", KnownDatastreams.IMG_FULL)).andReturn(true).anyTimes();
        EasyMock.expect(akubraMock.datastreamExists("uuid:xxxx", KnownDatastreams.OCR_ALTO)).andReturn(true).anyTimes();
        EasyMock.expect(akubraMock.getDatastreamMetadata("uuid:xxxx", KnownDatastreams.IMG_FULL.name())).andReturn(new DatastreamMetadata() {
                @Override
                public String getId() {
                    return "";
                }

                @Override
                public String getMimetype() {
                    return "image/png";
                }

                @Override
                public String getControlGroup() {
                    return "E";
                }

                @Override
                public String getLocation() {
                    return "";
                }

                @Override
                public Date getLastModified() {
                    return null;
                }

                @Override
                public Date getCreateDate() {
                    return null;
                }
            }).anyTimes();

        EasyMock.expect(akubraMock.getDatastreamContent("uuid:xxxx", KnownDatastreams.IMG_FULL)).andAnswer(()-> {
            InputStream is = DocumentServiceImpl.class.getResourceAsStream("image.jpg");
            if (is == null) {
                throw new AssertionError("Missing mods.xml");
            }

            DatastreamContentWrapper contentWrapper = new DatastreamContentWrapperImpl(is);
            return contentWrapper;

        }).anyTimes();


        EasyMock.expect(akubraMock.getDatastreamContent("uuid:xxxx", KnownDatastreams.BIBLIO_MODS))
            .andAnswer(() -> {
                InputStream is = DocumentServiceImpl.class.getResourceAsStream("mods.xml");
                if (is == null) {
                    throw new AssertionError("Missing mods.xml");
                }
                return new DatastreamContentWrapperImpl(is);
            }).anyTimes();

        EasyMock.expect(akubraMock.getDatastreamContent("uuid:xxxx", KnownDatastreams.BIBLIO_DC))
            .andAnswer(() -> {
                InputStream is = DocumentServiceImpl.class.getResourceAsStream("dc.xml");
                if (is == null) {
                    throw new AssertionError("Missing dc.xml");
                }
                    return new DatastreamContentWrapperImpl(is);
            }).anyTimes();

        EasyMock.expect(localeProviderMock.get()).andAnswer(()-> {
            return Locale.getDefault();
        }).anyTimes();

        EasyMock.expect(docServiceMock.isUseAlto()).andReturn(false).anyTimes();


        EasyMock.replay(akubraMock, solrMock, localeProviderMock, resourceBundleMock,relsExtHelperMock, docServiceMock);
        AbstractPage page = docServiceMock.createPage(kramDoc, "uuid:xxxx");
        //((ImagePage)page).setAltoXML(null);

        Assert.assertNotNull(page);
        Assert.assertTrue(page instanceof ImagePage);
        ImagePage imagePage = (ImagePage) page;
        kramDoc.addPage(imagePage);
        kramDoc.pageDimensionFromFirstPage();

        double physicalScale = imagePage.getScaleFactor();
        Assert.assertTrue(physicalScale == 0.00846668);
        double height = imagePage.getHeight();
        Assert.assertTrue(height == 3261);
        double width = imagePage.getWidth();
        Assert.assertTrue(width == 2314);
    }

    @Test
    public void testCreatePageFromIiifJsonWithoutPhysicalScale() throws IOException, LexerException, DocumentException {
        AkubraDocument kramDoc = new AkubraDocument("monograph","uuid:xx");

        SecuredAkubraRepository akubraMock = EasyMock.createMock(SecuredAkubraRepository.class);
        SolrAccess solrMock = EasyMock.createMock(SolrAccess.class);
        Provider<Locale> localeProviderMock = EasyMock.createMock(Provider.class);
        ResourceBundleService resourceBundleMock = EasyMock.createMock(ResourceBundleService.class);
        RelsExtHelper relsExtHelperMock = EasyMock.createMock(RelsExtHelper.class);

        DocumentServiceImpl docServiceMock = createMockBuilder(DocumentServiceImpl.class)
                .withConstructor(
                        SecuredAkubraRepository.class,
                        SolrAccess.class,
                        Provider.class,
                        ResourceBundleService.class
                )
                .withArgs(
                        akubraMock,
                        solrMock,
                        localeProviderMock,
                        resourceBundleMock
                )
                .addMockedMethod("iiifJson")
                .addMockedMethod("isUseAlto")
                .createMock();

        String tilesUrl = "http://lmda-k7.silvarium.cz/search/iiif/uuid:b7dcc0f3-6a33-11f1-896b-001b63bd97ba";
        EasyMock.expect(docServiceMock.iiifJson(tilesUrl)).andReturn(IIIF_JSON_WITHOUT_PHYSICAL_SCALE).anyTimes();

        EasyMock.expect(akubraMock.re()).andReturn(relsExtHelperMock).anyTimes();
        EasyMock.expect(relsExtHelperMock.getTilesUrl("uuid:b7dcc0f3-6a33-11f1-896b-001b63bd97ba")).andReturn(tilesUrl).anyTimes();
        EasyMock.expect(relsExtHelperMock.getModel("uuid:b7dcc0f3-6a33-11f1-896b-001b63bd97ba")).andReturn("page").anyTimes();

        EasyMock.expect(resourceBundleMock.getResourceBundle("base", Locale.getDefault())).andAnswer(()-> {
            return ResourceBundle.getBundle("base", Locale.getDefault());
        }).anyTimes();

        EasyMock.expect(akubraMock.datastreamExists("uuid:b7dcc0f3-6a33-11f1-896b-001b63bd97ba", KnownDatastreams.IMG_FULL)).andReturn(true).anyTimes();

        EasyMock.expect(akubraMock.getDatastreamContent("uuid:b7dcc0f3-6a33-11f1-896b-001b63bd97ba", KnownDatastreams.BIBLIO_MODS))
            .andAnswer(() -> {
                InputStream is = DocumentServiceImpl.class.getResourceAsStream("mods.xml");
                if (is == null) {
                    throw new AssertionError("Missing mods.xml");
                }
                return new DatastreamContentWrapperImpl(is);
            }).anyTimes();

        EasyMock.expect(akubraMock.getDatastreamContent("uuid:b7dcc0f3-6a33-11f1-896b-001b63bd97ba", KnownDatastreams.BIBLIO_DC))
            .andAnswer(() -> {
                InputStream is = DocumentServiceImpl.class.getResourceAsStream("dc.xml");
                if (is == null) {
                    throw new AssertionError("Missing dc.xml");
                }
                return new DatastreamContentWrapperImpl(is);
            }).anyTimes();

        EasyMock.expect(localeProviderMock.get()).andAnswer(()-> {
            return Locale.getDefault();
        }).anyTimes();

        EasyMock.expect(docServiceMock.isUseAlto()).andReturn(false).anyTimes();

        EasyMock.replay(akubraMock, solrMock, localeProviderMock, resourceBundleMock,relsExtHelperMock, docServiceMock);
        AbstractPage page = docServiceMock.createPage(kramDoc, "uuid:b7dcc0f3-6a33-11f1-896b-001b63bd97ba");

        Assert.assertNotNull(page);
        Assert.assertTrue(page instanceof ImagePage);

        ImagePage imagePage = (ImagePage) page;
        Assert.assertFalse(imagePage.isPhysicalDimensionsSet());
        Assert.assertEquals(1235, imagePage.getWidth(), 0.0);
        Assert.assertEquals(1654, imagePage.getHeight(), 0.0);
        Assert.assertEquals(0.24, imagePage.getScaleFactor(), 0.000001);
        Assert.assertNotNull(imagePage.getPageDimension());
        Assert.assertEquals(296.4, imagePage.getPageDimension().width(), 0.0001);
        Assert.assertEquals(396.96, imagePage.getPageDimension().height(), 0.0001);
    }
}
