package cz.incad.kramerius.gwtviewers.client.panels;

import cz.incad.kramerius.gwtviewers.client.panels.utils.ImageRotatePool;

/**
 * Listeners that can observe events from Effects pane
 * @author pavels
 */
public interface MoveListener {

	public void onMoveLeft(ImageRotatePool pool, boolean effectsPlayed);
	
	public void onMoveRight(ImageRotatePool pool, boolean effectsPlayed);

	public void onPointerLeft(ImageRotatePool pool);
	
	public void onPointerRight(ImageRotatePool pool);
}
