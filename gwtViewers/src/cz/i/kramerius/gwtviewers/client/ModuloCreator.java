package cz.i.kramerius.gwtviewers.client;

import com.google.gwt.user.client.Window;

public class ModuloCreator {

	public static final int MXPAGES = 300;
	
	public int createModulo(int numberOfPages) {
		Window.alert("Default ");
		if (numberOfPages > MXPAGES) {
			return numberOfPages /MXPAGES;
		} else return 1;
	}
}
