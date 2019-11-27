package cz.incad.kramerius.document;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.google.inject.Injector;

import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.document.model.AbstractPage;
import cz.incad.kramerius.document.model.PreparedDocument;
import cz.incad.kramerius.fedora.impl.DataPrepare;
import cz.incad.kramerius.pdf.OutOfRangeException;
import cz.incad.kramerius.utils.pid.LexerException;

public class DocumentServiceTest2 {

    // Cely dokumennt - turnOffPdfCheck = true 
    @Test
    public void testDocumentService_turnOffPdfCheckTrue() throws IOException, ParserConfigurationException, SAXException, LexerException, ProcessSubtreeException, SecurityException, NoSuchMethodException, OutOfRangeException {
        Injector injector = _DocumentServiceTestPrepare.prepareInjector("2", true);
        
        DocumentService docService = injector.getInstance(DocumentService.class);
        PreparedDocument doc = docService.buildDocumentAsFlat(DocumentServiceTest.PATHS_MAPPING.get(DataPrepare.DROBNUSTKY_PIDS[0]),DataPrepare.DROBNUSTKY_PIDS[0], 20, new int[]{300,300});
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
        for (int i = 0; i < 2; i++) {
            AbstractPage page = pages.get(i);
            String pid = relsExtOrder[i];
            Assert.assertEquals(pid, page.getUuid());
        }   
    }

    // Cely dokumennt - turnOffPdfCheck = false 
    @Test
    public void testDocumentService_turnOffPdfCheckFalse() throws IOException, ParserConfigurationException, SAXException, LexerException, ProcessSubtreeException, SecurityException, NoSuchMethodException, OutOfRangeException {
        Injector injector = _DocumentServiceTestPrepare.prepareInjector("2", false);
        
        DocumentService docService = injector.getInstance(DocumentService.class);
        try {
            PreparedDocument doc = docService.buildDocumentAsFlat(DocumentServiceTest.PATHS_MAPPING.get(DataPrepare.DROBNUSTKY_PIDS[0]),DataPrepare.DROBNUSTKY_PIDS[0], 20, new int[]{300,300});
            Assert.fail();
        } catch (OutOfRangeException e) {
            // ok
        }
    }

}
