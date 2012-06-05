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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.PropertyResourceBundle;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Test;

import sun.text.normalizer.Replaceable;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.processes.annotations.DefaultParameterValue;
import cz.incad.kramerius.processes.annotations.ParameterName;
import cz.incad.kramerius.processes.annotations.Process;
import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.IOUtils;

public class DefaultTemplateTest {

    public static String BUNLDE = 
        "# procesy\n"
        +"first=prvni\n"
        +"second=druhy\n"
        +"third=treti\n";

    @Test
    public void shouldRenderTemplate() throws IOException {
        Locale locale = new Locale("cs","CZ");

        LRProcessDefinition definition = EasyMock.createMock(LRProcessDefinition.class);
        
        EasyMock.expect(definition.getId()).andReturn("_4test_").anyTimes();
        EasyMock.expect(definition.getMainClass()).andReturn(FourTestProcess.class.getName()).anyTimes();
        
        
        
        DefaultTemplate template = new DefaultTemplate();
        

        ResourceBundleService bundleService = EasyMock.createMock(ResourceBundleService.class);
        EasyMock.expect(bundleService.getResourceBundle("labels", locale)).andReturn(new PropertyResourceBundle(new InputStreamReader(new ByteArrayInputStream(BUNLDE.getBytes()), Charset.forName("UTF-8")))).anyTimes();
        EasyMock.expect(bundleService.getResourceBundle("base", locale)).andReturn(new PropertyResourceBundle(new InputStreamReader(new ByteArrayInputStream(BUNLDE.getBytes()), Charset.forName("UTF-8")))).anyTimes();

        EasyMock.replay(bundleService, definition);
        
        Injector injector = Guice.createInjector(new _Module(locale,  bundleService));
        injector.injectMembers(template);
        
        StringWriter stringWriter = new StringWriter();
        template.executeGet(definition, stringWriter);
        
        InputStream resStream = DefaultTemplateTest.class.getResourceAsStream("expecting.txt");
        String expected = IOUtils.readAsString(resStream, Charset.forName("UTF-8"), true);
        
        Assert.assertEquals(expected, stringWriter.toString());
    }
    
    
    
    public static class FourTestProcess {

        @DefaultParameterValue("first")
        public static String DEFAULT_FIRST="default";
        
        @Process
        public static void process(@ParameterName("first")String first, @ParameterName("second") String second) {}
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
