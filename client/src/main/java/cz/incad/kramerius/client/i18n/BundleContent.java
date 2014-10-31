package cz.incad.kramerius.client.i18n;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.ws.rs.GET;

import cz.incad.kramerius.utils.StringUtils;


public class BundleContent  {

    private String myKey;
    private String value;

    Map<String, BundleContent> children = new HashMap<String, BundleContent>();

    public BundleContent(String key) {
        super();
        this.myKey = key;
    }
    
 
    public String getValue() {
        return this.value;
    }
    
    public void setValue(String v) {
        this.value = v;
    }
    
    public String getKey() {
        return myKey;
    }


    public BundleContent findOrCreateChildren(String key) {
        if (key.length() > this.myKey.length()) {
            String smallKey = StringUtils.minus(key,this.myKey+".");
            StringTokenizer tokenizer = new StringTokenizer(smallKey,".");
            Map<String, BundleContent> processingContent = this.children;
            BundleContent returingVal = this;
            while (tokenizer.hasMoreTokens()) {
                String tok = tokenizer.nextToken();
                if (processingContent.containsKey(tok)) {
                    returingVal = processingContent.get(tok);
                    processingContent = returingVal.children;
                } else {
                    returingVal = new BundleContent(tok);
                    processingContent.put(tok, returingVal);
                    processingContent = returingVal.children;
                };
            }
            return returingVal;
        } else if (key.equals(this.myKey)) {
            return this;
        } else return null;
    }
    
    
    
    public Object get(String key) {
        if (this.myKey.equals(key)) {
            return this;
        } else if (this.children.containsKey(key)) {
            return this.children.get(key);
        } else if (key.startsWith(this.myKey)) {
            String smallKey = StringUtils.minus (key,this.myKey+".");
            StringTokenizer tokenizer = new StringTokenizer(smallKey,".");
            BundleContent bcont = this;
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                if (bcont.children.containsKey(token)) {
                    bcont =  bcont.children.get(token);
                } else {
                    return "!"+key+"!";
                }
            }
            return bcont;
        } else  {
            StringTokenizer tokenizer = new StringTokenizer(key,".");
            if (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                if (this.children.containsKey(token)) {
                    return this.children.get(token);
                } else {
                    return "!"+key+"!";
                }
            } else {
                return "!"+key+"!";
            }
        }
    }
    
    
    
    @Override
    public String toString() {
        return this.value ;
    }

    
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((myKey == null) ? 0 : myKey.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
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
        BundleContent other = (BundleContent) obj;
        if (myKey == null) {
            if (other.myKey != null)
                return false;
        } else if (!myKey.equals(other.myKey))
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }
}
