package cz.incad.kramerius.ngwt.client.utils;

import com.google.gwt.user.client.Element;

public class ElementIDs {
	
	public static String getUUID(Element elm) {
		String id = elm.getId();
		String uuid = id.substring("img_".length());
		return uuid;
	}
	
	public static String createElementID(String uuid) {
		return "img_"+uuid;
	}
	
	public static boolean containsElementGivenUUID(String uuid, Element elm) {
		return getUUID(elm).equals(uuid);
	}
}
