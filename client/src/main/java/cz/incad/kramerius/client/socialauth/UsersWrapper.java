package cz.incad.kramerius.client.socialauth;

import java.util.Map;

public interface UsersWrapper {

    public static final String FIRST_NAME_KEY="firstName";
    public static final String LAST_NAME_KEY="lastName";
    
    
    public String getCalculatedName();
    
    public String getProperty(String key);
    
    
    
    
}
