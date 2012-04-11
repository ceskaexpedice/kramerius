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

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ListItem implements Command{

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ListItem.class.getName());
    
    private TextsArray textsArray;


    @Override
    public void load(Element elm, Commands cmnds) throws InstantiationException, IllegalAccessException {
        String name = elm.getNodeName();
        if (name.equals("li")) {
            NodeList nList = elm.getChildNodes();
            for (int i = 0, ll = nList.getLength(); i < ll; i++) {
                Node node = nList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    String nName = node.getNodeName();
                    Command itm = cmnds.createInstance(nName);
                    if (itm instanceof TextsArray) {
                        itm.load((Element) node, cmnds);
                        this.textsArray = (TextsArray) itm;
                    } else {
                        LOGGER.warning(" only texts accepting");
                    }
                }
            }
        }
    }

    @Override
    public Object acceptVisitor(CommandVisitor visitor, Object obj) {
        obj = visitor.visit(this, obj);
        if (this.textsArray != null) obj = this.textsArray.acceptVisitor(visitor, obj);
        return obj;
    }
}
