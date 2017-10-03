package cz.incad.kramerius.virtualcollections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This represents new collection object
 */
public class Collection {
    
    private String pid;
    private String label;
    private boolean canLeaveFlag;
    
    private int numberOfDocs;
    
    private List<Description> descriptions = new ArrayList<Description>();
    
    public Collection(String pid, String label, boolean canLeaveFlag) {
        super();
        this.pid = pid;
        this.canLeaveFlag = canLeaveFlag;
        this.label = label;
    }

    public String getPid() {
        return pid;
    }
    
    public String getLabel() {
        return label;
    }

    public boolean isCanLeaveFlag() {
        return canLeaveFlag;
    }

    public void changeCanLeaveFlag(boolean canLeave) {
        this.canLeaveFlag = canLeave;
    }

    public Description lookup(String langCode) {
        for (Description desc : descriptions) {
            if (desc.getLangCode().equals(langCode)) return desc;
        }
        return null;
    }
    
    public void addDescription(Description desc) {
        this.descriptions.add(desc);
    }
    
    public void removeDescription(Description desc) {
        this.descriptions.remove(desc);
    }
    
    public List<Description> getDescriptions() {
        return new ArrayList<Description>(this.descriptions);
    }

    

    public int getNumberOfDocs() {
		return numberOfDocs;
	}

	public void setNumberOfDocs(int numberOfDocs) {
		this.numberOfDocs = numberOfDocs;
	}

	@Override
    public String toString() {
        return "Collection [pid=" + pid + ", label=" + label + ", canLeaveFlag=" + canLeaveFlag + ", descriptions="
                + descriptions + "]";
    }



    public static class Description {
        private String langCode; // lang code

        private String name; // stored name for short text
        private String text; // short text
        
        private String longTextName; // stored name for long text
        private String longText; // stored long text
        
        
        public Description(String langCode, String name, String text) {
            super();
            this.name = name;
            this.text = text;
            this.langCode = langCode;
        }
        public Description(String langCode, String name, String text, String longTextName,String longText) {
            super();
            this.name = name;
            this.text = text;
            
            this.longTextName = longTextName;
            this.longText = longText;
            
            this.langCode = langCode;
        }

        public String getName() {
            return name;
        }
        
        public String getText() {
            return text;
        }
        
        public String getLangCode() {
            return langCode;
        }
        
        
        public String getLongTextName() {
            return longTextName;
        }
        
        public String getLongText() {
            return longText;
        }

        public boolean hasLongtext() {
            return this.longText != null;
        }


        @Override
        public String toString() {
            return "Descriptions [name=" + name + ", text=" + text + "]";
        }
        
        
    }
    
}
