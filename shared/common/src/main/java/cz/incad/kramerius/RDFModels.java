/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.kramerius;


/**
 * @author Administrator
 */


public class RDFModels {

    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(RDFModels.class.getName());

    public static KrameriusModels convertRDFToModel(String rdf) {
        if (rdf.contains("hasPage")) {
            return KrameriusModels.PAGE;
        } else if (rdf.contains("hasUnit")) {
            //return KrameriusModels.MONOGRAPHUNIT;
            //throw new RuntimeException("relation hasUnit doesn't uniquely determine target object"); //it can be: monograph -hasUnit-> monographUnit, but also: convolute -hasUnit-> monograph, or: convolute -hasUnit-> sheetmusic etc
            return null;
        } else if (rdf.contains("hasVolume")) {
            //return KrameriusModels.PERIODICALVOLUME;
            return null;
        } else if (rdf.contains("hasItem")) {
            //return KrameriusModels.PERIODICALITEM;
            //throw new RuntimeException("relation hasItem doesn't uniquely determine target object"); //it can be: periodicalvolume -hasItem-> periodicalitem, but also: periodicalvolume -hasItem-> supplement etc
            return null;
        } else if (rdf.contains("hasIntCompPart")) {
            //return KrameriusModels.INTERNALPART;
            //throw new RuntimeException("relation hasIntCompPart doesn't uniquely determine target object"); //it can be: periodicalitem -hasIntCompPart-> article, but also: periodicalitem -hasIntCompPart-> article
            return null;
        } else if (rdf.contains("isOnPage")) {
            return KrameriusModels.PAGE;
        } else if (rdf.contains("hasDonator")) {
            //return KrameriusModels.DONATOR;
            return null;
        } else {
            //System.out.println("Unsupported rdf: " + rdf);
            return null;
        }
    }

    public static String convertToRdf(KrameriusModels km) {
        switch (km) {
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
