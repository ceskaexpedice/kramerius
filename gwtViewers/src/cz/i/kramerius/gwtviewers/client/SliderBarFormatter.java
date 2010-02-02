package cz.i.kramerius.gwtviewers.client;

import com.google.gwt.widgetideas.client.SliderBar;
import com.google.gwt.widgetideas.client.SliderBar.LabelFormatter;

public class SliderBarFormatter implements LabelFormatter {

	private double min;
	private double max;
	
	
	
	public SliderBarFormatter(double min, double max) {
		super();
		this.min = min;
		this.max = max;
	}



	@Override
	public String formatLabel(SliderBar slider, double value) {
		// ?? 
		System.out.println("value == "+value);
		if (this.min == value) {
			return "1";
		} else if (this.max == value) {
			return ""+((int)this.max+1);
		}
		return null;
	}

}
