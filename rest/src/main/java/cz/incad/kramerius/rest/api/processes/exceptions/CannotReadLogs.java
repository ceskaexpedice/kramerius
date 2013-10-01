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
package cz.incad.kramerius.rest.api.processes.exceptions;

import java.io.IOException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.sun.jersey.api.Responses;

import cz.incad.kramerius.rest.api.exceptions.AbstractRestJSONException;

/**
 * Logs are not readable
 * @author pavels
 */
public class CannotReadLogs extends AbstractRestJSONException{

    public CannotReadLogs(String message) {
        super(message,Responses.PRECONDITION_FAILED);
    }

    public CannotReadLogs(String message, IOException ex) {
        super(message,ex,Responses.PRECONDITION_FAILED);
    }

}
