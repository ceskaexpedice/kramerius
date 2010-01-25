package cz.i.kramerius.gwtviewers.client.panels;

/**
 * Konfigurace viewPanelu
 * @author pavels
 *
 */
public class CoreConfiguration {

	private int viewPortWidth;
	private int viewPortHeight;
	
	private int imgDistances;

	
	public int getViewPortWidth() {
		return viewPortWidth;
	}

	public void setViewPortWidth(int viewPortWidth) {
		this.viewPortWidth = viewPortWidth;
	}

	public int getViewPortHeight() {
		return viewPortHeight;
	}

	public void setViewPortHeight(int viewPortHeight) {
		this.viewPortHeight = viewPortHeight;
	}

	public int getImgDistances() {
		return imgDistances;
	}

	public void setImgDistances(int imgDistances) {
		this.imgDistances = imgDistances;
	}
	
	public int getCenterWidth() {
		return this.viewPortWidth / 2;
	}
}
