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
package cz.incad.kramerius.rest.api.replication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Base64OutputStream;

import cz.incad.kramerius.utils.IOUtils;

/**
 * Stream dedicated for transfering raw FOXML enveloped in JSON format
 * @author pavels
 */
public class FOXMLStreamOutput implements StreamingOutput {

    private byte[] foxml;

    public FOXMLStreamOutput(byte[] foxml) {
        super();
        this.foxml = foxml;
    }

    @Override
    public void write(OutputStream os) throws IOException, WebApplicationException {
        os.write("{'raw':'".getBytes("UTF-8"));
        System.out.println(new String(this.foxml,"UTF-8"));
        Base64OutputStream bos64os = new Base64OutputStream(os,true,76,"|".getBytes("UTF-8"));
        IOUtils.copyStreams(new ByteArrayInputStream(this.foxml), bos64os);
        bos64os.flush();
        os.write("'}".getBytes("UTF-8"));
    }
    
    
}
