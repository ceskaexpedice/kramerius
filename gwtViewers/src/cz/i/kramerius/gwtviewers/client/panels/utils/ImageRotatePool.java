package cz.i.kramerius.gwtviewers.client.panels.utils;

import static cz.i.kramerius.gwtviewers.client.panels.utils.NoVisibleFillHelper.*;

import java.util.ArrayList;


import com.google.gwt.user.client.ui.Widget;

import cz.i.kramerius.gwtviewers.client.SimpleImageTO;
import cz.i.kramerius.gwtviewers.client.data.DataHandler;
import cz.i.kramerius.gwtviewers.client.panels.ImageMoveWrapper;



/**
 * Pool of images.  
 * @author pavels
 */
public class ImageRotatePool {

	public static int VIEW_IMAGES_Z_INDEX = 100;
	
	public static int PANE_VIEW_Z_INDEX = 95;
	
	public static int LEFTRIGNT_IMAGES_Z_INDEX = 10;
	public static int NOVIEW_IMAGES_Z_INDEX = 1;
	
	
	// visible images
	private ArrayList<ImageMoveWrapper> viewPortImages = new ArrayList<ImageMoveWrapper>();
	private ArrayList<ImageMoveWrapper> noVisible = new ArrayList<ImageMoveWrapper>();
	// leftSide
	private ImageMoveWrapper left = null;
	// right side
	private ImageMoveWrapper right = null;
	
	
	private int ukazovatko = 0;
	
	public ImageRotatePool(ImageMoveWrapper[] viewPortImages, ImageMoveWrapper[] novisibles, ImageMoveWrapper left, ImageMoveWrapper right) {
		super();
		for (ImageMoveWrapper img : viewPortImages) {
			this.viewPortImages.add(img);
		}
		for (ImageMoveWrapper img : novisibles) {
			this.noVisible.add(img);
		}
		this.left = left;
		this.right = right;
		
		fillNoVisibleImages();
	}

	public ImageMoveWrapper getWrapper(Widget widget) {
		for (ImageMoveWrapper wrap : this.viewPortImages) {
			if (wrap.getWidget() == widget) {
				return wrap;
			}
		}
		for (ImageMoveWrapper wrap : this.noVisible) {
			if (wrap.getWidget() == widget) {
				return wrap;
			}
		};
		if (this.left.getWidget() == widget) return this.left;
		if (this.right.getWidget() == widget) return this.right;
		return null;
	}
	
	
	public boolean rollLeft() {
		rollLeftPointer();
		// pravy do viewPort
		this.viewPortImages.add(this.right);
		
		// posledni z novisible do pravy
		ImageMoveWrapper lastNoVisible = this.noVisible.remove(this.noVisible.size()-1);
		this.right = lastNoVisible;	
		// levy do novisible
		this.noVisible.add(0,this.left);
		// posledni z viewPort do levy
		this.left = this.viewPortImages.remove(0);
		
		fillNoVisibleImages();
		return true;
	}

	public void rollLeftPointer() {
		this.ukazovatko +=1;
	}
	
	public void fillNoVisibleImages() {
		fillRightNoVisible(this);
		fillLeftNoVisible(this);
		
	}
	
	public boolean rollRight() {
		rollRightPointer();
		// levy do viewPort
		this.viewPortImages.add(0,this.left);
		// prvni z novisible do levy
		ImageMoveWrapper lastNoVisible = this.noVisible.remove(0);
		this.left = lastNoVisible;	
		// pravy do novisible
		this.noVisible.add(this.right);
		this.right = this.viewPortImages.remove(this.viewPortImages.size()-1);

		fillNoVisibleImages();
		return true;
	}

	public void rollRightPointer() {
		this.ukazovatko -=1;
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


	public ImageMoveWrapper getLeftSideImage() {
		return this.left;
	}
	
	public ImageMoveWrapper getRightSideImage() {
		return this.right;
	}
	

	public ArrayList<ImageMoveWrapper> getNoVisibleImages() {
		return new ArrayList<ImageMoveWrapper>(this.noVisible);
	}
	

	public boolean canRollLeft() {
		return true;
	}


	public boolean canRollRight() {
		return true;
	}

	
	public int getPointer() {
		return this.ukazovatko;
	}
	
	public void initWithPointer(int pointer) {
		if (pointer < 0) return;
		int maxImage = DataHandler.getMax();
		int visibleImages = this.viewPortImages.size();
		int maxLeft = maxImage - visibleImages;
		this.ukazovatko = Math.min(pointer, maxLeft);

		// prenastavit viewport images
		for (int i = 0, ll= this.viewPortImages.size(); i < ll; i++) {
			SimpleImageTO ito = DataHandler.getData().get(ukazovatko+i);
			modifyImageMoveWrapper(this.viewPortImages.get(i), ito, ito.getIdentification());
		}
		
		// prenastavit levy a pravy
		SimpleImageTO lito = this.ukazovatko > 1 ?  DataHandler.getData().get(ukazovatko-1) : DataHandler.getNaImage();
		modifyImageMoveWrapper(this.left, lito, lito.getIdentification());
		int rightImageIndex = this.ukazovatko+this.viewPortImages.size();
		SimpleImageTO rito = rightImageIndex < DataHandler.getMax() ? DataHandler.getData().get(rightImageIndex) : DataHandler.getNaImage();  
		modifyImageMoveWrapper(this.right, rito, rito.getIdentification());
		this.fillNoVisibleImages();
	}
	
	
	public void debugPool() {
		System.out.println("=========> Left <=========");
		System.out.println("\t"+this.left.getIndex());
		System.out.println("=========> Visible <=========");
		StringBuffer buf = new StringBuffer();
		for (ImageMoveWrapper mv : this.viewPortImages) {
			buf.append(mv.getIndex()+",");
		}
		System.out.println("\t"+buf.toString());
		System.out.println("=========> NoVisible <=========");
		buf = new StringBuffer();
		for (ImageMoveWrapper mv : this.noVisible) {
			buf.append(mv.getIndex()+",");
		}
		System.out.println("\t"+buf.toString());
		System.out.println("=========> Right <=========");
		System.out.println("\t"+this.right.getIndex());
		
		System.out.println("___________________________________________________________");
	}
}
