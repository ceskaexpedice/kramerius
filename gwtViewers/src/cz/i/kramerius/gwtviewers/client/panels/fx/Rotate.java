package cz.i.kramerius.gwtviewers.client.panels.fx;

import java.util.ArrayList;

import org.adamtacy.client.ui.effects.NEffect;
import org.adamtacy.client.ui.effects.ParallellCompositeEffect;

import cz.i.kramerius.gwtviewers.client.panels.CoreConfiguration;
import cz.i.kramerius.gwtviewers.client.panels.ImageMoveWrapper;
import cz.i.kramerius.gwtviewers.client.panels.utils.ImageRotateCalculatedPositions;
import cz.i.kramerius.gwtviewers.client.panels.utils.ImageRotatePool;
import cz.i.kramerius.gwtviewers.client.panels.utils.Point;

/**
 * Parallel effect for moving pictures
 * @author pavels
 */
public class Rotate extends ParallellCompositeEffect {
	
	public Rotate(CoreConfiguration conf, ImageRotatePool rotatePool, ImageRotateCalculatedPositions calcPos,ArrayList<ImageMoveWrapper> oldViewPortImages) {
		super();
		
		for (int i = 0; i < rotatePool.getViewPortSize(); i++) {
			ImageMoveWrapper viewPortImage = rotatePool.getViewPortImage(i);
			ImageMoveWrapper calculated = calcPos.getViewPortCalulcatedPosition(i);
			NEffect move = viewPortImage.move(new Point(calculated.getX(), calculated.getY()));
			this.registerEffect(move);
		}
		
		ImageMoveWrapper left = rotatePool.getLeftSideImage();
		ImageMoveWrapper leftCalculated = calcPos.getLeft();
		NEffect lmove = left.move(new Point(leftCalculated.getX(), leftCalculated.getY()));
		this.registerEffect(lmove);
		
		ImageMoveWrapper right = rotatePool.getRightSideImage();
		ImageMoveWrapper rightCalculated = calcPos.getRight();
		NEffect rmove = right.move(new Point(rightCalculated.getX(), rightCalculated.getY()));
		this.registerEffect(rmove);
		
	}

}


