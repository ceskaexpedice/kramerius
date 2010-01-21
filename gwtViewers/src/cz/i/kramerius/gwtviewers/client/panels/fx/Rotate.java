package cz.i.kramerius.gwtviewers.client.panels.fx;

import java.util.ArrayList;

import org.adamtacy.client.ui.effects.ParallellCompositeEffect;

import cz.i.kramerius.gwtviewers.client.panels.CoreConfiguration;
import cz.i.kramerius.gwtviewers.client.panels.ImageMoveWrapper;
import cz.i.kramerius.gwtviewers.client.panels.utils.ImageRotateCalculatedPositions;
import cz.i.kramerius.gwtviewers.client.panels.utils.ImageRotatePool;
import cz.i.kramerius.gwtviewers.client.panels.utils.Point;

public class Rotate extends ParallellCompositeEffect {
	

	public Rotate(CoreConfiguration conf, ImageRotatePool rotatePool, ImageRotateCalculatedPositions calcPos,ArrayList<ImageMoveWrapper> oldViewPortImages) {
		super();
		
		for (int i = 0; i < rotatePool.getViewPortSize(); i++) {
			ImageMoveWrapper viewPortImage = rotatePool.getViewPortImage(i);
			ImageMoveWrapper calculated = calcPos.getCaclulatePosition(i);
			CustomMove moveLeft = viewPortImage.moveLeft(new Point(calculated.getX(), calculated.getY()));
			this.registerEffect(moveLeft);
		}
		

		ArrayList<ImageMoveWrapper> leftSideImages = rotatePool.getLeftSideImages();
		for (int i = leftSideImages.size()-1; i >=0; i--) {
			ImageMoveWrapper leftImage = leftSideImages.get(i);
			ImageMoveWrapper calculated = calcPos.getCaclulateLeftPosition(i);
			CustomMove moveLeft = leftImage.moveLeft(new Point(calculated.getX(), calculated.getY()));
			
			this.registerEffect(moveLeft);
		}
		
	}

}


