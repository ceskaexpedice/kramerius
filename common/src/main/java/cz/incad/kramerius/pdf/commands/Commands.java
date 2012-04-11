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

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Commands implements Command {
    
    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(Commands.class.getName());

    private java.util.List<Command> loadedCommans = new ArrayList<Command>();
    
    private static Map<String, Class> CLZ_MAPPING = new HashMap<String, Class>(); {
        CLZ_MAPPING.put("li",ListItem.class);
        CLZ_MAPPING.put("ul",List.class);
        CLZ_MAPPING.put("ol",List.class);
        CLZ_MAPPING.put("break", Line.class);
        CLZ_MAPPING.put("hr", Line.class);
        CLZ_MAPPING.put("paragraph", Paragraph.class);
        CLZ_MAPPING.put("image", Image.class);
        CLZ_MAPPING.put("text", Text.class);
        CLZ_MAPPING.put("textsarray", TextsArray.class);
    };
    
    public Command createInstance(String nodeName) throws InstantiationException, IllegalAccessException {
        Class clz = CLZ_MAPPING.get(nodeName);
        if (clz != null) {
            return (Command) clz.newInstance();
        } else return null;
    }

    @Override
    public void load(Element elm, Commands cmnds) throws InstantiationException, IllegalAccessException {
        if (elm.getNodeName().equals("commands")) {
            NodeList nNodes = elm.getChildNodes();
            for (int i = 0,ll=nNodes.getLength(); i < ll; i++) {
                Node node = nNodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Command cmd = createInstance(node.getNodeName());
                    if (cmd != null) {
                        cmd.load((Element) node, cmnds);
                        this.loadedCommans.add(cmd);
                    } else LOGGER.warning("uknown component :"+node.getNodeName());
                }
            }
        }
    }

    @Override
    public Object acceptVisitor(CommandVisitor visitor, Object obj) {
        obj =  visitor.visit(this, obj);
        for (Command cmd : this.loadedCommans) {
            obj = cmd.acceptVisitor(visitor, obj);
        }
        return obj;
    }
    
    public java.util.List<Command> getCommands() {
        return this.loadedCommans;
    }
}
