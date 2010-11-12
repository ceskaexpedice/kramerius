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

import com.google.inject.Guice;
import com.google.inject.Injector;
import cz.incad.kramerius.KrameriusModels;
import cz.incad.kramerius.editor.server.HandlerTestUtils.GWTKrameriusObjectBuilder;
import cz.incad.kramerius.editor.server.HandlerTestUtils.RelationModelBuilder;
import cz.incad.kramerius.editor.share.GWTKrameriusObject;
import cz.incad.kramerius.editor.share.GWTKrameriusObject.Kind;
import cz.incad.kramerius.editor.share.GWTRelationModel;
import cz.incad.kramerius.editor.share.rpc.SaveRelationsQuery;
import cz.incad.kramerius.editor.share.rpc.SaveRelationsResult;
import cz.incad.kramerius.relation.RelationModel;
import cz.incad.kramerius.relation.RelationService;
import java.util.Set;
import net.customware.gwt.dispatch.server.ExecutionContext;
import org.easymock.Capture;
import org.easymock.EasyMock;
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
public class SaveRelationsQueryHandlerTest {

    private static class ExecuteData {
        RelationModel expModel;
        GWTKrameriusObject expGkobj;

        public ExecuteData(RelationModel expModel, GWTKrameriusObject expGkobj) {
            this.expModel = expModel;
            this.expGkobj = expGkobj;
        }
    }

    private Injector injector;
    private RelationService mockRelationService;
    private SaveRelationsQueryHandler queryHandler;
    private RemoteServices mockRemotes;

    public SaveRelationsQueryHandlerTest() {
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
        mockRelationService = injector.getInstance(RelationService.class);
        queryHandler = injector.getInstance(SaveRelationsQueryHandler.class);
        mockRemotes = injector.getInstance(RemoteServices.class);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of execute method, of class SaveRelationsQueryHandler.
     */
    @Test
    public void testExecute() throws Exception {
        System.out.println("execute");
        String pid = "uuid:00000000-0000-0000-0000-000000000001";
        ExecuteData testData = createTestExecuteData(pid);
        Capture<RelationModel> modelCapture = new Capture<RelationModel>();
        mockRemotes.reindex(pid);
        mockRelationService.save(EasyMock.eq(pid), EasyMock.capture(modelCapture));
        EasyMock.replay(mockRelationService, mockRemotes);

        SaveRelationsQuery action = new SaveRelationsQuery(new GWTRelationModel(testData.expGkobj));
        ExecutionContext context = null;
        SaveRelationsResult result = queryHandler.execute(action, context);
        assertEquals("save query result", SaveRelationsResult.SaveRelationsState.OK, result.get());

        assertTrue("model capture", modelCapture.hasCaptured());
        RelationModel resModel = modelCapture.getValue();
        assertRelationModelEquals(testData.expModel, resModel);
        EasyMock.verify(mockRelationService, mockRemotes);
    }

    private ExecuteData createTestExecuteData(String pid) {
        String title = "Monograph Title";
        String pagePid1 = "uuid:00000000-0000-0000-0000-000000000002";
        String pageTitle1 = "Page1 Title";
        String pagePid2 = "uuid:00000000-0000-0000-0000-000000000003";
        String pageTitle2 = "Page2 Title";
        String unitPid1 = "uuid:00000000-0000-0000-0000-000000000004";
        String unitTitle1 = "Unit1 Title";

        RelationModel expModel = new RelationModelBuilder(pid, KrameriusModels.MONOGRAPH)
                .addRelations(KrameriusModels.PAGE, pagePid1, pagePid2)
                .addRelations(KrameriusModels.MONOGRAPHUNIT, unitPid1)
                .toInstance();

        GWTKrameriusObject expGkobj = new GWTKrameriusObjectBuilder(pid, Kind.MONOGRAPH, title)
                .addRelations(Kind.PAGE, pagePid1, pageTitle1, pagePid2, pageTitle2)
                .addRelations(Kind.MONOGRAPH_UNIT, unitPid1, unitTitle1)
                .toInstance();

        return new ExecuteData(expModel, expGkobj);
    }

    private static void assertRelationModelEquals(RelationModel expModel, RelationModel resModel) {
        assertEquals("model kind", expModel.getKind(), resModel.getKind());
        Set<KrameriusModels> expRelationKinds = expModel.getRelationKinds();
        Set<KrameriusModels> resRelationKinds = resModel.getRelationKinds();
        assertEquals("relation kinds", expRelationKinds, resRelationKinds);

        for (KrameriusModels kind : expRelationKinds) {
            assertEquals(kind + " relations", expModel.getRelations(kind), resModel.getRelations(kind));
        }
    }

}