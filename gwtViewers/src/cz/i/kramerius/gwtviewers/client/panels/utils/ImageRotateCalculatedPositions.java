package cz.i.kramerius.gwtviewers.client.panels.utils;

import java.util.ArrayList;

import cz.i.kramerius.gwtviewers.client.panels.ImageMoveWrapper;

public class ImageRotateCalculatedPositions {
		
	private ArrayList<ImageMoveWrapper> calclulatedPositions = new ArrayList<ImageMoveWrapper>();
	private ArrayList<ImageMoveWrapper> calclulatedLeftPositions = new ArrayList<ImageMoveWrapper>();
	
	
	public ImageRotateCalculatedPositions(ImageMoveWrapper[] viewPortImages) {
		for (ImageMoveWrapper imageMoveWrapper : viewPortImages) {
			ImageMoveWrapper copy = imageMoveWrapper.copy(); {
				copy.setUrl("no-image.png");
				copy.setImageIdent("no-ident");
			}
			this.calclulatedPositions.add(copy);
			ImageMoveWrapper left = imageMoveWrapper.copy(); {
				left.setUrl("no-image.png");
				left.setImageIdent("no-ident");
			}
			this.calclulatedLeftPositions.add(left);
		}
	}

	public ArrayList<ImageMoveWrapper> getCalclulatedPositions() {
		return calclulatedPositions;
	}
	
	public ImageMoveWrapper getCaclulatePosition(int index) {
		return this.calclulatedPositions.get(index);
	}

	public ArrayList<ImageMoveWrapper> getCalclulatedLeftPositions() {
		return calclulatedLeftPositions;
	}
	
	public ImageMoveWrapper getCaclulateLeftPosition(int index) {
		return this.calclulatedLeftPositions.get(index);
	}
	
	
}
