/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.kramerius;


/**
 *
 * @author Administrator
 */



public class RDFModels {

    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(RDFModels.class.getName());


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
            //System.out.println("Unfffsupported rdf: " + rdf);
            return null;
        }
    }

    public static String convertToRdf(KrameriusModels km){
        switch(km){
            case MONOGRAPH:
                return "monograph";
            case MONOGRAPHUNIT:
                return "hasUnit";
            case PERIODICAL:
                return "periodical";
            case PERIODICALVOLUME:
                return "hasVolume";
            case PERIODICALITEM:
                return "hasItem";
            case INTERNALPART:
                return "hasIntCompPart";
            case PAGE:
                return "hasPage";
            case DONATOR:
                return "hasDonator";
            default:
                return km.toString();
        }
    }
}
