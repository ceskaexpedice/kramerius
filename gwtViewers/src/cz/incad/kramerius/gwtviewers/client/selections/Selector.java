package cz.incad.kramerius.gwtviewers.client.selections;


import cz.incad.kramerius.gwtviewers.client.GwtViewers;
import cz.incad.kramerius.gwtviewers.client.panels.ImageMoveWrapper;
import cz.incad.kramerius.gwtviewers.client.panels.MoveListener;
import cz.incad.kramerius.gwtviewers.client.panels.utils.ImageRotatePool;

/**
 * Trida pro definici selekce
 * @author pavels
 *
 */
public interface Selector extends MoveListener{

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
	

	public void changeSelection(ImageMoveWrapper wrapper, GwtViewers gwtViewers);
	
	public void markUnselect(ImageRotatePool pool);
	
	public void markSelect(ImageRotatePool pool);
	
}
