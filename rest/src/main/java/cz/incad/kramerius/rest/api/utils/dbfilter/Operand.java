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

import cz.incad.kramerius.rest.api.utils.dbfilter.DbFilterUtils.FormalNamesMapping;
import cz.incad.kramerius.utils.database.SQLFilter;
import cz.incad.kramerius.utils.database.SQLFilter.TypesMapping;


public abstract class Operand {

    protected String sval;
    
    public Operand(String sval) {
        super();
        this.sval = sval;
    }


    
    public abstract String getValue();
    
    public abstract Convert getConvert();

    /*
    private static boolean isString(String val) {
        boolean simpleQuote =  val.startsWith("'") && val.endsWith("'");
        boolean doubleQuote = val.startsWith("\"") && val.endsWith("\"");
        return simpleQuote || doubleQuote;
    }*/

    public static Operand createOperand(String val, FormalNamesMapping mapping, SQLFilter.TypesMapping types) {
    	return new KeywordOperand(val);
    }
    
}
