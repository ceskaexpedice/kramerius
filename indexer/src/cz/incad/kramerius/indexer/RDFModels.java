/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.kramerius.indexer;

import cz.incad.kramerius.indexer.KrameriusModels;

/**
 *
 * @author Administrator
 */



public class RDFModels {

    public static KrameriusModels convertRDFToModel(String rdf) {
        if(rdf.contains("hasPage")){
                return KrameriusModels.PAGE;
        }else if(rdf.contains("hasUnit")){
                return KrameriusModels.MONOGRAPHUNIT;
        }else if(rdf.contains("hasVolume")){
                return KrameriusModels.PERIODICALVOLUME;
        }else if(rdf.contains("hasItem")){
                return KrameriusModels.PERIODICALITEM;
        }else if(rdf.contains("hasIntCompPart")){
                return KrameriusModels.INTERNALPART;
        }else if(rdf.contains("isOnPage")){
                return KrameriusModels.PAGE;
        }else if(rdf.contains("hasDonator")){
                return KrameriusModels.DONATOR;
        }else{
            System.out.println("Unsupported rdf: " + rdf);
            return null;
        }
    }
}
