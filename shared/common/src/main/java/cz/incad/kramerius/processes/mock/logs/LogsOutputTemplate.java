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
package cz.incad.kramerius.processes.mock.logs;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Writer;

import cz.incad.kramerius.processes.LRProcess;
import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.processes.template.ProcessOutputTemplate;

public class LogsOutputTemplate implements ProcessOutputTemplate {

    public static String TEMPLATE_ID="logs_template";
    
    @Override
    public void renderOutput(LRProcess lrProcess, LRProcessDefinition definition, Writer writer) throws IOException {
//        RandomAccessFile raFile = lrProcess.getErrorProcessRAFile();
//        String standardStreamFolder = definition.getStandardStreamFolder();
        
        StringBuilder builder = new StringBuilder("<html><head></head><body>" +
        		"<table>" +
        		"<tr><td>TEST H1</td><td>TEST H2</td><td>TEST H3</td></tr>" +
                "<tr><td>TEST H1-abra</td><td>TEST H2-ka</td><td>TEST H3-dabra</td></tr>" +
        		"</table>" +
        		"</body></html>");
        writer.write(builder.toString());
        
    }

    @Override
    public void renderName(LRProcess lrProcess, LRProcessDefinition definition, Writer writer) throws IOException {
        writer.write("-name-jmeno-name-");
    }

    @Override
    public String getOutputTemplateId() {
        return TEMPLATE_ID;
    }
}
