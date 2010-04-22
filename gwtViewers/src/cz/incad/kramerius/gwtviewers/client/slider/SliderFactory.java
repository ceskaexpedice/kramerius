package cz.incad.kramerius.gwtviewers.client.slider;

import com.google.gwt.user.client.Window;

import cz.incad.kramerius.gwtviewers.client.events.EventsHandler;
import cz.incad.kramerius.gwtviewers.client.events.impl.JQuerySliderEventsHandler;
import cz.incad.kramerius.gwtviewers.client.slider.impl.JQuerySliderValue;

public class SliderFactory {
	
	
	public SliderValue createSliderWrapper(int min, int max, int cur, int width) {
		createJQuerySlider(min, max, cur, width);
		return new JQuerySliderValue();
	}
	
	public EventsHandler createSliderEventsHandler(SliderValue sliderValue, EventProcessor eventProcessor) {
		return new JQuerySliderEventsHandler(sliderValue, eventProcessor);
	}
	
	public native void createJQuerySlider(int min, int max, int cur, int w) /*-{
		$wnd.createSlider(min, max, cur, w);
	}-*/;
}
