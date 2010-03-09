package cz.incad.kramerius.gwtviewers.client.panels.utils;

import java.util.ArrayList;

import cz.incad.kramerius.gwtviewers.client.panels.ImageMoveWrapper;
import cz.incad.kramerius.gwtviewers.client.panels.ViewConfiguration;

public class CalculationHelper {

	public static void computePositions(ImageRotatePool imageRotatePool, ImageRotateCalculatedPositions imageRotateCalculatedPositions, ViewConfiguration configuration) {
		ArrayList<ImageMoveWrapper> viewPortImages = imageRotatePool.getVisibleImages();
		int previousImgWidth = 0;
		for (int i = 0; i < viewPortImages.size(); i++) {
			ImageMoveWrapper imageDTO = viewPortImages.get(i);
			int x =  previousImgWidth +configuration.getImgDistances() ;
			int y = configuration.getTop();
			ImageMoveWrapper calculatedPosition = imageRotateCalculatedPositions.getViewPortCalulcatedPosition(i);
			calculatedPosition.setX(x);
			calculatedPosition.setY(y);
			calculatedPosition.setImageIdent(imageDTO.getImageIdent());
			previousImgWidth =  x+imageDTO.getWidth() ;
		}
		
		ImageMoveWrapper left = imageRotatePool.getLeftSideImage();
		ImageMoveWrapper calculatedLeft = imageRotateCalculatedPositions.getLeft();
		calculatedLeft.setX(-left.getWidth());
		calculatedLeft.setY(configuration.getTop());
		calculatedLeft.setImageIdent(left.getImageIdent());
		
		ImageMoveWrapper right = imageRotatePool.getRightSideImage();
		ImageMoveWrapper calculatedRight = imageRotateCalculatedPositions.getRight();
		calculatedRight.setX(configuration.getViewPortWidth());
		calculatedRight.setY(configuration.getTop());
		calculatedRight.setImageIdent(right.getImageIdent());
	
		previousImgWidth = 0;
		ArrayList<ImageMoveWrapper> noVisibleImages = imageRotatePool.getNoVisibleImages();
		for (int i = 0; i < noVisibleImages.size(); i++) {
			ImageMoveWrapper noVisib = noVisibleImages.get(i);
			noVisib.setX(viewPortImages.get(i).getX());
			noVisib.setY(-500);
			
//			int x =  previousImgWidth +configuration.getImgDistances() ;
//			if (i == 0) x = -noVisib.getWidth()-configuration.getCenterWidth();
//			if (i == noVisibleImages.size()-1) x = configuration.getViewPortWidth();
//			int y = -noVisib.getHeight() - 10;
//			noVisib.setX(x);
//			//noVisib.propagateXToElement();
//			noVisib.setY(y);
//			noVisib.propagateYToElement();
//			previousImgWidth =  x+noVisibleImages.get(i).getWidth() ;
		}
//		for (ImageMoveWrapper noVisible : noVisibleImages) {
//			noVisible.setX(-500);
//			noVisible.setY(-500);
//			//noVisible.setImageIdent("no-ident");
//		}
	}

	public static void storePositions(ImageRotatePool imageRotatePool, ImageRotateCalculatedPositions imageRotateCalculatedPositions, ViewConfiguration configuration) {
		ArrayList<ImageMoveWrapper> viewPortImages = imageRotatePool.getVisibleImages();
		ArrayList<ImageMoveWrapper> calclulatedPositions = imageRotateCalculatedPositions.getViewPortImages();
		for (int i = 0; i < viewPortImages.size(); i++) {
			ImageMoveWrapper calculatedCopy = calclulatedPositions.get(i);
			ImageMoveWrapper viewPortImage = viewPortImages.get(i);
			viewPortImage.setX(calculatedCopy.getX());
			viewPortImage.setY(calculatedCopy.getY());
		}
	
		ImageMoveWrapper left = imageRotatePool.getLeftSideImage();
		ImageMoveWrapper leftCopy = imageRotateCalculatedPositions.getLeft();
		left.setX(leftCopy.getX());
		left.setY(leftCopy.getY());
		
		ImageMoveWrapper right = imageRotatePool.getRightSideImage();
		ImageMoveWrapper rightCopy = imageRotateCalculatedPositions.getRight();
		right.setX(rightCopy.getX());
		right.setY(rightCopy.getY());
	}

}
