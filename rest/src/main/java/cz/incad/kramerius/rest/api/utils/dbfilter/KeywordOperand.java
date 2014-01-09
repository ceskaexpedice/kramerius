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

import java.util.HashMap;
import java.util.Map;

import cz.incad.kramerius.rest.api.processes.filter.BatchStateConvert;
import cz.incad.kramerius.rest.api.processes.filter.StateConvert;

public class KeywordOperand extends Operand{

    
    private static Map<String,String> MAPPING_KEYS = new HashMap<String, String>(); static {
        MAPPING_KEYS.put("state", "status");
        MAPPING_KEYS.put("batchState", "batch_status");
        MAPPING_KEYS.put("def", "defid");
        MAPPING_KEYS.put("uuid", "uuid");
        MAPPING_KEYS.put("userid", "loginname");
        MAPPING_KEYS.put("userFirstname", "firstname");
        MAPPING_KEYS.put("userSurname", "surname");
        MAPPING_KEYS.put("finished", "finished");
        MAPPING_KEYS.put("planned", "planned");
        MAPPING_KEYS.put("started", "started");
    }
    
    private static Map<String,Convert> MAPPING_CONVERTS = new HashMap<String, Convert>(); static {
        MAPPING_CONVERTS.put("state", new StateConvert());
        MAPPING_CONVERTS.put("batchState", new BatchStateConvert());
        MAPPING_CONVERTS.put("finished", new DateConvert());
        MAPPING_CONVERTS.put("started", new DateConvert());
        MAPPING_CONVERTS.put("planned", new DateConvert());
        MAPPING_CONVERTS.put("pid", new IntegerConvert());
    }
    
    private Convert convert;
    
    public KeywordOperand(String sval) {
        super(sval);
        this.convert = MAPPING_CONVERTS.get(sval.trim());
    }


    @Override
    public String getValue() {
        if (MAPPING_KEYS.containsKey(this.sval)) {
            return MAPPING_KEYS.get(this.sval);
        } else return this.sval;
    }


    @Override
    public Convert getConvert() {
        return this.convert;
    }
}
