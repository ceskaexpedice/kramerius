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
package cz.incad.kramerius.pdf.commands;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import cz.incad.kramerius.pdf.Break;
import cz.incad.kramerius.utils.XMLUtils;

public class CommandsTest {
    
    
    @Test
    public void testCommands() throws ParserConfigurationException, SAXException, IOException, InstantiationException, IllegalAccessException {
        InputStream stream = this.getClass().getResourceAsStream("commands.xml");
        Document document = XMLUtils.parseDocument(stream);
        
        Commands cmnds = new Commands();
        cmnds.load(document.getDocumentElement(), cmnds);
        
        List<Command> commands = cmnds.getCommands();
        Assert.assertTrue(commands.size() == 4);

        Assert.assertEquals(commands.get(0).getClass(), Paragraph.class);
        Assert.assertEquals(commands.get(1).getClass(), Line.class);
        Assert.assertEquals(commands.get(2).getClass(), cz.incad.kramerius.pdf.commands.List.class);
        Assert.assertEquals(commands.get(3).getClass(), Image.class);
    }
    
    
    @Test
    public void testCommandsVisitor() throws ParserConfigurationException, SAXException, IOException, InstantiationException, IllegalAccessException {
        InputStream stream = this.getClass().getResourceAsStream("commands.xml");
        Document document = XMLUtils.parseDocument(stream);

        Commands cmnds = new Commands();
        cmnds.load(document.getDocumentElement(), cmnds);
        List<Command> list = cmnds.getCommands();
        
        
        CommandVisitor visitor = new CommandVisitor() {
            
            @Override
            public Object visit(Image image, Object obj) {
                List<String> visited = (List<String>) obj;
                visited.add(image.getClass().getName());
                return visited;
            }
            
            @Override
            public Object visit(Commands cmnds, Object obj) {
                List<String> visited = (List<String>) obj;
                visited.add(cmnds.getClass().getName());
                return visited;
            }
            
            @Override
            public Object visit(Paragraph text, Object obj) {
                List<String> visited = (List<String>) obj;
                visited.add(text.getClass().getName());
                return visited;
            }
            
            @Override
            public Object visit(ListItem listItem, Object obj) {
                List<String> visited = (List<String>) obj;
                visited.add(listItem.getClass().getName());
                return visited;
            }
            
            @Override
            public Object visit(cz.incad.kramerius.pdf.commands.List list, Object obj) {
                List<String> visited = (List<String>) obj;
                visited.add(list.getClass().getName());
                return visited;
            }
            
            @Override
            public Object visit(Line line, Object obj) {
                List<String> visited = (List<String>) obj;
                visited.add(line.getClass().getName());
                return visited;
            }

            @Override
            public Object visit(Text text, Object obj) {
                List<String> visited = (List<String>) obj;
                visited.add(text.getClass().getName());
                return visited;
            }
             
        };
        

        
        List<String> visited = (List<String>) cmnds.acceptVisitor(visitor, new ArrayList<String>());
        List<String> expected = new ArrayList<String>(); {
            expected.add(Commands.class.getName());
            expected.add(Paragraph.class.getName());
            expected.add(Text.class.getName());
            expected.add(Text.class.getName());
            expected.add(Text.class.getName());
            expected.add(Line.class.getName());
            expected.add(cz.incad.kramerius.pdf.commands.List.class.getName());
            expected.add(ListItem.class.getName());
            expected.add(Text.class.getName());
            expected.add(ListItem.class.getName());
            expected.add(Text.class.getName());
            expected.add(Image.class.getName());
        }
        
        Assert.assertEquals(expected, visited);
        
    }
}
