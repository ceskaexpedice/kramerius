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
/**
 * 
 */
package org.kramerius.processes.utils;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kramerius.processes.filetree.FileTreeTest;
import org.kramerius.processes.filetree.TreeItem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.XMLUtils;

/**
 * @author pavels
 *
 */
public class BasicStringTemplateGroupTest {

    public static String TEST_GROUP ="group testgroup; alert() ::=<<alert();>>";

    @Before
    public void setup() {
        FileTreeTest.createStructure();
    }
    
    
    @After
    public void teardown() throws IOException {
        FileUtils.deleteDirectory(FileTreeTest.testDir());
    }

    @Test
    public void testHTMLRender() throws IOException, ParserConfigurationException, SAXException {
        TreeItem model = TreeModelUtils.prepareTreeModel(FileTreeTest.testDir(), null);

        StringTemplateGroup parentGroup = BasicStringTemplateGroup.getBasicProcessesGroup();
        
        StringTemplateGroup subGroup = new StringTemplateGroup(new StringReader(TEST_GROUP));
        subGroup.setSuperGroup(parentGroup);
        
        StringTemplate template = subGroup.getInstanceOf("tree");
        template.setAttribute("root", model);
        
        Document docs = XMLUtils.parseDocument(new StringReader(template.toString()));
        List<Element> liElms = new ArrayList<Element>();
        NodeList nodelist = docs.getDocumentElement().getChildNodes();
        for (int i = 0; i < nodelist.getLength(); i++) {
            boolean lielm = (nodelist.item(i).getNodeType() == Node.ELEMENT_NODE) && (nodelist.item(i).getNodeName().equals("li"));
            if (lielm) liElms.add((Element) nodelist.item(i));
        }
        
        Assert.assertTrue(liElms.size() == FileTreeTest.H_NAMES.length);
    }


}
