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
/**
 * 
 */
package org.kramerius.processes.utils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;

/**
 * @author pavels
 */
public class BasicStringTemplateGroup {
    
    private static StringTemplateGroup BASIC_ST_GROUP = null;

    static void initialization() throws UnsupportedEncodingException {
        InputStream is = BasicStringTemplateGroup.class.getClassLoader().getResourceAsStream("org/kramerius/processes/input/processes.stg");
        BASIC_ST_GROUP = new StringTemplateGroup(new InputStreamReader(is, "UTF-8"),DefaultTemplateLexer.class);
    }
    
    public synchronized static StringTemplateGroup getBasicProcessesGroup() {
        try {
            if (BASIC_ST_GROUP == null) {
                initialization();
            }
            return BASIC_ST_GROUP;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
