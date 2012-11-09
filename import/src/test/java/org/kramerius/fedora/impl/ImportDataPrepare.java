/*
 * Copyright (C) 2010 Pavel Stastny
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
package org.kramerius.fedora.impl;

import static org.easymock.EasyMock.expect;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.imageio.stream.FileImageInputStream;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.easymock.EasyMock;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

/** Testing data  for FedoraAccess and SolrAccess*/
public class ImportDataPrepare {
    public static final String[] DROBNUSTKY_PIDS = "uuid:0eaa6730-9068-11dd-97de-000d606f5dc6,uuid:4a7c2e50-af36-11dd-9643-000d606f5dc6,uuid:4a7ec660-af36-11dd-a782-000d606f5dc6,uuid:4a79bd50-af36-11dd-a60c-000d606f5dc6,uuid:4a8a8630-af36-11dd-ae9c-000d606f5dc6,uuid:4a8cf730-af36-11dd-ae88-000d606f5dc6,uuid:4a80c230-af36-11dd-ace4-000d606f5dc6,uuid:4a835a40-af36-11dd-b951-000d606f5dc6,uuid:4a85f250-af36-11dd-8535-000d606f5dc6,uuid:430d7f60-b03b-11dd-82fa-000d606f5dc6,uuid:4308eb80-b03b-11dd-a0f6-000d606f5dc6,uuid:431e4840-b03b-11dd-8818-000d606f5dc6,uuid:43101770-b03b-11dd-8673-000d606f5dc6,uuid:4314ab50-b03b-11dd-89db-000d606f5dc6,uuid:43171c50-b03b-11dd-b0c2-000d606f5dc6,uuid:4319b460-b03b-11dd-83ca-000d606f5dc6,uuid:4320e050-b03b-11dd-9b4a-000d606f5dc6".split(",");
    public static final String[] DROBNUSTKY_NOT_EXISTS ="uuid:4a7c2e50-af36-11dd-9643-000d606f5dc6notexist,uuid:4a7ec660-af36-11dd-a782-000d606f5dc6notexist,uuid:4a79bd50-af36-11dd-a60c-000d606f5dc6notexist,uuid:4a8a8630-af36-11dd-ae9c-000d606f5dc6notexist,uuid:4a8cf730-af36-11dd-ae88-000d606f5dc6notexist,uuid:4a80c230-af36-11dd-ace4-000d606f5dc6notexist,uuid:4a835a40-af36-11dd-b951-000d606f5dc6notexist,uuid:4a85f250-af36-11dd-8535-000d606f5dc6notexist,uuid:430d7f60-b03b-11dd-82fa-000d606f5dc6notexist,uuid:4308eb80-b03b-11dd-a0f6-000d606f5dc6notexist,uuid:431e4840-b03b-11dd-8818-000d606f5dc6notexist,uuid:43101770-b03b-11dd-8673-000d606f5dc6notexist,uuid:4314ab50-b03b-11dd-89db-000d606f5dc6notexist,uuid:43171c50-b03b-11dd-b0c2-000d606f5dc6notexist,uuid:4319b460-b03b-11dd-83ca-000d606f5dc6notexist,uuid:4320e050-b03b-11dd-9b4a-000d606f5dc6notexist".split(",");
    
    
    public static final String MUJ_ZIVOT_S_HITLEREM = "uuid:3ee97ce8-e548-11e0-9867-005056be0007";
    
    public static final String[] NARODNI_LISTY = {
        // periodikum - Narodni listy
        "uuid:ae876087-435d-11dd-b505-00145e5790ea",

        // volume
        "uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6",

        // item - cisla
        "uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6",
        "uuid:983a4660-938d-11dc-913a-000d606f5dc6",
        "uuid:53255e00-938a-11dc-8b44-000d606f5dc6",
         
        // stranky prvniho cisla
        "uuid:b38eba10-91f6-11dc-9eec-000d606f5dc6",
        "uuid:94a3ed60-92d6-11dc-beb4-000d606f5dc6",
        "uuid:b3987e10-91f6-11dc-96f6-000d606f5dc6",
        "uuid:94a3ed60-92d6-11dc-93df-000d606f5dc6",
        "uuid:b3a21b00-91f6-11dc-b8b2-000d606f5dc6",
        "uuid:94a68570-92d6-11dc-be5a-000d606f5dc6",
        "uuid:b3ab42c0-91f6-11dc-b83a-000d606f5dc6",
        "uuid:94a8f670-92d6-11dc-9104-000d606f5dc6",
        "uuid:b3d89450-91f6-11dc-98d0-000d606f5dc6",
        "uuid:94b4b640-92d6-11dc-b0a2-000d606f5dc6",
        "uuid:b3e23140-91f6-11dc-a406-000d606f5dc6",
        "uuid:94b74e50-92d6-11dc-8465-000d606f5dc6",
        "uuid:b3ebce30-91f6-11dc-812e-000d606f5dc6",
        "uuid:94b9bf50-92d6-11dc-82e2-000d606f5dc6",
        "uuid:b3f2fa20-91f6-11dc-9cd2-000d606f5dc6",
        "uuid:94bbe230-92d6-11dc-924c-000d606f5dc6",
      
        
        "uuid:b3b4dfb0-91f6-11dc-8f6a-000d606f5dc6",
        "uuid:94ab8e80-92d6-11dc-8d2c-000d606f5dc6",
        "uuid:b3bc0ba0-91f6-11dc-b29a-000d606f5dc6",
        "uuid:94ad8a50-92d6-11dc-96c7-000d606f5dc6",
        "uuid:b3c5a890-91f6-11dc-aaef-000d606f5dc6",
        "uuid:94b02260-92d6-11dc-be98-000d606f5dc6",
        "uuid:b3cf6c90-91f6-11dc-93a2-000d606f5dc6",
        "uuid:94b2ba70-92d6-11dc-a884-000d606f5dc6",
        
        
        "uuid:b4204bb0-91f6-11dc-8258-000d606f5dc6",
        "uuid:94ccab10-92d6-11dc-b58e-000d606f5dc6",
        "uuid:b4297370-91f6-11dc-878b-000d606f5dc6",
        "uuid:94cf1c10-92d6-11dc-bb1b-000d606f5dc6",
        "uuid:b4307850-91f6-11dc-9b9d-000d606f5dc6",
        "uuid:94d1b420-92d6-11dc-8e76-000d606f5dc6",
        
    };

    public static final String[] NARODNI_LISTY_NOT_EXISTS = {
        // stranky prvniho cisla
        "uuid:b38eba10-91f6-11dc-9eec-000d606f5dc6notexist",
        "uuid:94a3ed60-92d6-11dc-beb4-000d606f5dc6notexist",
        "uuid:b3987e10-91f6-11dc-96f6-000d606f5dc6notexist",
        "uuid:94a3ed60-92d6-11dc-93df-000d606f5dc6notexist",
        "uuid:b3a21b00-91f6-11dc-b8b2-000d606f5dc6notexist",
        "uuid:94a68570-92d6-11dc-be5a-000d606f5dc6notexist",
        "uuid:b3ab42c0-91f6-11dc-b83a-000d606f5dc6notexist",
        "uuid:94a8f670-92d6-11dc-9104-000d606f5dc6notexist",
        "uuid:b3d89450-91f6-11dc-98d0-000d606f5dc6notexist",
        "uuid:94b4b640-92d6-11dc-b0a2-000d606f5dc6notexist",
        "uuid:b3e23140-91f6-11dc-a406-000d606f5dc6notexist",
        "uuid:94b74e50-92d6-11dc-8465-000d606f5dc6notexist",
        "uuid:b3ebce30-91f6-11dc-812e-000d606f5dc6notexist",
        "uuid:94b9bf50-92d6-11dc-82e2-000d606f5dc6notexist",
        "uuid:b3f2fa20-91f6-11dc-9cd2-000d606f5dc6notexist",
        "uuid:94bbe230-92d6-11dc-924c-000d606f5dc6notexist",
      
        
        "uuid:b3b4dfb0-91f6-11dc-8f6a-000d606f5dc6notexist",
        "uuid:94ab8e80-92d6-11dc-8d2c-000d606f5dc6notexist",
        "uuid:b3bc0ba0-91f6-11dc-b29a-000d606f5dc6notexist",
        "uuid:94ad8a50-92d6-11dc-96c7-000d606f5dc6notexist",
        "uuid:b3c5a890-91f6-11dc-aaef-000d606f5dc6notexist",
        "uuid:94b02260-92d6-11dc-be98-000d606f5dc6notexist",
        "uuid:b3cf6c90-91f6-11dc-93a2-000d606f5dc6notexist",
        "uuid:94b2ba70-92d6-11dc-a884-000d606f5dc6notexist",
        
        
        "uuid:b4204bb0-91f6-11dc-8258-000d606f5dc6notexist",
        "uuid:94ccab10-92d6-11dc-b58e-000d606f5dc6notexist",
        "uuid:b4297370-91f6-11dc-878b-000d606f5dc6notexist",
        "uuid:94cf1c10-92d6-11dc-bb1b-000d606f5dc6notexist",
        "uuid:b4307850-91f6-11dc-9b9d-000d606f5dc6notexist",
        "uuid:94d1b420-92d6-11dc-8e76-000d606f5dc6notexist",
        
    };
    
    public static InputStream datastreams33() {
        String path = "/org/kramerius/fedora/res/datastreams_3_3";
        InputStream resStream = FedoraAccessImpl.class.getResourceAsStream(path);
        return resStream;
    }

    public static InputStream datastreams34() {
        String path = "/org/kramerius/fedora/res/datastreams_3_4";
        InputStream resStream = FedoraAccessImpl.class.getResourceAsStream(path);
        return resStream;
    }

    public static InputStream dsProfile33() {
        String path = "/org/kramerius/fedora/res/dsprofile_3_3";
        InputStream resStream = FedoraAccessImpl.class.getResourceAsStream(path);
        return resStream;
    }

    public static InputStream dsProfile34() {
        String path = "/org/kramerius/fedora/res/dsprofile_3_4";
        InputStream resStream = FedoraAccessImpl.class.getResourceAsStream(path);
        return resStream;
    }

    public static InputStream fedoraProfile33() {
        String path = "/org/kramerius/fedora/res/describe_3_3";
        InputStream resStream = FedoraAccessImpl.class.getResourceAsStream(path);
        return resStream;
    }

    public static InputStream fedoraProfile34() {
        String path = "/org/kramerius/fedora/res/describe_3_4";
        InputStream resStream = FedoraAccessImpl.class.getResourceAsStream(path);
        return resStream;
    }

    public static void narodniListyRelsExt(FedoraAccess fa) throws IOException, ParserConfigurationException, SAXException, LexerException {
        for (int i = 0; i < NARODNI_LISTY.length; i++) {
            String pid = NARODNI_LISTY[i];
            relsExt(fa, pid);
        }        
    }
    
    public static void notConsistentNarodniListyRelsExt(FedoraAccess fa) throws IOException, ParserConfigurationException, SAXException, LexerException {
        for (int i = 0; i < NARODNI_LISTY.length; i++) {
            String pid = NARODNI_LISTY[i];
            notConsistentRelsExt(fa, pid);
        }
        for (int i = 0; i < NARODNI_LISTY_NOT_EXISTS.length; i++) {
            String pid = NARODNI_LISTY_NOT_EXISTS[i];
            expect(fa.getRelsExt(pid)).andThrow(new FileNotFoundException("Not found"));
        }

    }

    public static void drobnustkyRelsExt(FedoraAccess fa) throws IOException, ParserConfigurationException, SAXException, LexerException {
        for (int i = 0; i < DROBNUSTKY_PIDS.length; i++) {
            String pid = DROBNUSTKY_PIDS[i];
            relsExt(fa, pid);
        }        
    }

    public static void relsExt(FedoraAccess fa, String pid) throws LexerException, IOException, ParserConfigurationException, SAXException {
        PIDParser pidParser = new PIDParser(pid);
        pidParser.objectPid();
        String objectId = pidParser.getObjectId();
        
        String path = "/org/kramerius/fedora/res/"+objectId+".xml";
        InputStream resStream = FedoraAccessImpl.class.getResourceAsStream(path);
        expect(fa.getRelsExt(pid)).andReturn(XMLUtils.parseDocument(resStream, true)).anyTimes();
    }

    public static void notConsistentRelsExt(FedoraAccess fa, String pid) throws LexerException, IOException, ParserConfigurationException, SAXException {
        PIDParser pidParser = new PIDParser(pid);
        pidParser.objectPid();
        String objectId = pidParser.getObjectId();
        
        String path = "/org/kramerius/fedora/res/"+objectId+".nonconsistent.xml";
        InputStream resStream = FedoraAccessImpl.class.getResourceAsStream(path);
        expect(fa.getRelsExt(pid)).andReturn(XMLUtils.parseDocument(resStream, true)).anyTimes();
    }

    public static void narodniListyIMGFULL(FedoraAccess fa) throws IOException, LexerException {
        for (int i = 0; i < NARODNI_LISTY.length; i++) {
            String pid = NARODNI_LISTY[i];
            String model = MODELS_MAPPING.get(pid);
            PIDParser parser = new PIDParser(model);
            parser.disseminationURI();
            String objectId = parser.getObjectId();
            if (objectId.equals("page")) {
                expect(fa.isImageFULLAvailable(NARODNI_LISTY[i])).andReturn(true).anyTimes();
            } else {
                expect(fa.isImageFULLAvailable(NARODNI_LISTY[i])).andReturn(false).anyTimes();
                
            }
        }
        
    }
    
    public static void drobnustkyWithIMGFULL(FedoraAccess fa) throws IOException {
        for (int i = 0; i < DROBNUSTKY_PIDS.length; i++) {
            if (DROBNUSTKY_PIDS[i].equals("uuid:0eaa6730-9068-11dd-97de-000d606f5dc6")) {
                expect(fa.isImageFULLAvailable(DROBNUSTKY_PIDS[i])).andReturn(false).anyTimes();
            } else {
                expect(fa.isImageFULLAvailable(DROBNUSTKY_PIDS[i])).andReturn(true).anyTimes();
            }
        }
    }

    public static void narodniListyDCs(FedoraAccess fa) throws LexerException, ParserConfigurationException, SAXException, IOException {
        for (int i = 0; i < NARODNI_LISTY.length; i++) {
            dc(fa,NARODNI_LISTY[i]);
        }
    }

    public static void drobnustkyDCS(FedoraAccess fa) throws IOException, LexerException, ParserConfigurationException, SAXException {
        for (int i = 0; i < DROBNUSTKY_PIDS.length; i++) {
            dc(fa,DROBNUSTKY_PIDS[i]);
        }
    }
    
    public static void narodniListyMods(FedoraAccess fa) throws LexerException, ParserConfigurationException, SAXException, IOException {
        for (int i = 0; i < NARODNI_LISTY.length; i++) {
            mods(fa,NARODNI_LISTY[i]);
        }
    }
    
    public static void drobnustkyMODS(FedoraAccess fa) throws IOException, LexerException, ParserConfigurationException, SAXException {
        for (int i = 0; i < DROBNUSTKY_PIDS.length; i++) {
            mods(fa,DROBNUSTKY_PIDS[i]);
        }
    }
    
    
    public static void drobnustkyWithOutIMGFULL(FedoraAccess fa) throws IOException {
        for (int i = 0; i < DROBNUSTKY_PIDS.length; i++) {
            expect(fa.isImageFULLAvailable(DROBNUSTKY_PIDS[i])).andReturn(false);
        }
    }

    
    public static void dc(FedoraAccess fa, String pid) throws LexerException, IOException, ParserConfigurationException, SAXException {
        PIDParser pidParser = new PIDParser(pid);
        pidParser.objectPid();

        String path = "/org/kramerius/fedora/res/"+pidParser.getObjectId()+".dc.xml";
        InputStream resStream = FedoraAccessImpl.class.getResourceAsStream(path);
        Document document = XMLUtils.parseDocument(resStream, true);
        EasyMock.expect(fa.getDC(pid)).andReturn(document).anyTimes();
    }
    
    public static void mods(FedoraAccess fa, String pid) throws LexerException, ParserConfigurationException, SAXException, IOException {
        PIDParser pidParser = new PIDParser(pid);
        pidParser.objectPid();

        String path = "/org/kramerius/fedora/res/"+pidParser.getObjectId()+".mods.xml";
        InputStream resStream = FedoraAccessImpl.class.getResourceAsStream(path);
        Document document = XMLUtils.parseDocument(resStream, true);
        EasyMock.expect(fa.getBiblioMods(pid)).andReturn(document).anyTimes();
    }
    
    public static void dataStreams(FedoraAccess fa, String pid) throws IOException, ParserConfigurationException, SAXException, LexerException {
        PIDParser pidParser = new PIDParser(pid);
        pidParser.objectPid();

        String path = "/org/kramerius/fedora/res/"+pidParser.getObjectId()+".datastreams.xml";
        InputStream resStream = FedoraAccessImpl.class.getResourceAsStream(path);
        expect(fa.getFedoraDataStreamsList(pid)).andReturn(resStream);
    }
        
    public static Map<String, String> MODELS_MAPPING = new HashMap<String, String>();

    static {
        // Drobnustky
        ImportDataPrepare.MODELS_MAPPING.put("uuid:0eaa6730-9068-11dd-97de-000d606f5dc6", "info:fedora/model:monograph");
        // mapovani drobnustek - pouze drobnustky jsou monografie - ostatni jsou stranky
        for (int i = 1; i < ImportDataPrepare.DROBNUSTKY_PIDS.length; i++) {
            String pid = ImportDataPrepare.DROBNUSTKY_PIDS[i];
            ImportDataPrepare.MODELS_MAPPING.put(pid, "info:fedora/model:page");
        }

        // periodikum - Narodni listy
        ImportDataPrepare.MODELS_MAPPING.put("uuid:ae876087-435d-11dd-b505-00145e5790ea","info:fedora/model:periodical");
        // volume
        ImportDataPrepare.MODELS_MAPPING.put("uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6","info:fedora/model:periodicalvolume");

        // item - cisla
        ImportDataPrepare.MODELS_MAPPING.put("uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6","info:fedora/model:periodicalitem");
        ImportDataPrepare.MODELS_MAPPING.put("uuid:983a4660-938d-11dc-913a-000d606f5dc6","info:fedora/model:periodicalitem");
        ImportDataPrepare.MODELS_MAPPING.put("uuid:53255e00-938a-11dc-8b44-000d606f5dc6","info:fedora/model:periodicalitem");
         
        // stranky 
        ImportDataPrepare.MODELS_MAPPING.put("uuid:b38eba10-91f6-11dc-9eec-000d606f5dc6", "info:fedora/model:page");
        ImportDataPrepare.MODELS_MAPPING.put("uuid:94a3ed60-92d6-11dc-beb4-000d606f5dc6", "info:fedora/model:page");
        ImportDataPrepare.MODELS_MAPPING.put("uuid:b3987e10-91f6-11dc-96f6-000d606f5dc6", "info:fedora/model:page");
        ImportDataPrepare.MODELS_MAPPING.put("uuid:94a3ed60-92d6-11dc-93df-000d606f5dc6", "info:fedora/model:page");
        ImportDataPrepare.MODELS_MAPPING.put("uuid:b3a21b00-91f6-11dc-b8b2-000d606f5dc6", "info:fedora/model:page");
        ImportDataPrepare.MODELS_MAPPING.put("uuid:94a68570-92d6-11dc-be5a-000d606f5dc6", "info:fedora/model:page");
        ImportDataPrepare.MODELS_MAPPING.put("uuid:b3ab42c0-91f6-11dc-b83a-000d606f5dc6", "info:fedora/model:page");
        ImportDataPrepare.MODELS_MAPPING.put("uuid:94a8f670-92d6-11dc-9104-000d606f5dc6","info:fedora/model:page");
        ImportDataPrepare.MODELS_MAPPING.put("uuid:b3d89450-91f6-11dc-98d0-000d606f5dc6","info:fedora/model:page");
        ImportDataPrepare.MODELS_MAPPING.put("uuid:94b4b640-92d6-11dc-b0a2-000d606f5dc6","info:fedora/model:page");
        ImportDataPrepare.MODELS_MAPPING.put("uuid:b3e23140-91f6-11dc-a406-000d606f5dc6","info:fedora/model:page");
        ImportDataPrepare.MODELS_MAPPING.put("uuid:94b74e50-92d6-11dc-8465-000d606f5dc6","info:fedora/model:page");
        ImportDataPrepare.MODELS_MAPPING.put("uuid:b3ebce30-91f6-11dc-812e-000d606f5dc6","info:fedora/model:page");
        ImportDataPrepare.MODELS_MAPPING.put("uuid:94b9bf50-92d6-11dc-82e2-000d606f5dc6","info:fedora/model:page");
        ImportDataPrepare.MODELS_MAPPING.put("uuid:b3f2fa20-91f6-11dc-9cd2-000d606f5dc6","info:fedora/model:page");
        ImportDataPrepare.MODELS_MAPPING.put("uuid:94bbe230-92d6-11dc-924c-000d606f5dc6","info:fedora/model:page");
      
        
        ImportDataPrepare.MODELS_MAPPING.put("uuid:b3b4dfb0-91f6-11dc-8f6a-000d606f5dc6","info:fedora/model:page");
        ImportDataPrepare.MODELS_MAPPING.put("uuid:94ab8e80-92d6-11dc-8d2c-000d606f5dc6","info:fedora/model:page");
        ImportDataPrepare.MODELS_MAPPING.put("uuid:b3bc0ba0-91f6-11dc-b29a-000d606f5dc6","info:fedora/model:page");
        ImportDataPrepare.MODELS_MAPPING.put("uuid:94ad8a50-92d6-11dc-96c7-000d606f5dc6","info:fedora/model:page");
        ImportDataPrepare.MODELS_MAPPING.put("uuid:b3c5a890-91f6-11dc-aaef-000d606f5dc6","info:fedora/model:page");
        ImportDataPrepare.MODELS_MAPPING.put("uuid:94b02260-92d6-11dc-be98-000d606f5dc6","info:fedora/model:page");
        ImportDataPrepare.MODELS_MAPPING.put("uuid:b3cf6c90-91f6-11dc-93a2-000d606f5dc6","info:fedora/model:page");
        ImportDataPrepare.MODELS_MAPPING.put("uuid:94b2ba70-92d6-11dc-a884-000d606f5dc6","info:fedora/model:page");
        
        
        ImportDataPrepare.MODELS_MAPPING.put("uuid:b4204bb0-91f6-11dc-8258-000d606f5dc6","info:fedora/model:page");
        ImportDataPrepare.MODELS_MAPPING.put("uuid:94ccab10-92d6-11dc-b58e-000d606f5dc6","info:fedora/model:page");
        ImportDataPrepare.MODELS_MAPPING.put("uuid:b4297370-91f6-11dc-878b-000d606f5dc6","info:fedora/model:page");
        ImportDataPrepare.MODELS_MAPPING.put("uuid:94cf1c10-92d6-11dc-bb1b-000d606f5dc6","info:fedora/model:page");
        ImportDataPrepare.MODELS_MAPPING.put("uuid:b4307850-91f6-11dc-9b9d-000d606f5dc6","info:fedora/model:page");
        ImportDataPrepare.MODELS_MAPPING.put("uuid:94d1b420-92d6-11dc-8e76-000d606f5dc6","info:fedora/model:page");
        
    }

    public static Map<String, ObjectPidsPath> PATHS_MAPPING = new HashMap<String, ObjectPidsPath>();

    static {
        // monograph -> page
        ImportDataPrepare.PATHS_MAPPING.put(ImportDataPrepare.DROBNUSTKY_PIDS[0], new ObjectPidsPath(ImportDataPrepare.DROBNUSTKY_PIDS[0]));
        for (int i = 0; i < ImportDataPrepare.DROBNUSTKY_PIDS.length; i++) {
            String pid = ImportDataPrepare.DROBNUSTKY_PIDS[i];
            if (i > 0) {
                ImportDataPrepare.PATHS_MAPPING.put(pid, new ObjectPidsPath(ImportDataPrepare.DROBNUSTKY_PIDS[0], pid));
            }
        }
        
        
        // periodikum - Narodni listy
        ImportDataPrepare.PATHS_MAPPING.put("uuid:ae876087-435d-11dd-b505-00145e5790ea", 
                new ObjectPidsPath("uuid:ae876087-435d-11dd-b505-00145e5790ea"));

        // volume
        ImportDataPrepare.PATHS_MAPPING.put("uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6", 
                new ObjectPidsPath("uuid:ae876087-435d-11dd-b505-00145e5790ea","uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6"));

        // item - cisla
        ImportDataPrepare.PATHS_MAPPING.put("uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6", 
                new ObjectPidsPath("uuid:ae876087-435d-11dd-b505-00145e5790ea",
                                    "uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6",
                                    "uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6"));

        ImportDataPrepare.PATHS_MAPPING.put("uuid:983a4660-938d-11dc-913a-000d606f5dc6", 
                new ObjectPidsPath("uuid:ae876087-435d-11dd-b505-00145e5790ea",
                                    "uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6",
                                    "uuid:983a4660-938d-11dc-913a-000d606f5dc6"));

    
        ImportDataPrepare.PATHS_MAPPING.put("uuid:53255e00-938a-11dc-8b44-000d606f5dc6", 
                new ObjectPidsPath("uuid:ae876087-435d-11dd-b505-00145e5790ea",
                                    "uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6",
                                    "uuid:53255e00-938a-11dc-8b44-000d606f5dc6"));
        
        
        // stranky
        ImportDataPrepare.PATHS_MAPPING.put("uuid:b38eba10-91f6-11dc-9eec-000d606f5dc6", 
                new ObjectPidsPath("uuid:ae876087-435d-11dd-b505-00145e5790ea",
                                    "uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6",
                                    "uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6",
                                    "uuid:b38eba10-91f6-11dc-9eec-000d606f5dc6"));

        ImportDataPrepare.PATHS_MAPPING.put("uuid:94a3ed60-92d6-11dc-beb4-000d606f5dc6", 
                new ObjectPidsPath("uuid:ae876087-435d-11dd-b505-00145e5790ea",
                                    "uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6",
                                    "uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6",
                                    "uuid:94a3ed60-92d6-11dc-beb4-000d606f5dc6"));

        ImportDataPrepare.PATHS_MAPPING.put("uuid:b3987e10-91f6-11dc-96f6-000d606f5dc6", 
                new ObjectPidsPath("uuid:ae876087-435d-11dd-b505-00145e5790ea",
                                    "uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6",
                                    "uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6",
                                    "uuid:b3987e10-91f6-11dc-96f6-000d606f5dc6"));

        ImportDataPrepare.PATHS_MAPPING.put("uuid:94a3ed60-92d6-11dc-93df-000d606f5dc6", 
                new ObjectPidsPath("uuid:ae876087-435d-11dd-b505-00145e5790ea",
                                    "uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6",
                                    "uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6",
                                    "uuid:94a3ed60-92d6-11dc-93df-000d606f5dc6"));

        ImportDataPrepare.PATHS_MAPPING.put("uuid:b3a21b00-91f6-11dc-b8b2-000d606f5dc6", 
                new ObjectPidsPath("uuid:ae876087-435d-11dd-b505-00145e5790ea",
                                    "uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6",
                                    "uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6",
                                    "uuid:b3a21b00-91f6-11dc-b8b2-000d606f5dc6"));

        ImportDataPrepare.PATHS_MAPPING.put("uuid:94a68570-92d6-11dc-be5a-000d606f5dc6", 
                new ObjectPidsPath("uuid:ae876087-435d-11dd-b505-00145e5790ea",
                                    "uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6",
                                    "uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6",
                                    "uuid:94a68570-92d6-11dc-be5a-000d606f5dc6"));

        ImportDataPrepare.PATHS_MAPPING.put("uuid:b3ab42c0-91f6-11dc-b83a-000d606f5dc6", 
                new ObjectPidsPath("uuid:ae876087-435d-11dd-b505-00145e5790ea",
                                    "uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6",
                                    "uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6",
                                    "uuid:b3ab42c0-91f6-11dc-b83a-000d606f5dc6"));

        ImportDataPrepare.PATHS_MAPPING.put("uuid:94a8f670-92d6-11dc-9104-000d606f5dc6", 
                new ObjectPidsPath("uuid:ae876087-435d-11dd-b505-00145e5790ea",
                                    "uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6",
                                    "uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6",
                                    "uuid:94a8f670-92d6-11dc-9104-000d606f5dc6"));

        ImportDataPrepare.PATHS_MAPPING.put("uuid:b3d89450-91f6-11dc-98d0-000d606f5dc6", 
                new ObjectPidsPath("uuid:ae876087-435d-11dd-b505-00145e5790ea",
                                    "uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6",
                                    "uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6",
                                    "uuid:b3d89450-91f6-11dc-98d0-000d606f5dc6"));

        ImportDataPrepare.PATHS_MAPPING.put("uuid:94b4b640-92d6-11dc-b0a2-000d606f5dc6", 
                new ObjectPidsPath("uuid:ae876087-435d-11dd-b505-00145e5790ea",
                                    "uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6",
                                    "uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6",
                                    "uuid:94b4b640-92d6-11dc-b0a2-000d606f5dc6"));

        ImportDataPrepare.PATHS_MAPPING.put("uuid:b3e23140-91f6-11dc-a406-000d606f5dc6", 
                new ObjectPidsPath("uuid:ae876087-435d-11dd-b505-00145e5790ea",
                                    "uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6",
                                    "uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6",
                                    "uuid:b3e23140-91f6-11dc-a406-000d606f5dc6"));

        ImportDataPrepare.PATHS_MAPPING.put("uuid:94b74e50-92d6-11dc-8465-000d606f5dc6", 
                new ObjectPidsPath("uuid:ae876087-435d-11dd-b505-00145e5790ea",
                                    "uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6",
                                    "uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6",
                                    "uuid:94b74e50-92d6-11dc-8465-000d606f5dc6"));

        ImportDataPrepare.PATHS_MAPPING.put("uuid:b3ebce30-91f6-11dc-812e-000d606f5dc6", 
                new ObjectPidsPath("uuid:ae876087-435d-11dd-b505-00145e5790ea",
                                    "uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6",
                                    "uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6",
                                    "uuid:b3ebce30-91f6-11dc-812e-000d606f5dc6"));

        ImportDataPrepare.PATHS_MAPPING.put("uuid:94b9bf50-92d6-11dc-82e2-000d606f5dc6", 
                new ObjectPidsPath("uuid:ae876087-435d-11dd-b505-00145e5790ea",
                                    "uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6",
                                    "uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6",
                                    "uuid:94b9bf50-92d6-11dc-82e2-000d606f5dc6"));

        ImportDataPrepare.PATHS_MAPPING.put("uuid:b3f2fa20-91f6-11dc-9cd2-000d606f5dc6", 
                new ObjectPidsPath("uuid:ae876087-435d-11dd-b505-00145e5790ea",
                                    "uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6",
                                    "uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6",
                                    "uuid:b3f2fa20-91f6-11dc-9cd2-000d606f5dc6"));

        ImportDataPrepare.PATHS_MAPPING.put("uuid:94bbe230-92d6-11dc-924c-000d606f5dc6", 
                new ObjectPidsPath("uuid:ae876087-435d-11dd-b505-00145e5790ea",
                                    "uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6",
                                    "uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6",
                                    "uuid:94bbe230-92d6-11dc-924c-000d606f5dc6"));

        
        ImportDataPrepare.PATHS_MAPPING.put("uuid:b3b4dfb0-91f6-11dc-8f6a-000d606f5dc6", 
                new ObjectPidsPath("uuid:ae876087-435d-11dd-b505-00145e5790ea",
                                    "uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6",
                                    "uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6",
                                    "uuid:b3b4dfb0-91f6-11dc-8f6a-000d606f5dc6"));

        ImportDataPrepare.PATHS_MAPPING.put("uuid:94ab8e80-92d6-11dc-8d2c-000d606f5dc6", 
                new ObjectPidsPath("uuid:ae876087-435d-11dd-b505-00145e5790ea",
                                    "uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6",
                                    "uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6",
                                    "uuid:94ab8e80-92d6-11dc-8d2c-000d606f5dc6"));


        ImportDataPrepare.PATHS_MAPPING.put("uuid:b3bc0ba0-91f6-11dc-b29a-000d606f5dc6", 
                new ObjectPidsPath("uuid:ae876087-435d-11dd-b505-00145e5790ea",
                                    "uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6",
                                    "uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6",
                                    "uuid:b3bc0ba0-91f6-11dc-b29a-000d606f5dc6"));

        ImportDataPrepare.PATHS_MAPPING.put("uuid:94ad8a50-92d6-11dc-96c7-000d606f5dc6", 
                new ObjectPidsPath("uuid:ae876087-435d-11dd-b505-00145e5790ea",
                                    "uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6",
                                    "uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6",
                                    "uuid:94ad8a50-92d6-11dc-96c7-000d606f5dc6"));

        ImportDataPrepare.PATHS_MAPPING.put("uuid:b3c5a890-91f6-11dc-aaef-000d606f5dc6", 
                new ObjectPidsPath("uuid:ae876087-435d-11dd-b505-00145e5790ea",
                                    "uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6",
                                    "uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6",
                                    "uuid:b3c5a890-91f6-11dc-aaef-000d606f5dc6"));

        ImportDataPrepare.PATHS_MAPPING.put("uuid:94b02260-92d6-11dc-be98-000d606f5dc6", 
                new ObjectPidsPath("uuid:ae876087-435d-11dd-b505-00145e5790ea",
                                    "uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6",
                                    "uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6",
                                    "uuid:94b02260-92d6-11dc-be98-000d606f5dc6"));

        ImportDataPrepare.PATHS_MAPPING.put("uuid:b3cf6c90-91f6-11dc-93a2-000d606f5dc6", 
                new ObjectPidsPath("uuid:ae876087-435d-11dd-b505-00145e5790ea",
                                    "uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6",
                                    "uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6",
                                    "uuid:b3cf6c90-91f6-11dc-93a2-000d606f5dc6"));

        ImportDataPrepare.PATHS_MAPPING.put("uuid:94b2ba70-92d6-11dc-a884-000d606f5dc6", 
                new ObjectPidsPath("uuid:ae876087-435d-11dd-b505-00145e5790ea",
                                    "uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6",
                                    "uuid:b32d1210-91f6-11dc-94d0-000d606f5dc6",
                                    "uuid:94b2ba70-92d6-11dc-a884-000d606f5dc6"));

        
        
        ImportDataPrepare.PATHS_MAPPING.put("uuid:b4204bb0-91f6-11dc-8258-000d606f5dc6", 
                new ObjectPidsPath("uuid:ae876087-435d-11dd-b505-00145e5790ea",
                                    "uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6",
                                    "uuid:53255e00-938a-11dc-8b44-000d606f5dc6",
                                    "uuid:b4204bb0-91f6-11dc-8258-000d606f5dc6"));

        ImportDataPrepare.PATHS_MAPPING.put("uuid:94ccab10-92d6-11dc-b58e-000d606f5dc6", 
                new ObjectPidsPath("uuid:ae876087-435d-11dd-b505-00145e5790ea",
                                    "uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6",
                                    "uuid:53255e00-938a-11dc-8b44-000d606f5dc6",
                                    "uuid:94ccab10-92d6-11dc-b58e-000d606f5dc6"));

        ImportDataPrepare.PATHS_MAPPING.put("uuid:b4297370-91f6-11dc-878b-000d606f5dc6", 
                new ObjectPidsPath("uuid:ae876087-435d-11dd-b505-00145e5790ea",
                                    "uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6",
                                    "uuid:53255e00-938a-11dc-8b44-000d606f5dc6",
                                    "uuid:b4297370-91f6-11dc-878b-000d606f5dc6"));

        ImportDataPrepare.PATHS_MAPPING.put("uuid:94cf1c10-92d6-11dc-bb1b-000d606f5dc6", 
                new ObjectPidsPath("uuid:ae876087-435d-11dd-b505-00145e5790ea",
                                    "uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6",
                                    "uuid:53255e00-938a-11dc-8b44-000d606f5dc6",
                                    "uuid:94cf1c10-92d6-11dc-bb1b-000d606f5dc6"));

        ImportDataPrepare.PATHS_MAPPING.put("uuid:b4307850-91f6-11dc-9b9d-000d606f5dc6", 
                new ObjectPidsPath("uuid:ae876087-435d-11dd-b505-00145e5790ea",
                                    "uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6",
                                    "uuid:53255e00-938a-11dc-8b44-000d606f5dc6",
                                    "uuid:b4307850-91f6-11dc-9b9d-000d606f5dc6"));

        ImportDataPrepare.PATHS_MAPPING.put("uuid:94d1b420-92d6-11dc-8e76-000d606f5dc6", 
                new ObjectPidsPath("uuid:ae876087-435d-11dd-b505-00145e5790ea",
                                    "uuid:b2f18fb0-91f6-11dc-9f72-000d606f5dc6",
                                    "uuid:53255e00-938a-11dc-8b44-000d606f5dc6",
                                    "uuid:94d1b420-92d6-11dc-8e76-000d606f5dc6"));


    }

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException, TransformerException {
        File folder = new File("src/test/resources/org/kramerius/fedora/res");
        File[] lfiles = folder.listFiles(new FileFilter() {
            
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".nonconsistent.xml");
            }
        });
        if (lfiles != null) {
            for (File file : lfiles) {
                Document doc = XMLUtils.parseDocument(new FileReader(file),true);
                boolean changed = false;
                Stack<Element> elms = new Stack<Element>();
                elms.push(doc.getDocumentElement());
                while(!elms.isEmpty()) {
                    Element top = elms.pop();
                    if (top.getLocalName().equals("hasPage")) {
                        changed = true;
                        Node clonedNode = top.cloneNode(false);
                        doc.adoptNode(clonedNode);
                        Attr clonedHasPageAttr = top.getAttributeNode( "rdf:resource");
                        clonedHasPageAttr.setValue(clonedHasPageAttr.getValue()+"notexist");
                        top.getParentNode().appendChild(clonedNode);
                    }

                    NodeList nList = top.getChildNodes();
                    for (int i = 0, ll= nList.getLength(); i < ll; i++) {
                        Node n = nList.item(i);
                        if (n.getNodeType() == Node.ELEMENT_NODE) {
                            elms.push((Element) n);
                        }
                    }
                    
                }
                if (changed) {
                    
                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    Transformer transformer = transformerFactory.newTransformer();
                    transformer.setOutputProperty("indent", "yes");
                    transformer.transform(new DOMSource(doc), new StreamResult(file));
                    System.out.println(file.getAbsolutePath());
                }
            }
        }
    }

    public static void createNoncosistent() throws IOException {
        File folder = new File("src/test/resources/org/kramerius/fedora/res");
        File[] lfiles = folder.listFiles(new FileFilter() {
            
            @Override
            public boolean accept(File pathname) {
                if (pathname.getName().endsWith(".mods.xml")) return false;
                if (pathname.getName().endsWith(".dc.xml")) return false;
                return pathname.getName().endsWith(".xml");
            }
        });
        if (lfiles != null) {
            for (File oldFile : lfiles) {
                String oldName = oldFile.getName();
                File nFile = new File(folder, oldName.substring(0, oldName.length()-4)+".nonconsistent.xml");
                System.out.println("new file is '"+nFile.getAbsolutePath()+"'");
                IOUtils.copyFiles(oldFile, nFile);
            }
        }
    }
}
