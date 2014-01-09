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
package cz.incad.kramerius.rest.api.utils.dbfilter;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

import cz.incad.kramerius.rest.api.processes.LRResource;

public class DateConvert implements Convert {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(DateConvert.class.getName());
    
    @Override
    public Object convert(String str) {
        try {
            Date parsed = LRResource.FORMAT.parse(str);
            return new Timestamp(parsed.getTime());
        } catch (ParseException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
        return str;
    }

    
}
