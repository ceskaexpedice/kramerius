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
package org.kramerius.replications.output;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.kramerius.replications.outputs.OutputTemplate;

import cz.incad.kramerius.processes.LRProcess;

public class OutputTemplateTest {

    @Test
    public void testTemplate() throws IOException {
//        Properties props = new Properties();
//        props.put("url", "http://krameriusdemo.mzk.cz/search/handle/uuid:b79dd230-d2fb-11dc-b6af-000d606f5dc6");
//        
//        LRProcess process = EasyMock.createMock(LRProcess.class);
//        
//        EasyMock.expect(process.getParametersMapping()).andReturn(props).anyTimes();
//        EasyMock.expect(process.processWorkingDirectory()).andReturn(new File(System.getProperty("user.dir"))).anyTimes();
//
//        EasyMock.replay(process);
//        
//        
//        StringWriter stWriter = new StringWriter();
//        OutputTemplate outTemplate = new OutputTemplate();
//
//        // testuje jenom jestli se rendrovani podarilo
//        outTemplate.renderOutput(process, null, stWriter);
//        Assert.assertNotNull(stWriter.toString());
//        Assert.assertTrue(!stWriter.toString().trim().equals(""));
//        System.out.println(stWriter);
    }
}
