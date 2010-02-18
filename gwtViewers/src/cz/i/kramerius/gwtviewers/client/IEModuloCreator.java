package cz.i.kramerius.gwtviewers.client;

import com.google.gwt.user.client.Window;

public class IEModuloCreator extends ModuloCreator {

	public static final int IEMXPAGES = 20;
	
	@Override
	public int createModulo(int numberOfPages) {
		Window.alert("IE");
		if (numberOfPages > IEMXPAGES) {
			int i = numberOfPages / IEMXPAGES;
			Window.alert("modulo is "+i);
			return i;
		} else {
			return 1;
		}
	}
	
}
