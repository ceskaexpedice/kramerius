package cz.incad.kramerius.gwtviewers.client.panels.utils;

import static cz.incad.kramerius.gwtviewers.client.panels.utils.NoVisibleFillHelper.*;

import java.util.ArrayList;


import com.google.gwt.user.client.ui.Widget;

import cz.incad.kramerius.gwtviewers.client.SimpleImageTO;
import cz.incad.kramerius.gwtviewers.client.data.DataHandler;
import cz.incad.kramerius.gwtviewers.client.panels.ImageMoveWrapper;



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
	private ArrayList<ImageMoveWrapper> visible = new ArrayList<ImageMoveWrapper>();
	private ArrayList<ImageMoveWrapper> noVisible = new ArrayList<ImageMoveWrapper>();
	// leftSide
	private ImageMoveWrapper left = null;
	// right side
	private ImageMoveWrapper right = null;
	
	
	private int ukazovatko = 0;
	
	public ImageRotatePool(ArrayList<ImageMoveWrapper> visibleImgs, ArrayList<ImageMoveWrapper> novisibles, ImageMoveWrapper left, ImageMoveWrapper right, int pointer) {
		super();
		for (ImageMoveWrapper img : visibleImgs) {
			this.visible.add(img);
		}
		for (ImageMoveWrapper img : novisibles) {
			this.noVisible.add(img);
		}
		this.left = left;
		this.right = right;
		this.ukazovatko = pointer;
		fillNoVisibleImages();
	}
	
	public ImageRotatePool(ArrayList<ImageMoveWrapper> viewPortImages, ArrayList<ImageMoveWrapper> novisibles, ImageMoveWrapper left, ImageMoveWrapper right) {
		this(viewPortImages, novisibles, left, right, 0);
	}
	
	public ImageMoveWrapper getWrapper(String id) {
		for (ImageMoveWrapper wrap : this.visible) {
			if (wrap.getImageIdent().equals(id)) {
				return wrap;
			}
		}
		for (ImageMoveWrapper wrap : this.noVisible) {
			if (wrap.getImageIdent().equals(id)) {
				return wrap;
			}
		};
		if (this.left.getImageIdent().equals(id)) return this.left;
		if (this.right.getImageIdent().equals(id)) return this.right;
		return null;
		
	}

	public ImageMoveWrapper getWrapper(Widget widget) {
		for (ImageMoveWrapper wrap : this.visible) {
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
		this.visible.add(this.right);
		
		// posledni z novisible do pravy
		ImageMoveWrapper lastNoVisible = this.noVisible.remove(this.noVisible.size()-1);
		this.right = lastNoVisible;	
		// levy do novisible
		this.noVisible.add(0,this.left);
		// posledni z viewPort do levy

		this.left = this.visible.remove(0);
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
		this.visible.add(0,this.left);
		// prvni z novisible do levy
		ImageMoveWrapper lastNoVisible = this.noVisible.remove(0);
		this.left = lastNoVisible;	

		// pravy do novisible
		this.noVisible.add(this.right);
		this.right = this.visible.remove(this.visible.size()-1);

		fillNoVisibleImages();

		return true;
	}

	public void rollRightPointer() {
		this.ukazovatko -=1;
	}

	public ArrayList<ImageMoveWrapper> getVisibleImages() {
		return new ArrayList<ImageMoveWrapper>(visible);
	}
	
	public ImageMoveWrapper getVisibleImage(int index) {
		return this.visible.get(index);
	}

	public int getVisibleImageSize() {
		return visible.size();
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

	public void setPointer(int ukazovatko) {
		this.ukazovatko = ukazovatko;
	}

	public void initWithPointer(int pointer) {
		if (pointer < 0) return;
		int maxImage = DataHandler.get().getMax();
		int visibleImages = this.visible.size();
		int maxLeft = maxImage - visibleImages;
		this.ukazovatko = Math.min(pointer, maxLeft);

		// prenastavit viewport images
		for (int i = 0, ll= this.visible.size(); i < ll; i++) {
			SimpleImageTO ito = DataHandler.get().getData().get(ukazovatko+i);
			modifyImageMoveWrapper(this.visible.get(i), ito, ito.getIdentification());
		}
		
		// prenastavit levy a pravy
		SimpleImageTO lito = this.ukazovatko > 1 ?  DataHandler.get().getData().get(ukazovatko-1) : DataHandler.get().getNaImage();
		modifyImageMoveWrapper(this.left, lito, lito.getIdentification());
		int rightImageIndex = this.ukazovatko+this.visible.size();
		SimpleImageTO rito = rightImageIndex < DataHandler.get().getMax() ? DataHandler.get().getData().get(rightImageIndex) : DataHandler.get().getNaImage();  
		modifyImageMoveWrapper(this.right, rito, rito.getIdentification());
		this.fillNoVisibleImages();
	}
	
	
	public void debugPool() {
		System.out.println("=========> Left <=========");
		System.out.println("\t"+this.left.getIndex()+"("+this.left.getImageIdent()+")");
		System.out.println("=========> Visible <=========");
		StringBuffer buf = new StringBuffer();
		for (ImageMoveWrapper mv : this.visible) {
			buf.append(mv.getIndex()+"("+mv.getImageIdent()+"),");
		}
		System.out.println("\t"+buf.toString());
		System.out.println("=========> NoVisible <=========");
		buf = new StringBuffer();
		for (ImageMoveWrapper mv : this.noVisible) {
			buf.append(mv.getIndex()+"("+mv.getImageIdent()+"),");
		}
		System.out.println("\t"+buf.toString());
		System.out.println("=========> Right <=========");
		System.out.println("\t"+this.right.getIndex()+"("+right.getImageIdent()+")");
		
		System.out.println("___________________________________________________________");
	}

	public ImageMoveWrapper getNoVisibleImage(int i) {
		return this.noVisible.get(i);
	}
	
}
