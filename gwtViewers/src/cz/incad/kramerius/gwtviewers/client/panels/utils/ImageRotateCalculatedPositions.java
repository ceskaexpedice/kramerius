package cz.incad.kramerius.gwtviewers.client.panels.utils;

import java.util.ArrayList;

import cz.incad.kramerius.gwtviewers.client.panels.ImageMoveWrapper;

public class ImageRotateCalculatedPositions {
		
	// visible images
	private ArrayList<ImageMoveWrapper> viewPortImages = new ArrayList<ImageMoveWrapper>();
	private ArrayList<ImageMoveWrapper> noVisible = new ArrayList<ImageMoveWrapper>();
	// leftSide
	private ImageMoveWrapper left = null;
	// right side
	private ImageMoveWrapper right = null;
	
	public ImageRotateCalculatedPositions( ImageRotatePool pool) {
		for (ImageMoveWrapper imageMoveWrapper : pool.getVisibleImages()) {
			ImageMoveWrapper copy = imageMoveWrapper.copy(); {
//				copy.setUrl("no-image.png");
//				copy.setImageIdent("no-ident");
			}
			this.viewPortImages.add(copy);
		}

		for (ImageMoveWrapper nvOrig : pool.getNoVisibleImages()) {
			ImageMoveWrapper nv = nvOrig.copy(); {
			}
			this.noVisible.add(nv);
		}
		
		this.left = pool.getLeftSideImage().copy();
		this.right = pool.getRightSideImage().copy();
	}

	public ArrayList<ImageMoveWrapper> getViewPortImages() {
		return viewPortImages;
	}

	public void setViewPortImages(ArrayList<ImageMoveWrapper> viewPortImages) {
		this.viewPortImages = viewPortImages;
	}

	public ArrayList<ImageMoveWrapper> getNoVisible() {
		return noVisible;
	}

	public void setNoVisible(ArrayList<ImageMoveWrapper> noVisible) {
		this.noVisible = noVisible;
	}

	public ImageMoveWrapper getLeft() {
		return left;
	}

	public void setLeft(ImageMoveWrapper left) {
		this.left = left;
	}

	public ImageMoveWrapper getRight() {
		return right;
	}

	public void setRight(ImageMoveWrapper right) {
		this.right = right;
	}

	public ImageMoveWrapper getViewPortCalulcatedPosition(int i) {
		return this.viewPortImages.get(i);
	}

}
