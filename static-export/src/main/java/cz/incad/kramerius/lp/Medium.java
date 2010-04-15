package cz.incad.kramerius.lp;

/**
 * Medium enums
 * @author pavels
 */
public enum Medium {
	/**
	 * Standard CD
	 */
	CD(650l<<20), 
	/**
	 * Standard DVD
	 */
	DVD(4l<<30);
	
	private long size;
	
	private Medium(long size) {
		this.size = size;
	}
	
	public long getSize() {
		return this.size;
	}
	
}
