package cz.i.kramerius.gwtviewers.client;

public class ModuloCreator {

	public static final int MXPAGES = 300;
	
	public int createModulo(int numberOfPages) {
		if (numberOfPages > MXPAGES) {
			return numberOfPages /MXPAGES;
		} else return 1;
	}
}
