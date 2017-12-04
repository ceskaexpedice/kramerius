/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.incad.kramerius;

/**
 * This enums contains all models used in kramerius
 * @author Administrator
 */
@Deprecated
public enum KrameriusModels {


    MONOGRAPH("monograph"),
    MONOGRAPHUNIT("monographunit"),
    PERIODICAL("periodical"),
    PERIODICALVOLUME("periodicalvolume"),
    PERIODICALITEM("periodicalitem"),
    PAGE("page"),
    INTERNALPART("internalpart"),
    DONATOR("donator"),
    ARTICLE("article"),
    SUPPLEMENT("supplement"),
    PICTURE("picture"),
    COLLECTION("collection"),
    GRAPHIC("graphic"),
    MANUSCRIPT("manuscript"),
    MAP("map"),
    REPOSITORY("repository"),
    SHEETMUSIC("sheetmusic"),
    SOUNDRECORDING("soundrecording"),
    SOUNDUNIT("soundunit"),
    TRACK("track");

    private KrameriusModels(String value) {
		this.value = value;
	}
    private String value;

    /**
     * Returns raw value
     * @return
     */
	public String getValue() {
		return value;
	}

	/**
	 * Parsing enum object from given raw string 
	 * @param s raw string
	 * @return parsed enum object
	 */
	public static KrameriusModels parseString(String s) {
		KrameriusModels[] values = values();
		for (KrameriusModels model : values) {
			if (model.getValue().equalsIgnoreCase(s)) return model;
		}
        throw new RuntimeException("Unsupported type");
    }
	
	/**
	 * String representation method
	 * @param km enum object instance
	 * @return string representation
	 */
	public static String toString(KrameriusModels km) {
    	return km.getValue();
    }
}
