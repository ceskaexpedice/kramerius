package cz.incad.kramerius.gwtviewers.client;

import com.google.gwt.user.client.Window;

public class MozillaModuloCreator extends ModuloCreator {
	
	public static final int MOZILLAMXPAGES = 200;
	
	@Override
	public int createModulo(int numberOfPages) {
		if (numberOfPages > MOZILLAMXPAGES) {
			return numberOfPages / MOZILLAMXPAGES;
		} else {
			return 1;
		}
	}
}
