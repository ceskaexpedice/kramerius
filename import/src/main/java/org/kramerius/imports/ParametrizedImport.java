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
package org.kramerius.imports;

import cz.incad.kramerius.processes.annotations.ParameterName;
import cz.incad.kramerius.processes.annotations.Process;
import cz.incad.kramerius.processes.impl.ProcessStarter;

import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.XMLUtils;
import org.apache.commons.io.FileUtils;
import org.kramerius.Import;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.logging.Level;

/**
 * Parametrized import proces
 * @author pavels
 *
 */
public class ParametrizedImport {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ParametrizedImport.class.getName());
    

    
    @Process
    public static void process( @ParameterName("importDirectory") File importDirectory, 
            @ParameterName("startIndexer")Boolean startIndexer,
            @ParameterName("updateExisting")Boolean updateExisting,
            @ParameterName("onlyReplaceDC")Boolean replaceDC,
            @ParameterName("onlyReplaceMODS")Boolean replaceMODs) throws IOException, ParserConfigurationException, SAXException {

        System.setProperty("import.directory", importDirectory.getAbsolutePath());
        System.setProperty("ingest.startIndexer", startIndexer.toString());
        System.setProperty("ingest.updateExisting", updateExisting.toString()); // only RELS-EXT update
        System.setProperty("ingest.skip", "false");   //import se bude vždy spouštět


        try {
            //TODO: I18N
            ProcessStarter.updateName("Parametrizovany import z '"+importDirectory.getAbsolutePath()+"'");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(), e);
        }

        // update metadata
        if (replaceDC || replaceMODs) {
            // prepare updatelist
            prepareFile(importDirectory, replaceDC, replaceMODs);
        }

        //standard import program
        Import.main(new String[0]);
    }

    private static final String DEFAULT_LIST_NAME = "update.list";
    private static String prepareFile(File importDirectory, Boolean replaceDC, Boolean replaceMODs) throws IOException, SAXException, ParserConfigurationException {
        StringBuilder builder = new StringBuilder();
        File[] files = importDirectory.listFiles();
        if (files != null) {
            for (File f : files) {
                if (!f.getName().endsWith("xml")) continue;
                Document document = XMLUtils.parseDocument(new FileInputStream(f), true);
                String pid = document.getDocumentElement().getAttribute("PID");
                builder.append(pid);
                if (replaceDC) {
                    builder.append(' ').append(FedoraUtils.DC_STREAM);
                }
                if (replaceMODs) {
                    builder.append(' ').append(FedoraUtils.BIBLIO_MODS_STREAM);
                }
                builder.append('\n');
            }
        }
        File f = new File(importDirectory, DEFAULT_LIST_NAME);
        if (!f.exists()) {
            f.createNewFile();
        }
        FileUtils.writeStringToFile(f, builder.toString());
        return f.getAbsolutePath();
    }
}
