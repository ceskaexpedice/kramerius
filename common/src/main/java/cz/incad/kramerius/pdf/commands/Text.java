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

public class Text implements Command {

    private String fontFormalName;
    private String text;
    
    public String getFontFormalName() {
        return fontFormalName;
    }

    public String getText() {
        return text;
    }
    
    @Override
    public void load(Element elm, Commands cmnds) throws InstantiationException, IllegalAccessException {
        if (elm.getNodeName().equals("text")) {
            this.fontFormalName = elm.getAttribute("font-formal-name");
            this.text = elm.getTextContent();
        }        
    }

    @Override
    public Object acceptVisitor(CommandVisitor visitor, Object obj) {
        obj = visitor.visit(this, obj);
        return obj;
    }
}
