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
package cz.incad.kramerius.rest.api.processes.exceptions;

import org.antlr.stringtemplate.StringTemplate;

/**
 * Utility class 
 * @author pavels
 */
public class ExceptionMessageBuilder {

    /**
     * Creates error message string 
     * @param status response status
     * @param message error message
     * @return Constructed message
     */
    public static String createErrorMessage(int status, String message) {
        StringTemplate template = new StringTemplate("{status:$status$,message:'$message$'}");
        template.setAttribute("message", message);
        return template.toString();
    }
}
