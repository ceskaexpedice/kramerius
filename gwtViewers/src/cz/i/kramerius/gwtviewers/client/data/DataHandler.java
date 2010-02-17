package cz.i.kramerius.gwtviewers.client.data;

import java.util.List;

import cz.i.kramerius.gwtviewers.client.SimpleImageTO;

public class DataHandler {

	private static List<SimpleImageTO> data;
	private static SimpleImageTO naImage;
	private static int max;
	
	public static List<SimpleImageTO> getData() {
		return data;
	}
	public static void setData(List<SimpleImageTO> data) {
		DataHandler.data = data;
		naImage = createNASiTO(data.get(0));
		max = data.size();
	}
	public static SimpleImageTO getNaImage() {
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
	public static int getMax() {
		return max;
	}
	public static void setMax(int max) {
		DataHandler.max = max;
	}
}
