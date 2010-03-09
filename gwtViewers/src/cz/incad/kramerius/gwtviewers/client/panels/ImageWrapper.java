package cz.incad.kramerius.gwtviewers.client.panels;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;


/**
 * Basic properties for image
 * @author pavels
 */
public class ImageWrapper {

	private int x;
	private int y;
	private int width;
	private int height;
	private String url;
	private String imageIdent;
	private int index;
	
	// TODO: Vyhodit, udelat jinak
	private Image image = null;

	public ImageWrapper(int x, int y, int width, int height, String imageIdent) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.imageIdent = imageIdent;
	}
	public ImageWrapper(int x, int y, int width, int height, String url, String imageIdent) {
		this(x,y,width,height,imageIdent);
		this.url = url;
		if (this.image == null) {
			getImage().setUrl(url);
		}
	}


	public void setImageIdent(String imageIdent) {
		this.imageIdent = imageIdent;
	}


	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
		this.getImage().setUrl(url);
	}

	public Image getImage() {
		if (this.image == null) {
			this.image = new Image();
		}
		return image;
	}
	
	public Widget getWidget() {
		return getImage();
	}

	public void setImage(Image image) {
		this.image = image;
	}

	public String getImageIdent() {
		return imageIdent;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public void modifyHeightAndWidth() {
		this.image.setWidth(this.width+"px");
		this.image.setHeight(this.height+"px");
	}
	
}
