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

import java.io.StringReader;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import cz.incad.kramerius.processes.utils.ProcessUtils;
import cz.incad.kramerius.utils.params.ParamsLexer;
import cz.incad.kramerius.utils.params.ParamsParser;

/**
 * Special process for aggregation
 * @author pavels
 */
public class ProcessAggregator {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ProcessAggregator.class.getName());
    
    public static void main(String[] args) throws Exception {
       // String uuid = System.getProperty(ProcessStarter.UUID_KEY);

        String def = args[0];
        String[] processDefsParams = Arrays.copyOfRange(args, 1, args.length);  
       
        if (processDefsParams.length > 0) {
            String parameter = URLDecoder.decode(processDefsParams[0], "UTF-8");
            ParamsParser parser = new ParamsParser(new ParamsLexer(new StringReader(parameter)));
            List<Object> paramsList = parser.params();
            LOGGER.info("starting process ("+def+" with params "+paramsList);
            List<String> paramsStringList = paramsList.stream().map(Object::toString).collect(Collectors.toList());
            // TODO pepo ProcessUtils.startProcess(def, paramsStringList.toArray(new String[paramsStringList.size()]));
        }
        
        //TODO: pepo
        //ProcessStarter.updateName("Davkove spusteny process ["+def+"]");
    }
}
