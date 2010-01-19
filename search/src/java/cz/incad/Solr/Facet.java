/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.Solr;

import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author Administrator
 */
public class Facet {

    public ArrayList<FacetInfo> infos;
    public String name;
    public String displayName;

    public Facet(String _name) {
        name = _name;
        displayName = _name;
        infos = new ArrayList<FacetInfo>();
    }

    public void addFacetInfo(FacetInfo facetInfo) {
        infos.add(facetInfo);
    }

    public void sortByName() {
        Collections.sort(infos, new CzechComparator());
    }

    public boolean hasUnused() {
        for (int i = 0; i < infos.size(); i++) {
            if (!infos.get(i).used) {
                return true;
            }
        }
        if (infos.size() == 0) {
            return false;
        }
        return true;
    }
    
    public int getFacetsNumber(){
        return infos.size();
    }
    
    public FacetInfo getFacetInfoByName(String _name){
        for (int i = 0; i < infos.size(); i++) {
            if (infos.get(i).name.equals(_name)) {
                return infos.get(i);
            }
        }
        return null;
    }
}
