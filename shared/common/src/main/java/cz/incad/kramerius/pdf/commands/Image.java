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

import cz.incad.kramerius.utils.StringUtils;
import org.w3c.dom.Element;

/**
 * Represents big image, page
 * @author happy
 */
public class Image extends AbstractITextCommand implements ITextCommand {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(Image.class.getName());
    
    private String pid;
    private String file;

    private String url;

    private String x;
    private String y;
    private String width;
    private String height;

    @Override
    public void load(Element elm, ITextCommands cmnds) throws InstantiationException, IllegalAccessException {
        if (elm.getNodeName().equals("image")) {

            this.scaledMeasurements = this.scaledMeasurementsFromAttributes(elm);
            // data
            String file = elm.getAttribute("file");
            String pid = elm.getAttribute("pid");
            String url = elm.getAttribute("url");

            String x = elm.getAttribute("x");
            String y = elm.getAttribute("y");
            String width = elm.getAttribute("width");
            String height = elm.getAttribute("height");

            if ((pid != null) && (!pid.equals(""))) {
                this.pid = pid;
            } 
            if ((file != null) && (!file.equals(""))) {
                this.file = file;
            }
            if ((url != null) && (!url.equals(""))) {
                this.url = url;
            }
            if (!StringUtils.isAnyString(this.pid) && !StringUtils.isAnyString(this.file) && !StringUtils.isAnyString(this.url) ) {
                LOGGER.log(Level.WARNING, "cannot load image component. No pid, no file ");
            }

            if ((x != null) && (!x.equals(""))) {
                this.x = x;
            }

            if ((y != null) && (!y.equals(""))) {
                this.y = y;
            }

            if ((width != null) && (!width.equals(""))) {
                this.width = width;
            }
            if ((height != null) && (!height.equals(""))) {
                this.height = height;
            }
        } else {
           LOGGER.log(Level.WARNING, "cannot load image component. No image elm."); 
        }
    }

    

    
    @Override
    public void process(ITextCommandProcessListener procsListener, ITextCommands xmlDocs) {
        procsListener.before(this, xmlDocs);
        procsListener.after(this, xmlDocs);
    }

    public String getFile() {
        return this.file;
    }

    public String getPid() {
        return pid;
    }

    public String getUrl() {
        return this.url;
    }

    public String getX() {
        return x;
    }

    public String getY() {
        return y;
    }

    public String getWidth() {
        return width;
    }

    public String getHeight() {
        return height;
    }
}
