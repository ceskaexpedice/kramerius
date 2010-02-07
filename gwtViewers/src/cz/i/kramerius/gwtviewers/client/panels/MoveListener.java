package cz.i.kramerius.gwtviewers.client.panels;

import cz.i.kramerius.gwtviewers.client.panels.utils.ImageRotatePool;

public interface MoveListener {
	
	public void onMoveLeft(ImageRotatePool pool, boolean effectsPlayed);
	
	public void onMoveRight(ImageRotatePool pool, boolean effectsPlayed);
}
