package cz.i.kramerius.gwtviewers.client.panels.utils;

import java.util.ArrayList;

import cz.i.kramerius.gwtviewers.client.panels.ImageMoveWrapper;



public class ImageRotatePool {

	
	private ArrayList<ImageMoveWrapper> viewPortImages = new ArrayList<ImageMoveWrapper>();
	private ArrayList<ImageMoveWrapper> leftSide = new ArrayList<ImageMoveWrapper>();
	
	private int viewPortSize=2;
	
	private int ukazovatko = 0;
	
	public ImageRotatePool(ImageMoveWrapper[] viewPortImages, ImageMoveWrapper[] novisibles) {
		super();
		for (ImageMoveWrapper img : viewPortImages) {
			this.viewPortImages.add(img);
		}
	}

	
	
	
	public boolean rollLeft() {
		this.ukazovatko +=1;
		if (this.viewPortImages.size() > this.viewPortSize) {
			ImageMoveWrapper removed = this.viewPortImages.remove(0);
			this.leftSide.add(removed);
			return true;
		} else return false;
		
	}
	
	public boolean rollRight() {
		this.ukazovatko -=1;
		if (this.leftSide.size()>0) {
			ImageMoveWrapper removed = this.leftSide.remove(leftSide.size()-1);
			this.viewPortImages.add(0,removed);
			return true;
		} else return false;
	}

	public ArrayList<ImageMoveWrapper> getViewPortImages() {
		return new ArrayList<ImageMoveWrapper>(viewPortImages);
	}
	
	public ImageMoveWrapper getViewPortImage(int index) {
		return this.viewPortImages.get(index);
	}

	public int getViewPortSize() {
		return viewPortImages.size();
	}




	public ImageMoveWrapper getLeftSideImage(int i) {
		return this.leftSide.get(i);
	}




	public ArrayList<ImageMoveWrapper> getLeftSideImages() {
		return new ArrayList<ImageMoveWrapper>(this.leftSide);
	}




	public boolean canRollLeft() {
		return (this.viewPortImages.size() > this.viewPortSize);
	}




	public boolean canRollRight() {
		return this.leftSide.size() > 0;
	}

	
	public int getPointer() {
		return this.ukazovatko;
	}
}
