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
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class TextsArray extends AbstractITextCommand {
    
    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(TextsArray.class.getName());
    
    private List<Text> texts = new ArrayList<Text>();
    
    @Override
    public void load(Element elm, ITextCommands cmnds) throws InstantiationException, IllegalAccessException {

        this.hyphenation = this.hyphenationFromAttibutes(elm);

        NodeList nList = elm.getChildNodes();
        for (int i = 0, ll = nList.getLength(); i < ll; i++) {
            Node node = nList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                String nName = node.getNodeName();
                ITextCommand itm = cmnds.createInstance(nName);
                if (itm instanceof Text) {
                    itm.load((Element) node, cmnds);
                    itm.setParent(this);
                    this.texts.add((Text) itm);
                } else {
                    LOGGER.warning(" only texts accepting");
                }
            }
        }
    }

    @Override
    public void process(ITextCommandProcessListener procsListener) {
        procsListener.before(this);
        for (Text txt : this.texts) {
            txt.process(procsListener);
        }
        procsListener.after(this);
    }

    
}
