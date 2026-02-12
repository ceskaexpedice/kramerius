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

public class PageBreak extends AbstractITextCommand implements ITextCommand {

    private Float width;
    private Float height;


    @Override
    public void load(Element elm, ITextCommands cmnds) throws InstantiationException, IllegalAccessException {
        String sWidth = elm.getAttribute("width");
        String sHeight = elm.getAttribute("height");
        if (sWidth != null && sHeight != null) {
            this.width = Float.parseFloat(sWidth);
            this.height = Float.parseFloat(sHeight);
        }
    }

    @Override
    public void process(ITextCommandProcessListener procsListener, ITextCommands xmlDocs) {
        procsListener.before(this, xmlDocs);
        procsListener.after(this, xmlDocs);
    }

    public Float getWidth() {
        return width;
    }

    public Float getHeight() {
        return height;
    }
}
