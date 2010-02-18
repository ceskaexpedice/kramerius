package cz.i.kramerius.gwtviewers.client;

import com.google.gwt.user.client.Window;

public class MozillaModuloCreator extends ModuloCreator {
	
	public static final int MOZILLAMXPAGES = 250;
	
	@Override
	public int createModulo(int numberOfPages) {
		Window.alert("Mozilla ");
		if (numberOfPages > MOZILLAMXPAGES) {
			return numberOfPages / MOZILLAMXPAGES;
		} else {
			return 1;
		}
	}

	
}
