package cz.incad.kramerius.ngwt.client;

import java.io.Serializable;

public class ImageMetadata implements Serializable {

	private String url;
	private int width;
	private int height;
	private int offset;
	
	private String identification;
	private boolean lastPage;
	private boolean firstPage;
	private int index;

	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
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
	
	public String getIdentification() {
		return identification;
	}
	
	public void setIdentification(String identification) {
		this.identification = identification;
	}
	
	public boolean isLastPage() {
		return lastPage;
	}
	
	public void setLastPage(boolean lastPage) {
		this.lastPage = lastPage;
	}
	
	public boolean isFirstPage() {
		return firstPage;
	}
	
	public void setFirstPage(boolean firstPage) {
		this.firstPage = firstPage;
	}
	
	public int getIndex() {
		return index;
	}
	
	public void setIndex(int index) {
		this.index = index;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}
	
	
}
