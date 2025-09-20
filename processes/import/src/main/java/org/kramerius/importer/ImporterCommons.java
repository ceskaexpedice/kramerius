/*
 * Copyright (C) 2025  Inovatika
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
package org.kramerius.importer;

import cz.incad.kramerius.utils.conf.KConfiguration;
import org.ceskaexpedice.fedoramodel.DatastreamType;
import org.ceskaexpedice.fedoramodel.DigitalObject;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImporterCommons {

    public static final Logger LOGGER = Logger.getLogger(ImporterCommons.class.getName());

    public static Object marshallingLock = new Object();
    public static Unmarshaller unmarshaller = null;
    public static Marshaller datastreamMarshaller = null;
    public static DocumentBuilder docBuilder;

    static {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(DigitalObject.class);
            ImporterCommons.unmarshaller = jaxbContext.createUnmarshaller();
            JAXBContext jaxbdatastreamContext = JAXBContext.newInstance(DatastreamType.class);
            datastreamMarshaller = jaxbdatastreamContext.createMarshaller();
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            docBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Cannot init JAXB", e);
            throw new RuntimeException(e);
        }
    }

}
