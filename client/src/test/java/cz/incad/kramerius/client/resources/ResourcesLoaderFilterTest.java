package cz.incad.kramerius.client.resources;

import junit.framework.TestCase;

public class ResourcesLoaderFilterTest extends TestCase {

    
    public void testMatch() {
        String url = "/menuitems.def";
        boolean matches = url.matches(".*\\.def");
        System.out.println(matches);
 
        url = "/menuitems.nondef";
        matches = url.matches(".*\\.def");
        
        System.out.println(matches);
    }
}
