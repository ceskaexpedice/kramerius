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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.antlr.stringtemplate.StringTemplate;

import cz.incad.kramerius.ObjectPidsPath;

/**
 * List of pids dedicated for replication
 * @author pavels
 */
public class PIDListStreamOutput implements StreamingOutput {

    private List<String> pidList;
    private List<ObjectPidsPath> paths;
    
    public PIDListStreamOutput(List<String> pidList, List<ObjectPidsPath> path) {
        super();
        this.pidList = pidList;
        this.paths = path;
    }

    @Override
    public void write(OutputStream os) throws IOException, WebApplicationException {
        os.write("{'pids':[".getBytes("UTF-8"));
        for (int i = 0,ll=this.pidList.size(); i < ll; i++) {
            StringBuilder builder = new StringBuilder();
            if (i > 0) builder.append(",");
            builder.append('\'').append(pidList.get(i)).append('\'');
            os.write(builder.toString().getBytes("UTF-8"));
        }
        os.write(("], 'paths':["+toPathArray(this.paths)+"]}").getBytes("UTF-8"));
    }
    
    static String toPathArray(List<ObjectPidsPath> paths) {
        String stringTemplate = "$paths:{p|'$p.pathFromRootToLeaf;separator=\"/\"$'};separator=\",\"$";
        StringTemplate template = new StringTemplate(stringTemplate);
        template.setAttribute("paths", paths);
        return template.toString();
    }

}
