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
package cz.incad.Kramerius.views.inc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;

import cz.incad.kramerius.Initializable;
import cz.incad.kramerius.utils.IOUtils;

/**
 * @author pavels
 *
 */
public class FooterViewObject implements Initializable{



    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(FooterViewObject.class.getName());

    private Properties properties;
    
    public FooterViewObject() {
        super();
    }

    public String getVersion() {
        if (this.properties != null) {
            return this.properties.getProperty("version");
        } else return "";
    }

    private InputStream getInputStream() throws IOException {
        InputStream revisions = this.getClass().getClassLoader().getResourceAsStream("build.properties");
        if (revisions != null) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            IOUtils.copyStreams(revisions,true, bos, false);
            return new ByteArrayInputStream(bos.toByteArray());
        } else return null;
    }
    
    public String getRevision() {
        if (this.properties != null) {
            return this.properties.getProperty("hash");
        } else return "";
    }

    /* (non-Javadoc)
     * @see cz.incad.Kramerius.Initializable#init()
     */
    @Override
    public void init() {
        InputStream is = null;
        try {
            is = getInputStream();
            if (is != null) {
                this.properties = new Properties();
                this.properties.load(is);
            } else LOGGER.severe("cannot load build properties ");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } finally {
            if (is != null) IOUtils.tryClose(is);
        }
    }
}
