package cz.incad.kramerius.gwtviewers.client;

import java.util.ArrayList;

import cz.incad.kramerius.gwtviewers.client.SimpleImageTO;

public class DataUtils {

	public static ArrayList<SimpleImageTO> createImages(int max) {
		ArrayList<SimpleImageTO> sit = new ArrayList<SimpleImageTO>();
		for (int i = 0; i < max; i++) {
			SimpleImageTO one = new SimpleImageTO();
			one.setFirstPage(i==0);
			one.setLastPage(i==max-1);
			one.setIdentification(""+i);
			one.setUrl("none");
			one.setHeight(220);
			one.setWidth(170);
			one.setIndex(i);
			sit.add(one);
		}
		return sit;
	}

	public static ArrayList<SimpleImageTO> createImages(String[] ids) {
		ArrayList<SimpleImageTO> sit = new ArrayList<SimpleImageTO>();
		for (int i = 0; i < ids.length; i++) {
			SimpleImageTO one = new SimpleImageTO();
			one.setFirstPage(i==0);
			one.setLastPage(i==ids.length-1);
			one.setIdentification(ids[i]);
			one.setUrl("http://"+ids[i]);
			one.setHeight(220);
			one.setWidth(170);
			one.setIndex(i);
			sit.add(one);
		}
		return sit;
	}
}
