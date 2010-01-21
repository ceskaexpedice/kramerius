package cz.i.kramerius.gwtviewers.client.panels;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;


public class ImageCore {

	private int x;
	private int y;
	private int width;
	private int height;
	private String url;
	private Image image = new Image();
	private String imageIdent;
	
	public ImageCore(int x, int y, int width, int height, String url, String imageIdent) {
		super();
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.url = url;
		this.image.setUrl(url);
		this.imageIdent = imageIdent;
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
		this.image.setUrl(url);
	}

	public Widget getWidget() {
		return image;
	}

	public void setImage(Image image) {
		this.image = image;
	}

	public String getImageIdent() {
		return imageIdent;
	}
}
