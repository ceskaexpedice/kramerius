/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.Solr;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

/**
 *
 * @author Administrator
 */
public class CzechComparator implements Comparator {

    //Collator czCollator = Collator.getInstance(new Locale("cs", "CS"));
    Collator czCollator = Collator.getInstance(new Locale("cs"));

    public int compare(Object o1, Object o2) {
        if (o1.getClass().getSimpleName().equalsIgnoreCase("String")) {
            return czCollator.compare((String) o1, (String) o2);
        } else if (o1.getClass().getSimpleName().equalsIgnoreCase("FacetInfo")) {
            return czCollator.compare(((FacetInfo) o1).name, ((FacetInfo) o2).name);
        }else{
            return 0;
        }

    }
}
