package cz.incad.kramerius.ngwt.client;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gwt.user.client.rpc.IsSerializable;


public class PagesResultSet implements IsSerializable {
	

	private HashMap<Integer, Integer> indexToPositionMapping;
	
	private ArrayList<ImageMetadata> data;
	private int width;
	private int avgWidth;
	
	public PagesResultSet() {
		super();
	}

	public int getWidth() {
		return width;
	}


	public void setWidth(int width) {
		this.width = width;
	}


	public ArrayList<ImageMetadata> getData() {
		return data;
	}

	
	


	public void setData(ArrayList<ImageMetadata> data) {
		this.data = data;
	}

	public HashMap<Integer, Integer> getIndexToPositionMapping() {
		return indexToPositionMapping;
	}

	public void setIndexToPositionMapping(
			HashMap<Integer, Integer> indexToPositionMapping) {
		this.indexToPositionMapping = indexToPositionMapping;
	}

	public int getAvgWidth() {
		return avgWidth;
	}

	public void setAvgWidth(int avgWidth) {
		this.avgWidth = avgWidth;
	}


	
	
}
