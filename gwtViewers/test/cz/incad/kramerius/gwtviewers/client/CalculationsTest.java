package cz.incad.kramerius.gwtviewers.client;

import java.util.ArrayList;


import com.google.gwt.junit.client.GWTTestCase;

import cz.incad.kramerius.gwtviewers.client.data.DataHandler;
import cz.incad.kramerius.gwtviewers.client.panels.ImageMoveWrapper;
import cz.incad.kramerius.gwtviewers.client.panels.ViewConfiguration;
import cz.incad.kramerius.gwtviewers.client.panels.utils.CalculationHelper;
import cz.incad.kramerius.gwtviewers.client.panels.utils.ImageRotateCalculatedPositions;
import cz.incad.kramerius.gwtviewers.client.panels.utils.ImageRotatePool;

public class CalculationsTest extends GWTTestCase {

	@Override
	public String getModuleName() {
		return "cz.incad.kramerius.gwtviewers.GwtViewers";
	}

//	public void testRight() {
//		ImageMoveWrapper[] wrappers = generateImages();
//		ImageMoveWrapper[] noVisible = generateCopies(wrappers);
//		ImageMoveWrapper left = generateLeft(noVisible);
//		ImageMoveWrapper right = generateRight(noVisible);
//		CoreConfiguration coreConfiguration = generateConfiguration();
//		
//		ImageRotateCalculatedPositions imageRotateCalculatedPositions = new ImageRotateCalculatedPositions(wrappers, noVisible, left, right);
//		ImageRotatePool imageRotatePool = new ImageRotatePool(wrappers, noVisible, left, right,2);
//		
//		Helper.computePositions(imageRotatePool, imageRotateCalculatedPositions, coreConfiguration);
//		Helper.storePositions(imageRotatePool, imageRotateCalculatedPositions, coreConfiguration);
//		
//		deb(imageRotatePool);
//		System.out.println("---- <<>> ----");
//
//		imageRotatePool.rollRight();
//		Helper.computePositions(imageRotatePool, imageRotateCalculatedPositions, coreConfiguration);
//		Helper.storePositions(imageRotatePool, imageRotateCalculatedPositions, coreConfiguration);
//		deb(imageRotatePool);
//		System.out.println("---- <<>> ----");
//	}
	
//	public void testLeft() {
//		ImageMoveWrapper[] wrappers = generateImages();
//		ImageMoveWrapper[] noVisible = generateCopies(wrappers);
//		ImageMoveWrapper left = generateLeft(noVisible);
//		ImageMoveWrapper right = generateRight(noVisible);
//		CoreConfiguration coreConfiguration = generateConfiguration();
//		
//		ImageRotateCalculatedPositions imageRotateCalculatedPositions = new ImageRotateCalculatedPositions(wrappers, noVisible, left, right);
//		ImageRotatePool imageRotatePool = new ImageRotatePool(wrappers, noVisible, left, right,2);
//		
//		Helper.computePositions(imageRotatePool, imageRotateCalculatedPositions, coreConfiguration);
//		Helper.storePositions(imageRotatePool, imageRotateCalculatedPositions, coreConfiguration);
//		
//		deb(imageRotatePool);
//		System.out.println("---- <<AFTER INIT>> ----");
//
//		imageRotatePool.rollLeft();
//		Helper.computePositions(imageRotatePool, imageRotateCalculatedPositions, coreConfiguration);
//		Helper.storePositions(imageRotatePool, imageRotateCalculatedPositions, coreConfiguration);
//		deb(imageRotatePool);
//		System.out.println("---- <<AFTER ROLL>> ----");
//	}


	public void testCase() {
		
		ArrayList<ImageMoveWrapper> wrappers = generateImages();
		ArrayList<ImageMoveWrapper>  noVisible = generateCopies(wrappers);
		//DataHandler.get().setData();
		ImageMoveWrapper left = generateLeft(noVisible);
		ImageMoveWrapper right = generateRight(noVisible);
		ViewConfiguration coreConfiguration = generateConfiguration();
		
		ImageRotatePool imageRotatePool = new ImageRotatePool(wrappers, noVisible, left, right);
		ImageRotateCalculatedPositions imageRotateCalculatedPositions = new ImageRotateCalculatedPositions(imageRotatePool);
		
		CalculationHelper.computePositions(imageRotatePool, imageRotateCalculatedPositions, coreConfiguration);
		CalculationHelper.storePositions(imageRotatePool, imageRotateCalculatedPositions, coreConfiguration);
		
		deb(imageRotatePool);
		System.out.println("==================");
	
		imageRotatePool.rollRight();
		CalculationHelper.computePositions(imageRotatePool, imageRotateCalculatedPositions, coreConfiguration);
		CalculationHelper.storePositions(imageRotatePool, imageRotateCalculatedPositions, coreConfiguration);
		System.out.println("---- >> AFTER RIGHT ----");
		deb(imageRotatePool);
		
		imageRotatePool.rollRight();
		CalculationHelper.computePositions(imageRotatePool, imageRotateCalculatedPositions, coreConfiguration);
		CalculationHelper.storePositions(imageRotatePool, imageRotateCalculatedPositions, coreConfiguration);
		System.out.println("---- >> AFTER RIGHT ----");
		deb(imageRotatePool);

		imageRotatePool.rollLeft();
		CalculationHelper.computePositions(imageRotatePool, imageRotateCalculatedPositions, coreConfiguration);
		CalculationHelper.storePositions(imageRotatePool, imageRotateCalculatedPositions, coreConfiguration);
		System.out.println("---- << AFTER LEFT ----");
		deb(imageRotatePool);

		imageRotatePool.rollLeft();
		CalculationHelper.computePositions(imageRotatePool, imageRotateCalculatedPositions, coreConfiguration);
		CalculationHelper.storePositions(imageRotatePool, imageRotateCalculatedPositions, coreConfiguration);
		System.out.println("---- << AFTER LEFT ----");
		deb(imageRotatePool);

	}

	
	
	private ViewConfiguration generateConfiguration() {
		ViewConfiguration coreConfiguration = ViewConfiguration.getConfiguration();
		coreConfiguration.setViewPortHeight(100);
		coreConfiguration.setViewPortWidth(300);
		return coreConfiguration;
	}

	private ImageMoveWrapper generateRight(ArrayList<ImageMoveWrapper> noVisible) {
		ImageMoveWrapper right = noVisible.get(0).copy();
		{
			right.setImageIdent("right");
		}
		return right;
	}

	private ImageMoveWrapper generateLeft(ArrayList<ImageMoveWrapper> noVisible) {
		ImageMoveWrapper left = noVisible.get(0).copy();
		{
			left.setImageIdent("left");
		}
		return left;
	}

	private ArrayList<ImageMoveWrapper> generateCopies(ArrayList<ImageMoveWrapper> wrappers) {
		int size = 10;
		ArrayList<ImageMoveWrapper> noVisible = new ArrayList<ImageMoveWrapper>(size);
		for (int i = 0; i < size; i++) {
			ImageMoveWrapper copy = wrappers.get(i).copy();
			copy.setImageIdent("novisible["+i+"]");
			copy.debugXY();
			noVisible.add(copy);
		}
		return noVisible;
	}

	private ArrayList<ImageMoveWrapper> generateImages() {
		int size = 10;
		ArrayList<ImageMoveWrapper> wrappers = new ArrayList<ImageMoveWrapper>(size);
		for (int i = 0; i < size; i++) {
			wrappers.add(new ImageMoveWrapper(0, 0, 100, 100, "nourl", "visible["+i+"]"));
		}
		return wrappers;
	}

	private void deb(ImageRotatePool imageRotatePool) {
		ArrayList<ImageMoveWrapper> viewPortImages = imageRotatePool.getVisibleImages();
		for (ImageMoveWrapper vwPI : viewPortImages) {
			vwPI.debugXY();
		}
		System.out.println("|>");
		imageRotatePool.getLeftSideImage().debugXY();
		imageRotatePool.getRightSideImage().debugXY();
		System.out.println("|>");
		
		for (ImageMoveWrapper noVis : imageRotatePool.getNoVisibleImages()) {
			noVis.debugXY();
		}
	}
	
}
