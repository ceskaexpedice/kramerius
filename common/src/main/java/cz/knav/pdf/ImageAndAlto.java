package cz.knav.pdf;

import org.w3c.dom.Document;

import com.lowagie.text.Image;


public class ImageAndAlto {
	
	Image image;
	Document alto;
	
	public ImageAndAlto(Image image, Document alto) {
		super();
		this.image = image;
		this.alto = alto;
	}
	
	public Image getImage() {
		return image;
	}
	public Document getAlto() {
		return alto;
	}

}