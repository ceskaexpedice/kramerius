package cz.incad.kramerius.gwtviewers.client.panels.utils;

public class Dimension {

	public int width = 0; public int height = 0;

	public Dimension() {
		super();
	}

	public Dimension(int width, int height) {
		super();
		this.width = width;
		this.height = height;
	}



	@Override
	public String toString() {
		return "width="+this.width +", height="+this.height;
	}

}
