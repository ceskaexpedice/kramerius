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
package cz.incad.kramerius.processes.mock.wizard;

import cz.incad.kramerius.processes.annotations.DefaultParameterValue;
import cz.incad.kramerius.processes.annotations.ParameterName;
import cz.incad.kramerius.processes.annotations.Process;

public class DefaultWizardMockLPProcess {
    
    public static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(DefaultWizardMockLPProcess.class.getName());
    
    @DefaultParameterValue("name")
    public static String DEFAULT_NAME = "DEFAULT";

    @DefaultParameterValue("value")
    public static String DEFAULT_VALUE = "VALUE";

    @Process
    public static void process(@ParameterName("name") String name, @ParameterName("value") String value) {
        LOGGER.info("parameter name is :"+name);
        LOGGER.info("parametr value is "+value);
    }
}
