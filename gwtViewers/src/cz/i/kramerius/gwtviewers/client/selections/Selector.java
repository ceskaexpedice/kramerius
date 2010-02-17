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
	

	public void changeSelection(ImageMoveWrapper wrapper);
	
	public void markUnselect();
	
	public void markSelect();
	
}
