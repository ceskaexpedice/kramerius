/*
 * Copyright (C) Jan 11, 2024 Pavel Stastny
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
package cz.incad.kramerius.rest.oai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import cz.incad.kramerius.utils.conf.KConfiguration;

public class OAISets {
    
    private OAISet defaultSet;
    private List<OAISet> sets = new ArrayList<>();
    
    public OAISets(String host) {

        /* Default sets are disabled
        sets.addAll(
            Arrays.asList(
                
            new OAISet( host, "monograph", 
                "Set of monographs", 
                "-", "model:monograph"),
            
            new OAISet(host,"periodical", 
                    "Set of periodicals", "-", 
                    "model:periodical"),
            
            new OAISet(host,"periodicalitem", 
                    "Set of periodical items", 
                    "-", "model:periodicalitem"),
            
            new OAISet(host,"periodicalvolume", 
                    "Set of periodical volumes", 
                    "-", "model:periodicalvolume"),
            
            new OAISet(host,"manuscript", 
                    "Set of periodical manuscripts", 
                    "-", "model:manuscript"),
            
            new OAISet(host,"graphic", 
                    "Set of periodical graphics", 
                    "-", "model:graphic"),
            
            new OAISet(host,"map", 
                    "Set of periodical maps", 
                    "-", "model:map"),
            
            new OAISet(host,"sheetmusic", 
                    "Set of periodical sheetmusics", 
                    "-", "model:sheetmusic"),
            
            new OAISet(host,"article", 
                    "Set of periodical articles", 
                    "-", "model:article"),
            
            new OAISet(host,"supplement", 
                    "Set of periodical supplements", 
                    "-", "model:supplement")
        ));
        */
        
        
        this.defaultSet =  this.sets.get(0);
        // configuration for new sets
        Map<String, OAISet> configuredSets = new HashMap();
        Iterator<String> keys = KConfiguration.getInstance().getConfiguration().getKeys("oai.set");
        while(keys.hasNext()) {
            String key = keys.next();
            String rest = key.substring("oai.set.".length());
            String[] values = rest.split("\\.");
            if (values.length == 2) {
                String spec = values[0];
                if (!configuredSets.containsKey(spec)) {
                    configuredSets.put(spec, new OAISet(host));
                    configuredSets.get(spec).setSetSpec(spec);
                }
                String property = values[1];
                switch(property) {
                    case "name":{
                        OAISet set = configuredSets.get(spec);
                        set.setSetName(KConfiguration.getInstance().getProperty(key));
                    }
                    break;
                    case "desc":
                    case "description":{
                        OAISet set = configuredSets.get(spec);
                        set.setSetDescription(KConfiguration.getInstance().getProperty(key));
                    }
                    break;
                    case "filter":{
                        OAISet set = configuredSets.get(spec);
                        set.setFilterQuery(KConfiguration.getInstance().getProperty(key));
                    }
                    break;
                    default: {
                        OAISet set = configuredSets.get(spec);
                        set.getAdditionalsInfo().put(key, KConfiguration.getInstance().getProperty(key));
                    }
                    break;
                }
            }
        }
        configuredSets.values().stream().filter(oai -> oai.getFilterQuery() != null).forEach(sets::add);
        LOGGER.info("OAI -> Configured sets :"+this.sets);
    }
    
    public OAISet findBySet(String setName) {
        Optional<OAISet> found = this.sets.stream()
                .filter(oaiSet -> oaiSet.getSetSpec().equals(setName))
                .findFirst();
        return found.isPresent() ?  found.get() : null; 
    }
    
    public OAISet findByToken(String token) {
        Optional<OAISet> found = this.sets.stream()
                .filter(oaiSet -> oaiSet.isMyResumptionToken(token))
                .findFirst();
        return found.get();
    }
    
    

    public List<OAISet> getAOISets() {
        return this.sets;
    }
    
    public OAISet getDefaultSet() {
        return defaultSet;
    }
    
    public static final Logger LOGGER = Logger.getLogger(OAISets.class.getName());
}
