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
package cz.incad.kramerius.fedora.impl;

import static org.easymock.EasyMock.expect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import org.easymock.EasyMock;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

public class DataPrepare {

    public static final String[] DROBNUSTKY_PIDS = "uuid:0eaa6730-9068-11dd-97de-000d606f5dc6,uuid:4a7c2e50-af36-11dd-9643-000d606f5dc6,uuid:4a7ec660-af36-11dd-a782-000d606f5dc6,uuid:4a79bd50-af36-11dd-a60c-000d606f5dc6,uuid:4a8a8630-af36-11dd-ae9c-000d606f5dc6,uuid:4a8cf730-af36-11dd-ae88-000d606f5dc6,uuid:4a80c230-af36-11dd-ace4-000d606f5dc6,uuid:4a835a40-af36-11dd-b951-000d606f5dc6,uuid:4a85f250-af36-11dd-8535-000d606f5dc6,uuid:430d7f60-b03b-11dd-82fa-000d606f5dc6,uuid:4308eb80-b03b-11dd-a0f6-000d606f5dc6,uuid:431e4840-b03b-11dd-8818-000d606f5dc6,uuid:43101770-b03b-11dd-8673-000d606f5dc6,uuid:4314ab50-b03b-11dd-89db-000d606f5dc6,uuid:43171c50-b03b-11dd-b0c2-000d606f5dc6,uuid:4319b460-b03b-11dd-83ca-000d606f5dc6,uuid:4320e050-b03b-11dd-9b4a-000d606f5dc6".split(",");

    
    public static final String MUJ_ZIVOT_S_HITLEREM = "uuid:3ee97ce8-e548-11e0-9867-005056be0007";
    
    
    public static InputStream datastreams33() {
        String path = "/cz/incad/kramerius/fedora/res/datastreams_3_3";
        InputStream resStream = FedoraAccessImpl.class.getResourceAsStream(path);
        return resStream;
    }

    public static InputStream datastreams34() {
        String path = "/cz/incad/kramerius/fedora/res/datastreams_3_4";
        InputStream resStream = FedoraAccessImpl.class.getResourceAsStream(path);
        return resStream;
    }

    public static InputStream dsProfile33() {
        String path = "/cz/incad/kramerius/fedora/res/dsprofile_3_3";
        InputStream resStream = FedoraAccessImpl.class.getResourceAsStream(path);
        return resStream;
    }

    public static InputStream dsProfile34() {
        String path = "/cz/incad/kramerius/fedora/res/dsprofile_3_4";
        InputStream resStream = FedoraAccessImpl.class.getResourceAsStream(path);
        return resStream;
    }

    public static InputStream fedoraProfile33() {
        String path = "/cz/incad/kramerius/fedora/res/describe_3_3";
        InputStream resStream = FedoraAccessImpl.class.getResourceAsStream(path);
        return resStream;
    }

    public static InputStream fedoraProfile34() {
        String path = "/cz/incad/kramerius/fedora/res/describe_3_4";
        InputStream resStream = FedoraAccessImpl.class.getResourceAsStream(path);
        return resStream;
    }

    // DROBNUSTKY
    public static void drobnustkyRelsExt(FedoraAccess fa) throws IOException, ParserConfigurationException, SAXException, LexerException {
        for (int i = 0; i < DROBNUSTKY_PIDS.length; i++) {
            PIDParser pidParser = new PIDParser(DROBNUSTKY_PIDS[i]);
            pidParser.objectPid();
            String objectId = pidParser.getObjectId();
            
            String path = "/cz/incad/kramerius/fedora/res/"+objectId+".xml";
            InputStream resStream = FedoraAccessImpl.class.getResourceAsStream(path);
            expect(fa.getRelsExt(DROBNUSTKY_PIDS[i])).andReturn(XMLUtils.parseDocument(resStream, true)).anyTimes();
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

    public static void drobnustkyDCS(FedoraAccess fa) throws IOException, LexerException, ParserConfigurationException, SAXException {
        for (int i = 0; i < DROBNUSTKY_PIDS.length; i++) {
            dc(fa,DROBNUSTKY_PIDS[i]);
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

    
    public static void relsExt(FedoraAccess fa, String pid) throws IOException, ParserConfigurationException, SAXException, LexerException {
        PIDParser pidParser = new PIDParser(pid);
        pidParser.objectPid();
        String path = "/cz/incad/kramerius/fedora/res/"+pidParser.getObjectId()+".xml";
        InputStream resStream = FedoraAccessImpl.class.getResourceAsStream(path);
        expect(fa.getRelsExt(pid)).andReturn(XMLUtils.parseDocument(resStream, true));
    }
    
    public static void dc(FedoraAccess fa, String pid) throws LexerException, IOException, ParserConfigurationException, SAXException {
        PIDParser pidParser = new PIDParser(pid);
        pidParser.objectPid();

        String path = "/cz/incad/kramerius/fedora/res/"+pidParser.getObjectId()+".dc.xml";
        InputStream resStream = FedoraAccessImpl.class.getResourceAsStream(path);
        Document document = XMLUtils.parseDocument(resStream, true);
        EasyMock.expect(fa.getDC(pid)).andReturn(document).anyTimes();
    }
    
    public static void mods(FedoraAccess fa, String pid) throws LexerException, ParserConfigurationException, SAXException, IOException {
        PIDParser pidParser = new PIDParser(pid);
        pidParser.objectPid();

        String path = "/cz/incad/kramerius/fedora/res/"+pidParser.getObjectId()+".mods.xml";
        InputStream resStream = FedoraAccessImpl.class.getResourceAsStream(path);
        Document document = XMLUtils.parseDocument(resStream, true);
        EasyMock.expect(fa.getBiblioMods(pid)).andReturn(document).anyTimes();
    }
    
    public static void dataStreams(FedoraAccess fa, String pid) throws IOException, ParserConfigurationException, SAXException, LexerException {
        PIDParser pidParser = new PIDParser(pid);
        pidParser.objectPid();

        String path = "/cz/incad/kramerius/fedora/res/"+pidParser.getObjectId()+".datastreams.xml";
        InputStream resStream = FedoraAccessImpl.class.getResourceAsStream(path);
        expect(fa.getFedoraDataStreamsList(pid)).andReturn(resStream);
    }

//    uuid:4308eb80-b03b-11dd-a0f6-000d606f5dc6
//    uuid:4a79bd50-af36-11dd-a60c-000d606f5dc6
//    uuid:430d7f60-b03b-11dd-82fa-000d606f5dc6
//    uuid:4a7c2e50-af36-11dd-9643-000d606f5dc6
//
//    uuid:43101770-b03b-11dd-8673-000d606f5dc6
//    uuid:4a7ec660-af36-11dd-a782-000d606f5dc6
//    uuid:4314ab50-b03b-11dd-89db-000d606f5dc6
//    uuid:4a80c230-af36-11dd-ace4-000d606f5dc6
//    uuid:43171c50-b03b-11dd-b0c2-000d606f5dc6
//    uuid:4a835a40-af36-11dd-b951-000d606f5dc6
//    uuid:4319b460-b03b-11dd-83ca-000d606f5dc6
//    uuid:4a85f250-af36-11dd-8535-000d606f5dc6
//    uuid:431e4840-b03b-11dd-8818-000d606f5dc6
//
//    uuid:4a8a8630-af36-11dd-ae9c-000d606f5dc6
//    uuid:4320e050-b03b-11dd-9b4a-000d606f5dc6
//    uuid:4a8cf730-af36-11dd-ae88-000d606f5dc6

    
    public static void main(String[] args) throws IOException, LexerException {
        String[] pids = new String[] {
                "uuid:0eaa6730-9068-11dd-97de-000d606f5dc6",
              "uuid:4308eb80-b03b-11dd-a0f6-000d606f5dc6",
              "uuid:4a79bd50-af36-11dd-a60c-000d606f5dc6",
              "uuid:430d7f60-b03b-11dd-82fa-000d606f5dc6",
              "uuid:4a7c2e50-af36-11dd-9643-000d606f5dc6",
              "uuid:43101770-b03b-11dd-8673-000d606f5dc6",
              "uuid:4a7ec660-af36-11dd-a782-000d606f5dc6",
              "uuid:4314ab50-b03b-11dd-89db-000d606f5dc6",
              "uuid:4a80c230-af36-11dd-ace4-000d606f5dc6",
              "uuid:43171c50-b03b-11dd-b0c2-000d606f5dc6",
              "uuid:4a835a40-af36-11dd-b951-000d606f5dc6",
              "uuid:4319b460-b03b-11dd-83ca-000d606f5dc6",
              "uuid:4a85f250-af36-11dd-8535-000d606f5dc6",
              "uuid:431e4840-b03b-11dd-8818-000d606f5dc6",
              "uuid:4a8a8630-af36-11dd-ae9c-000d606f5dc6",
              "uuid:4320e050-b03b-11dd-9b4a-000d606f5dc6",
              "uuid:4a8cf730-af36-11dd-ae88-000d606f5dc6"
        };
        for (int i = 0; i < pids.length; i++) {
            String pid = pids[i];
            URL url = new URL("http://localhost:8080/fedora/objects/"+pid+"/datastreams/BIBLIO_MODS/content");
            InputStream stream = url.openConnection().getInputStream();
            PIDParser pidParser = new PIDParser(pid);
            pidParser.objectPid();
            
            File file = new File(pidParser.getObjectId()+".mods.xml");
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);

            IOUtils.copyStreams(stream, fos);
            
            fos.close();
        }

    }
    
}
