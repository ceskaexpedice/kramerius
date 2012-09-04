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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.antlr.stringtemplate.StringTemplate;

import cz.incad.kramerius.document.model.DCConent;
import cz.incad.kramerius.utils.StringUtils;

/**
 * Stream output for object of descriptions
 * @author pavels
 */
public class DescriptionStreamOutput implements StreamingOutput {

    private DCConent dcConent;
    
    
    public DescriptionStreamOutput(DCConent dcConent) {
        super();
        this.dcConent = dcConent;
    }


    
    private static String formatArray(List<String> strs) {
        StringTemplate template = new StringTemplate("[$array:{itm|'$itm$'};separator=\",\"$]");
        template.setAttribute("array", strs);
        return template.toString();
    }
    

    @Override
    public void write(OutputStream os) throws IOException, WebApplicationException {
        os.write("{".getBytes("UTF-8"));
        
        os.write("'identifiers':".getBytes("UTF-8")); 
        os.write(formatArray(Arrays.asList(this.dcConent.getIdentifiers())).getBytes("UTF-8")); 
        os.write(",'publishers':".getBytes("UTF-8")); 
        String publishers = formatArray(Arrays.asList(this.dcConent.getPublishers()));
        System.out.println(publishers);
        os.write(publishers.getBytes("UTF-8")); 
        os.write(",'creators':".getBytes("UTF-8")); 

        String array = formatArray(Arrays.asList(this.dcConent.getCreators()));
        System.out.println(array);
        
        os.write(array.getBytes("UTF-8")); 

        os.write(",'title':'".getBytes("UTF-8")); 
        os.write((dcConent.getTitle()+"'").getBytes("UTF-8")); 
        os.write(",'type':'".getBytes("UTF-8")); 
        os.write((dcConent.getType()+"'").getBytes("UTF-8")); 
        os.write(",'date':'".getBytes("UTF-8")); 
        os.write((dcConent.getDate()+"'").getBytes("UTF-8")); 

        
        os.write("}".getBytes("UTF-8"));
    }

}
