package cz.incad.kramerius.gwtviewers.client.slider.impl;

import com.google.gwt.user.client.Window;

import cz.incad.kramerius.gwtviewers.client.slider.SliderValue;

public class JQuerySliderValue extends SliderValue {

	@Override
	public double getValue() {
		int jQuerySliderValue = getJQuerySliderValue();
		return new Double(jQuerySliderValue).doubleValue();
	}

	
	@Override
	public void setValue(double value) {
		setJQuerySliderValue(value);
	}

	public native int getJQuerySliderValue() /*-{
		if (!$wnd.getSliderValue) {
			alert("expect function getSliderValue");
		} else {
			var sliderValue = $wnd.getSliderValue();
			if (sliderValue) return sliderValue;
			else return 0;
		}	
	}-*/;


	public native void setJQuerySliderValue(double value) /*-{
		if (!$wnd.setSliderValue) {
			alert("expect function setSliderValue");
		} else {
			$wnd.setSliderValue(value);
		}	
	}-*/;
	

}
