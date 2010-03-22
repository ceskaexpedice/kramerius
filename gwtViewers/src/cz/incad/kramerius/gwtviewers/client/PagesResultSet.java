package cz.incad.kramerius.gwtviewers.client;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.IsSerializable;

public class PagesResultSet implements IsSerializable {

	// data
	ArrayList<SimpleImageTO> data;
	// koren stromu
	String masterSimpleImageTOId;
	// prave vybrany
	String currentSimpleImageTOId;
	int currentSimpleImageTOIndex;
	
	public PagesResultSet() {
		super();
	}

	public ArrayList<SimpleImageTO> getData() {
		return data;
	}

	public String getCurrentSimpleImageTOId() {
		return currentSimpleImageTOId;
	}

	public int getCurrentSimpleImageTOIndex() {
		return currentSimpleImageTOIndex;
	}

	public void setData(ArrayList<SimpleImageTO> data) {
		this.data = data;
	}

	public void setCurrentSimpleImageTOId(String currentSimpleImageTOId) {
		this.currentSimpleImageTOId = currentSimpleImageTOId;
	}

	public void setCurrentSimpleImageTOIndex(int currentSimpleImageTOIndex) {
		this.currentSimpleImageTOIndex = currentSimpleImageTOIndex;
	}

	public String getMasterSimpleImageTOId() {
		return masterSimpleImageTOId;
	}

	public void setMasterSimpleImageTOId(String masterSimpleImageTOId) {
		this.masterSimpleImageTOId = masterSimpleImageTOId;
	}
	
	
}
