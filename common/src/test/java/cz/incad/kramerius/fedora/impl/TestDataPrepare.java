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

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.utils.XMLUtils;

public class TestDataPrepare {

    public static final String[] DROBNUSTKY_UUIDS = "0eaa6730-9068-11dd-97de-000d606f5dc6,4a7c2e50-af36-11dd-9643-000d606f5dc6,4a7ec660-af36-11dd-a782-000d606f5dc6,4a79bd50-af36-11dd-a60c-000d606f5dc6,4a8a8630-af36-11dd-ae9c-000d606f5dc6,4a8cf730-af36-11dd-ae88-000d606f5dc6,4a80c230-af36-11dd-ace4-000d606f5dc6,4a835a40-af36-11dd-b951-000d606f5dc6,4a85f250-af36-11dd-8535-000d606f5dc6,430d7f60-b03b-11dd-82fa-000d606f5dc6,4308eb80-b03b-11dd-a0f6-000d606f5dc6,431e4840-b03b-11dd-8818-000d606f5dc6,43101770-b03b-11dd-8673-000d606f5dc6,4314ab50-b03b-11dd-89db-000d606f5dc6,43171c50-b03b-11dd-b0c2-000d606f5dc6,4319b460-b03b-11dd-83ca-000d606f5dc6,4320e050-b03b-11dd-9b4a-000d606f5dc6".split(",");

    public static void drobnustkyRelsExt(FedoraAccess fa) throws IOException, ParserConfigurationException, SAXException {
        for (int i = 0; i < DROBNUSTKY_UUIDS.length; i++) {
            String path = "/cz/incad/kramerius/fedora/res/"+DROBNUSTKY_UUIDS[i]+".xml";
            InputStream resStream = FedoraAccessImpl.class.getResourceAsStream(path);
            expect(fa.getRelsExt(DROBNUSTKY_UUIDS[i])).andReturn(XMLUtils.parseDocument(resStream, true));
        }        
    }

    public static void drobnustkyWithIMGFULL(FedoraAccess fa) throws IOException {
        for (int i = 0; i < DROBNUSTKY_UUIDS.length; i++) {
            if (DROBNUSTKY_UUIDS[i].equals("0eaa6730-9068-11dd-97de-000d606f5dc6")) {
                expect(fa.isImageFULLAvailable(DROBNUSTKY_UUIDS[i])).andReturn(false);
            } else {
                expect(fa.isImageFULLAvailable(DROBNUSTKY_UUIDS[i])).andReturn(true);
            }
        }
    }

    public static void drobnustkyWithOutIMGFULL(FedoraAccess fa) throws IOException {
        for (int i = 0; i < DROBNUSTKY_UUIDS.length; i++) {
            expect(fa.isImageFULLAvailable(DROBNUSTKY_UUIDS[i])).andReturn(false);
        }
    }

}
