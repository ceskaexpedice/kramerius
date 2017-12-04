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

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ListItem extends AbstractITextCommand implements ITextCommand{

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ListItem.class.getName());
    
    
    private java.util.List<ITextCommand> cmdns = new ArrayList<ITextCommand>();


    private int spacingAfter = -1;
    private int spacingBefore = -1;


    private String listSymbol = null;
    

    @Override
    public void load(Element elm, ITextCommands cmnds) throws InstantiationException, IllegalAccessException {
        String name = elm.getNodeName();
        if (name.equals("item")) {
           
            if (notEmptyAttribute(elm,"spacing-after")) {
                this.spacingAfter = Integer.parseInt(elm.getAttribute("spacing-after"));
            }
            if (notEmptyAttribute(elm,"spacing-before")) {
                this.spacingBefore = Integer.parseInt(elm.getAttribute("spacing-before"));
            }
            
            if (elm.hasAttribute("list-symbol")) {
                this.listSymbol = elm.getAttribute("list-symbol");
            }
            NodeList nList = elm.getChildNodes();
            for (int i = 0, ll = nList.getLength(); i < ll; i++) {
                Node node = nList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    String nName = node.getNodeName();
                    ITextCommand itm = cmnds.createInstance(nName);
                    if (itm instanceof TextsArray) {
                        itm.load((Element) node, cmnds);
                        this.setParent(this);
                        this.cmdns.add(itm);
                    } else if (itm instanceof List) {
                        itm.load((Element) node, cmnds);
                        this.setParent(this);
                        this.cmdns.add(itm);
                    } else if (itm instanceof Paragraph) {
                        itm.load((Element) node, cmnds);
                        this.setParent(this);
                        this.cmdns.add(itm);
                    } else {
                        LOGGER.warning(" only texts accepting but "+itm );
                    }
                }
            }
        }
    }

    
    public boolean isSpacingAfterDefined() {
        return this.spacingAfter != -1;
    }
    
    public boolean isSpacingBeforeDefined() {
        return this.spacingBefore != -1;
    }

    
    public int getSpacingAfter() {
        return spacingAfter;
    }
    
    public int getSpacingBefore() {
        return spacingBefore;
    }

    public String getListSymbol() {
        return listSymbol;
    }

    @Override
    public void process(ITextCommandProcessListener procsListener) {
        procsListener.before(this);
        for (ITextCommand cmd : this.cmdns) {
            cmd.process(procsListener);
        }
        procsListener.after(this);
    }
}
