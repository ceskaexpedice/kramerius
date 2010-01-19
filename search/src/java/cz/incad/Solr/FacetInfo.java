/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.incad.Solr;

/**
 *
 * @author Administrator
 */
public class FacetInfo {
    public String name;
    public int count;
    public String displayName;
    public String url;
    public boolean used = false;
    
    public FacetInfo(String _name, int _count, String _url){
        name = _name;
        displayName = _name;
        count = _count;
        url = _url;
    }
}
