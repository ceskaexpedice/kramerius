package cz.incad.kramerius.gwtviewers.client;

import java.util.ArrayList;

import com.google.gwt.junit.client.GWTTestCase;

import cz.incad.kramerius.gwtviewers.client.GwtViewers;
import cz.incad.kramerius.gwtviewers.client.SimpleImageTO;
import cz.incad.kramerius.gwtviewers.client.data.DataHandler;
import cz.incad.kramerius.gwtviewers.client.panels.ImageMoveWrapper;
import cz.incad.kramerius.gwtviewers.client.panels.ViewConfiguration;
import cz.incad.kramerius.gwtviewers.client.panels.utils.ImageRotatePool;
import cz.incad.kramerius.gwtviewers.client.panels.utils.NoVisibleFillHelper;

public class ChangeSelectionTest extends GWTTestCase {

	@Override
	public String getModuleName() {
		return "cz.incad.kramerius.gwtviewers.GwtViewers";
	}

	
	public void testLeftRight() {
		ImageRotatePool pool = createDrobnustkyPool();
		pool.debugPool();
//		checkRightSide(pool);
//		pool.debugPool();
		pool.rollLeft();
		pool.debugPool();
		pool.rollRight();
		pool.debugPool();
	}
	
//	public void testToLeftToRight() {
//		ImageRotatePool pool = createPool(100);
//		checkRightSide(pool);
//		pool.debugPool();
//		for (int i = 0; i < 96; i++) { 
//			pool.rollLeft(); 
//			checkRightSide(pool);
//			if (i > 0) checkLeftSide(pool);
//			if (i>1) checkNoVisibleLeft(pool);
//			if (i < 95) checkNoVisibleRight(pool);
//		}
//		for (int i = 0; i < 96; i++) { 
//			pool.rollRight(); 
//			checkRightSide(pool);
//			if (i < 95) checkLeftSide(pool);
//		}
//	}
//
//	public void testInit() {
//		ImageRotatePool poolToRotate = createPool(100);
//		checkRightSide(poolToRotate);
//		ImageRotatePool poolToInit = createPool(100);
//		poolToRotate.debugPool();
//		for (int i = 0; i < 96; i++) { 
//			poolToRotate.rollLeft(); 
//			checkRightSide(poolToRotate);
//			if (i > 0) checkLeftSide(poolToRotate);
//			if (i>1) checkNoVisibleLeft(poolToRotate);
//			if (i < 95) checkNoVisibleRight(poolToRotate);
//		}
//		for (int i = 0; i < 96; i++) {
//			poolToInit.rollLeftPointer();
//		}
//		poolToInit.initWithPointer(poolToInit.getPointer());
//
//		assertTrue(poolToRotate.getPointer() == poolToInit.getPointer());
//		assertTrue(poolToRotate.getLeftSideImage().getImageIdent().equals(poolToInit.getLeftSideImage().getImageIdent()));
//		assertTrue(poolToRotate.getRightSideImage().getImageIdent().equals(poolToInit.getRightSideImage().getImageIdent()));
//		testIWCollections(poolToRotate.getViewPortImages(), poolToInit.getViewPortImages());
//		testIWCollections(poolToRotate.getNoVisibleImages(), poolToInit.getNoVisibleImages());
//	}

	private void testIWCollections(
			ArrayList<ImageMoveWrapper> rotateViewImages,
			ArrayList<ImageMoveWrapper> initViewImages) {
		for (int i = 0; i < rotateViewImages.size(); i++) {
			ImageMoveWrapper iwRotate = rotateViewImages.get(i);
			ImageMoveWrapper iwInit = initViewImages.get(i);
			assertTrue(iwRotate.getImageIdent().equals(iwInit.getImageIdent()));
		}
	}
	private void checkNoVisibleRight(ImageRotatePool pool) {
		int noVisible = pool.getNoVisibleImages().get(2).getIndex();
		int right = pool.getRightSideImage().getIndex();
		assertTrue(noVisible == right+1);
	}

	private void checkRightSide(ImageRotatePool pool) {
		int maxRightVisibleIndex = pool.getVisibleImages().get(2).getIndex();
		int rightSideIndex = pool.getRightSideImage().getIndex();
		assertTrue(maxRightVisibleIndex+1 == rightSideIndex);
	}


	private void checkNoVisibleLeft(ImageRotatePool pool) {
		int novisible = pool.getNoVisibleImages().get(0).getIndex();
		int left = pool.getLeftSideImage().getIndex();
		assertTrue(left-1 == novisible);
	}

	private void checkLeftSide(ImageRotatePool pool) {
		int minLeftVisibleIndex = pool.getVisibleImages().get(0).getIndex();
		int leftSideIndex = pool.getLeftSideImage().getIndex();
		assertTrue(minLeftVisibleIndex-1 == leftSideIndex);
	}

	
	private ImageRotatePool createDrobnustkyPool() {
		String idsString = "4308eb80-b03b-11dd-a0f6-000d606f5dc6, 4a79bd50-af36-11dd-a60c-000d606f5dc6, 430d7f60-b03b-11dd-82fa-000d606f5dc6, 4a7c2e50-af36-11dd-9643-000d606f5dc6, 43101770-b03b-11dd-8673-000d606f5dc6, 4a7ec660-af36-11dd-a782-000d606f5dc6, 4314ab50-b03b-11dd-89db-000d606f5dc6, 4a80c230-af36-11dd-ace4-000d606f5dc6, 43171c50-b03b-11dd-b0c2-000d606f5dc6, 4a835a40-af36-11dd-b951-000d606f5dc6, 4319b460-b03b-11dd-83ca-000d606f5dc6, 4a85f250-af36-11dd-8535-000d606f5dc6, 431e4840-b03b-11dd-8818-000d606f5dc6, 4a8a8630-af36-11dd-ae9c-000d606f5dc6, 4320e050-b03b-11dd-9b4a-000d606f5dc6, 4a8cf730-af36-11dd-ae88-000d606f5dc6";
		String[] ids = idsString.split(",");
		ArrayList<SimpleImageTO> itos = DataUtils.createImages(ids);
		DataHandler.get().setData(itos);
		// pozice 0
		int numberOfImages = 7;
		
		ArrayList<ImageMoveWrapper> viewPortImages = new ArrayList<ImageMoveWrapper>(numberOfImages);
		for (int i = 0; i < numberOfImages; i++) {
			ImageMoveWrapper wrapper = GwtViewers.createImageMoveWrapper(i,""+i);
			wrapper.getWidget().getElement().getStyle().setZIndex(ImageRotatePool.VIEW_IMAGES_Z_INDEX);
			viewPortImages.add(wrapper);
			//viewPortImages[i] = wrapper;
		}
		

		//TODO: Zmenit
		ImageMoveWrapper rcopy = GwtViewers.createImageMoveWrapper(numberOfImages,"R");
		rcopy.getWidget().getElement().getStyle().setZIndex(ImageRotatePool.LEFTRIGNT_IMAGES_Z_INDEX);

		ArrayList<ImageMoveWrapper> noVisibleImages = new ArrayList<ImageMoveWrapper>();
		//ImageMoveWrapper[] noVisibleImages = new ImageMoveWrapper[numberOfImages];
		for (int i = 0; i < numberOfImages; i++) {
			ImageMoveWrapper wrapper = GwtViewers.createImageMoveWrapper(i+numberOfImages+1,"n"+i);
			wrapper.getWidget().getElement().getStyle().setZIndex(ImageRotatePool.NOVIEW_IMAGES_Z_INDEX);
			noVisibleImages.add(wrapper);
		}
		
		ImageMoveWrapper lcopy = GwtViewers.createImageMoveWrapper(DataHandler.get().getMax(),"L");
		lcopy.getWidget().getElement().getStyle().setZIndex(ImageRotatePool.LEFTRIGNT_IMAGES_Z_INDEX);
		
		ImageRotatePool pool = new ImageRotatePool(viewPortImages, noVisibleImages, lcopy, rcopy);
		return pool;
	}
	
	private ImageRotatePool createPool(int max) {
		ArrayList<SimpleImageTO> itos = DataUtils.createImages(max);
		DataHandler.get().setData(itos);
		
		ArrayList<ImageMoveWrapper> viewPortImages = new ArrayList<ImageMoveWrapper>();
		for (int i = 0; i < 3; i++) {
			ImageMoveWrapper wrapper = GwtViewers.createImageMoveWrapper(i,""+i);
			viewPortImages.add(wrapper);
		}
		

		int width = 5;
		for (ImageMoveWrapper imw : viewPortImages) {
			width +=  imw.getWidth();
			width += 5;
		}

		ImageMoveWrapper rcopy = GwtViewers.createImageMoveWrapper(3,"R");

		ArrayList<ImageMoveWrapper> noVisibleImages = new ArrayList<ImageMoveWrapper>(3);
		for (int i = 0; i < 3; i++) {
			ImageMoveWrapper wrapper = GwtViewers.createImageMoveWrapper(i+4,"n"+i);
			viewPortImages.add(wrapper);
		}
		
		ImageMoveWrapper lcopy = GwtViewers.createImageMoveWrapper(DataHandler.get().getMax(),"L");
		ImageRotatePool pool = new ImageRotatePool(viewPortImages, noVisibleImages, lcopy, rcopy);
		return pool;
	}

}
