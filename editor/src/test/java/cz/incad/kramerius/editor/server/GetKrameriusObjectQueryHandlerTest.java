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

package cz.incad.kramerius.editor.server;

import cz.incad.kramerius.editor.server.HandlerTestUtils.RelationModelBuilder;
import cz.incad.kramerius.editor.server.HandlerTestUtils.GWTKrameriusObjectBuilder;
import cz.incad.kramerius.editor.share.GWTKrameriusObject.Kind;
import java.io.IOException;
import java.util.Map;
import java.util.List;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.KrameriusModels;
import cz.incad.kramerius.editor.share.GWTKrameriusObject;
import cz.incad.kramerius.editor.share.rpc.GetKrameriusObjectQuery;
import cz.incad.kramerius.editor.share.rpc.GetKrameriusObjectResult;
import cz.incad.kramerius.relation.RelationModel;
import cz.incad.kramerius.relation.RelationService;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jan Pokorsky
 */
public class GetKrameriusObjectQueryHandlerTest {

    private Injector injector;
    private GetKrameriusObjectQueryHandler queryHandler;
    private RelationService mockRelationService;
    private FedoraAccess mockFedora;
    private RemoteServices mockRemotes;

    public GetKrameriusObjectQueryHandlerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        injector = Guice.createInjector(new TestGuiceModule());
        mockFedora = injector.getInstance(Key.get(FedoraAccess.class, Names.named("rawFedoraAccess")));
        mockRelationService = injector.getInstance(RelationService.class);
        mockRemotes = injector.getInstance(RemoteServices.class);
        queryHandler = injector.getInstance(GetKrameriusObjectQueryHandler.class);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of execute method, of class GetKrameriusObjectQueryHandler.
     */
    @Test
    public void testExecute() throws Exception {
        System.out.println("execute");
        String pid = "uuid:00000000-0000-0000-0000-000000000001";
        ExecuteData testData = createTestExecuteData(pid);

        EasyMock.expect(mockRelationService.load(pid)).andReturn(testData.relModel);
        EasyMock.expect(mockRemotes.fetchDCName(EasyMock.anyObject(String.class)))
                // inject real implementation
                .andDelegateTo(new RemoteServicesImpl(mockFedora)).anyTimes();
        expectGetDCDataStreams(testData);
        EasyMock.replay(mockFedora, mockRelationService, mockRemotes);

        GetKrameriusObjectQuery action = new GetKrameriusObjectQuery(pid);
        GetKrameriusObjectResult result = queryHandler.execute(action, null);

        assertNotNull("result", result);

        GWTKrameriusObject expGkobj = testData.gkobj;
        GWTKrameriusObject gkobj = result.getResult();
        assertGWTKrameriusObjectEquals(expGkobj, gkobj);
        EasyMock.verify(mockFedora, mockRelationService, mockRemotes);
    }

    private static String DC_TEMPLATE =
            "<oai_dc:dc xmlns:dc=\"http://purl.org/dc/elements/1.1/\""
            + " xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\""
            + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
            + " xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd\">"
            + "  <dc:title>%s</dc:title>"
            + "</oai_dc:dc>";

    /** XXX this should be moved to commons test of DCUtils.titleFromDC() */
    private void expectGetDCDataStreams(final ExecuteData testData) throws IOException {
        final Capture<String> pidCapture = new Capture<String>();
        EasyMock.expect(mockFedora.getDataStream(
                EasyMock.capture(pidCapture),
                EasyMock.eq("DC"))
                ).andAnswer(new IAnswer<InputStream>() {

            @Override
            public InputStream answer() throws Throwable {
                String pid = pidCapture.getValue();
                String title = testData.pid2title.get(pid);
                if (title == null) {
                    throw new IllegalStateException("undefined title for pid: " + pid);
                }
                byte[] buf = String.format(DC_TEMPLATE, title).getBytes("UTF-8");
                return new ByteArrayInputStream(buf);
            }
        }).anyTimes();
    }

    private ExecuteData createTestExecuteData(String pid) {
        Map<String, String> pid2title = new HashMap<String, String>();
        String title = "Monograph Title";
        pid2title.put(pid, title);
        String pagePid1 = "uuid:00000000-0000-0000-0000-000000000002";
        String pageTitle1 = "Page1 Title";
        pid2title.put(pagePid1, pageTitle1);
        String pagePid2 = "uuid:00000000-0000-0000-0000-000000000003";
        String pageTitle2 = "Page2 Title";
        pid2title.put(pagePid2, pageTitle2);
        String unitPid1 = "uuid:00000000-0000-0000-0000-000000000004";
        String unitTitle1 = "Unit1 Title";
        pid2title.put(unitPid1, unitTitle1);

        RelationModel model = new RelationModelBuilder(pid, KrameriusModels.MONOGRAPH)
                .addRelations(KrameriusModels.PAGE, pagePid1, pagePid2)
                .addRelations(KrameriusModels.MONOGRAPHUNIT, unitPid1)
                .toInstance();

        GWTKrameriusObject expGkobj = new GWTKrameriusObjectBuilder(pid, Kind.MONOGRAPH, title)
                .addRelations(Kind.PAGE, pagePid1, pageTitle1, pagePid2, pageTitle2)
                .addRelations(Kind.MONOGRAPH_UNIT, unitPid1, unitTitle1)
                .toInstance();

        return new ExecuteData(model, expGkobj, pid2title);
    }

    private static final class ExecuteData {
        RelationModel relModel;
        GWTKrameriusObject gkobj;
        private final Map<String, String> pid2title;

        public ExecuteData(RelationModel rrm, GWTKrameriusObject gkobj, Map<String, String> pid2title) {
            this.relModel = rrm;
            this.gkobj = gkobj;
            this.pid2title = pid2title;
        }
    }

    private boolean assertGWTKrameriusObjectEquals(GWTKrameriusObject exp, GWTKrameriusObject res) {
        assertEquals("pid", exp.getPID(), res.getPID());
        assertEquals("kind", exp.getKind(), res.getKind());
        assertEquals("title", exp.getTitle(), res.getTitle());
        assertRelationMapEquals(exp.getRelations(), res.getRelations());
        return true;
    }

    private boolean assertRelationMapEquals(Map<Kind, List<GWTKrameriusObject>> exp, Map<Kind, List<GWTKrameriusObject>> res) {
        if (exp != null) {
            assertEquals("relation kinds", exp.keySet(), res.keySet());
            for (Kind expRelKind : exp.keySet()) {
                assertRelationListEquals(exp.get(expRelKind), res.get(expRelKind));
            }
        } else {
            assertNull("relations", res);
        }
        return true;
    }

    private boolean assertRelationListEquals(List<GWTKrameriusObject> exp, List<GWTKrameriusObject> res) {
        assertNotNull("relation list", res);
        assertEquals("list size", exp.size(), res.size());
        for (int i = 0; i < exp.size(); i++) {
            GWTKrameriusObject expObj = exp.get(i);
            GWTKrameriusObject resObj = res.get(i);
            assertGWTKrameriusObjectEquals(expObj, resObj);
        }
        return true;
    }

//    /**
//     * Test of rollback method, of class GetKrameriusObjectQueryHandler.
//     */
//    @Test
//    public void testRollback() throws Exception {
//        System.out.println("rollback");
//        GetKrameriusObjectQuery action = null;
//        GetKrameriusObjectResult result_2 = null;
//        ExecutionContext context = null;
//        GetKrameriusObjectQueryHandler instance = null;
//        instance.rollback(action, result_2, context);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

}