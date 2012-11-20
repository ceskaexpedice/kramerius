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
package cz.incad.kramerius.processes.impl;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class LRPRocessDefinitionTest {


    @Test
    public void shouldHaveSecuredAction() {
        LRProcessDefinitionManagerImpl impl = new LRProcessDefinitionManagerImpl(KConfiguration.getInstance(), null, null );
        LRProcessDefinition definition = impl.getLongRunningProcessDefinition("wmock");
        Assert.assertNotNull(definition);
        Assert.assertNotNull(definition.getSecuredAction());
        Assert.assertEquals(definition.getSecuredAction(),"mock");
    }
    
    @Test
    public void shouldLoadMockDefinition() {
        LRProcessDefinitionManagerImpl impl = new LRProcessDefinitionManagerImpl(KConfiguration.getInstance(), null, null );
        LRProcessDefinition definition = impl.getLongRunningProcessDefinition("mock");
        Assert.assertNotNull(definition);
        List<String> jvmParams = definition.getJavaProcessParameters();
        Assert.assertTrue(jvmParams.size() == 2);
        Assert.assertEquals("-Xmx1024m", jvmParams.get(0));
        Assert.assertEquals("-Xms256m", jvmParams.get(1));
        
        List<String> params = definition.getParameters();
        Assert.assertTrue(params.size() == 2);
        Assert.assertEquals("one",params.get(0));
        Assert.assertEquals("two",params.get(1));
    }
    
    @Test
    public void shouldTEmplatedWMock() {
        LRProcessDefinitionManagerImpl impl = new LRProcessDefinitionManagerImpl(KConfiguration.getInstance(), null, null );
        LRProcessDefinition definition = impl.getLongRunningProcessDefinition("wmock");
        Assert.assertNotNull(definition);
        List<String> jvmParams = definition.getJavaProcessParameters();

        Assert.assertTrue(jvmParams.size() == 2);
        Assert.assertEquals("-Xmx1024m", jvmParams.get(0));
        Assert.assertEquals("-Xms256m", jvmParams.get(1));

        List<String> params = definition.getParameters();
        Assert.assertTrue(params.size() == 2);
        Assert.assertEquals("/home/pavels/tmp",params.get(0));
        Assert.assertEquals("two",params.get(1));
    }
}
