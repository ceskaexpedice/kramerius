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
import java.util.stream.Collectors;

import cz.incad.kramerius.rest.apiNew.ConfigManager;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class OAISets {
    
    public static final String DEFAULT_SET_KEY = "DEFAULT";
    
    
    private OAISet defaultSet;
    private List<OAISet> sets = new ArrayList<>();
    
    public OAISets(String host) {
        loadFromStandardConfiguration(host);
    }
    
    public OAISets(ConfigManager manager, String host) {
        loadFromConfigurationManager(manager, host);
    }

    

    /*
    private void embeddedSets(String host) {
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
    }*/

    private void loadFromConfigurationManager(ConfigManager confManager, String host) {

        Map<String, OAISet> configuredSets = new HashMap();
        List<String> keys = confManager.getKeysByRegularExpression("^oai\\.set.*");
        for (String key : keys) {
            String rest = key.substring("oai.set.".length());
            String[] values = rest.split("\\.");
            if (values.length == 2) {
                String spec = values[0];
                OAISet pSet = null;
                if (spec.equals(DEFAULT_SET_KEY))  {
                    if(this.defaultSet == null) {
                        this.defaultSet = new OAISet(host);
                        this.defaultSet.setSetSpec(spec);
                    }
                    pSet = this.defaultSet;
                } else {
                    if (!configuredSets.containsKey(spec)) {
                        configuredSets.put(spec, new OAISet(host));
                        configuredSets.get(spec).setSetSpec(spec);
                    }
                    pSet = configuredSets.get(spec);
                }
                
                String property = values[1];
                switch(property) {
                    case "name":{
                        //OAISet set = configuredSets.get(spec);
                        pSet.setSetName( confManager.getProperty(key));
                    }
                    break;
                    case "desc":
                    case "description":{
                        pSet.setSetDescription(confManager.getProperty(key));
                    }
                    break;
                    case "filter":{
                        pSet.setFilterQuery(confManager.getProperty(key));
                    }
                    break;
                    default: {
                        pSet.getAdditionalsInfo().put(key, confManager.getProperty(key));
                    }
                    break;
                }
            }
        }
        configuredSets.values().stream().filter(oai -> oai.getFilterQuery() != null).forEach(sets::add);
    }

    private void loadFromStandardConfiguration(String host) {
        Map<String, OAISet> configuredSets = new HashMap();
        Iterator<String> keys = KConfiguration.getInstance().getConfiguration().getKeys("oai.set");
        while(keys.hasNext()) {
            String key = keys.next();
            String rest = key.substring("oai.set.".length());
            String[] values = rest.split("\\.");
            if (values.length == 2) {
                String spec = values[0];
                
                if (!spec.equals(DEFAULT_SET_KEY))  {
                    if (this.defaultSet == null) {
                        this.defaultSet = new OAISet(host);
                    }
                    loadOAISetPropertiesFromConf(key, values, this.defaultSet);
                } else {
                    if (!configuredSets.containsKey(spec)) {
                        configuredSets.put(spec, new OAISet(host));
                        configuredSets.get(spec).setSetSpec(spec);
                    }
                    OAISet pset = configuredSets.get(spec);
                    loadOAISetPropertiesFromConf(key, values, pset);
                }
            }
        }
       
        configuredSets.values().stream().filter(oai -> oai.getFilterQuery() != null).forEach(sets::add);
        //LOGGER.info("OAI -> Configured sets");
    }

    private void loadOAISetPropertiesFromConf(String key, String[] values, OAISet pset) {
        String property = values[1];
        switch(property) {
            case "name":{
                //OAISet set = configuredSets.get(spec);
                pset.setSetName(KConfiguration.getInstance().getProperty(key));
            }
            break;
            case "desc":
            case "description":{
                //OAISet set = configuredSets.get(spec);
                pset.setSetDescription(KConfiguration.getInstance().getProperty(key));
            }
            break;
            case "filter":{
                //OAISet set = configuredSets.get(spec);
                pset.setFilterQuery(KConfiguration.getInstance().getProperty(key));
            }
            break;
            default: {
                //OAISet set = configuredSets.get(spec);
                pset.getAdditionalsInfo().put(key, KConfiguration.getInstance().getProperty(key));
            }
            break;
        }
    }
    
    public OAISet findBySet(String setName) {
        LOGGER.info(String.format("Finding %s",setName));
        LOGGER.info("ALL sets " + this.sets.stream().map(OAISet::getSetSpec).collect(Collectors.toList()));
        if (setName != null && !setName.equals(DEFAULT_SET_KEY)) {
            Optional<OAISet> found = this.sets.stream()
                    .filter(oaiSet -> oaiSet.getSetSpec().equals(setName))
                    .findFirst();
            return found.isPresent() ?  found.get() : null; 
        } else {
            return this.defaultSet;        
        }
    }
    
    public OAISet findByToken(String token) {
        if (this.defaultSet.isMyResumptionToken(token)) {
            return this.defaultSet;
        } else {
            Optional<OAISet> found = this.sets.stream()
                    .filter(oaiSet -> oaiSet.isMyResumptionToken(token))
                    .findFirst();
            
            return found.get();
        }
    }
    
    

    public List<OAISet> getAOISets() {
        return this.sets;
    }
    
    public OAISet getDefaultSet() {
        return defaultSet;
    }
    
    public static final Logger LOGGER = Logger.getLogger(OAISets.class.getName());
}
