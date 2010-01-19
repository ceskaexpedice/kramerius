/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.incad.Kramerius;

/**
 *
 * @author Administrator
 */
public enum KrameriusModels {

    MONOGRAPH, MONOGRAPHUNIT, PERIODICAL, PERIODICALVOLUME, PERIODICALITEM, PAGE, INTERNALPART;

    public static KrameriusModels parseString(String s) {
        if (s.equalsIgnoreCase("monograph")) {
            return KrameriusModels.MONOGRAPH;
        } else if (s.equalsIgnoreCase("MONOGRAPHUNIT")) {
            return KrameriusModels.MONOGRAPHUNIT;
        } else if (s.equalsIgnoreCase("PERIODICAL")) {
            return KrameriusModels.PERIODICAL;
        } else if (s.equalsIgnoreCase("PERIODICALVOLUME")) {
            return KrameriusModels.PERIODICALVOLUME;
        } else if (s.equalsIgnoreCase("PERIODICALITEM")) {
            return KrameriusModels.PERIODICALITEM;
        } else if (s.equalsIgnoreCase("INTERNALPART")) {
            return KrameriusModels.INTERNALPART;
        } else if (s.equalsIgnoreCase("PAGE")) {
            return KrameriusModels.PAGE;
        } else {
            throw new RuntimeException("Unsupported type");
        }
    }
    
    public static String toString(KrameriusModels km) {
        switch(km){
            case MONOGRAPH:
                return "monograph";
            case MONOGRAPHUNIT:
                return "monographunit";
            case PERIODICAL:
                return "periodical";
            case PERIODICALVOLUME:
                return "periodicalvolume";
            case PERIODICALITEM:
                return "periodicalitem";
            case INTERNALPART:
                return "internalpart";
            case PAGE:
                return "page";
            default:
                return km.toString();
        }
    }
}
