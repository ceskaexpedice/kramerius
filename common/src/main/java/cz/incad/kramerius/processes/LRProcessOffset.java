package cz.incad.kramerius.processes;

public class LRProcessOffset {
	
	private String offset;
	private String size;

	public LRProcessOffset(String offset, String size) {
		super();
		this.offset = offset;
		this.size = size;
	}
	
	public String getSQLOffset	() {
		return "OFFSET "+this.offset+" ROWS FETCH NEXT "+this.size+" ROWS ONLY";
	}

	public String getOffset() {
		return offset;
	}

	public String getSize() {
		return size;
	}
	
	
	public String getNextOffset() {
		int iOffset = Integer.parseInt(offset);
		int iSize = Integer.parseInt(size);
		return ""+(iOffset + iSize + 1);
	}

	public String getPrevOffset() {
		int iOffset = Integer.parseInt(offset);
		int iSize = Integer.parseInt(size);
		return ""+(iOffset - iSize - 1);
	}
}
