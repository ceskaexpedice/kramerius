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


public class Paragraph extends AbstractITextCommand  {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(Paragraph.class.getName());
    
    private TextsArray textsArray;
    private List lists;
    
    private int spacingAfter = -1;
    private int spacingBefore = -1;

    private String alignment = null;
    
    @Override
    public void load(Element elm, ITextCommands cmnds) throws InstantiationException, IllegalAccessException {
        String name = elm.getNodeName();
        if (name.equals("paragraph")) {
            if (notEmptyAttribute(elm,"spacing-after")) {
                this.spacingAfter = Integer.parseInt(elm.getAttribute("spacing-after"));
            }
            if (notEmptyAttribute(elm,"spacing-before")) {
                this.spacingBefore = Integer.parseInt(elm.getAttribute("spacing-before"));
            }
            if (notEmptyAttribute(elm,"alignment")) {
                this.alignment = elm.getAttribute("alignment");
            }
            this.hyphenation = this.hyphenationFromAttibutes(elm);
            
            NodeList nList = elm.getChildNodes();
            for (int i = 0, ll = nList.getLength(); i < ll; i++) {
                Node node = nList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    String nName = node.getNodeName();
                    ITextCommand itm = cmnds.createInstance(nName);
                    if (itm instanceof TextsArray) {
                        itm.load((Element) node, cmnds);
                        this.textsArray = (TextsArray) itm;
                        this.textsArray.setParent(this);
                    } else if (itm instanceof List) {
                        itm.load((Element) node, cmnds);
                        this.lists = (List) itm;
                        this.lists.setParent(this);
                    } else {
                        LOGGER.warning(" only List or TextArray accepting");
                    }
                }
            }
        }
    }

    
    public TextsArray getTextsArray() {
        return textsArray;
    }
    
    public List getList() {
        return lists;
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
    
    @Override
    public void process(ITextCommandProcessListener procsListener) {
        procsListener.before(this);

        if (textsArray != null) {
            textsArray.process(procsListener);
        }
        if (this.lists != null) {
            this.lists.process(procsListener);
        }
        
        procsListener.after(this);
    }

    public String getAlignment() {
        return this.alignment;
    }

    public void setAlignment(String a) {
        this.alignment = a;
    }

    public boolean isAlignmentDefined() {
        return this.alignment != null;
    }
}
