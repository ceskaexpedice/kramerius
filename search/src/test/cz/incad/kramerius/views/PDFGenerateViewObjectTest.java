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
package cz.incad.kramerius.views;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Test;
import org.xml.sax.SAXException;

import antlr.RecognitionException;
import antlr.TokenStreamException;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.name.Names;

import cz.incad.Kramerius.views.AbstractPrintViewObject.RadioItem;
import cz.incad.Kramerius.views.PdfGenerateViewObject;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.service.TextsService;
import cz.incad.kramerius.utils.XMLUtils;

public class PDFGenerateViewObjectTest  {

    public static Map<String, Boolean> IMG_FULL_MAPPINGS = new HashMap<String, Boolean>(); static {
        IMG_FULL_MAPPINGS.put("uuid:00dbc770-8138-11e0-b63f-000d606f5dc6", true);
        IMG_FULL_MAPPINGS.put("uuid:045b1250-7e47-11e0-add1-000d606f5dc6", false);
        IMG_FULL_MAPPINGS.put("uuid:17b01dc0-96f7-11de-9176-000d606f5dc6", true);
        IMG_FULL_MAPPINGS.put("uuid:8f526130-8b0d-11de-8994-000d606f5dc6", false);
        IMG_FULL_MAPPINGS.put("uuid:91214030-80bb-11e0-b482-000d606f5dc6", false);
        IMG_FULL_MAPPINGS.put("uuid:ab7e5a19-bddb-11e0-bff9-0016e6840575", false);
        IMG_FULL_MAPPINGS.put("uuid:f7e50720-80b6-11e0-9ec7-000d606f5dc6", false);
    }
    

    public static Map<String, ObjectPidsPath> PATHS_MAPPING = new HashMap<String, ObjectPidsPath>(); static {
        // monograph -> page
        PATHS_MAPPING.put("uuid:00dbc770-8138-11e0-b63f-000d606f5dc6", new ObjectPidsPath("uuid:8f526130-8b0d-11de-8994-000d606f5dc6","uuid:00dbc770-8138-11e0-b63f-000d606f5dc6"));
        // monograph
        PATHS_MAPPING.put("uuid:8f526130-8b0d-11de-8994-000d606f5dc6", new ObjectPidsPath("uuid:8f526130-8b0d-11de-8994-000d606f5dc6"));
        // periodikum
        PATHS_MAPPING.put("uuid:045b1250-7e47-11e0-add1-000d606f5dc6", new ObjectPidsPath("uuid:045b1250-7e47-11e0-add1-000d606f5dc6"));

        // rocnik
        PATHS_MAPPING.put("uuid:f7e50720-80b6-11e0-9ec7-000d606f5dc6", new ObjectPidsPath("uuid:045b1250-7e47-11e0-add1-000d606f5dc6","uuid:f7e50720-80b6-11e0-9ec7-000d606f5dc6"));

        // vytisk
        PATHS_MAPPING.put("uuid:91214030-80bb-11e0-b482-000d606f5dc6", new ObjectPidsPath("uuid:045b1250-7e47-11e0-add1-000d606f5dc6","uuid:f7e50720-80b6-11e0-9ec7-000d606f5dc6", "uuid:91214030-80bb-11e0-b482-000d606f5dc6"));

        // stranka
        PATHS_MAPPING.put("uuid:17b01dc0-96f7-11de-9176-000d606f5dc6", new ObjectPidsPath("uuid:045b1250-7e47-11e0-add1-000d606f5dc6","uuid:f7e50720-80b6-11e0-9ec7-000d606f5dc6", "uuid:91214030-80bb-11e0-b482-000d606f5dc6","uuid:17b01dc0-96f7-11de-9176-000d606f5dc6"));

        // clanek 
        PATHS_MAPPING.put("uuid:ab7e5a19-bddb-11e0-bff9-0016e6840575", new ObjectPidsPath("uuid:045b1250-7e47-11e0-add1-000d606f5dc6","uuid:f7e50720-80b6-11e0-9ec7-000d606f5dc6", "uuid:91214030-80bb-11e0-b482-000d606f5dc6","uuid:ab7e5a19-bddb-11e0-bff9-0016e6840575"));
        
    }
    
    private static final String TEXT = 
        "<doc>"+
            "<head> Podmínky využití</head>"+
            "<desc> "+
                " Knihovna poskytuje přístup k digitalizovaným dokumentům pouze pro nekomerční, vědecké, studijní"+
                " účely a pouze pro osobní potřeby uživatelů. Část dokumentů digitální knihovny podléhá autorským"+
                " právům. Využitím digitální knihovny  a vygenerováním kopie části digitalizovaného"+
                " dokumentu se uživatel zavazuje dodržovat tyto podmínky využití, které musí být součástí každé"+
                " zhotovené kopie. Jakékoli další kopírování materiálu z digitální knihovny není možné bez"+
                " případného písemného svolení knihovny."+
            "</desc> "+
        "</doc>";


    @Test
    public void testTitles() throws IOException, RecognitionException, TokenStreamException, ParserConfigurationException, SAXException {
        Injector injector = prepare(new String[] {
                "uuid:8f526130-8b0d-11de-8994-000d606f5dc6",
                "uuid:045b1250-7e47-11e0-add1-000d606f5dc6",
                "uuid:ab7e5a19-bddb-11e0-bff9-0016e6840575"
        });

        PdfGenerateViewObject view = new PdfGenerateViewObject();
        injector.injectMembers(view);
        view.init();

        Assert.assertEquals(" Podmínky využití", view.getHeader());
        List<RadioItem> items = view.getItems();
        Assert.assertTrue(items.size() == 3);

        RadioItem monograph = items.get(0);
        Assert.assertTrue(monograph.isMaster());
        Assert.assertEquals("Monografie:Verspätete Erwägungen", monograph.getName());
        Assert.assertTrue(monograph.getPids().size() == 1);
        
        RadioItem periodical = items.get(1);
        Assert.assertTrue(periodical.isMaster());
        Assert.assertEquals("Periodikum:Dějiny a současnost", periodical.getName());
        Assert.assertTrue(periodical.getPids().size() == 1);

        RadioItem intpart = items.get(2);
        Assert.assertTrue(intpart.isMaster());
        Assert.assertEquals("Interní součást:„Lidé si budou vypravovat o jejich moudrosti\" | Výtisk:3 | Ročník:33 | Periodikum:Dějiny a současnost", intpart.getName());
        Assert.assertTrue(intpart.getPids().size() == 1);
    }
    
    
    

    @Test
    public void testTwoPageTitles() throws IOException, RecognitionException, TokenStreamException, ParserConfigurationException, SAXException {
        Injector injector = prepare(new String[] {
                "uuid:17b01dc0-96f7-11de-9176-000d606f5dc6",
                "uuid:00dbc770-8138-11e0-b63f-000d606f5dc6"
        });

        PdfGenerateViewObject view = new PdfGenerateViewObject();
        injector.injectMembers(view);
        view.init();

        List<RadioItem> items = view.getItems();
        Assert.assertTrue(items.size() == 1);
        RadioItem item = items.get(0);
        Assert.assertTrue(item.getPids().size() == 2);
        Assert.assertEquals("Stránky: 4 - 6", item.getName());
        
        String[] details = item.getDetailedItemNames();
        Assert.assertTrue(details.length == 2);
        Assert.assertEquals("Stránka:4 | Výtisk:3 | Ročník:33 | Periodikum:Dějiny a současnost", details[0].trim());
        Assert.assertEquals("Stránka:6 | Monografie:Verspätete Erwägungen", details[1]);
        
    }

    public Injector prepare(String[] processedPids) throws IOException, ParserConfigurationException, SAXException {

        Locale locale = Locale.getDefault();
        
        TextsService textsService = EasyMock.createMock(TextsService.class);
        EasyMock.expect(textsService.getText("first_page_nolines_xml", locale)).andReturn( TEXT);

        
        FedoraAccess fedoraAccess = EasyMock.createMock(FedoraAccess.class);
        List<String> processedPidsAsList = Arrays.asList(processedPids);
        for (String pid : processedPids) {
            EasyMock.expect(fedoraAccess.isImageFULLAvailable(pid)).andReturn(IMG_FULL_MAPPINGS.get(pid)).anyTimes();

            InputStream is = PDFGenerateViewObjectTest.class.getResourceAsStream("dc."+pid.replace(":", "")+".xml");
            EasyMock.expect(fedoraAccess.getDC(pid)).andReturn(XMLUtils.parseDocument(is,true)).anyTimes();

            // dc pro cestu 
            String[] path = PATHS_MAPPING.get(pid).getPathFromLeafToRoot();
            for (String subPid : path) {
                if (!processedPidsAsList.contains(subPid)) {

                    InputStream subIs = PDFGenerateViewObjectTest.class.getResourceAsStream("dc."+subPid.replace(":", "")+".xml");
                    EasyMock.expect(fedoraAccess.getDC(subPid)).andReturn(XMLUtils.parseDocument(subIs,true)).anyTimes();

                    EasyMock.expect(fedoraAccess.isImageFULLAvailable(subPid)).andReturn(IMG_FULL_MAPPINGS.get(subPid)).anyTimes();
                }
            }
        }
        
        
        ResourceBundleService bundleService = EasyMock.createMock(ResourceBundleService.class);
        InputStream resStream = this.getClass().getClassLoader().getResourceAsStream("labels_cs.properties");
        EasyMock.expect(bundleService.getResourceBundle("labels", locale)).andReturn(new PropertyResourceBundle(new InputStreamReader(resStream, Charset.forName("UTF-8")))).anyTimes();
        
        SolrAccess solrAccess = EasyMock.createMock(SolrAccess.class);
        for (String pid : processedPids) {
            EasyMock.expect(solrAccess.getPath(pid)).andReturn(new ObjectPidsPath[] { PATHS_MAPPING.get(pid) }).anyTimes();
        }
        
        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < processedPids.length; i++) {
            if (i>0) builder.append(";");
            String escapedPid = processedPids[i].replaceAll(":", "\\\\:");
            builder.append(escapedPid);
        }
        EasyMock.expect(request.getParameter("pids")).andReturn("{"+builder.toString()+"}").anyTimes();

        
        EasyMock.replay(textsService, fedoraAccess, bundleService, solrAccess,request);

        
        Injector injector = Guice.createInjector(
                new _Module(locale, textsService, fedoraAccess, bundleService, solrAccess, request)
        );
        return injector;
    }

    
    public static class _Module extends AbstractModule {
        
        private Locale locale;
        private TextsService textsService;
        private FedoraAccess fa;
        private ResourceBundleService bundleService;
        private SolrAccess solrAccess;
        private HttpServletRequest request;
        
        
        public _Module(Locale locale, TextsService textsService, FedoraAccess fa, ResourceBundleService bundleService, SolrAccess solrAccess, HttpServletRequest req) {
            super();
            this.locale = locale;
            this.textsService = textsService;
            this.fa = fa;
            this.bundleService = bundleService;
            this.solrAccess = solrAccess;
            this.request = req;
        }


        @Override
        protected void configure() {
            bind(Locale.class).toInstance(this.locale);
            bind(TextsService.class).toInstance(this.textsService);
            bind(FedoraAccess.class).annotatedWith(Names.named("securedFedoraAccess")).toInstance(this.fa);
            bind(SolrAccess.class).toInstance(solrAccess);
            bind(ResourceBundleService.class).toInstance(this.bundleService);
        }
        
        @Provides
        protected HttpServletRequest getHttpServletRequest() {
            return this.request;
        }
    }
}
