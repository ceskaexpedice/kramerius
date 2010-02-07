package cz.i.kramerius.gwtviewers.client;

import java.util.ArrayList;

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

}
