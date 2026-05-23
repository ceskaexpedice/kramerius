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
/**
 *
 */
package org.kramerius.importmets;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class MetsConvertorTest {

    @Before
    public void setup() {
        MetsConvertor.CONVERT_ONLY = true;
    }


    @After
    public void teardown() {
        MetsConvertor.CONVERT_ONLY = false;
    }

    @Test
    public void testOrcidExistsAfterConvert() throws Exception {
        Path inputFolder = Paths.get("src/test/resources/mets");
        Path outputFolder = Paths.get("build/test-output");
        if (!Files.exists(outputFolder)) {
            Files.createDirectories(outputFolder);
        }
        String[] args = new String[]{
                "true",
                inputFolder.toAbsolutePath().toString(),
                outputFolder.toAbsolutePath().toString()
        };

        System.out.println("Spouštím konverzi z: " + args[1]);
        MetsConvertor.main(args);

        // Najít vygenerovaný soubor (měl by tam být jeden)
        File[] files = outputFolder.toFile().listFiles((dir, name) -> name.endsWith(".xml"));
        Assert.assertNotNull(files);
        Assert.assertTrue(files.length > 0);
        File outputFile = files[0];

        // Parsování XML a ověření ORCIDu pomocí XPath
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false); // Pro jednodušší XPath v testech vypneme namespaces
        Document doc = factory.newDocumentBuilder().parse(outputFile);

        XPath xpath = XPathFactory.newInstance().newXPath();
        String query = "//*[local-name()='nameIdentifier' and @type='orcid']";
        String orcidValue = (String) xpath.evaluate(query, doc, XPathConstants.STRING);

        Assert.assertFalse(orcidValue.isEmpty());
        Assert.assertEquals("0000-0003-2358-7134", orcidValue.trim());
    }


}
