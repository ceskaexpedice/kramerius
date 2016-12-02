package cz.incad.kramerius.statistics.filters;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import cz.incad.kramerius.statistics.filters.VisibilityFilter.VisbilityType;

/**
 * Represents visibility filter (public | private | all)
 * @author pstastny
 */
public class VisibilityFilter implements StatisticsFilter {
    public static final Logger LOGGER = Logger.getLogger(VisibilityFilter.class.getName());
    
    public static enum VisbilityType {
        PUBLIC,
        PRIVATE,
        ALL
    }
    
    private VisbilityType selected = VisbilityType.ALL;

    public VisbilityType getSelected() {
        return selected;
    }

    public void setSelected(VisbilityType selected) {
        this.selected = selected;
    }
    
    public Map<String, Boolean> asMap() {
        Map<String, Boolean> retVal = new HashMap<String, Boolean>();
        VisbilityType[] values = VisbilityType.values();
        for (VisbilityType t : values) {
            boolean b = t == selected;
            retVal.put(t.name().toUpperCase(), new Boolean(b));
            retVal.put(t.name().toLowerCase(), new Boolean(b));
        }
        return retVal;
    }
}
