package cz.incad.kramerius.gwtviewers.client.slider;

import com.google.gwt.widgetideas.client.SliderBar;
import com.google.gwt.widgetideas.client.SliderBar.LabelFormatter;

public class SliderBarFormatter implements LabelFormatter {

	private double min;
	private double max;
	
	private double epsilon = 0.01;
	
	
	public SliderBarFormatter(double min, double max) {
		super();
		this.min = min;
		this.max = max;
	}

	public boolean equalsNumber(double value, double expected) {
		double minBounds = expected - this.epsilon;
		double maxBounds = expected + this.epsilon;
		return value > minBounds && value < maxBounds;
	}


	@Override
	public String formatLabel(SliderBar slider, double value) {
		if (this.equalsNumber(value, this.min)) {
			return "1";
		} else if (this.equalsNumber(value, this.max)) {
			return ""+((int)this.max+1);
		}
		return null;
	}

}
