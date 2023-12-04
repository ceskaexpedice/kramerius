/*
 * Copyright (C) Nov 29, 2023 Pavel Stastny
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
package cz.inovatika.collections;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.xml.sax.SAXException;

import com.sun.jersey.api.client.Client;

import cz.incad.kramerius.processes.new_api.ProcessScheduler;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.inovatika.collections.migrations.FromK5Instance;

public class Restore {

    public static final Logger LOGGER = Logger.getLogger(Restore.class.getName());
    
    public static void main(String[] args) throws TransformerException, ParserConfigurationException, SAXException, IOException, JAXBException, InterruptedException, SolrServerException {
        LOGGER.log(Level.INFO, "Process parameters: " + Arrays.asList(args).toString());
        if (args.length > 1) {
            String authToken = args[0];
            String target = args[1];

            String parentZipFolder = KConfiguration.getInstance().getConfiguration().getString("collections.backup.folder");
            if (parentZipFolder == null) throw new IllegalStateException("configuration property 'collections.backup.folder' must be set ");
            String zipFile = parentZipFolder + File.separator + target;
            
            

            String tmpDirPath = System.getProperty("java.io.tmpdir");
            String subdirectoryPath = tmpDirPath + File.separator +  target;
            FileUtils.forceMkdir(new File(subdirectoryPath));
            unzip(zipFile, subdirectoryPath);
            

            LOGGER.info("Scheduling import "+subdirectoryPath);
            FromK5Instance.importTmpDir(subdirectoryPath, true, authToken);
        } else {
            throw new IllegalArgumentException("expecting 2 arguments (authtoken, zipfile)");
        }
    }
    
    
    public static void unzip(String zipFile, String outputFolder) throws IOException {
        LOGGER.info("Unzipping file to "+outputFolder);

        byte[] buffer = new byte[1024];

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                String fileName = zipEntry.getName();
                File newFile = new File(outputFolder + File.separator + fileName);

                // Vytvoření nadřazeného adresáře pro soubor, pokud neexistuje
                new File(newFile.getParent()).mkdirs();

                try (FileOutputStream fos = new FileOutputStream(newFile)) {
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                }

                zipEntry = zis.getNextEntry();
            }

            zis.closeEntry();
        }
    }
}
