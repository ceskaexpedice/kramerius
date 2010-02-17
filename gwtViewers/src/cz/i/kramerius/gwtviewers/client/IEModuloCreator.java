package cz.i.kramerius.gwtviewers.client;

public class IEModuloCreator extends ModuloCreator {

	public static final int IEMXPAGES = 20;
	
	@Override
	public int createModulo(int numberOfPages) {
		if (numberOfPages > IEMXPAGES) {
			return numberOfPages / IEMXPAGES;
		} else {
			return 1;
		}
	}
	
}
