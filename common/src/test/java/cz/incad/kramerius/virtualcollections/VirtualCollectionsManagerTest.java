/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.kramerius.virtualcollections;

import cz.incad.kramerius.pdf.utils.SimpleFedoraAccessModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import cz.incad.kramerius.AbstractGuiceTestCase;
import cz.incad.kramerius.FedoraAccess;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author alberto
 */
public class VirtualCollectionsManagerTest extends AbstractGuiceTestCase{
    
    final FedoraAccess fedoraAccess = injector().getInstance(FedoraAccess.class);
    
    @Override
    protected Injector injector() {
        Injector injector = Guice.createInjector(new SimpleFedoraAccessModule());
        return injector;
    }
    
    public VirtualCollectionsManagerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getVirtualCollections method, of class VirtualCollectionsManager.
     */
    @Test
    public void testGetVirtualCollections() {
        System.out.println("getVirtualCollections");
        String[] langs = null;
        List expResult = null;
        List result = VirtualCollectionsManager.getVirtualCollections(fedoraAccess, langs);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of create method, of class VirtualCollectionsManager.
     */
    @Test
    public void testCreate() throws Exception {
        System.out.println("create");
        String label = "";
        String pid = "";
        VirtualCollectionsManager.create(label, pid, fedoraAccess);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of delete method, of class VirtualCollectionsManager.
     */
    @Test
    public void testDelete() throws Exception {
        System.out.println("delete");
        String pid = "";
        VirtualCollectionsManager.delete(pid, fedoraAccess);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of removeDocumentsFromCollection method, of class VirtualCollectionsManager.
     */
    @Test
    public void testRemoveDocumentsFromCollection() throws Exception {
        System.out.println("removeDocumentsFromCollection");
        String collection = "vc:chemie";
        VirtualCollectionsManager.removeDocumentsFromCollection(collection, fedoraAccess);
    }

    /**
     * Test of modify method, of class VirtualCollectionsManager.
     */
    @Test
    public void testModify() throws Exception {
        System.out.println("modify");
        String pid = "";
        String label = "";
        VirtualCollectionsManager.modify(pid, label, fedoraAccess);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of modifyDatastream method, of class VirtualCollectionsManager.
     */
    @Test
    public void testModifyDatastream() throws Exception {
        System.out.println("modifyDatastream");
        String pid = "";
        String lang = "";
        String ds = "";
        String k4url = "";
        VirtualCollectionsManager.modifyDatastream(pid, lang, ds, fedoraAccess, k4url);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addToCollection method, of class VirtualCollectionsManager.
     */
    @Test
    public void testAddToCollection() throws Exception {
        System.out.println("addToCollection");
        String pid = "uuid:0eaa6730-9068-11dd-97de-000d606f5dc6";
        String collection = "vc:chemie";
        VirtualCollectionsManager.addToCollection(pid, collection, fedoraAccess);
    }
    
    /**
     * Test of addToCollection method, of class VirtualCollectionsManager.
     */
    @Test
    public void testRemoveFromCollection() throws Exception {
        System.out.println("removeFromCollection");
        String pid = "uuid:0eaa6730-9068-11dd-97de-000d606f5dc6";
        String collection = "vc:chemie";
        VirtualCollectionsManager.removeFromCollection(pid, collection, fedoraAccess);
    }

    /**
     * Test of removeCollections method, of class VirtualCollectionsManager.
     */
    @Test
    public void testRemoveCollections() throws Exception {
        System.out.println("removeCollections");
        String pid = "uuid:0eaa6730-9068-11dd-97de-000d606f5dc6";
        VirtualCollectionsManager.removeCollections(pid, fedoraAccess);
    }

    /**
     * Test of main method, of class VirtualCollectionsManager.
     */
    @Test
    public void testMain() throws Exception {
        System.out.println("main");
        String[] args = null;
        VirtualCollectionsManager.main(args);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}
