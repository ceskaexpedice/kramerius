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
package cz.incad.Kramerius.views;

import java.io.IOException;
import java.io.StringWriter;
import java.util.logging.Level;

import cz.incad.kramerius.processes.LRProcess;
import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.processes.template.OutputTemplateFactory;
import cz.incad.kramerius.processes.template.ProcessOutputTemplate;

public class OutputTemplateViewObjectItem {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(OutputTemplateViewObjectItem.class.getName());
    
    private String clz;
    private LRProcess lrProcess;
    private LRProcessDefinition lrProcessDefinition;
    private ProcessOutputTemplate outTemplate;
    
    
    public OutputTemplateViewObjectItem(String clz, OutputTemplateFactory factory, LRProcess lrProcess, LRProcessDefinition definition) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        super();
        this.clz = clz;
        this.outTemplate = factory.create(this.clz);
        this.lrProcess = lrProcess;
        this.lrProcessDefinition = definition;
    }
    
    
    public String getId() {
        return this.outTemplate.getOutputTemplateId();
    }
    
    public String getName() {
        try {
            StringWriter stWriter = new StringWriter();
            outTemplate.renderName(this.lrProcess, this.lrProcessDefinition, stWriter);
            return stWriter.toString();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return "";
        }
    }
}
