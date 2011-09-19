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
package cz.incad.Kramerius.security.rightscommands.post;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.incad.kramerius.utils.pid.Token.TokenType;


public class SimpleJSONObjects {
/*
 * 
    options[params][1][objects][]
    options[params][1][shortDesc]
    data[securedAction]
    data[param][objects][]
    options[params][2][objects][]
    options[params][2][shortDesc]
    data[priority]
    affectedObjects[]
    options[params][0][shortDesc]
    data[param][ident]
    options[params][1][ident]
    options[params][0][ident]
    data[param][shortDesc]
    action
    data[justcreated]
    options[roles][]
    data[condition]
    options[params][0][objects][]
    options[params][2][ident]
*/

    

    
    
    
    public static enum TypeValueEnum {
        ARRAY_TYPE {
            @Override
            public Object createObject(String val) {
                return new ArrayList();
            }

            @Override
            public boolean isGoodType(Object obj) {
                return obj instanceof List;
            }
        }, 
        MAP_TYPE {

            @Override
            public Object createObject(String val) {
                return new HashMap();
            }

            @Override
            public boolean isGoodType(Object obj) {
                return obj instanceof Map;
            }
            
        }, 
        RAWVAL_TYPE {

            @Override
            public Object createObject(String val) {
                return val != null ? val:"";
            }

            @Override
            public boolean isGoodType(Object obj) {
                return obj instanceof String;
            }
            
        };
        public abstract Object createObject(String val);
        public abstract boolean isGoodType(Object obj);
    }
    
    public static class Token {
        public static int INT = 0;
        public static int WORD = 1;
        public static int LB = 2;
        public static int RB = 3;
        
        
        
        private String value;
        private int type = 0;
        
        private TypeValueEnum containerType = TypeValueEnum.RAWVAL_TYPE; 
        
        private Token(String value, int type) {
            super();
            this.value = value;
            this.type = type;
        }

        public String getValue() {
            return value;
        }


        public int getType() {
            return type;
        }
        

        @Override
        public String toString() {
            return this.value+"("+this.containerType.name()+")";
        }

        public TypeValueEnum getContainerType() {
            return containerType;
        }
        
        
        public void setContainerType(TypeValueEnum containerType) {
            this.containerType = containerType;
        }
    }
    
    private List<Token> path = new ArrayList<Token>();
    
    public int sequence(String str, int index) {
        boolean konec = inTheEnd(str, index);
        if (konec) return 0;
        StringBuilder builder = new StringBuilder();
        boolean isDigit = true;
        while(!konec) {
            char charAt = str.charAt(index++);
            if ((charAt == '[' || charAt == ']')) {
                break;
            }
            isDigit = isDigit && (charAt >= '0' && charAt <= '9');
            builder.append(charAt);
            konec = inTheEnd(str, index);
        }
        
        if (builder.length() > 0) {
            if (isDigit)  {
                path.add(new Token(builder.toString(), Token.INT));
            } else {
                path.add(new Token(builder.toString(), Token.WORD));
            }
            
            if (path.size() >= 2) {
                Token parentToken = path.get(path.size()-2);
                Token curToken = path.get(path.size() -1);
                if (curToken.getType() == Token.INT) {
                    parentToken.setContainerType(TypeValueEnum.ARRAY_TYPE);
                } else {
                    parentToken.setContainerType(TypeValueEnum.MAP_TYPE);
                }
            }

            
            return builder.length();
        }  else return 0;
    }
    
    public boolean inTheEnd(String str, int index) {
        return index >= str.length();
    }
    public int lb(String str, int index) {
        if (inTheEnd(str, index)) return 0;
        char charAt = str.charAt(index);
        if (charAt == '[') {
            return 1;
        } else return 0;
    }

    public int rb(String str, int index) {
        if (inTheEnd(str, index)) return 0;
        char charAt = str.charAt(index);
        if (charAt == ']') {
            return 1;
        } else return 0;
    }


    public void parse(String str) {
        int index = 0;
        
        int expected = lb(str, index);
        index = index  + expected;
        index = index + sequence(str, index);
        if (expected == rb(str, index)) {
            index = index + expected;
        }
        
        while(!inTheEnd(str, index)) {
            expected = lb(str, index);
            index = index  + expected;
            index = index + sequence(str, index);
            if (expected == rb(str, index)) {
                index = index + expected;
            }
        }
    }

    public List<Token> getPath() {
        return path;
    }
    
    public static void main(String[] args) {
        //final String string = "[three][four]aone";
        final String string = "[three][array]0";
        final Map map = new HashMap();
        

        SimpleJSONObjects simpleJSONObjects = new SimpleJSONObjects();
        System.out.println(string);
        simpleJSONObjects.parse(string);
        System.out.println(simpleJSONObjects.getPath());
        //simpleJSONObjects.createMap(string, map,"Test... ");
        simpleJSONObjects.createMap("", map, "10");
        System.out.println(map);
        
        
        
//        new SimpleJSONObjects().parse("options[params][1][shortDesc]", actions);
//        new SimpleJSONObjects().parse("data[securedAction]", actions);
//        new SimpleJSONObjects().parse("data[param][objects][]", actions);

//        options[params][1][shortDesc]
//                           data[securedAction]
//                           data[param][objects][]
//                           options[params][2][objects][]
//                           options[params][2][shortDesc]
//                           data[priority]
//                           affectedObjects[]
//                           options[params][0][shortDesc]
//                           data[param][ident]
//                           options[params][1][ident]
//                           options[params][0][ident]
//                           data[param][shortDesc]
//                           action
//                           data[justcreated]
//                           options[roles][]
//                           data[condition]
//                           options[params][0][objects][]
//                           options[params][2][ident]

    }

    public void createMap(String key, Map initialMap, String value) {

        this.parse(key);
        
        Object obj = initialMap;
        
        List<Token> pth = this.getPath();
        while(!pth.isEmpty()) {
            Token token = pth.remove(0);
            
            // konec
            if (obj == null) break;
            
            if (obj instanceof Map) {
                Map map = ((Map)obj);
                if (map.containsKey(token.getValue())) {
                    if (!token.getContainerType().isGoodType(map.get(token.getValue()))) {
                        map.put(token.getValue(), token.getContainerType().createObject(value));
                    }
                } else {
                    map.put(token.getValue(), token.getContainerType().createObject(value));
                }
                obj = map.get(token.getValue());
            } else if (obj instanceof List) {
                List list = (List)obj;
                String sval = token.getValue();
                int index = Integer.parseInt(sval);
                if (index+1 > list.size() || list.get(index) == null) {
                    for (int i = list.size(); i < index+1; i++) {
                        list.add(null);
                    }
                    list.set(index, token.getContainerType().createObject(value));;
                }
                obj = list.get(index);
            } else {
                obj = null;
            }
            
        }
    }

}
