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

public class List extends AbstractITextCommand implements ITextCommand {

    protected java.util.List<ITextCommand> items = new ArrayList<ITextCommand>();
    
    protected String  listType; // ordered, unordered
    protected String orderingType; //alphabetical, numerical
    
    protected Boolean autoIndent = null;
    protected int symbolIndent = -1;
    
    public ITextCommand[] getItems() {
        return (ITextCommand[]) this.items.toArray(new ITextCommand[this.items.size()]);
    }
    
    @Override
    public void load(Element elm, ITextCommands cmnds) throws InstantiationException, IllegalAccessException {
        String name = elm.getNodeName();
        if (name.equals("list")) {
            if (notEmptyAttribute(elm, "auto-indent")) {
                this.autoIndent = Boolean.parseBoolean(elm.getAttribute("auto-indent"));
            }
            
            if (notEmptyAttribute(elm, "symbol-indent")) {
                this.symbolIndent = Integer.parseInt(elm.getAttribute("symbol-indent"));
            }
            
            loadOrdering(elm);
            loadItems(elm, cmnds);
        }
    }

    protected void loadItems(Element elm, ITextCommands cmnds) throws InstantiationException, IllegalAccessException {
        NodeList nList = elm.getChildNodes();
        for (int i = 0, ll = nList.getLength(); i < ll; i++) {
            Node node = nList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                String nName = node.getNodeName();
                ITextCommand itm = cmnds.createInstance(nName);
                itm.setParent(this);
                itm.load((Element) node, cmnds);
                this.items.add(itm);
            }
        }
    }

    protected void loadOrdering(Element elm) {
        if (super.notEmptyAttribute(elm, "ordering-type")) {
            this.orderingType = elm.getAttribute("ordering-type");
        }
        if (super.notEmptyAttribute(elm, "list-type")) {
            this.listType = elm.getAttribute("list-type");
        }
    }

    public String getListType() {
        return listType;
    }
    
    public String getOrderingType() {
        return orderingType;
    }
    
    @Override
    public void process(ITextCommandProcessListener procsListener) {
        procsListener.before(this);
        for (ITextCommand cmd : this.items) {
            cmd.process(procsListener);
        }
        procsListener.after(this);
    }

    public Boolean getAutoIndent() {
        return autoIndent;
    }
    
    public int getSymbolIndent() {
        return symbolIndent;
    }
}
