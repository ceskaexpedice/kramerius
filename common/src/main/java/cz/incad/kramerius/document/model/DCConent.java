package cz.incad.kramerius.document.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Stores infromation from DC stream
 * @author pavels
 */
public  class DCConent {
    
    private String title;
    // muze byt vicekrat ?? 
    private String type;
    private String date;
    private String[] identifiers;
    
    private String[] publishers;
    private String[] creators;
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    
    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
    }
    
    public String[] getIdentifiers() {
        return identifiers;
    }
    
    
    public void setIdentifiers(String[] identifiers) {
        this.identifiers = identifiers;
    }

    public String[] getPublishers() {
        return publishers;
    }

    public void setPublishers(String[] publishers) {
        this.publishers = publishers;
    }

    public String[] getCreators() {
        return creators;
    }

    public void setCreators(String[] creators) {
        this.creators = creators;
    }
    
    
    
    public static DCConent collectFirstWin(List<DCConent> contents) {
        String type = null;
        String title = null;
        String date = null;
        List<String> creators = new ArrayList<String>();
        List<String> publishers = new ArrayList<String>();
        List<String> identifiers = new ArrayList<String>();
        for (DCConent dcConent : contents) {
            if (date == null ) date = dcConent.getDate();
            if (title == null) title = dcConent.getTitle();
            if (type == null) type =  dcConent.getType();
            creators.addAll(Arrays.asList(dcConent.getCreators()));
            publishers.addAll(Arrays.asList(dcConent.getPublishers()));
            identifiers.addAll(Arrays.asList(dcConent.getIdentifiers()));
        }

        DCConent cont = new DCConent();
        cont.setCreators((String[]) creators.toArray(new String[creators.size()]));
        cont.setPublishers((String[]) publishers.toArray(new String[publishers.size()]));
        cont.setIdentifiers((String[]) identifiers.toArray(new String[identifiers.size()]));

        return cont;
    }
    

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(creators);
        result = prime * result + ((date == null) ? 0 : date.hashCode());
        result = prime * result + Arrays.hashCode(identifiers);
        result = prime * result + Arrays.hashCode(publishers);
        result = prime * result + ((title == null) ? 0 : title.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DCConent other = (DCConent) obj;
        if (!Arrays.equals(creators, other.creators))
            return false;
        if (date == null) {
            if (other.date != null)
                return false;
        } else if (!date.equals(other.date))
            return false;
        if (!Arrays.equals(identifiers, other.identifiers))
            return false;
        if (!Arrays.equals(publishers, other.publishers))
            return false;
        if (title == null) {
            if (other.title != null)
                return false;
        } else if (!title.equals(other.title))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }
}
