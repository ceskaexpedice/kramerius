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
package org.kramerius.replications.outputs;

import static org.easymock.EasyMock.createMockBuilder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Properties;
import java.util.PropertyResourceBundle;

import junit.framework.Assert;

import net.sf.json.JSONObject;

import org.easymock.EasyMock;
import org.junit.Test;
import org.kramerius.k3replications.input.InputTemplateTest._TestLocaleProvider;
import org.kramerius.replications.outputs.OutputTemplate;

import com.google.inject.Provider;

import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.processes.BatchStates;
import cz.incad.kramerius.processes.LRProcess;
import cz.incad.kramerius.processes.States;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

/**
 * @author pavels
 *
 */
public class OutputTemplateTest {
    
    public static final String BUNDLES ="" +
    "convert.directory=convert.directory\n" +
    "target.directory=target.directory\n" +
    "convert.selection.dialog=convert.selection.dialog";
    public interface _TestLocaleProvider extends Provider<Locale> {}

    @Test
    public void shouldRenderTemplate() throws FileNotFoundException, IOException {
        OutputTemplate oTemplate = EasyMock.createMockBuilder(OutputTemplate.class)
            .addMockedMethod("description")
            .createMock();
        
        JSONObject jsonObject = description1();
        LRProcess lrPRocess = lrProcess();
        
        EasyMock.expect(oTemplate.description(lrPRocess)).andReturn(jsonObject).anyTimes();

        ResourceBundleService resb = EasyMock.createMock(ResourceBundleService.class);
        PropertyResourceBundle resourceBundle = new PropertyResourceBundle(new StringReader(BUNDLES));
        EasyMock.expect(resb.getResourceBundle("labels", Locale.getDefault())).andReturn(resourceBundle).anyTimes();

        Provider<Locale> localeProvider = EasyMock.createMock(_TestLocaleProvider.class);
        EasyMock.expect(localeProvider.get()).andReturn(Locale.getDefault()).anyTimes();

        
        EasyMock.replay(lrPRocess, oTemplate, resb,localeProvider);
        
        Assert.assertNotNull(oTemplate.description(lrPRocess));
        
        oTemplate.resourceBundleService = resb;
        oTemplate.localesProvider = localeProvider;
        
        StringWriter writer = new StringWriter();
        oTemplate.renderOutput(lrPRocess, null, writer);
        String rendered = writer.toString();
        Assert.assertNotNull(rendered);
    }

    @Test
    public void shouldRenderTemplate2() throws FileNotFoundException, IOException {
        OutputTemplate oTemplate = EasyMock.createMockBuilder(OutputTemplate.class)
            .addMockedMethod("description")
            .createMock();
        
        JSONObject jsonObject = description2();
        LRProcess lrPRocess = lrProcess();
        
        EasyMock.expect(oTemplate.description(lrPRocess)).andReturn(jsonObject).anyTimes();

        ResourceBundleService resb = EasyMock.createMock(ResourceBundleService.class);
        PropertyResourceBundle resourceBundle = new PropertyResourceBundle(new StringReader(BUNDLES));
        EasyMock.expect(resb.getResourceBundle("labels", Locale.getDefault())).andReturn(resourceBundle).anyTimes();

        Provider<Locale> localeProvider = EasyMock.createMock(_TestLocaleProvider.class);
        EasyMock.expect(localeProvider.get()).andReturn(Locale.getDefault()).anyTimes();

        
        EasyMock.replay(lrPRocess, oTemplate, resb,localeProvider);
        
        Assert.assertNotNull(oTemplate.description(lrPRocess));
        
        oTemplate.resourceBundleService = resb;
        oTemplate.localesProvider = localeProvider;
        
        StringWriter writer = new StringWriter();
        oTemplate.renderOutput(lrPRocess, null, writer);
        String rendered = writer.toString();
        Assert.assertNotNull(rendered);
    }

    @Test
    public void testReplace() {
        String str = "Akafist' Svatěj Velikomučenice Varvare";
        OutputTemplate template = new OutputTemplate();
        String escaped = template.escapedJavascriptString(str);
        Assert.assertEquals("Akafist\\' Svatěj Velikomučenice Varvare", escaped);
    }

    @Test
    public void testReplace2() {
        String str = "Kniha zlatá, anebo, Nowý Zwěstowatel wsseho \n dobrého a vžitečného pro Národ Slowenský";
        OutputTemplate template = new OutputTemplate();
        String escaped = template.escapedJavascriptString(str);
        Assert.assertEquals("Kniha zlatá, anebo, Nowý Zwěstowatel wsseho \\n dobrého a vžitečného pro Národ Slowenský", escaped);
    }

    public LRProcess lrProcess() throws FileNotFoundException {
        LRProcess lrPRocess = EasyMock.createMock(LRProcess.class);
        Properties props = new Properties();
        props.put("url", "http://,.....");
        EasyMock.expect(lrPRocess.getParametersMapping()).andReturn(props).anyTimes();
        EasyMock.expect(lrPRocess.processWorkingDirectory()).andReturn(new File(System.getProperty("user.dir"))).anyTimes();
        EasyMock.expect(lrPRocess.getProcessState()).andReturn(States.FAILED).anyTimes();
        EasyMock.expect(lrPRocess.getBatchState()).andReturn(BatchStates.BATCH_FAILED).anyTimes();
        
        EasyMock.expect(lrPRocess.getErrorProcessOutputStream()).andReturn(new ByteArrayInputStream("someerror".getBytes())).anyTimes();
        EasyMock.expect(lrPRocess.getUUID()).andReturn("xxxxx").anyTimes();
        
        return lrPRocess;
    }

    public JSONObject description1() throws IOException {
        InputStream is = OutputTemplate.class.getClassLoader().getResourceAsStream("org/kramerius/replications/description.txt");
        String stringInput = IOUtils.readAsString(is, Charset.forName("UTF-8"), true);

        Assert.assertNotNull(is);
        JSONObject jsonObject = JSONObject.fromObject(stringInput);
        System.out.println(jsonObject);
        Assert.assertNotNull(jsonObject);
        return jsonObject;
    }

    public JSONObject description2() throws IOException {
        InputStream is = OutputTemplate.class.getClassLoader().getResourceAsStream("org/kramerius/replications/description2.txt");
        String stringInput = IOUtils.readAsString(is, Charset.forName("UTF-8"), true);

        Assert.assertNotNull(is);
        JSONObject jsonObject = JSONObject.fromObject(stringInput);
        System.out.println(jsonObject);
        Assert.assertNotNull(jsonObject);
        return jsonObject;
    }
}
