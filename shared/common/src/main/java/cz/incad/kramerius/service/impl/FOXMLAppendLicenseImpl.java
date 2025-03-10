/*
 * Copyright (C) May 24, 2024 Pavel Stastny
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
package cz.incad.kramerius.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang3.tuple.Triple;
import org.ceskaexpedice.akubra.pid.LexerException;
import org.ceskaexpedice.akubra.relsext.RelsExtUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.common.base.Functions;
import com.google.common.collect.Lists;

import cz.incad.kramerius.service.FOXMLAppendLicenseService;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class FOXMLAppendLicenseImpl implements FOXMLAppendLicenseService {

    public static final Logger LOGGER = Logger.getLogger(FOXMLAppendLicenseImpl.class.getName());

    // Default configuration prefix key
    public static final String LICENSE_RULES = "licenses.rules";

    // Default rules
    // monograph=>monographunit
    // If both monograph and monographunit are found in the package, the license is
    // assigned to monographunit and monograph is assigned to containsLicenses.

    // periodical=>periodicalvolume
    // If both periodical and periodicalvolume are found in the package, the license
    // is assigned to periodicalvolume and periodical is assigned to
    // containsLicenses.
    @Override
    public void appendLicense(String path, String license) throws FileNotFoundException, ParserConfigurationException,
            SAXException, IOException, XPathExpressionException, LexerException {
        
        Map<String, List<Triple<String, String, File>>> modelsMapping = new HashMap<>();

        File folder = new File(path);

        Files.walkFileTree(folder.toPath(), Collections.singleton(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
                new FileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if (!Files.isRegularFile(file)) {
                            return FileVisitResult.CONTINUE;
                        }

                        try {
                            if (file.toFile().getName().toLowerCase().endsWith(".xml")) {
                                Document parsedDocument = XMLUtils.parseDocument(new FileInputStream(file.toFile()),
                                        true);
                                String pid = parsedDocument.getDocumentElement().getAttribute("PID");
                                Element relsExt = RelsExtUtils.getRELSEXTFromGivenFOXML(parsedDocument);
                                String model = RelsExtUtils.getModel(relsExt);

                                Triple<String, String, File> triple = Triple.of(pid, model, file.toFile());
                                if (!modelsMapping.containsKey(model)) {
                                    modelsMapping.put(model, new ArrayList<>());
                                }
                                modelsMapping.get(model).add(triple);
                            }
                        } catch (ParserConfigurationException | SAXException | IOException e) {
                            LOGGER.log(Level.SEVERE, e.getMessage());
                        }

                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                        LOGGER.log(Level.SEVERE, "Error processing file: " + file.toString(), exc);

                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        if (exc != null) {
                            LOGGER.log(Level.SEVERE, "Error searching directory : " + dir.toString(), exc);
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });

        boolean appliedRule = false;
        List<String> rules = KConfiguration.getInstance().getConfiguration()
                .getList(LICENSE_RULES, Arrays.asList("monograph->monographunit", "periodical->periodicalvolume"))
                .stream().map(Object::toString).collect(Collectors.toList());
        for (String rule : rules) {
            String[] split = rule.split("->");
            if (split.length > 1) {
                String leftSide = split[0];
                String rightSide = split[1];
                if (modelsMapping.containsKey(leftSide) && modelsMapping.containsKey(rightSide)) {
                    LOGGER.info(String.format("Applying license according to rule  %s", rule));

                    List<Triple<String, String, File>> containsLicense = modelsMapping.get(leftSide);
                    for (Triple<String, String, File> containsLicenseTripple : containsLicense) {
                        File right = containsLicenseTripple.getRight();
                        Document parentDoc = XMLUtils.parseDocument(new FileInputStream(right), true);
                        Element relsExt = RelsExtUtils.getRELSEXTFromGivenFOXML(parentDoc);
                        if (relsExt != null) {
                            List<String> containsLicenses = RelsExtUtils.getContainsLicenses(relsExt);
                            if (!containsLicenses.contains(license)) {
                                LOGGER.info(String.format("Applying 'containsLicense' to %s",
                                        containsLicenseTripple.getLeft()));
                                RelsExtUtils.addRDFLiteral(relsExt, license, "containsLicense");
                                changeDoc(right, parentDoc);
                            }
                        }
                    }

                    List<Triple<String, String, File>> licenses = modelsMapping.get(rightSide);
                    for (Triple<String, String, File> licenseTripple : licenses) {
                        File right = licenseTripple.getRight();
                        Document parentDoc = XMLUtils.parseDocument(new FileInputStream(right), true);
                        Element relsExt = RelsExtUtils.getRELSEXTFromGivenFOXML(parentDoc);
                        if (relsExt != null) {
                            List<String> realContainsLicenses = RelsExtUtils.getLicenses(relsExt);
                            if (!realContainsLicenses.contains(license)) {
                                LOGGER.info(String.format("Applying 'license' to %s", licenseTripple.getLeft()));
                                RelsExtUtils.addRDFLiteral(relsExt, license, "license");
                                changeDoc(right, parentDoc);
                            }
                        }
                    }
                    appliedRule = true;
                }
            }
        }

        if (!appliedRule) {
            LOGGER.info("No rule applied ");
            List<String> licensesModels = Lists.transform(
                    KConfiguration.getInstance().getConfiguration().getList("fedora.defaultLicenseModels"),
                    Functions.toStringFunction());
            for (String licModel : licensesModels) {
                if (modelsMapping.containsKey(licModel)) {
                    LOGGER.info(String.format("Applying license to model %s", licModel));
                    List<Triple<String, String, File>> licenses = modelsMapping.get(licModel);
                    for (Triple<String, String, File> licenseTripple : licenses) {
                        File right = licenseTripple.getRight();
                        Document parentDoc = XMLUtils.parseDocument(new FileInputStream(right), true);
                        Element relsExt = RelsExtUtils.getRELSEXTFromGivenFOXML(parentDoc);
                        if (relsExt != null) {
                            List<String> realLicenses = RelsExtUtils.getLicenses(relsExt);
                            if (!realLicenses.contains(license)) {
                                LOGGER.info(String.format("Applying 'license' to %s", licenseTripple.getLeft()));
                                RelsExtUtils.addRDFLiteral(relsExt, license, "license");
                                changeDoc(right, parentDoc);
                            }
                        }
                    }
                }
            }
        }
    }

    private void changeDoc(File file, Document parentDoc) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            XMLUtils.print(parentDoc, fos);
        } catch (FileNotFoundException | TransformerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public static void main(String[] args) throws FileNotFoundException, XPathExpressionException,
            ParserConfigurationException, SAXException, IOException, LexerException {
        // File folder1 = new
        // File("c:\\Users\\happy\\.kramerius4\\import\\045b1250-7e47-11e0-add1-000d606f5dc6\\");
        File folder1 = new File("c:\\Users\\happy\\.kramerius4\\import\\change\\");

        // File folder1 = new
        // File("c:\\Users\\happy\\.kramerius4\\import\\5035a48a-5e2e-486c-8127-2fa650842e46\\");
        // File folder1 = new
        // File("c:\\Users\\happy\\.kramerius4\\import\\530719f5-ee95-4449-8ce7-12b0f4cadb22\\");
        // File folder1 = new
        // File("c:\\Users\\happy\\.kramerius4\\import\\9cd57b03-0797-4e8f-90cb-6987dd633034\\");
        // File folder1 = new
        // File("c:\\Users\\happy\\.kramerius4\\import\\aba007-0003c4\\");
        // File folder1 = new
        // File("c:\\Users\\happy\\.kramerius4\\import\\b82784d7-435d-11dd-b505-00145e5790ea_nc\\");

        FOXMLAppendLicenseImpl impl = new FOXMLAppendLicenseImpl();
        impl.appendLicense(folder1.getAbsolutePath(), "public");
    }
}
