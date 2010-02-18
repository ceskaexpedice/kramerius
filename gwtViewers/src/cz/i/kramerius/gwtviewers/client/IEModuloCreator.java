package cz.i.kramerius.gwtviewers.client;

import com.google.gwt.user.client.Window;

public class IEModuloCreator extends ModuloCreator {

	public static final int IEMXPAGES = 20;
	
	@Override
	public int createModulo(int numberOfPages) {
		Window.alert("Internet Explorer");
		if (numberOfPages > IEMXPAGES) {
			return numberOfPages / IEMXPAGES;
		} else {
			return 1;
		}
	}
	
}
