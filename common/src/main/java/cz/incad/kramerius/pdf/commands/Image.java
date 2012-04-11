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

import java.util.logging.Level;

import org.w3c.dom.Element;

public class Image implements Command {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(Image.class.getName());
    
    private String pid;
    
    @Override
    public void load(Element elm, Commands cmnds) throws InstantiationException, IllegalAccessException {
        if (elm.getNodeName().equals("image")) {
            String pid = elm.getAttribute("pid");
            if ((pid != null) && (!pid.equals(""))) {
                this.pid = pid;
            } else {
                LOGGER.log(Level.WARNING, "cannot load image component. No pid "); 
            }
        } else {
           LOGGER.log(Level.WARNING, "cannot load image component. No image elm."); 
        }
    }

    @Override
    public Object acceptVisitor(CommandVisitor visitor, Object obj) {
        return visitor.visit(this, obj);
    }
    

    public String getPid() {
        return pid;
    }
}
