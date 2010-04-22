package cz.incad.kramerius.gwtviewers.client.slider.impl;

import com.google.gwt.widgetideas.client.SliderBar;

import cz.incad.kramerius.gwtviewers.client.GwtViewers;
import cz.incad.kramerius.gwtviewers.client.slider.SliderValue;

public class GWTSliderValue extends SliderValue {

	private SliderBar sliderBar;

	public GWTSliderValue(SliderBar sliderBar) {
		super();
		this.sliderBar = sliderBar;
	}

	@Override
	public double getValue() {
		return this.sliderBar.getCurrentValue();
	}

	@Override
	public void setValue(double value) {
		this.sliderBar.setCurrentValue(value);
	}

	
}
