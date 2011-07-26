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
import java.util.Arrays;

import cz.incad.kramerius.processes.utils.ProcessUtils;

/**
 * Special process for aggregation
 * @author pavels
 */
public class ProcessAggregator {

    public static void main(String[] args) throws IOException {
        String token = System.getProperty(ProcessStarter.TOKEN_KEY);
        // params parsing... 
        
        String def = args[0];
        String[] processDefsParams = Arrays.copyOfRange(args, 1, args.length);
        
        for (int i = 0; i < processDefsParams.length; i++) {
            ProcessUtils.startProcess(def, processDefsParams[i]);
        }

        //TODO: I18N
        ProcessStarter.updateName("Davkove spusteny process ["+def+"]");
    }
}
