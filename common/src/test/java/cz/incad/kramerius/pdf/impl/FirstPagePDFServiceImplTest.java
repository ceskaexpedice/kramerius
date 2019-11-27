/*
 * Copyright (C) 2012 Pavel Stastny
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
package cz.incad.kramerius.pdf.impl;

import static org.easymock.EasyMock.createMockBuilder;
import static org.easymock.EasyMock.replay;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.Set;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import junit.framework.Assert;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.easymock.EasyMock;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfWriter;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.document.DocumentService;
import cz.incad.kramerius.document.impl.DocumentServiceImpl;
import cz.incad.kramerius.document.model.PreparedDocument;
import cz.incad.kramerius.fedora.impl.DataPrepare;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.pdf.FirstPagePDFService;
import cz.incad.kramerius.pdf.GeneratePDFService;
import cz.incad.kramerius.pdf.OutOfRangeException;
import cz.incad.kramerius.pdf.commands.ITextCommands;
import cz.incad.kramerius.pdf.commands.render.RenderPDF;
import cz.incad.kramerius.pdf.impl.FirstPagePDFServiceImpl.DetailItem;
import cz.incad.kramerius.pdf.utils.pdf.FontMap;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.service.TextsService;
import cz.incad.kramerius.service.impl.TextsServiceImpl;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

public class FirstPagePDFServiceImplTest {

    public static String BUNLDE = "# procesy\n"
        + "key=value\n"
    + "pdf.digitallibrary=Digitální knihovna\n"
    + "pdf.pdfcontainstitle=Generované PDF obsahuje\n"


    + "fedora.model=Modely Fedora\n"
    + "fedora.model.page=Stránka\n"
    + "fedora.model.monograph=Monografie\n"
    + "fedora.model.periodical=Periodikum\n"
    + "fedora.model.monographunit=Volná část\n"
    + "fedora.model.periodicalvolume=Ročník\n"
    + "fedora.model.periodicalitem=Výtisk\n"
    + "fedora.model.internalpart=Interní součást\n"
    + "fedora.model.article=Článek\n"
    + "fedora.model.manuscript=Rukopis\n"
    + "fedora.model.map=Mapa\n"
    + "fedora.model.graphic=Grafika\n"
    + "fedora.model.sheetmusic=Hudebnina\n"

    + "#plurals\n"
    + "fedora.model.pages=Stránky\n"
    + "fedora.model.monographs=Monografie\n"
    + "fedora.model.periodicals=Periodika\n"
    + "fedora.model.monographunits=Volné části\n"
    + "fedora.model.periodicalvolumes=Ročníky\n"
    + "fedora.model.periodicalitems=Výtisky\n"
    + "fedora.model.internalparts=Interní součásti\n"
    + "fedora.model.articles=Články\n"
    + "fedora.model.manuscripts=Rukopisy\n"
    + "fedora.model.maps=Mapy\n"
    + "fedora.model.graphics=Grafiky\n"
    + "fedora.model.sheetmusics=Hudebniny\n"

    + "pdf.title=Titul\n"
    + "pdf.authors=Autori\n"
    + "pdf.subtitle=Podtitul\n"
    + "pdf.parttitle=Jmeno casti\n"
    + "pdf.publishers=Vydavatele\n"
    + "pdf.typeofresource=Typ zdroje\n"


    + "pdf.TitlePage=Tituln\u00ED strana\n"
    + "pdf.FrontCover=Predn\u00ED obal\n"
    + "pdf.BackCover=Zadn\u00ED obal\n"
    + "pdf.Advertisement=Reklama\n"
    + "pdf.FrontEndSheet=Predn\u00ED list\n"
    + "pdf.TableOfContents=Obsah\n"
    + "pdf.NormalPage=Strana\n"
    + "pdf.Blank=Pr\u00E1zdn\u00E1 strana\n"
    + "pdf.Index=Index\n"

    + "pdf.dc.issn=ISSN\n"
    + "pdf.dc.isbn=ISBN\n"

    + "pdf.dc.publisher=Vydavatel\n"
    + "pdf.dc.publishers=Vydavatelé\n"


    + "pdf.dc.creator=Autor\n"
    + "pdf.dc.creators=Autoři\n"
    + "pdf.dc.date=Rok vydání\n"

    + "pdf.fp.author=Autor\n"
    + "pdf.fp.authors=Autori\n"


    + "pdf.fp.publisher=Vydavatel\n"
    + "pdf.fp.publisherDate=Vydáváno v letech\n"
    + "pdf.fp.articletitle=Název článku\n"

    + "pdf.fp.title=Hlavní název\n"
    + "pdf.fp.titles=Hlavní názvy\n"

    + "pdf.fp.alternativetitle=Alternativní název\n"

    + "pdf.fp.subTitle=Podtitul\n"
    + "pdf.fp.alternativeSubtitle=Alternativní podtitul\n"

    + "pdf.fp.volumeNumber=Číslo ročníku\n"
    + "pdf.fp.volumeNumbers=Čísla ročníků\n"

    + "pdf.fp.periodicalVolumeDate=Rok ročníku\n"

    + "pdf.fp.issueNumber=Číslo výtisku\n"
    + "pdf.fp.issueNumbers=Čísla výtisků\n"

    + "pdf.fp.periodicalIssueDate=Datum vydání čísla\n"
    + "pdf.fp.model=Typ\n"
    + "pdf.fp.issn=Identifikátor ISSN\n"
    + "pdf.fp.issns=Identifikátory ISSN\n"

    + "pdf.fp.isbn=Identifikátor ISBN\n"
    + "pdf.fp.isbns=Identifikátory ISBN\n"

    + "pdf.fp.sici=Identifikátor SICI\n"
    + "pdf.fp.sicis=Identifikátory SICIs\n"

    + "pdf.fp.coden=Identifikátor SICI\n"
    + "pdf.fp.codens=Identifikátory SICIs\n"

    + "pdf.fp.pages=Stránky\n"
    + "pdf.fp.page=Stránka\n"

;


    @Test
    public void testGenerateParent_DROBNUSTKY() throws SecurityException, NoSuchMethodException, IOException, ParserConfigurationException, SAXException, LexerException, ProcessSubtreeException, DocumentException, XPathExpressionException, JAXBException, OutOfRangeException {
        StatisticsAccessLog acLog = EasyMock.createMock(StatisticsAccessLog.class);
        //hyph-country="CZ" hyph-lang="cs"
        Locale locale = new Locale("cs","CZ");

        FedoraAccessImpl fa33 = createMockBuilder(FedoraAccessImpl.class)
        .withConstructor(KConfiguration.getInstance(),acLog)
        .addMockedMethod("getFedoraDescribeStream").addMockedMethod("getRelsExt").addMockedMethod("isImageFULLAvailable").addMockedMethod("getDC").addMockedMethod("getBiblioMods")
                //.addMockedMethod(FedoraAccessImpl.class.getMethod("getKrameriusModelName", String.class))
                .createMock();

        EasyMock.expect(fa33.getFedoraDescribeStream()).andReturn(DataPrepare.fedoraProfile33());

        DataPrepare.drobnustkyRelsExt(fa33);
        DataPrepare.drobnustkyWithIMGFULL(fa33);
        DataPrepare.drobnustkyDCS(fa33);
        DataPrepare.drobnustkyMODS(fa33);


        ResourceBundleService bundleService = EasyMock.createMock(ResourceBundleService.class);
        EasyMock.expect(bundleService.getResourceBundle("labels", locale)).andReturn(new PropertyResourceBundle(new StringReader(BUNLDE))).anyTimes();
        EasyMock.expect(bundleService.getResourceBundle("base", locale)).andReturn(new PropertyResourceBundle(new StringReader(BUNLDE))).anyTimes();


        SolrAccess solrAccess = EasyMock.createMock(SolrAccess.class);
        Set<String> keys = DataPrepare.PATHS_MAPPING.keySet();
        for (String key : keys) {
            EasyMock.expect(solrAccess.getPath(key)).andReturn(new ObjectPidsPath[] { DataPrepare.PATHS_MAPPING.get(key) }).anyTimes();
        }

        replay(fa33, solrAccess, bundleService,acLog);

        Injector injector = Guice.createInjector(new _Module(locale, fa33, bundleService, solrAccess));

        FirstPagePDFService fpageService = injector.getInstance(FirstPagePDFService.class);

        DocumentService docService = injector.getInstance(DocumentService.class);

        // vytvoreny dokument
        PreparedDocument renderedDocument = docService.buildDocumentAsFlat(DataPrepare.PATHS_MAPPING.get(DataPrepare.DROBNUSTKY_PIDS[0]), DataPrepare.DROBNUSTKY_PIDS[0], 20, null);
        Assert.assertNotNull(renderedDocument.getPages().size() > 0);

        // vygenerovana xml pro itext
        String generatedTemplate = ((FirstPagePDFServiceImpl)fpageService).templateParent(renderedDocument, DataPrepare.PATHS_MAPPING.get( DataPrepare.DROBNUSTKY_PIDS[0]));
        
        Document renderedDoc = XMLUtils.parseDocument(new StringReader(generatedTemplate));

        InputStream expected = FirstPagePDFServiceImplTest.class.getResourceAsStream("drobnustky_parent_first_page.xml");
        String expectedString = IOUtils.readAsString(expected, Charset.forName("UTF-8"), true);
        Document expectedDoc = XMLUtils.parseDocument(new StringReader(expectedString));


        Document expectedWOws = XMLUnit.getWhitespaceStrippedDocument(expectedDoc);
        Document renderedWOws = XMLUnit.getWhitespaceStrippedDocument(renderedDoc);

        // vlastni generovani z xml do pdf uz testovat nelze
        Diff diff = XMLUnit.compareXML(expectedWOws, renderedWOws);
        Assert.assertTrue(diff.toString(),diff.similar());
    }

    @Test
    public void testGenerateParent_DROBNUSTKYPage() throws SecurityException, NoSuchMethodException, IOException, ParserConfigurationException, SAXException, LexerException, ProcessSubtreeException, DocumentException, XPathExpressionException, JAXBException, OutOfRangeException {
        StatisticsAccessLog acLog = EasyMock.createMock(StatisticsAccessLog.class);
        Locale locale = new Locale("cs","CZ");

        FedoraAccessImpl fa33 = createMockBuilder(FedoraAccessImpl.class)
        .withConstructor(KConfiguration.getInstance(), acLog)
        .addMockedMethod("getFedoraDescribeStream").addMockedMethod("getRelsExt").addMockedMethod("isImageFULLAvailable").addMockedMethod("getDC").addMockedMethod("getBiblioMods")
                .addMockedMethod(FedoraAccessImpl.class.getMethod("getKrameriusModelName", String.class))
                .createMock();

        EasyMock.expect(fa33.getFedoraDescribeStream()).andReturn(DataPrepare.fedoraProfile33());

        for (int i = 0; i < DataPrepare.DROBNUSTKY_PIDS.length; i++) {
            String pid = DataPrepare.DROBNUSTKY_PIDS[i];
            String model = DataPrepare.MODELS_MAPPING.get(pid);
            PIDParser parser = new PIDParser(model);
            parser.disseminationURI();
            String objectId = parser.getObjectId();

            EasyMock.expect(fa33.getKrameriusModelName(pid)).andReturn(objectId).anyTimes();
        }

        DataPrepare.drobnustkyRelsExt(fa33);
        DataPrepare.drobnustkyWithIMGFULL(fa33);
        DataPrepare.drobnustkyDCS(fa33);
        DataPrepare.drobnustkyMODS(fa33);


        ResourceBundleService bundleService = EasyMock.createMock(ResourceBundleService.class);
        EasyMock.expect(bundleService.getResourceBundle("labels", locale)).andReturn(new PropertyResourceBundle(new StringReader(BUNLDE))).anyTimes();
        EasyMock.expect(bundleService.getResourceBundle("base", locale)).andReturn(new PropertyResourceBundle(new StringReader(BUNLDE))).anyTimes();


        SolrAccess solrAccess = EasyMock.createMock(SolrAccess.class);
        Set<String> keys = DataPrepare.PATHS_MAPPING.keySet();
        for (String key : keys) {
            EasyMock.expect(solrAccess.getPath(key)).andReturn(new ObjectPidsPath[] { DataPrepare.PATHS_MAPPING.get(key) }).anyTimes();
        }

        replay(fa33, solrAccess, bundleService,acLog);

        Injector injector = Guice.createInjector(new _Module(locale, fa33, bundleService, solrAccess));

        FirstPagePDFService fpageService = injector.getInstance(FirstPagePDFService.class);

        DocumentService docService = injector.getInstance(DocumentService.class);

        // vytvoreny dokument
        PreparedDocument renderedDocument = docService.buildDocumentAsFlat(DataPrepare.PATHS_MAPPING.get(DataPrepare.DROBNUSTKY_PIDS[2]), DataPrepare.DROBNUSTKY_PIDS[2], 20, null);
        Assert.assertNotNull(renderedDocument.getPages().size() > 0);

        // vygenerovana xml pro itext
        String generatedTemplate = ((FirstPagePDFServiceImpl)fpageService).templateParent(renderedDocument, DataPrepare.PATHS_MAPPING.get(DataPrepare.DROBNUSTKY_PIDS[2]));

        Document renderedDoc = XMLUtils.parseDocument(new StringReader(generatedTemplate));

        InputStream expected = FirstPagePDFServiceImplTest.class.getResourceAsStream("drobnustky_pages_selection.xml");
        String expectedString = IOUtils.readAsString(expected, Charset.forName("UTF-8"), true);
        Document expectedDoc = XMLUtils.parseDocument(new StringReader(expectedString));


        Document expectedWOws = XMLUnit.getWhitespaceStrippedDocument(expectedDoc);
        Document renderedWOws = XMLUnit.getWhitespaceStrippedDocument(renderedDoc);

        // vlastni generovani z xml do pdf uz testovat nelze
        Diff diff = XMLUnit.compareXML(expectedWOws, renderedWOws);
        Assert.assertTrue(diff.toString(),diff.similar());
    }


    @Test
    public void testGenerateSelection_NarodniListy() throws SecurityException, NoSuchMethodException, IOException, ParserConfigurationException, SAXException, LexerException, ProcessSubtreeException, DocumentException, InstantiationException, IllegalAccessException, XPathExpressionException, JAXBException, OutOfRangeException {
        StatisticsAccessLog acLog = EasyMock.createMock(StatisticsAccessLog.class);
        Locale locale = new Locale("cs","CZ");

        FedoraAccessImpl fa33 = createMockBuilder(FedoraAccessImpl.class).withConstructor(KConfiguration.getInstance(),acLog)
        .addMockedMethod("getFedoraDescribeStream")
        .addMockedMethod("getRelsExt").addMockedMethod("isImageFULLAvailable")
        .addMockedMethod("getDC").addMockedMethod("getBiblioMods")
        .addMockedMethod(FedoraAccessImpl.class.getMethod("getKrameriusModelName", String.class))
        .createMock();

        EasyMock.expect(fa33.getFedoraDescribeStream()).andReturn(DataPrepare.fedoraProfile33());

        for (int i = 0; i < DataPrepare.NARODNI_LISTY.length; i++) {
            String pid = DataPrepare.NARODNI_LISTY[i];
            String model = DataPrepare.MODELS_MAPPING.get(pid);
            PIDParser parser = new PIDParser(model);
            parser.disseminationURI();
            String objectId = parser.getObjectId();

            EasyMock.expect(fa33.getKrameriusModelName(pid)).andReturn(objectId).anyTimes();
        }

        for (int i = 0; i < DataPrepare.DROBNUSTKY_PIDS.length; i++) {
            String pid = DataPrepare.DROBNUSTKY_PIDS[i];
            String model = DataPrepare.MODELS_MAPPING.get(pid);
            PIDParser parser = new PIDParser(model);
            parser.disseminationURI();
            String objectId = parser.getObjectId();

            EasyMock.expect(fa33.getKrameriusModelName(pid)).andReturn(objectId).anyTimes();
        }

        DataPrepare.narodniListyRelsExt(fa33);
        DataPrepare.narodniListyIMGFULL(fa33);
        DataPrepare.narodniListyDCs(fa33);
        DataPrepare.narodniListyMods(fa33);

        DataPrepare.drobnustkyRelsExt(fa33);
        DataPrepare.drobnustkyWithIMGFULL(fa33);
        DataPrepare.drobnustkyDCS(fa33);
        DataPrepare.drobnustkyMODS(fa33);


        ResourceBundleService bundleService = EasyMock.createMock(ResourceBundleService.class);
        EasyMock.expect(bundleService.getResourceBundle("labels", locale)).andReturn(new PropertyResourceBundle(new StringReader(BUNLDE))).anyTimes();
        EasyMock.expect(bundleService.getResourceBundle("base", locale)).andReturn(new PropertyResourceBundle(new StringReader(BUNLDE))).anyTimes();


        SolrAccess solrAccess = EasyMock.createMock(SolrAccess.class);
        Set<String> keys = DataPrepare.PATHS_MAPPING.keySet();
        for (String key : keys) {
            EasyMock.expect(solrAccess.getPath(key)).andReturn(new ObjectPidsPath[] { DataPrepare.PATHS_MAPPING.get(key) }).anyTimes();
        }



        replay(fa33, solrAccess, bundleService,acLog);

        Injector injector = Guice.createInjector(new _Module(locale, fa33, bundleService, solrAccess));

        FirstPagePDFService fpageService = injector.getInstance(FirstPagePDFService.class);

        DocumentService docService = injector.getInstance(DocumentService.class);



        String[] pids = {

                "uuid:b38eba10-91f6-11dc-9eec-000d606f5dc6",
                "uuid:94a3ed60-92d6-11dc-beb4-000d606f5dc6",
                "uuid:b3987e10-91f6-11dc-96f6-000d606f5dc6",
                "uuid:94a3ed60-92d6-11dc-93df-000d606f5dc6",
                "uuid:b3a21b00-91f6-11dc-b8b2-000d606f5dc6",
                "uuid:94a68570-92d6-11dc-be5a-000d606f5dc6",
        };

        // vytvoreny dokument
        PreparedDocument renderedDocument =
            docService.buildDocumentFromSelection(pids, null);
            //docService.buildDocumentAsFlat(DataPrepare.PATHS_MAPPING.get(pid), pid, 20, null);
        Assert.assertNotNull(renderedDocument.getPages().size() > 0);

        // vygenerovana xml pro itext
        String generatedTemplate = ((FirstPagePDFServiceImpl)fpageService).templateSelection(renderedDocument,pids);
        Document renderedDoc = XMLUtils.parseDocument(new StringReader(generatedTemplate));


        InputStream expected = FirstPagePDFServiceImplTest.class.getResourceAsStream("narodni_listy_selection_pages.xml");
        String docString = IOUtils.readAsString(expected, Charset.forName("UTF-8"), true);
        Document expectedDoc = XMLUtils.parseDocument(new StringReader(docString));


        Document expectedWOws = XMLUnit.getWhitespaceStrippedDocument(expectedDoc);
        Document renderedWOws = XMLUnit.getWhitespaceStrippedDocument(renderedDoc);

        // vlastni generovani z xml do pdf uz testovat nelze
        Diff diff = XMLUnit.compareXML(expectedWOws, renderedWOws);
        Assert.assertTrue(diff.toString(),diff.similar());
    }

    @Test
    public void testGenerateParent_NarodniListy() throws SecurityException, NoSuchMethodException, IOException, ParserConfigurationException, SAXException, LexerException, ProcessSubtreeException, DocumentException, InstantiationException, IllegalAccessException, XPathExpressionException, JAXBException, OutOfRangeException {
        StatisticsAccessLog acLog = EasyMock.createMock(StatisticsAccessLog.class);
        Locale locale = new Locale("cs","CZ");

        FedoraAccessImpl fa33 = createMockBuilder(FedoraAccessImpl.class).withConstructor(KConfiguration.getInstance(),acLog)
        .addMockedMethod("getFedoraDescribeStream")
        .addMockedMethod("getRelsExt").addMockedMethod("isImageFULLAvailable")
        .addMockedMethod("getDC").addMockedMethod("getBiblioMods")
        .addMockedMethod(FedoraAccessImpl.class.getMethod("getKrameriusModelName", String.class))
        .createMock();

        EasyMock.expect(fa33.getFedoraDescribeStream()).andReturn(DataPrepare.fedoraProfile33());

        for (int i = 0; i < DataPrepare.NARODNI_LISTY.length; i++) {
            String pid = DataPrepare.NARODNI_LISTY[i];
            String model = DataPrepare.MODELS_MAPPING.get(pid);
            PIDParser parser = new PIDParser(model);
            parser.disseminationURI();
            String objectId = parser.getObjectId();

            EasyMock.expect(fa33.getKrameriusModelName(pid)).andReturn(objectId).anyTimes();
        }

        for (int i = 0; i < DataPrepare.DROBNUSTKY_PIDS.length; i++) {
            String pid = DataPrepare.DROBNUSTKY_PIDS[i];
            String model = DataPrepare.MODELS_MAPPING.get(pid);
            PIDParser parser = new PIDParser(model);
            parser.disseminationURI();
            String objectId = parser.getObjectId();

            EasyMock.expect(fa33.getKrameriusModelName(pid)).andReturn(objectId).anyTimes();
        }

        DataPrepare.narodniListyRelsExt(fa33);
        DataPrepare.narodniListyIMGFULL(fa33);
        DataPrepare.narodniListyDCs(fa33);
        DataPrepare.narodniListyMods(fa33);

        DataPrepare.drobnustkyRelsExt(fa33);
        DataPrepare.drobnustkyWithIMGFULL(fa33);
        DataPrepare.drobnustkyDCS(fa33);
        DataPrepare.drobnustkyMODS(fa33);


        ResourceBundleService bundleService = EasyMock.createMock(ResourceBundleService.class);
        EasyMock.expect(bundleService.getResourceBundle("labels", locale)).andReturn(new PropertyResourceBundle(new StringReader(BUNLDE))).anyTimes();
        EasyMock.expect(bundleService.getResourceBundle("base", locale)).andReturn(new PropertyResourceBundle(new StringReader(BUNLDE))).anyTimes();



        SolrAccess solrAccess = EasyMock.createMock(SolrAccess.class);
        Set<String> keys = DataPrepare.PATHS_MAPPING.keySet();
        for (String key : keys) {
            EasyMock.expect(solrAccess.getPath(key)).andReturn(new ObjectPidsPath[] { DataPrepare.PATHS_MAPPING.get(key) }).anyTimes();
        }



        replay(fa33, solrAccess, bundleService,acLog);
        Injector injector = Guice.createInjector(new _Module(locale, fa33, bundleService, solrAccess));
        FirstPagePDFService fpageService = injector.getInstance(FirstPagePDFService.class);
        DocumentService docService = injector.getInstance(DocumentService.class);


        String pid = "uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6";

        // vytvoreny dokument
        PreparedDocument renderedDocument =
            docService.buildDocumentAsFlat(DataPrepare.PATHS_MAPPING.get(pid), pid, 20, null);
        Assert.assertNotNull(renderedDocument.getPages().size() > 0);

        // vygenerovana xml pro itext
        String generatedTemplate = ((FirstPagePDFServiceImpl)fpageService).templateParent(renderedDocument,DataPrepare.PATHS_MAPPING.get(pid));
        Document renderedDoc = XMLUtils.parseDocument(new StringReader(generatedTemplate));


        InputStream expected = FirstPagePDFServiceImplTest.class.getResourceAsStream("narodni_listy_parent.xml");
        Document expectedDoc = XMLUtils.parseDocument(new InputStreamReader(expected, "UTF-8" ));


        Document expectedWOws = XMLUnit.getWhitespaceStrippedDocument(expectedDoc);
        Document renderedWOws = XMLUnit.getWhitespaceStrippedDocument(renderedDoc);

        // vlastni generovani z xml do pdf uz testovat nelze
        Diff diff = XMLUnit.compareXML(expectedWOws, renderedWOws);
        Assert.assertTrue(diff.toString(),diff.similar());
    }


    public void toTmpPDF(Document renderedDoc, GeneratePDFService pdfService) throws InstantiationException, IllegalAccessException, IOException, FileNotFoundException, DocumentException {
        ITextCommands cmnds = new ITextCommands();
        cmnds.load(renderedDoc.getDocumentElement(), cmnds);

        File tmpFile = File.createTempFile("prefix", "postfix");
        System.out.println(tmpFile);
        FileOutputStream fos = new FileOutputStream(tmpFile);

        com.lowagie.text.Document pdfDoc = new com.lowagie.text.Document();

        PdfWriter writer = PdfWriter.getInstance(pdfDoc, fos);
        pdfDoc.open();

        FedoraAccess fa = new FedoraAccessImpl(KConfiguration.getInstance(), null);
        RenderPDF render = new RenderPDF(new FontMap(pdfService.fontsFolder()), fa);
        render.render(pdfDoc,writer, cmnds);

        pdfDoc.close();
    }


    @Test
    public void testGenerateSelection_NarodniListyDrobnustky() throws SecurityException, NoSuchMethodException, IOException, ParserConfigurationException, SAXException, LexerException, ProcessSubtreeException, DocumentException, InstantiationException, IllegalAccessException, XPathExpressionException, JAXBException, OutOfRangeException {
        StatisticsAccessLog acLog = EasyMock.createMock(StatisticsAccessLog.class);
        Locale locale = new Locale("cs","CZ");

        FedoraAccessImpl fa33 = createMockBuilder(FedoraAccessImpl.class).withConstructor(KConfiguration.getInstance(),acLog)
        .addMockedMethod("getFedoraDescribeStream")
        .addMockedMethod("getRelsExt").addMockedMethod("isImageFULLAvailable")
        .addMockedMethod("getDC").addMockedMethod("getBiblioMods")
        .addMockedMethod(FedoraAccessImpl.class.getMethod("getKrameriusModelName", String.class))
        .createMock();

        EasyMock.expect(fa33.getFedoraDescribeStream()).andReturn(DataPrepare.fedoraProfile33());

        for (int i = 0; i < DataPrepare.NARODNI_LISTY.length; i++) {
            String pid = DataPrepare.NARODNI_LISTY[i];
            String model = DataPrepare.MODELS_MAPPING.get(pid);
            PIDParser parser = new PIDParser(model);
            parser.disseminationURI();
            String objectId = parser.getObjectId();

            EasyMock.expect(fa33.getKrameriusModelName(pid)).andReturn(objectId).anyTimes();
        }


        for (int i = 0; i < DataPrepare.DROBNUSTKY_PIDS.length; i++) {
            String pid = DataPrepare.DROBNUSTKY_PIDS[i];
            String model = DataPrepare.MODELS_MAPPING.get(pid);
            PIDParser parser = new PIDParser(model);
            parser.disseminationURI();
            String objectId = parser.getObjectId();

            EasyMock.expect(fa33.getKrameriusModelName(pid)).andReturn(objectId).anyTimes();
        }

        DataPrepare.narodniListyRelsExt(fa33);
        DataPrepare.narodniListyIMGFULL(fa33);
        DataPrepare.narodniListyDCs(fa33);
        DataPrepare.narodniListyMods(fa33);

        DataPrepare.drobnustkyRelsExt(fa33);
        DataPrepare.drobnustkyWithIMGFULL(fa33);
        DataPrepare.drobnustkyDCS(fa33);
        DataPrepare.drobnustkyMODS(fa33);


        ResourceBundleService bundleService = EasyMock.createMock(ResourceBundleService.class);
        EasyMock.expect(bundleService.getResourceBundle("labels", locale)).andReturn(new PropertyResourceBundle(new StringReader(BUNLDE))).anyTimes();
        EasyMock.expect(bundleService.getResourceBundle("base", locale)).andReturn(new PropertyResourceBundle(new StringReader(BUNLDE))).anyTimes();


        SolrAccess solrAccess = EasyMock.createMock(SolrAccess.class);
        Set<String> keys = DataPrepare.PATHS_MAPPING.keySet();
        for (String key : keys) {
            EasyMock.expect(solrAccess.getPath(key)).andReturn(new ObjectPidsPath[] { DataPrepare.PATHS_MAPPING.get(key) }).anyTimes();
        }



        replay(fa33, solrAccess, bundleService,acLog);

        Injector injector = Guice.createInjector(new _Module(locale, fa33, bundleService, solrAccess));

        FirstPagePDFService fpageService = injector.getInstance(FirstPagePDFService.class);

        DocumentService docService = injector.getInstance(DocumentService.class);



        String[] pids = {

                "uuid:b38eba10-91f6-11dc-9eec-000d606f5dc6",
                "uuid:94a3ed60-92d6-11dc-beb4-000d606f5dc6",
                "uuid:b3987e10-91f6-11dc-96f6-000d606f5dc6",
                "uuid:94a3ed60-92d6-11dc-93df-000d606f5dc6",
                "uuid:b3a21b00-91f6-11dc-b8b2-000d606f5dc6",
                "uuid:94a68570-92d6-11dc-be5a-000d606f5dc6",


                "uuid:4a7c2e50-af36-11dd-9643-000d606f5dc6",
                "uuid:4a7ec660-af36-11dd-a782-000d606f5dc6"
        };

        // vytvoreny dokument
        PreparedDocument renderedDocument =
            docService.buildDocumentFromSelection(pids, null);
            //docService.buildDocumentAsFlat(DataPrepare.PATHS_MAPPING.get(pid), pid, 20, null);
        Assert.assertNotNull(renderedDocument.getPages().size() > 0);

        // vygenerovana xml pro itext
        String generatedTemplate = ((FirstPagePDFServiceImpl)fpageService).templateSelection(renderedDocument,pids);
        Document renderedDoc = XMLUtils.parseDocument(new StringReader(generatedTemplate));


        InputStream expected = FirstPagePDFServiceImplTest.class.getResourceAsStream("narodni_listy_drobnustky_selection_pages.xml");
        Document expectedDoc = XMLUtils.parseDocument(new InputStreamReader(expected, "UTF-8"));


        Document expectedWOws = XMLUnit.getWhitespaceStrippedDocument(expectedDoc);
        Document renderedWOws = XMLUnit.getWhitespaceStrippedDocument(renderedDoc);

        // vlastni generovani z xml do pdf uz testovat nelze
        Diff diff = XMLUnit.compareXML(expectedWOws, renderedWOws);
        Assert.assertTrue(diff.toString(),diff.similar());
    }

    
    
    @Test
    public void testEscapingInPrepareViewObject() throws SecurityException, NoSuchMethodException, IOException, ParserConfigurationException, SAXException, LexerException, ProcessSubtreeException, DocumentException, InstantiationException, IllegalAccessException, XPathExpressionException, JAXBException, OutOfRangeException {
        DetailItem item = new FirstPagePDFServiceImpl.DetailItem("Hlavni nazev", "Svět ledu & ohně: oficiální dějiny Západozemí a Hry o trůny");
        Assert.assertEquals("Svět ledu &amp; ohně: oficiální dějiny Západozemí a Hry o trůny", item.getValue());
        FirstPagePDFServiceImpl.FirstPageViewObject viewObject = new FirstPagePDFServiceImpl.FirstPageViewObject();

        viewObject.setConditionUsage("<& >");
        Assert.assertEquals("&lt;&amp; &gt;", viewObject.getConditionUsage());
        
        viewObject.setDitigalLibrary("\" <& >");
        Assert.assertEquals("&quot; &lt;&amp; &gt;", viewObject.getDitigalLibrary());
    }



    class _Module extends AbstractModule {

        private Locale locale;
        private FedoraAccess fedoraAccess;
        private ResourceBundleService resourceBundleService;
        private SolrAccess solrAccess;


        public _Module(Locale locale, FedoraAccess fedoraAccess, ResourceBundleService resourceBundleService,SolrAccess solrAccess) {
            super();
            this.locale = locale;
            this.fedoraAccess = fedoraAccess;
            this.resourceBundleService = resourceBundleService;
            this.solrAccess = solrAccess;
        }

        @Override
        protected void configure() {
            bind(FedoraAccess.class).annotatedWith(Names.named("securedFedoraAccess")).toInstance(this.fedoraAccess);
            bind(SolrAccess.class).toInstance(this.solrAccess);
            bind(ResourceBundleService.class).toInstance(this.resourceBundleService);
            bind(TextsService.class).to(TextsServiceImpl.class);


            bind(DocumentService.class).to(DocumentServiceImpl.class);
            bind(FirstPagePDFService.class).to(FirstPagePDFServiceImpl.class);
        }

        @Provides
        public Locale getLocale() {
            return this.locale;
        }
    }
}
