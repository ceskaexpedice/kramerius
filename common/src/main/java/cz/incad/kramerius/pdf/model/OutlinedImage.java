package cz.incad.kramerius.pdf.model;

import com.qbizm.kramerius.ext.ontheflypdf.edi.Element;
import com.qbizm.kramerius.ext.ontheflypdf.edi.Image;

/**
 * Represents scanned image with some description
 * @author pavels
 */
public class OutlinedImage extends Element implements Outlineable<Image>{

	private String dest;
	private Image image;
	
	public OutlinedImage(String dest, Image image) {
		super();
		this.dest = dest;
		this.image = image;
	}

	
	public Image getImage() {
		return image;
	}
	
	public void setImage(Image image) {
		this.image = image;
	}

	public int getHeight() {
		return image.getHeight();
	}

	public String getHref() {
		return image.getHref();
	}

	public int getWidth() {
		return image.getWidth();
	}

	@Override
	public String getDest() {
		return this.dest;
	}


	@Override
	public Image getWrapped() {
		return this.image;
	}
}
