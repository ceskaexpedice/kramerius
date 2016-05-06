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
package cz.incad.kramerius.auth.thirdparty.shibb.rules.objects;

import javax.servlet.http.HttpServletRequest;

/**
 * Represents string literal
 * @see Value
 * @author pavels
 */
public class StringValue implements Value {
    
    private String value;

    public StringValue(String value) {
        super();
        //this.value = value.substring(1,value.length()-1);
        this.value = value;
    }

    @Override
    public String getValue(HttpServletRequest request) {
        return this.value;
    }

    @Override
    public boolean match(Value val, HttpServletRequest request) {
        return this.value.equals(val.getValue(request));
    }
    
    
    
}
