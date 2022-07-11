package cz.incad.kramerius.statistics.filters;

public class IdentifiersFilter implements  StatisticsFilter {

    private String identifier;
    
    public String getIdentifier() {
        return identifier;
    }
    
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}
