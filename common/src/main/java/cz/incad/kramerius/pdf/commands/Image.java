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

public class Image extends AbstractITextCommand implements ITextCommand {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(Image.class.getName());
    
    private String pid;
    private String file;
    
    @Override
    public void load(Element elm, ITextCommands cmnds) throws InstantiationException, IllegalAccessException {
        if (elm.getNodeName().equals("image")) {
            String file = elm.getAttribute("file");
            String pid = elm.getAttribute("pid");
            if ((pid != null) && (!pid.equals(""))) {
                this.pid = pid;
            } 
            if ((file != null) && (!file.equals(""))) {
                this.file = file;
            } else {
                LOGGER.log(Level.WARNING, "cannot load image component. No pid, no file "); 
            }
        } else {
           LOGGER.log(Level.WARNING, "cannot load image component. No image elm."); 
        }
    }

    

    
    @Override
    public void process(ITextCommandProcessListener procsListener) {
        procsListener.before(this);
        procsListener.after(this);
    }


    public String getFile() {
        return this.file;
    }

    public String getPid() {
        return pid;
    }
}
