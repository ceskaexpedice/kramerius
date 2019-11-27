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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import cz.incad.kramerius.pdf.Break;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.XMLUtils;

public class CommandsTest {
    
    @Test
    public void testCommands() throws ParserConfigurationException, SAXException, IOException, InstantiationException, IllegalAccessException {
        InputStream stream = CommandsTest.class.getResourceAsStream("commands.xml");
        Document document = XMLUtils.parseDocument(stream);
        
        ITextCommands cmnds = new ITextCommands();
        cmnds.load(document.getDocumentElement(), cmnds);
        
        List<ITextCommand> commands = cmnds.getCommands();
        Assert.assertTrue(commands.size() == 5);

        Assert.assertEquals(commands.get(0).getClass(), Paragraph.class);
        Assert.assertEquals(commands.get(1).getClass(), Line.class);
        Assert.assertEquals(commands.get(2).getClass(), cz.incad.kramerius.pdf.commands.List.class);
        Assert.assertEquals(commands.get(3).getClass(), Image.class);
        Assert.assertEquals(commands.get(4).getClass(), Image.class);
    }
    
    
    @Test
    public void testCommandsListener() throws ParserConfigurationException, SAXException, IOException, InstantiationException, IllegalAccessException {
        InputStream stream = CommandsTest.class.getResourceAsStream("commands.xml");
        Document document = XMLUtils.parseDocument(stream);

        ITextCommands cmnds = new ITextCommands();
        cmnds.load(document.getDocumentElement(), cmnds);
        List<String> clzNames = classNames(cmnds);
        List<String> expected = new ArrayList<String>();{
            expected.add(Paragraph.class.getName());
            expected.add(Line.class.getName());
            expected.add(cz.incad.kramerius.pdf.commands.List.class.getName());
            expected.add(Image.class.getName());
            expected.add(Image.class.getName());
        }
        Assert.assertEquals(expected, clzNames);

        
        final List<String> processesed = new ArrayList<String>();
        ITextCommandProcessListener listener = new ITextCommandProcessListener() {
            
            @Override
            public void before(ITextCommand iTextCommand) {
                processesed.add(iTextCommand.getClass().getName());
            }

            @Override
            public void after(ITextCommand iTextCommand) {
            }
        };
        cmnds.process(listener);

        // top - down
        expected = new ArrayList<String>();{
            expected.add(ITextCommands.class.getName());
            expected.add(Paragraph.class.getName());
            expected.add(TextsArray.class.getName());
            expected.add(Text.class.getName());
            expected.add(Text.class.getName());
            expected.add(Text.class.getName());

            expected.add(Line.class.getName());

            expected.add(cz.incad.kramerius.pdf.commands.List.class.getName());
            expected.add(ListItem.class.getName());
            expected.add(TextsArray.class.getName());
            expected.add(Text.class.getName());

            expected.add(ListItem.class.getName());
            expected.add(TextsArray.class.getName());
            expected.add(Text.class.getName());

            expected.add(Image.class.getName());
            expected.add(Image.class.getName());
        }

        Assert.assertEquals(expected, processesed);

        processesed.clear();
        listener = new ITextCommandProcessListener() {
            
            @Override
            public void before(ITextCommand iTextCommand) {
            }

            @Override
            public void after(ITextCommand iTextCommand) {
                processesed.add(iTextCommand.getClass().getName());
            }
        };
        cmnds.process(listener);

        // bottom - up
        expected = new ArrayList<String>();{
            expected.add(Text.class.getName());
            expected.add(Text.class.getName());
            expected.add(Text.class.getName());
            expected.add(TextsArray.class.getName());
            expected.add(Paragraph.class.getName());

            expected.add(Line.class.getName());

            expected.add(Text.class.getName());
            expected.add(TextsArray.class.getName());
            expected.add(ListItem.class.getName());

            expected.add(Text.class.getName());
            expected.add(TextsArray.class.getName());
            expected.add(ListItem.class.getName());

            expected.add(cz.incad.kramerius.pdf.commands.List.class.getName());

            expected.add(Image.class.getName());
            expected.add(Image.class.getName());

            expected.add(ITextCommands.class.getName());
        }
        Assert.assertEquals(expected, processesed);

    }


    public List<String> classNames(ITextCommands cmnds) {
        List<String> list = new ArrayList<String>();
        for (ITextCommand cmd : cmnds.getCommands()) {
            list.add(cmd.getClass().getName());
        }
        return list;
    }
}
