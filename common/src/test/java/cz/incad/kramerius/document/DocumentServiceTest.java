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
package cz.incad.kramerius.document;

import static junit.framework.Assert.assertEquals;
import static org.easymock.EasyMock.replay;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.xml.crypto.Data;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;

import org.apache.http.pool.ConnFactory;
import org.easymock.EasyMock;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.name.Names;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.document.impl.DocumentServiceImpl;
import cz.incad.kramerius.document.model.AbstractPage;
import cz.incad.kramerius.document.model.PreparedDocument;
import cz.incad.kramerius.document.model.DCConent;
import cz.incad.kramerius.document.model.OutlineItem;
import cz.incad.kramerius.fedora.impl.DataPrepare;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.pdf.OutOfRangeException;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;

public class DocumentServiceTest {

    public static String BUNLDE = "# procesy\n"
                                    +"key=value\n";

    public static Map<String, String> MODELS_MAPPING = new HashMap<String, String>(); static {
        MODELS_MAPPING.put("uuid:0eaa6730-9068-11dd-97de-000d606f5dc6", "info:fedora/model:monograph");
        for (int i = 0; i < DataPrepare.DROBNUSTKY_PIDS.length; i++) {
            String pid = DataPrepare.DROBNUSTKY_PIDS[i];
            if (i > 0) {
                MODELS_MAPPING.put(pid, "info:fedora/model:page");
            }
        }
    }
    
    public static Map<String, ObjectPidsPath> PATHS_MAPPING = new HashMap<String, ObjectPidsPath>(); static {
        // monograph -> page
        PATHS_MAPPING.put(DataPrepare.DROBNUSTKY_PIDS[0], new ObjectPidsPath(DataPrepare.DROBNUSTKY_PIDS[0]));
        for (int i = 0; i < DataPrepare.DROBNUSTKY_PIDS.length; i++) {
            String pid = DataPrepare.DROBNUSTKY_PIDS[i];
            if (i > 0) {
                PATHS_MAPPING.put(pid, new ObjectPidsPath(DataPrepare.DROBNUSTKY_PIDS[0],pid));
            }
        }
    }

    
    // vytvori dokument od urciteho pidu
    @Test
    public void testDocumentServiceFromPid() throws IOException, ParserConfigurationException, SAXException, LexerException, ProcessSubtreeException, SecurityException, NoSuchMethodException, OutOfRangeException {
        Injector injector = _DocumentServiceTestPrepare.prepareInjector("20",true);

        DocumentService docService = injector.getInstance(DocumentService.class);
        PreparedDocument doc = docService.buildDocumentAsFlat(PATHS_MAPPING.get(DataPrepare.DROBNUSTKY_PIDS[0]), "uuid:4a7ec660-af36-11dd-a782-000d606f5dc6", 3, new int[]{300,300});
        
        List<AbstractPage> pages = doc.getPages();
        Assert.assertTrue(pages.size() == 3);

        String[] relsExtOrder = new String[] {
                "uuid:4a7ec660-af36-11dd-a782-000d606f5dc6",
                "uuid:4314ab50-b03b-11dd-89db-000d606f5dc6",
                "uuid:4a80c230-af36-11dd-ace4-000d606f5dc6"};
        for (int i = 0; i < relsExtOrder.length; i++) {
            AbstractPage page = pages.get(i);
            String pid = relsExtOrder[i];
            Assert.assertEquals(pid, page.getUuid());
        }   
    }


    @Test
    public void testDocumentServiceFromPid3() throws IOException, ParserConfigurationException, SAXException, LexerException, ProcessSubtreeException, SecurityException, NoSuchMethodException, OutOfRangeException {
        Injector injector = _DocumentServiceTestPrepare.prepareInjector("20",true);
        
        DocumentService docService = injector.getInstance(DocumentService.class);
        PreparedDocument doc = docService.buildDocumentAsFlat(PATHS_MAPPING.get(DataPrepare.DROBNUSTKY_PIDS[0]), "uuid:4308eb80-b03b-11dd-a0f6-000d606f5dc6", 20, new int[]{300,300});
        List<AbstractPage> pages = doc.getPages();
        Assert.assertTrue(pages.size() == 16);

        String[] relsExtOrder = new String[] {

                "uuid:4308eb80-b03b-11dd-a0f6-000d606f5dc6",
                "uuid:4a79bd50-af36-11dd-a60c-000d606f5dc6",
                "uuid:430d7f60-b03b-11dd-82fa-000d606f5dc6",
                "uuid:4a7c2e50-af36-11dd-9643-000d606f5dc6",

                "uuid:43101770-b03b-11dd-8673-000d606f5dc6",
                "uuid:4a7ec660-af36-11dd-a782-000d606f5dc6",
                "uuid:4314ab50-b03b-11dd-89db-000d606f5dc6",
                "uuid:4a80c230-af36-11dd-ace4-000d606f5dc6",

                "uuid:43171c50-b03b-11dd-b0c2-000d606f5dc6",
                "uuid:4a835a40-af36-11dd-b951-000d606f5dc6",
                "uuid:4319b460-b03b-11dd-83ca-000d606f5dc6",
                "uuid:4a85f250-af36-11dd-8535-000d606f5dc6",
                "uuid:431e4840-b03b-11dd-8818-000d606f5dc6",

                "uuid:4a8a8630-af36-11dd-ae9c-000d606f5dc6",
                "uuid:4320e050-b03b-11dd-9b4a-000d606f5dc6",
                "uuid:4a8cf730-af36-11dd-ae88-000d606f5dc6"
        };
        for (int i = 0; i < relsExtOrder.length; i++) {
            AbstractPage page = pages.get(i);
            String pid = relsExtOrder[i];
            Assert.assertEquals(pid, page.getUuid());
        }   
    }
    // vytovori cely dokument od prvniho pidu
    @Test
    public void testDocumentServiceFromPid2() throws IOException, ParserConfigurationException, SAXException, LexerException, ProcessSubtreeException, SecurityException, NoSuchMethodException, OutOfRangeException {
        Injector injector = _DocumentServiceTestPrepare.prepareInjector("20",false);
        
        DocumentService docService = injector.getInstance(DocumentService.class);
        PreparedDocument doc = docService.buildDocumentAsFlat(PATHS_MAPPING.get(DataPrepare.DROBNUSTKY_PIDS[0]), "uuid:4308eb80-b03b-11dd-a0f6-000d606f5dc6", 20, new int[]{300,300});
        List<AbstractPage> pages = doc.getPages();
        Assert.assertTrue(pages.size() == 16);

        String[] relsExtOrder = new String[] {

                "uuid:4308eb80-b03b-11dd-a0f6-000d606f5dc6",
                "uuid:4a79bd50-af36-11dd-a60c-000d606f5dc6",
                "uuid:430d7f60-b03b-11dd-82fa-000d606f5dc6",
                "uuid:4a7c2e50-af36-11dd-9643-000d606f5dc6",

                "uuid:43101770-b03b-11dd-8673-000d606f5dc6",
                "uuid:4a7ec660-af36-11dd-a782-000d606f5dc6",
                "uuid:4314ab50-b03b-11dd-89db-000d606f5dc6",
                "uuid:4a80c230-af36-11dd-ace4-000d606f5dc6",

                "uuid:43171c50-b03b-11dd-b0c2-000d606f5dc6",
                "uuid:4a835a40-af36-11dd-b951-000d606f5dc6",
                "uuid:4319b460-b03b-11dd-83ca-000d606f5dc6",
                "uuid:4a85f250-af36-11dd-8535-000d606f5dc6",
                "uuid:431e4840-b03b-11dd-8818-000d606f5dc6",

                "uuid:4a8a8630-af36-11dd-ae9c-000d606f5dc6",
                "uuid:4320e050-b03b-11dd-9b4a-000d606f5dc6",
                "uuid:4a8cf730-af36-11dd-ae88-000d606f5dc6"
        };
        for (int i = 0; i < relsExtOrder.length; i++) {
            AbstractPage page = pages.get(i);
            String pid = relsExtOrder[i];
            Assert.assertEquals(pid, page.getUuid());
        }   
    }



    // vytvori cely dokument..  omezeny pocet stranek
    @Test
    public void testDocumentServiceFromNonPagePidReducedPages() throws IOException, ParserConfigurationException, SAXException, LexerException, ProcessSubtreeException, SecurityException, NoSuchMethodException, OutOfRangeException {
        Injector injector = _DocumentServiceTestPrepare.prepareInjector("20", false);
        
        DocumentService docService = injector.getInstance(DocumentService.class);
        PreparedDocument doc = docService.buildDocumentAsFlat(PATHS_MAPPING.get(DataPrepare.DROBNUSTKY_PIDS[0]),DataPrepare.DROBNUSTKY_PIDS[0], 2, new int[]{300,300});
        List<AbstractPage> pages = doc.getPages();
        Assert.assertTrue(pages.size() == 2);

        String[] relsExtOrder = new String[] {

                "uuid:4308eb80-b03b-11dd-a0f6-000d606f5dc6",
                "uuid:4a79bd50-af36-11dd-a60c-000d606f5dc6",
                "uuid:430d7f60-b03b-11dd-82fa-000d606f5dc6",
                "uuid:4a7c2e50-af36-11dd-9643-000d606f5dc6",

                "uuid:43101770-b03b-11dd-8673-000d606f5dc6",
                "uuid:4a7ec660-af36-11dd-a782-000d606f5dc6",
                "uuid:4314ab50-b03b-11dd-89db-000d606f5dc6",
                "uuid:4a80c230-af36-11dd-ace4-000d606f5dc6",

                "uuid:43171c50-b03b-11dd-b0c2-000d606f5dc6",
                "uuid:4a835a40-af36-11dd-b951-000d606f5dc6",
                "uuid:4319b460-b03b-11dd-83ca-000d606f5dc6",
                "uuid:4a85f250-af36-11dd-8535-000d606f5dc6",
                "uuid:431e4840-b03b-11dd-8818-000d606f5dc6",

                "uuid:4a8a8630-af36-11dd-ae9c-000d606f5dc6",
                "uuid:4320e050-b03b-11dd-9b4a-000d606f5dc6",
                "uuid:4a8cf730-af36-11dd-ae88-000d606f5dc6"
        };
        for (int i = 0; i < 2; i++) {
            AbstractPage page = pages.get(i);
            String pid = relsExtOrder[i];
            Assert.assertEquals(pid, page.getUuid());
        }   
    }

    // vytvori cely dokument.. 
    @Test
    public void testDocumentServiceFromNonPagePid() throws IOException, ParserConfigurationException, SAXException, LexerException, ProcessSubtreeException, SecurityException, NoSuchMethodException, OutOfRangeException {
        Injector injector = _DocumentServiceTestPrepare.prepareInjector("20", false);
        
        DocumentService docService = injector.getInstance(DocumentService.class);
        PreparedDocument doc = docService.buildDocumentAsFlat(PATHS_MAPPING.get(DataPrepare.DROBNUSTKY_PIDS[0]),DataPrepare.DROBNUSTKY_PIDS[0], 20, new int[]{300,300});
        List<AbstractPage> pages = doc.getPages();
        Assert.assertTrue(pages.size() == 16);

        String[] relsExtOrder = new String[] {

                "uuid:4308eb80-b03b-11dd-a0f6-000d606f5dc6",
                "uuid:4a79bd50-af36-11dd-a60c-000d606f5dc6",
                "uuid:430d7f60-b03b-11dd-82fa-000d606f5dc6",
                "uuid:4a7c2e50-af36-11dd-9643-000d606f5dc6",

                "uuid:43101770-b03b-11dd-8673-000d606f5dc6",
                "uuid:4a7ec660-af36-11dd-a782-000d606f5dc6",
                "uuid:4314ab50-b03b-11dd-89db-000d606f5dc6",
                "uuid:4a80c230-af36-11dd-ace4-000d606f5dc6",

                "uuid:43171c50-b03b-11dd-b0c2-000d606f5dc6",
                "uuid:4a835a40-af36-11dd-b951-000d606f5dc6",
                "uuid:4319b460-b03b-11dd-83ca-000d606f5dc6",
                "uuid:4a85f250-af36-11dd-8535-000d606f5dc6",
                "uuid:431e4840-b03b-11dd-8818-000d606f5dc6",

                "uuid:4a8a8630-af36-11dd-ae9c-000d606f5dc6",
                "uuid:4320e050-b03b-11dd-9b4a-000d606f5dc6",
                "uuid:4a8cf730-af36-11dd-ae88-000d606f5dc6"
        };
        for (int i = 0; i < relsExtOrder.length; i++) {
            AbstractPage page = pages.get(i);
            String pid = relsExtOrder[i];
            Assert.assertEquals(pid, page.getUuid());
        }   
    }
    
    // vytvori dokument od urciteho pidu
    @Test
    public void testDocumentServiceTreeFromPid() throws IOException, ParserConfigurationException, SAXException, LexerException, ProcessSubtreeException, SecurityException, NoSuchMethodException {
        Injector injector = _DocumentServiceTestPrepare.prepareInjector("20",false);
        
        DocumentService docService = injector.getInstance(DocumentService.class);
        ObjectPidsPath path = PATHS_MAPPING.get(DataPrepare.DROBNUSTKY_PIDS[0]);
        PreparedDocument doc = docService.buildDocumentAsTree(path, path.getLeaf(), new int[]{300,300});
        
        String model = doc.getModel();
        List<AbstractPage> pages = doc.getPages();
        for (AbstractPage page : pages) {
            System.out.println(page);
        }
        
        OutlineItem itemRoot = doc.getOutlineItemRoot();
        OutlineItem[] children = itemRoot.getChildren();
        for (OutlineItem itm : children) {
            System.out.println(itm);
        }

//        List<AbstractPage> pages = doc.getPages();
//        Assert.assertTrue(pages.size() == 3);
//        
//
//        String[] relsExtOrder = new String[] {
//                "uuid:4a7ec660-af36-11dd-a782-000d606f5dc6",
//                "uuid:4314ab50-b03b-11dd-89db-000d606f5dc6",
//                "uuid:4a80c230-af36-11dd-ace4-000d606f5dc6"};
//        for (int i = 0; i < relsExtOrder.length; i++) {
//            AbstractPage page = pages.get(i);
//            String pid = relsExtOrder[i];
//            Assert.assertEquals(pid, page.getUuid());
//        }   
    }

}


