package cz.i.kramerius.gwtviewers.client;

public class MozillaModuloCreator extends ModuloCreator {
	
	public static final int MOZILLAMXPAGES = 250;
	
	@Override
	public int createModulo(int numberOfPages) {
		if (numberOfPages > MOZILLAMXPAGES) {
			return numberOfPages / MOZILLAMXPAGES;
		} else {
			return 1;
		}
	}

	
}
