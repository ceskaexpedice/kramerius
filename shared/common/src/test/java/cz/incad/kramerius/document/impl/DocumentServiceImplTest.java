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

    @Test
    public void testCreatePage() throws IOException, LexerException, DocumentException {
        AkubraDocument kramDoc = new AkubraDocument("monograph","uuid:xx");

        SecuredAkubraRepository akubraMock = EasyMock.createMock(SecuredAkubraRepository.class);
        SolrAccess solrMock = EasyMock.createMock(SolrAccess.class);
        Provider<Locale> localeProviderMock = EasyMock.createMock(Provider.class);
        ResourceBundleService resourceBundleMock = EasyMock.createMock(ResourceBundleService.class);
        RelsExtHelper relsExtHelperMock = EasyMock.createMock(RelsExtHelper.class);

        /*
                    SecuredAkubraRepository akubraRepository,
            @Named("new-index") SolrAccess solrAccess,
            Provider<Locale> localeProvider, TextsService textsService,
            ResourceBundleService resourceBundleService) {

         */

//        SimplePDFServiceImpl simplePDFServiceMock = createMockBuilder(
//                SimplePDFServiceImpl.class).
//                withConstructor(
//                        SecuredAkubraRepository.class,
//                        SolrAccess.class,
//                        Provider.class,
//                        ResourceBundleService.class
//                ).withArgs(null, null, null, null).createMock();


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

        EasyMock.replay(akubraMock, solrMock, localeProviderMock, resourceBundleMock,relsExtHelperMock, docServiceMock);
        AbstractPage page = docServiceMock.createPage(kramDoc, "uuid:xxxx");
        Assert.assertNotNull(page);
        Assert.assertTrue(page instanceof ImagePage);
        ImagePage imagePage = (ImagePage) page;
        kramDoc.addPage(imagePage);
        kramDoc.pageDimensionFromFirstPage();

        /*
                - 0.00635001
                - 4340.0
                - 2977.0
         */

        /*
        ],
  "maxHeight": 12000,
  "service": [
    {
      "physicalScale": 0.00846668,
      "profile": "http://iiif.io/api/annex/services/physdim",
      "physicalUnits": "cm",
      "@context": "http://iiif.io/api/annex/services/physdim/1/context.json"
    }
  ],
  "width": 2314,
  "height": 3261,
         */
        double physicalScale = imagePage.getScaleFactor();
        Assert.assertTrue(physicalScale == 0.00846668);
        double height = imagePage.getHeight();
        Assert.assertTrue(height == 3261);
        double width = imagePage.getWidth();
        Assert.assertTrue(width == 2314);

        String template = SimplePDFServiceImpl.template(kramDoc,
                akubraMock, null, Locale.getDefault());
        System.out.println(template);

        /*
            @Inject
    public SimplePDFServiceImpl(
            SecuredAkubraRepository akubraRepository,
            @Named("new-index") SolrAccess solrAccess,
            Provider<Locale> localeProvider, TextsService textsService,
            ResourceBundleService resourceBundleService) {

         */

        File tmpFile = File.createTempFile("temp",".pdf");
        System.out.println(tmpFile.getAbsolutePath());

        SimplePDFServiceImpl simplePDFService = new SimplePDFServiceImpl(
                akubraMock, solrMock, localeProviderMock, null, null);
        simplePDFService.pdf(kramDoc, new FileOutputStream(tmpFile), null);
    }
}
