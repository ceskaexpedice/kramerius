package cz.incad.kramerius.gwtviewers.client.panels.utils;

public class Point {
	
	public int x = 0; public int y = 0;

	public Point() {
		super();
	}

	public Point(int x, int y) {
		super();
		this.x = x;
		this.y = y;
	}

	@Override
	public String toString() {
		return "X="+this.x +", Y="+this.y;
	}
}
