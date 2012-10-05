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
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

/**
 * List of pids dedicated for replication
 * @author pavels
 */
public class PIDListStreamOutput implements StreamingOutput {

    private List<String> pidList;

    public PIDListStreamOutput(List<String> pidList) {
        super();
        this.pidList = pidList;
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
        os.write("]}".getBytes("UTF-8"));
    }
}
