package cz.incad.utils;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 *
 * @author Administrator
 */
public class ResourceBundleService {

    public static ResourceBundle getBundle(String bundle, String locale) {
        return ResourceBundle.getBundle(bundle, new Locale(locale), ResourceBundleService.class.getClassLoader());
    }

    public static String getString(ResourceBundle bundle, String key) {
        if(bundle.containsKey(key)){
            return bundle.getString(key);
        }
        return key;
    }
}
