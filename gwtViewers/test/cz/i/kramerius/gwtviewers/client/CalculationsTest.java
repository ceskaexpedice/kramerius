package cz.i.kramerius.gwtviewers.client;

import java.util.ArrayList;


import com.google.gwt.junit.client.GWTTestCase;

import cz.i.kramerius.gwtviewers.client.panels.Configuration;
import cz.i.kramerius.gwtviewers.client.panels.ImageMoveWrapper;
import cz.i.kramerius.gwtviewers.client.panels.utils.CalculationHelper;
import cz.i.kramerius.gwtviewers.client.panels.utils.ImageRotateCalculatedPositions;
import cz.i.kramerius.gwtviewers.client.panels.utils.ImageRotatePool;

public class CalculationsTest extends GWTTestCase {

	@Override
	public String getModuleName() {
		return "cz.i.kramerius.gwtviewers.GwtViewers";
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
		ImageMoveWrapper[] wrappers = generateImages();
		ImageMoveWrapper[] noVisible = generateCopies(wrappers);
		ImageMoveWrapper left = generateLeft(noVisible);
		ImageMoveWrapper right = generateRight(noVisible);
		Configuration coreConfiguration = generateConfiguration();
		
		ImageRotateCalculatedPositions imageRotateCalculatedPositions = new ImageRotateCalculatedPositions(wrappers, noVisible, left, right);
		ImageRotatePool imageRotatePool = new ImageRotatePool(wrappers, noVisible, left, right,2);
		
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

	
	
	private Configuration generateConfiguration() {
		Configuration coreConfiguration = new Configuration();
		coreConfiguration.setViewPortHeight(100);
		coreConfiguration.setViewPortWidth(300);
		return coreConfiguration;
	}

	private ImageMoveWrapper generateRight(ImageMoveWrapper[] noVisible) {
		ImageMoveWrapper right = noVisible[0].copy();
		{
			right.setImageIdent("right");
		}
		return right;
	}

	private ImageMoveWrapper generateLeft(ImageMoveWrapper[] noVisible) {
		ImageMoveWrapper left = noVisible[0].copy();
		{
			left.setImageIdent("left");
		}
		return left;
	}

	private ImageMoveWrapper[] generateCopies(ImageMoveWrapper[] wrappers) {
		ImageMoveWrapper[] noVisible = new ImageMoveWrapper[10];
		for (int i = 0; i < noVisible.length; i++) {
			noVisible[i] = wrappers[i].copy();
			noVisible[i].setImageIdent("novisible["+i+"]");
			noVisible[i].debugXY();
		}
		return noVisible;
	}

	private ImageMoveWrapper[] generateImages() {
		ImageMoveWrapper[] wrappers = new ImageMoveWrapper[10];
		for (int i = 0; i < wrappers.length; i++) {
			wrappers[i] = new ImageMoveWrapper(0, 0, 100, 100, "nourl", "visible["+i+"]");
		}
		return wrappers;
	}

	private void deb(ImageRotatePool imageRotatePool) {
		ArrayList<ImageMoveWrapper> viewPortImages = imageRotatePool.getViewPortImages();
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
