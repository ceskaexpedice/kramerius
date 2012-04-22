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
package cz.incad.kramerius.pdf.commands.lists;

import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cz.incad.kramerius.pdf.commands.AbstractITextCommand;
import cz.incad.kramerius.pdf.commands.ITextCommand;
import cz.incad.kramerius.pdf.commands.ITextCommandProcessListener;
import cz.incad.kramerius.pdf.commands.ITextCommands;
import cz.incad.kramerius.pdf.commands.List;

public class GreekList extends List {

    @Override
    public void load(Element elm, ITextCommands cmnds) throws InstantiationException, IllegalAccessException {
        String name = elm.getNodeName();
        if (name.equals("greeklist")) {
            loadOrdering(elm);
            loadItems(elm, cmnds);
        }
    }
    
}
