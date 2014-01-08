/*
 * Copyright (C) 2013 Pavel Stastny
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import cz.incad.kramerius.utils.database.SQLFilter;
import cz.incad.kramerius.utils.database.SQLFilter.Op;
import cz.incad.kramerius.utils.database.SQLFilter.Tripple;
import cz.incad.kramerius.utils.database.SQLFilter.TypesMapping;

public class DbFilterUtils {

	/**
	 * transform table :  <formal_name> , <db_name>
	 * 
	 * Transform formal name to raw db name
	 * @param transformTable Transform table 
	 * @param formalName formal name
	 * @return
	 */
	public static String transform(FormalNamesMapping mapping, String formalName) {
		String rawName = mapping.lookUpRawName(formalName);
		if (rawName != null) return rawName;
		return formalName;
	}
	
	/**
	 * Supports only '=' operator
	 * @param filterMap
	 * @return
	 */
    public static SQLFilter simpleFilter(Map<String, String>filterMap, TypesMapping types) {
        List<Tripple> tripples = new ArrayList<SQLFilter.Tripple>();
        for (String key : filterMap.keySet()) {
            String val = filterMap.get(key);
            if (val != null ) {
                tripples.add(new Tripple(key, val, Op.EQ.name()));
            }
        }
        return  SQLFilter.createFilter(types,tripples);
    }
    
//    public static Tripple createTripple(String trpl, FormalNamesMapping fnames, TypesMapping types) {
//    	StringTokenizer tokenizer = new StringTokenizer(trpl, Op.EQ.getRawString()+Op.GT.getRawString()+Op.LT.getRawString(), true);
//        if(tokenizer.hasMoreTokens()) {
//        	String left = tokenizer.nextToken();
//            Op op = tokenizer.hasMoreTokens() ? Op.findByString(tokenizer.nextToken()): null;
//            String right = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : null;
//            
//            if (types.containsRawName(left)) {
//                return  new Tripple(left, right, op.getRawString());
//            } else {
//            	return new Tripple(right, left, op.getRawString());
//            }
//            
//        } 
//        return null;
//    }
    
    /**
     * Formal name <-> Raw name
     * @author pavels
     *
     */
    private static class FormalNameAssociation {
    	private String formalName;
    	private String rawName;
    	
    	public FormalNameAssociation(String fn, String rn) {
    		this.formalName = fn;
    		this.rawName = rn;
    	}
    	
    	public String getFormalName() {
			return formalName;
		}
    	
    	public String getRawName() {
			return rawName;
		}
    }
    
    
    public static class FormalNamesMapping {

    	private List<FormalNameAssociation> mappings = new ArrayList<DbFilterUtils.FormalNameAssociation>();
    	
    	public FormalNamesMapping() {}

    	public void map(String formalName, String rawName) {
    		mappings.add(new FormalNameAssociation(formalName, rawName));
    	}

    	public String lookUpRawName(String formalName) {
    		for (FormalNameAssociation asoc : this.mappings) {
				if (asoc.getFormalName().equals(formalName)) return asoc.getRawName();
			}
    		return null;
    	}

    	public String lookUpFormalName(String rawName) {
    		for (FormalNameAssociation asoc : this.mappings) {
				if (asoc.getRawName().equals(rawName)) return asoc.getFormalName();
    		}    		
    		return null;
    	}
	}
}
