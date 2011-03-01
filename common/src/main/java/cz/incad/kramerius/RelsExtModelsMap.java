/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.kramerius;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import cz.incad.kramerius.utils.conf.KConfiguration;
import java.util.ArrayList;

/**
 *
 * @author Alberto
 */
public class RelsExtModelsMap {

    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;
    @Inject
    KConfiguration configuration;
    static ArrayList<Correspondence> correspondences = new ArrayList<Correspondence>();

    public RelsExtModelsMap() {
        String[] prop = configuration.getPropertyList("relsExtModelsMap");
        for (int i = 0; i < prop.length; i = i + 2) {
            correspondences.add(new Correspondence(prop[i], prop[i + 1]));
        }
    }

    public static ArrayList<String> getModelsOfRelation(String rel) {
        ArrayList<String> ret = new ArrayList<String>();
        for (int i=0; i<correspondences.size(); i++) {
            if(correspondences.get(i).rel.equals(rel)){
                ret.add(correspondences.get(i).model);
            }
        }
        return ret;
    }

    public static ArrayList<String> getRelsOfModel(String model) {
        ArrayList<String> ret = new ArrayList<String>();
        for (int i=0; i<correspondences.size(); i++) {
            if(correspondences.get(i).model.equals(model)){
                ret.add(correspondences.get(i).rel);
            }
        }

        return ret;
    }

    class Correspondence{
        public String rel;
        public String model;
        Correspondence(String rel, String model){
            this.rel = rel;
            this.model = model;
        }
    }
}
