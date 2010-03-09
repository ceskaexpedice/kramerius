package cz.incad.kramerius.gwtviewers.client;

import com.google.gwt.user.client.Window;

public class ModuloCreator {

	public static final int MXPAGES = 50;
	
	public int createModulo(int numberOfPages) {
		if (numberOfPages > MXPAGES) {
			return numberOfPages /MXPAGES;
		} else return 1;
	}
}
