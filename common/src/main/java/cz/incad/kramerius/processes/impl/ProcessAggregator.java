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
package cz.incad.kramerius.processes.impl;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Arrays;

import cz.incad.kramerius.processes.utils.ProcessUtils;

/**
 * Special process for aggregation
 * @author pavels
 */
public class ProcessAggregator {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ProcessAggregator.class.getName());
    
    public static void main(String[] args) throws Exception {
        String def = args[0];
        String[] processDefsParams = Arrays.copyOfRange(args, 1, args.length);  
       
        for (int i = 0; i < processDefsParams.length; i++) {
            LOGGER.info("starting process ("+def+" with params "+Arrays.asList(processDefsParams[i]));
            String encodedParams =  URLEncoder.encode(processDefsParams[i], "UTF-8");
            
            ProcessUtils.startProcess(def, encodedParams);
        }
        
        //TODO: I18N
        ProcessStarter.updateName("Davkove spusteny process ["+def+"]");
    }
}
