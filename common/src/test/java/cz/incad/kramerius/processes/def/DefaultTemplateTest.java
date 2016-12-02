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
package cz.incad.kramerius.processes.def;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;
import java.util.PropertyResourceBundle;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.easymock.EasyMock;
import org.junit.Test;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.gwt.dom.client.Node;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;

import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.processes.annotations.DefaultParameterValue;
import cz.incad.kramerius.processes.annotations.ParameterName;
import cz.incad.kramerius.processes.annotations.Process;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.WhitespaceUtility;
import cz.incad.kramerius.utils.XMLUtils;

public class DefaultTemplateTest {

    public static String BUNLDE = "# procesy\n" + "processes.defaultfields.first=prvni\n"
            + "processes.defaultfields.second=druhy\n" + "processes.defaultfields.third=treti\n";

    @Test
    public void shouldRenderTemplate() throws IOException, ParserConfigurationException, SAXException {
        Locale locale = new Locale("cs", "CZ");

        LRProcessDefinition definition = EasyMock.createMock(LRProcessDefinition.class);

        EasyMock.expect(definition.getId()).andReturn("_4test_").anyTimes();
        EasyMock.expect(definition.getMainClass()).andReturn(FourTestProcess.class.getName()).anyTimes();

        DefaultTemplate template = new DefaultTemplate();

        ResourceBundleService bundleService = EasyMock.createMock(ResourceBundleService.class);
        EasyMock.expect(bundleService.getResourceBundle("labels", locale))
                .andReturn(new PropertyResourceBundle(
                        new InputStreamReader(new ByteArrayInputStream(BUNLDE.getBytes()), Charset.forName("UTF-8"))))
                .anyTimes();
        EasyMock.expect(bundleService.getResourceBundle("base", locale))
                .andReturn(new PropertyResourceBundle(
                        new InputStreamReader(new ByteArrayInputStream(BUNLDE.getBytes()), Charset.forName("UTF-8"))))
                .anyTimes();

        EasyMock.replay(bundleService, definition);

        Injector injector = Guice.createInjector(new _Module(locale, bundleService));
        injector.injectMembers(template);

        StringWriter stringWriter = new StringWriter();
        template.renderInput(definition, stringWriter, null);
        Document parsedDocument = XMLUtils.parseDocument(new StringReader(stringWriter.toString()));
        Assert.assertTrue(parsedDocument.getDocumentElement().getNodeName().equals("div"));

        InputStream resStream = DefaultTemplateTest.class.getResourceAsStream("expecting.txt");
        Document expected = XMLUtils.parseDocument(resStream);
        // different formatting in comments
        List<org.w3c.dom.Node> findNodesByType = XMLUtils.findNodesByType(expected.getDocumentElement(),
                org.w3c.dom.Node.COMMENT_NODE);
        StringBuilder expetedComments = new StringBuilder();
        for (org.w3c.dom.Node node : findNodesByType) {
            Comment comm  = (Comment) node;
            org.w3c.dom.Node parentNode = node.getParentNode();
            parentNode.removeChild(node);
            expetedComments.append(comm.getData());
        }
        
        StringBuilder parsedComments = new StringBuilder();
        findNodesByType = XMLUtils.findNodesByType(parsedDocument.getDocumentElement(), org.w3c.dom.Node.COMMENT_NODE);
        for (org.w3c.dom.Node node : findNodesByType) {
            Comment comm  = (Comment) node;
            org.w3c.dom.Node parentNode = node.getParentNode();
            parentNode.removeChild(node);
            parsedComments.append(comm.getData());
        }
        Assert.assertEquals(WhitespaceUtility.replace(expetedComments.toString()), WhitespaceUtility.replace(parsedComments.toString()));
        Diff diff = XMLUnit.compareXML(parsedDocument, expected);
        Assert.assertTrue(diff.toString(), diff.similar());

    }

    public static class FourTestProcess {

        @DefaultParameterValue("first")
        public static String DEFAULT_FIRST = "default";

        @Process
        public static void process(@ParameterName("first") String first, @ParameterName("second") String second) {
        }
    }

    class _Module extends AbstractModule {

        private Locale locale;
        private ResourceBundleService resourceBundleService;

        public _Module(Locale locale, ResourceBundleService resourceBundleService) {
            super();
            this.locale = locale;
            this.resourceBundleService = resourceBundleService;
        }

        @Override
        protected void configure() {
            bind(ResourceBundleService.class).toInstance(this.resourceBundleService);
        }

        @Provides
        public Locale getLocale() {
            return this.locale;
        }
    }

}
