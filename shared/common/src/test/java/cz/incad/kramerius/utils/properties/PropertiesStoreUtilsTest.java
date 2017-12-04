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
package cz.incad.kramerius.utils.properties;

import java.util.Properties;

import junit.framework.Assert;

import org.junit.Test;

public class PropertiesStoreUtilsTest {

    @Test
    public void shouldStoreProperties() {
        Properties props = new Properties();
        props.put("inputFolder", "/home/karel4/one");
        props.put("outputFolder", "/home/karel4/two");
        props.put("name", "- PROCESS NAME - ");
        
        String line = PropertiesStoreUtils.storeProperties(props);
        Assert.assertEquals("inputFolder=/home/karel4/one;name=- PROCESS NAME - ;outputFolder=/home/karel4/two", line);
    }
    
    @Test
    public void shouldLoadProperties() {
        Properties props = PropertiesStoreUtils.loadProperties("inputFolder=/home/karel4/one;name=- PROCESS NAME - ;outputFolder=/home/karel4/two");
        Assert.assertTrue(props.size() == 3);
        Assert.assertEquals(props.getProperty("inputFolder"), "/home/karel4/one");
        Assert.assertEquals(props.getProperty("outputFolder"), "/home/karel4/two");
        Assert.assertEquals(props.getProperty("name"), "- PROCESS NAME - ");
    }

    @Test
    public void shouldStorePropertiesWithEsacpeChar() {
        Properties props = new Properties();
        props.put("inputFolder", "/home/karel4;/one");
        props.put("outputFolder", "/home/karel4;/two");
        props.put("name", ";- PROCESS NAME - ;");
        
        String line = PropertiesStoreUtils.storeProperties(props);
        Assert.assertEquals("inputFolder=/home/karel4\\;/one;name=\\;- PROCESS NAME - \\;;outputFolder=/home/karel4\\;/two", line);
    }

    @Test
    public void shouldLoadPropertiesWithEsacpeChar() {
        String line = "inputFolder=/home/karel4\\;/one;name=\\;- PROCESS NAME - \\;;outputFolder=/home/karel4\\;/two";
        Properties props = PropertiesStoreUtils.loadProperties(line);
        Assert.assertTrue(props.size() == 3);
        Assert.assertEquals(props.getProperty("inputFolder"), "/home/karel4;/one");
        Assert.assertEquals(props.getProperty("outputFolder"), "/home/karel4;/two");
        Assert.assertEquals(props.getProperty("name"), ";- PROCESS NAME - ;");
    }
}
