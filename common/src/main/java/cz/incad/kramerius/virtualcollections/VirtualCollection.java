/*
 * Copyright (C) 2011 Alberto Hernandez
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
package cz.incad.kramerius.virtualcollections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//TODO: remove
class VirtualCollection {
    
    protected String label;
    protected String pid;
    protected List<CollectionDescription> descriptions = new ArrayList<CollectionDescription>();
    private boolean canLeave;
    
    public String getLabel(){
        return this.label;
    }
    
    public String getPid(){
        return this.pid;
    }
    
    public List<CollectionDescription> getDescriptions(){
        return this.descriptions;
    }
    
    public Map<String, String> getDescriptionsMap(){
        Map map = new HashMap<String, String>();
        for(CollectionDescription cd : descriptions){
            map.put(cd.lang, cd.text);
        }
        return map;
    }
    
    public VirtualCollection(String label, String pid, boolean canLeave){
        this.label = label;
        this.pid = pid;
        this.canLeave = canLeave;
    }
    
    public void addDescription(String lang, String text){
        descriptions.add(new CollectionDescription(lang, text));
    }
    
    public String getDescriptionLocale(String lang){
        for(CollectionDescription ds : descriptions){
            if(lang.equals(ds.lang)){
                return ds.text;
            }
        }
        return null;
    }

    /**
     * @return the canLeave
     */
    public boolean isCanLeave() {
        return canLeave;
    }
    
    public class CollectionDescription{
        protected String text;
        protected String lang;
        
        public CollectionDescription(String lang, String text){
            this.lang = lang;
            this.text = text;
        }

        public String getText(){
            return this.text;
        }

        public String getLang(){
            return this.lang;
        }
    }
}
