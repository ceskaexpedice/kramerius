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
package cz.incad.kramerius.processes.template;

import java.io.IOException;
import java.io.Writer;

import cz.incad.kramerius.processes.LRProcess;
import cz.incad.kramerius.processes.LRProcessDefinition;

/**
 * Represents output template for process
 * @author pavels
 */
public interface ProcessOutputTemplate {

    /**
     * Renders process output
     * @param lrProcess LR process
     * @param definition Definition
     * @param writer Output writer
     * @throws IOException
     */
    public void renderOutput(LRProcess lrProcess, LRProcessDefinition definition, Writer writer) throws IOException;
    
    
    /**
     * Render name of output template
     * @param lrProcess Long running process
     * @param definition Process definition
     * @param writer Output writer
     * @throws IOException
     */
    public void renderName(LRProcess lrProcess, LRProcessDefinition definition, Writer writer) throws IOException;
    
    /**
     * Returns identification of this template
     * @return
     */
    public String getOutputTemplateId();
}
