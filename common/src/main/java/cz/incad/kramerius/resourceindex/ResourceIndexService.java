/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.incad.kramerius.resourceindex;


import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.commons.configuration.Configuration;

/**
 *
 * @author Alberto
 */
public class ResourceIndexService {
    
    public static IResourceIndex getResourceIndexImpl() throws ClassNotFoundException, InstantiationException, IllegalAccessException{
        Configuration config = KConfiguration.getInstance().getConfiguration();
        String className = config.getString("resource.index.service.class");
        ClassLoader classLoader = ResourceIndexService.class.getClassLoader();

        return (IResourceIndex) classLoader.loadClass(className).newInstance();
    }
}
