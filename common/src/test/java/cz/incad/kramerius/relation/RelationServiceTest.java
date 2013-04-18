/*
 * Copyright (C) 2010 Jan Pokorsky
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

package cz.incad.kramerius.relation;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.KrameriusModels;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.fedora.api.ArrayOfString;
import org.fedora.api.FedoraAPIM;
import org.junit.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 *
 * @author Jan Pokorsky
 */
public class RelationServiceTest {

    private interface ModelHandler {
        void handle(RelationModel model);
    }

    private static final String RELS_EXT = "RELS-EXT";
    private static final Pattern XML_DECLARATION_PATTERN = Pattern.compile("<\\?[xX][mM][lL]");
    private Injector injector;
    private FedoraAccess mockFedora;

    public RelationServiceTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        injector = Guice.createInjector(new RelationGuiceModule());
        mockFedora = injector.getInstance(
                Key.get(FedoraAccess.class, Names.named("rawFedoraAccess")));
        assertNotNull("missing FedoraAccess", mockFedora);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of load method, of class RelationService.
     */
    @Test
    public void testLoad() throws Exception {
        System.out.println("load");
        String pid = "uuid:0eaa6730-9068-11dd-97de-000d606f5dc6";
        URL relResource = RelationServiceTest.class.getResource("RelationServiceTestLoad.xml");
        assertNotNull("missing RELS-EXT", relResource);

        EasyMock.expect(mockFedora.getDataStream(pid, RELS_EXT)).andReturn(relResource.openStream());
        EasyMock.replay(mockFedora);

        RelationService service = injector.getInstance(RelationService.class);
        assertNotNull("missing RelationService", service);

        RelationModel result = service.load(pid);
        assertNotNull("RelationModel not loaded", result);

        Set<KrameriusModels> resultKinds = result.getRelationKinds();
        Set<KrameriusModels> expKinds = EnumSet.of(KrameriusModels.PAGE, KrameriusModels.DONATOR);
        assertEquals(expKinds, resultKinds);

        assertEquals(KrameriusModels.MONOGRAPH, result.getKind());

        List<Relation> expPages = Arrays.asList(
                new Relation("uuid:4308eb80-b03b-11dd-a0f6-000d606f5dc6", KrameriusModels.PAGE),
                new Relation("uuid:4a79bd50-af36-11dd-a60c-000d606f5dc6", KrameriusModels.PAGE)
                );

        assertEquals(expPages, result.getRelations(KrameriusModels.PAGE));

        List<Relation> expDonators = Arrays.asList(
                new Relation("donator:norway", KrameriusModels.DONATOR)
                );

        assertEquals(expDonators, result.getRelations(KrameriusModels.DONATOR));
        EasyMock.verify(mockFedora);
    }

    /**
     * Test of save method, of class RelationService.
     */
    @Test
    public void testSaveOnAddPage() throws Exception {
        doTestSaveImpl(new ModelHandler() {

            @Override
            public void handle(RelationModel model) {
                List<Relation> pages = model.getRelations(KrameriusModels.PAGE);
                pages.add(new Relation("relation:inserted", KrameriusModels.PAGE));
            }
        }, "RelationServiceTestLoad.xml", "RelationServiceTestSaveOnAddPage.golden.xml");
    }

    @Test
    public void testSaveOnRemovePage() throws Exception {
        doTestSaveImpl(new ModelHandler() {

            @Override
            public void handle(RelationModel model) {
                List<Relation> pages = model.getRelations(KrameriusModels.PAGE);
                pages.remove(0);
            }
        }, "RelationServiceTestLoad.xml", "RelationServiceTestSaveOnRemovePage.golden.xml");
    }

    @Test
    public void testSaveOnRemoveRelationKindPage() throws Exception {
        final RelationModel[] handledModel = new RelationModel[1];
        doTestSaveImpl(new ModelHandler() {

            @Override
            public void handle(RelationModel model) {
                model.removeRelationKind(KrameriusModels.PAGE);
                handledModel[0] = model;
            }
        }, "RelationServiceTestLoad.xml", "RelationServiceTestSaveOnRemoveAllPages.golden.xml");

        assertNull("model not trimmed", handledModel[0].getRelations(KrameriusModels.PAGE));
    }

    @Test
    public void testSaveOnRemoveAllPages() throws Exception {
        final RelationModel[] handledModel = new RelationModel[1];
        doTestSaveImpl(new ModelHandler() {

            @Override
            public void handle(RelationModel model) {
                List<Relation> pages = model.getRelations(KrameriusModels.PAGE);
                pages.clear();
                handledModel[0] = model;
            }
        }, "RelationServiceTestLoad.xml", "RelationServiceTestSaveOnRemoveAllPages.golden.xml");

        assertNull("model not trimmed", handledModel[0].getRelations(KrameriusModels.PAGE));
    }

    /*@Test
    public void testSaveOnChangeOrder() throws Exception {
        doTestSaveImpl(new ModelHandler() {

            @Override
            public void handle(RelationModel model) {
                List<Relation> pages = model.getRelations(KrameriusModels.PAGE);
                Collections.reverse(pages);
            }
        }, "RelationServiceTestLoad.xml", "RelationServiceTestSaveOnChangeOrder.golden.xml");
    }

    @Test
    public void testSaveOnNoChange() throws Exception {
        doTestSaveImpl(new ModelHandler() {

            @Override
            public void handle(RelationModel model) {
                // do nothing
            }
        }, "RelationServiceTestLoad.xml", "noChange.mock.xml");
    } */

    private String doTestSaveImpl(ModelHandler modelHandler, String loadPath, String goldenPath) throws Exception {
        String pid = "uuid:0eaa6730-9068-11dd-97de-000d606f5dc6";
        final URL relResource = RelationServiceTest.class.getResource(loadPath);
        assertNotNull("missing RELS-EXT", relResource);

        FedoraAPIM mockAPIM = EasyMock.createMock(FedoraAPIM.class);
        Capture<byte[]> captureStreamBytes = new Capture<byte[]>();
        EasyMock.reset(mockFedora, mockAPIM);
        EasyMock.expect(mockFedora.getDataStream(pid, RELS_EXT)).andAnswer(new IAnswer<InputStream>() {

            @Override
            public InputStream answer() throws Throwable {
                return relResource.openStream();
            }
        }).times(2);

        if (!"noChange.mock.xml".equals(goldenPath)) {
            // mock modification
            EasyMock.expect(mockFedora.getAPIM()).andReturn(mockAPIM);
            EasyMock.expect(mockAPIM.modifyDatastreamByValue(
                    EasyMock.eq(pid), EasyMock.eq(RELS_EXT),
                    EasyMock.<ArrayOfString>anyObject(), EasyMock.<String>anyObject(),
                    EasyMock.<String>anyObject(), EasyMock.<String>anyObject(),
                    EasyMock.capture(captureStreamBytes),
                    EasyMock.<String>anyObject(), EasyMock.<String>anyObject(),
                    EasyMock.<String>anyObject(), EasyMock.anyBoolean())
                    ).andReturn(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date()));
        }
        EasyMock.replay(mockFedora, mockAPIM);

        RelationService service = injector.getInstance(RelationService.class);
        assertNotNull("missing RelationService", service);

        RelationModel model = service.load(pid);
        assertNotNull("RelationModel not loaded", model);
        modelHandler.handle(model);

        service.save(pid, model);

        EasyMock.verify(mockFedora, mockAPIM);
        if ("noChange.mock.xml".equals(goldenPath)) {
            // test no change
            assertFalse("do not touch the fedora in case of no modification", captureStreamBytes.hasCaptured());
            return null;
        }

        assertTrue(captureStreamBytes.hasCaptured());
        byte[] streamBytes = captureStreamBytes.getValue();
        assertNotNull(streamBytes);
        String msContent = new String(streamBytes, "UTF-8");
        assertNotNull(msContent);
        Reader expStream = new InputStreamReader(
                RelationServiceTest.class.getResourceAsStream(goldenPath));
        Reader resStream = new StringReader(msContent);

        XMLUnit.setIgnoreWhitespace(true);
        DetailedDiff diff = new DetailedDiff(new Diff(expStream, resStream));
        assertTrue(diff.toString(), diff.identical());
        assertFalse("forbidden xml declaration\n" + msContent, XML_DECLARATION_PATTERN.matcher(msContent).find());
//        System.out.println("msContent:\n" + msContent);
        return msContent;
    }
}