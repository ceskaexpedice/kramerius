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

	
    MONOGRAPH("monograph"), 
    MONOGRAPHUNIT("monographunit"), 
    PERIODICAL("periodical"), 
    PERIODICALVOLUME("periodicalvolume"), 
    PERIODICALITEM("periodicalitem"), 
    PAGE("page"), 
    INTERNALPART("internalpart");

    private KrameriusModels(String value) {
		this.value = value;
	}
	private String value;

	public String getValue() {
		return value;
	}


	public static KrameriusModels parseString(String s) {
		KrameriusModels[] values = values();
		for (KrameriusModels model : values) {
			if (model.getValue().equalsIgnoreCase(s)) return model;
		}
        throw new RuntimeException("Unsupported type");
    }
    
    public static String toString(KrameriusModels km) {
    	return km.getValue();
    }
}
