package cz.i.kramerius.gwtviewers.client;

import java.util.ArrayList;

import com.google.gwt.junit.client.GWTTestCase;

import cz.i.kramerius.gwtviewers.client.panels.ImageMoveWrapper;
import cz.i.kramerius.gwtviewers.client.panels.utils.ImageRotatePool;
import cz.i.kramerius.gwtviewers.client.panels.utils.NoVisibleFillHelper;

public class RotatePoolTest extends GWTTestCase {

	@Override
	public String getModuleName() {
		return "cz.i.kramerius.gwtviewers.GwtViewers";
	}

	public void testToLeftToRight() {
		ImageRotatePool pool = createPool(100);
		checkRightSide(pool);
		pool.debugPool();
		for (int i = 0; i < 96; i++) { 
			pool.rollLeft(); 
			checkRightSide(pool);
			if (i > 0) checkLeftSide(pool);
			if (i>1) checkNoVisibleLeft(pool);
			if (i < 95) checkNoVisibleRight(pool);
		}
		for (int i = 0; i < 96; i++) { 
			pool.rollRight(); 
			checkRightSide(pool);
			if (i < 95) checkLeftSide(pool);
		}
		
	}

	private void checkNoVisibleRight(ImageRotatePool pool) {
		int noVisible = pool.getNoVisibleImages().get(2).getIndex();
		int right = pool.getRightSideImage().getIndex();
		assertTrue(noVisible == right+1);
	}

	private void checkRightSide(ImageRotatePool pool) {
		int maxRightVisibleIndex = pool.getViewPortImages().get(2).getIndex();
		int rightSideIndex = pool.getRightSideImage().getIndex();
		assertTrue(maxRightVisibleIndex+1 == rightSideIndex);
	}


	private void checkNoVisibleLeft(ImageRotatePool pool) {
		int novisible = pool.getNoVisibleImages().get(0).getIndex();
		int left = pool.getLeftSideImage().getIndex();
		assertTrue(left-1 == novisible);
	}

	private void checkLeftSide(ImageRotatePool pool) {
		int minLeftVisibleIndex = pool.getViewPortImages().get(0).getIndex();
		int leftSideIndex = pool.getLeftSideImage().getIndex();
		assertTrue(minLeftVisibleIndex-1 == leftSideIndex);
	}

	
	private ImageRotatePool createPool(int max) {
	
		ArrayList<SimpleImageTO> itos = DataUtils.createImages(max);
		DataHandler.setData(itos);
		
		ImageMoveWrapper[] viewPortImages = new ImageMoveWrapper[3];
		for (int i = 0; i < viewPortImages.length; i++) {
			SimpleImageTO ito = itos.get(i);
			ImageMoveWrapper wrapper = GwtViewers.createImageMoveWrapper(ito,""+i);
			viewPortImages[i] = wrapper;
		}
		

		int width = 5;
		for (ImageMoveWrapper imw : viewPortImages) {
			width +=  imw.getWidth();
			width += 5;
		}

		ImageMoveWrapper rcopy = GwtViewers.createImageMoveWrapper(DataHandler.getData().get(3),"R");

		ImageMoveWrapper[] noVisibleImages = new ImageMoveWrapper[3];
		for (int i = 0; i < noVisibleImages.length; i++) {
			SimpleImageTO ito = itos.get(i+4);
			ImageMoveWrapper wrapper = GwtViewers.createImageMoveWrapper(ito,"n"+i);
			noVisibleImages[i] = wrapper;
		}
		
		ImageMoveWrapper lcopy = GwtViewers.createImageMoveWrapper(DataHandler.getNaImage(),"L");
		ImageRotatePool pool = new ImageRotatePool(viewPortImages, noVisibleImages, lcopy, rcopy, 300);
		return pool;
	}

}
