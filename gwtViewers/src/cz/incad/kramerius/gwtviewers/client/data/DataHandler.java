package cz.incad.kramerius.gwtviewers.client.data;

import java.util.List;

import cz.incad.kramerius.gwtviewers.client.SimpleImageTO;

public class DataHandler {


	private String masterUUID;
	private List<SimpleImageTO> data;
	private SimpleImageTO naImage;
	private int max;
	
	private String currentId;
	private int currentIndex;
	
	
	public List<SimpleImageTO> getData() {
		return data;
	}
	public void setData(List<SimpleImageTO> data) {
		this.data = data;
		naImage = createNASiTO(data.get(0));
		max = data.size();
	}
	public SimpleImageTO getNaImage() {
		return naImage;
	}


	private static SimpleImageTO createNASiTO(SimpleImageTO parent) {
		SimpleImageTO sit = new SimpleImageTO();
		sit.setFirstPage(false);
		sit.setLastPage(false);
		sit.setUrl("na.png");
		sit.setWidth(parent.getWidth());
		sit.setHeight(parent.getHeight());
		sit.setIdentification("NA");
		return sit;
	}
	
	
	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}
	
	public String getCurrentId() {
		return currentId;
	}
	public void setCurrentId(String currentId) {
		this.currentId = currentId;
	}
	public int getCurrentIndex() {
		return currentIndex;
	}
	public void setCurrentIndex(int currentIndex) {
		this.currentIndex = currentIndex;
	}

	public String getMasterUUID() {
		return masterUUID;
	}
	public void setMasterId(String masterUUID) {
		this.masterUUID = masterUUID;
	}

	public boolean anyData() {
		return this.data != null && !this.data.isEmpty();
	}


	private static DataHandler _instance = new DataHandler();
	public static DataHandler get() {
		return _instance;
	}
}
