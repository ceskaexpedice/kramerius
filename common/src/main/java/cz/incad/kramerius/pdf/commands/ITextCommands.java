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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cz.incad.kramerius.utils.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cz.incad.kramerius.pdf.commands.lists.GreekList;
import cz.incad.kramerius.pdf.commands.lists.RomanList;

/**
 * IText commands 
 * @author pavels
 */
public class ITextCommands extends AbstractITextCommand implements ITextCommand {
    
    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ITextCommands.class.getName());

    private java.util.List<ITextCommand> loadedCommands = new ArrayList<>();

    private String footer;
    private String header;

    private static Map<String, Class> CLZ_MAPPING = new HashMap<>(); {
        CLZ_MAPPING.put("item",ListItem.class);
        CLZ_MAPPING.put("list",List.class);
        CLZ_MAPPING.put("break", Line.class);
        CLZ_MAPPING.put("line", Line.class);
        CLZ_MAPPING.put("pagebreak", PageBreak.class);

        CLZ_MAPPING.put("paragraph", Paragraph.class);
        CLZ_MAPPING.put("image", Image.class);
        CLZ_MAPPING.put("text", Text.class);
        CLZ_MAPPING.put("textsarray", TextsArray.class);

        CLZ_MAPPING.put("greeklist", GreekList.class);
        CLZ_MAPPING.put("romanlist", RomanList.class);
    };
    
    public ITextCommand createInstance(String nodeName) throws InstantiationException, IllegalAccessException {
        Class clz = CLZ_MAPPING.get(nodeName);
        if (clz != null) {
            return (ITextCommand) clz.newInstance();
        } else return null;
    }

    @Override
    public void load(Element elm, ITextCommands cmnds) throws InstantiationException, IllegalAccessException {
        String pageFooter = elm.getAttribute("page-footer");
        if (StringUtils.isAnyString(pageFooter)) {
            this.footer = pageFooter;
        }
        String pageHeader = elm.getAttribute("page-header");
        if (StringUtils.isAnyString(pageHeader)) {
            this.header = pageHeader;
        }
        if (elm.getNodeName().equals("commands")) {
            NodeList nNodes = elm.getChildNodes();
            for (int i = 0,ll=nNodes.getLength(); i < ll; i++) {
                Node node = nNodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    ITextCommand cmd = createInstance(node.getNodeName());
                    if (cmd != null) {
                        cmd.load((Element) node, cmnds);
                        cmd.setParent(this);
                        this.loadedCommands.add(cmd);
                    } else LOGGER.warning("uknown component :"+node.getNodeName());
                }
            }
        }
    }
    
    public java.util.List<ITextCommand> getCommands() {
        return this.loadedCommands;
    }

    @Override
    public void process(ITextCommandProcessListener procsListener) {
        procsListener.before(this);
        for (ITextCommand cmd : this.loadedCommands) {
            cmd.process(procsListener);
        }
        procsListener.after(this);
    }


    public void setFooter(String footer) {
        this.footer = footer;
    }

    public String getFooter() {
        return footer;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }
}
