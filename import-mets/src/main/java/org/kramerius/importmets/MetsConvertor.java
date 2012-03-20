package org.kramerius.importmets;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.sax.SAXSource;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.kramerius.dc.OaiDcType;
import org.kramerius.importmets.convertor.MetsPeriodicalConvertor;
import org.kramerius.importmets.convertor.MonographConvertor;
import org.kramerius.importmets.utils.XMLTools;
import org.kramerius.importmets.valueobj.ConvertorConfig;
import org.kramerius.importmets.valueobj.ServiceException;
import org.kramerius.mets.Mets;
import org.kramerius.mods.ModsDefinition;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.qbizm.kramerius.imp.jaxb.DigitalObject;
import com.qbizm.kramerius.imp.jaxb.Monograph;
import com.qbizm.kramerius.imp.jaxb.periodical.Periodical;

import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;


/**
 * Nastroj pro konverzi XML z ANL do formatu Fedora Object XML
 *
 * @author vlahoda
 */
public class MetsConvertor {

    private static final Logger log = Logger.getLogger(MetsConvertor.class);

    private static Marshaller marshaller = null;
    private static Unmarshaller unmarshaller = null;

    public static void main(String[] args) throws InterruptedException, JAXBException, FileNotFoundException, SAXException, ServiceException {

        if (args.length  != 3) {
            System.out.println("ANL METS to FOXML conversion tool.\n");
            System.out.println("Usage: conversion-tool defaultVisibility <input-file> <output-folder>");
            System.exit(1);
        }

        boolean defaultVisibility = Boolean.parseBoolean(args[0]);

        String importRoot = null;
        if (args.length == 1){
            importRoot = KConfiguration.getInstance().getConfiguration().getString("migration.directory");
        } else{
            importRoot = args[1];
        }
        String exportRoot = null;
        if (args.length == 3) {
            exportRoot = args[2];
        } else {
            exportRoot = importRoot + "-converted";
        }

        convert(importRoot, exportRoot,  defaultVisibility, null);

    }



    public static String convert(String importRoot, String exportRoot, boolean defaultVisibility, String titleId) throws InterruptedException, JAXBException, FileNotFoundException, SAXException, ServiceException {
        System.setProperty("java.awt.headless", "true");
        StringBuffer convertedURI = new StringBuffer();

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Mets.class, DigitalObject.class);
            marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "utf-8");
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            try{
                marshaller.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper", new NamespacePrefixMapperInternalImpl());
            } catch (PropertyException ex){
                marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapperImpl());
            }
            marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "info:fedora/fedora-system:def/foxml# http://www.fedora.info/definitions/1/0/foxml1-1.xsd");

            unmarshaller = jaxbContext.createUnmarshaller();


        } catch (Exception e) {
            log.error("Cannot init JAXB", e);
            throw new RuntimeException(e);
        }

        File importFile = new File(importRoot);
        if (!importFile.exists()) {
            System.err.println("Import root folder doesn't exist: " + importFile.getAbsolutePath());
            System.exit(1);
        }

        //visitAllDirsAndFiles(importFile, importRoot, exportRoot,   defaultVisibility,  convertedURI, titleId);


        File exportFolderFile = IOUtils.checkDirectory(exportRoot);
        if (!useContractSubfolders()){
            IOUtils.cleanDirectory(exportFolderFile);
        }
        String importFolder = importFile.getParent();
        if (importFolder == null) {
            importFolder = ".";
        }
        //String subFolderName = importFolder.substring(importRoot.length());

        String exportFolder = exportRoot; //+ subFolderName;

        ConvertorConfig config = new ConvertorConfig();
        config.setMarshaller(marshaller);
        config.setExportFolder(exportFolder);
        config.setImportFolder(importFolder);

        config.setDefaultVisibility(defaultVisibility);
        int l=5;
        try{
            l=KConfiguration.getInstance().getConfiguration().getInt("contractNo.length");
        }catch(NumberFormatException ex){
            log.error("Cannot parse property contractNo.length", ex);
        }
        config.setContractLength(l);
        //try {
            convertOneDirectory(unmarshaller, importFile, config, convertedURI, titleId);

        return convertedURI.toString();
    }







    private static void visitAllDirsAndFiles(File importFile, String importRoot, String exportRoot, boolean defaultVisibility, StringBuffer convertedURI, String titleId) throws InterruptedException, JAXBException, FileNotFoundException, SAXException, ServiceException {

        if (importFile.isDirectory()) {
            String subFolderName = importFile.getAbsolutePath().substring(importRoot.length());

            String exportFolder = exportRoot + subFolderName;

            File exportFolderFile = IOUtils.checkDirectory(exportFolder);
            if (!useContractSubfolders()){
                IOUtils.cleanDirectory(exportFolderFile);
            }
            File[] children = importFile.listFiles();
            for (int i = 0; i < children.length; i++) {
                visitAllDirsAndFiles(children[i], importRoot, exportRoot, defaultVisibility, convertedURI, titleId);
            }
        } else {
            if (importFile.getName().endsWith(".xml")) {

                String importFolder = importFile.getParent();
                if (importFolder == null) {
                    importFolder = ".";
                }
                String subFolderName = importFolder.substring(importRoot.length());

                String exportFolder = exportRoot + subFolderName;

                ConvertorConfig config = new ConvertorConfig();
                config.setMarshaller(marshaller);
                config.setExportFolder(exportFolder);
                config.setImportFolder(importFolder);

                config.setDefaultVisibility(defaultVisibility);
                int l=5;
                try{
                    l=KConfiguration.getInstance().getConfiguration().getInt("contractNo.length");
                }catch(NumberFormatException ex){
                    log.error("Cannot parse property contractNo.length", ex);
                }
                config.setContractLength(l);
                //try {
                    convertOneDirectory(unmarshaller, importFile, config, convertedURI, titleId);
                /*} catch (InterruptedException e) {
                    log.error("Cannot convert "+importFile, e);
                } catch (JAXBException e) {
                    log.error("Cannot convert "+importFile, e);
                } catch (FileNotFoundException e) {
                    log.error("Cannot convert "+importFile, e);
                } catch (SAXException e) {
                    log.error("Cannot convert "+importFile, e);
                }*/

            }
        }
    }

    private static void convertOneDirectory(Unmarshaller unmarshaller, File importFile, ConvertorConfig config, StringBuffer convertedURI, String titleId) throws InterruptedException, JAXBException, FileNotFoundException, SAXException, ServiceException {
        long timeStart = System.currentTimeMillis();

        XMLReader reader = XMLReaderFactory.createXMLReader();
        reader.setEntityResolver(new EntityResolver(){
            @Override
            public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                    return new InputSource(new ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?>".getBytes()));
            }
        });
        SAXSource saxSource = new SAXSource( reader, new InputSource( new FileInputStream(importFile) ) );
        Object source = unmarshaller.unmarshal(saxSource);
        log.info("File "+importFile +" loaded: "+source);

/*
        XMLTools reader = new XMLTools();
        reader.loadXmlFromFile(importFile);
        log.info("File "+importFile +" loaded");
        try {
            log.info("TEXT:"+reader.nodeToString(reader.getListOfNodes("/mets/dmdSec/mdWrap/xmlData").item(0)));
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
*/
        int objectCounter = 0;
        MetsPeriodicalConvertor pc = new MetsPeriodicalConvertor(config, unmarshaller);
        pc.convert((Mets)source, convertedURI);
        objectCounter = pc.getObjectCounter();

        /*
        //try {
            if (source instanceof Monograph) {
                MonographConvertor mc = new MonographConvertor(config);
                Monograph monograph = (Monograph) source;
                mc.convert(monograph, convertedURI);
                objectCounter = mc.getObjectCounter();
            } else if (source instanceof Periodical) {
                PeriodicalConvertor pc = new PeriodicalConvertor(config);
                Periodical periodical = (Periodical) source;
                pc.convert(periodical, convertedURI);
                objectCounter = pc.getObjectCounter();
            } else {
                throw new UnsupportedOperationException("Unsupported object class: " + source.getClass());
            }
            if (useContractSubfolders()&&config.getContract()!=null&&copyOriginal()){
                String targetName = null;
                if (titleId == null || titleId.trim().isEmpty()){
                    targetName = config.getContract()+".k3";
                } else{
                    targetName = titleId.replace(':','_')+".k3";
                }
                File target = new File(config.getExportFolder() + System.getProperty("file.separator") +targetName);
                try {
                    FileUtils.copyFile(importFile, target);
                } catch (IOException e) {
                    log.error(importFile.getName() + ": copyOriginal failed", e);
                }
            }
        //} catch (ServiceException e) {
        //    log.error(importFile.getName() + ": conversion failed", e);
        //}*/

        long timeFinish = System.currentTimeMillis();
        if (log.isInfoEnabled()) {
            log.info("Elapsed time: " + ((timeFinish - timeStart) / 1000.0) + " seconds. "+objectCounter + " digital objects (files) written.");
        }
    }


    /**
   *
   */

    public static class NamespacePrefixMapperInternalImpl extends com.sun.xml.internal.bind.marshaller.NamespacePrefixMapper {

        public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
            if ("info:fedora/fedora-system:def/foxml#".equals(namespaceUri)) {
                return "foxml";
            }
            if ("http://www.loc.gov/mods/v3".equals(namespaceUri)) {
                return "mods";
            }
            if ("http://purl.org/dc/elements/1.1/".equals(namespaceUri)){
                return "dc";
            }
            if ("http://www.openarchives.org/OAI/2.0/oai_dc/".equals(namespaceUri)){
                return "oai_dc";
            }
            if ("info:fedora/fedora-system:def/model#".equals(namespaceUri)){
                return "fedora-model";
            }
            if ("http://www.w3.org/1999/02/22-rdf-syntax-ns#".equals(namespaceUri)){
                return "rdf";
            }
            if ("http://www.nsdl.org/ontologies/relationships#".equals(namespaceUri)){
                return "kramerius";
            }
            if ("http://www.w3.org/1999/xlink".equals(namespaceUri)){
                return "xlink";
            }
            if ("http://www.loc.gov/METS/".equals(namespaceUri)){
                return "mets";
            }
            return suggestion;
        }

    }

    public static class NamespacePrefixMapperImpl extends com.sun.xml.bind.marshaller.NamespacePrefixMapper {

        public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
            if ("info:fedora/fedora-system:def/foxml#".equals(namespaceUri)) {
                return "foxml";
            }
            if ("http://www.loc.gov/mods/v3".equals(namespaceUri)) {
                return "mods";
            }
            if ("http://purl.org/dc/elements/1.1/".equals(namespaceUri)){
                return "dc";
            }
            if ("http://www.openarchives.org/OAI/2.0/oai_dc/".equals(namespaceUri)){
                return "oai_dc";
            }
            if ("info:fedora/fedora-system:def/model#".equals(namespaceUri)){
                return "fedora-model";
            }
            if ("http://www.w3.org/1999/02/22-rdf-syntax-ns#".equals(namespaceUri)){
                return "rdf";
            }
            if ("http://www.nsdl.org/ontologies/relationships#".equals(namespaceUri)){
                return "kramerius";
            }
            if ("http://www.w3.org/1999/xlink".equals(namespaceUri)){
                return "xlink";
            }
            if ("http://www.loc.gov/METS/".equals(namespaceUri)){
                return "mets";
            }
            return suggestion;
        }

    }



    public static boolean useContractSubfolders(){
        return KConfiguration.getInstance().getConfiguration().getBoolean("convert.useContractSubfolders", false);
    }

    public static boolean copyOriginal(){
        return KConfiguration.getInstance().getConfiguration().getBoolean("convert.copyOriginal", false);
    }

}
