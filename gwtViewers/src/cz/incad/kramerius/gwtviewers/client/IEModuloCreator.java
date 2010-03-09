package cz.incad.kramerius.gwtviewers.client;

import com.google.gwt.user.client.Window;

public class IEModuloCreator extends ModuloCreator {

	public static final int IEMXPAGES = 20;
	
	@Override
	public int createModulo(int numberOfPages) {
		if (numberOfPages > IEMXPAGES) {
			int i = numberOfPages / IEMXPAGES;
			return i;
		} else {
			return 1;
		}
	}
	
}
