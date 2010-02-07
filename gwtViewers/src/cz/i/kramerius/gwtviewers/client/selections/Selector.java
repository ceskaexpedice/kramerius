package cz.i.kramerius.gwtviewers.client.selections;

import cz.i.kramerius.gwtviewers.client.panels.ImageMoveWrapper;
import cz.i.kramerius.gwtviewers.client.panels.utils.ImageRotatePool;

/**
 * Trida pro definici selekce
 * @author pavels
 *
 */
public interface Selector {

	/**
	 * Vraci prave vybrany wrapper
	 * @param rotatePool
	 * @return
	 */
	public ImageMoveWrapper getSelection(ImageRotatePool rotatePool);

	/**
	 * Rekne, zda podany wrapper je vybrany
	 * @param wrapper
	 * @param pool
	 * @return
	 */
	public boolean isSelected(ImageMoveWrapper wrapper, ImageRotatePool pool);
	
	/**
	 * Factory metoda -> vytvari dekorator selekce
	 * @return
	 */
	public SelectionDecorator createSelectionDecorator();


	public SelectionDecorator getSelectionDecorator();

	public int moveToSelect(ImageMoveWrapper wrapper, ImageRotatePool pool);

	public int selectionToSliderPosition(ImageRotatePool pool, int what);

	public  int sliderPositionToSelection(ImageRotatePool pool, int what);

}
