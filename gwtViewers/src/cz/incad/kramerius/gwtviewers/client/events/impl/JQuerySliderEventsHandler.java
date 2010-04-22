package cz.incad.kramerius.gwtviewers.client.events.impl;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;

import cz.incad.kramerius.gwtviewers.client.events.EventBus;
import cz.incad.kramerius.gwtviewers.client.events.EventsHandler;
import cz.incad.kramerius.gwtviewers.client.events.MovingEvent;
import cz.incad.kramerius.gwtviewers.client.slider.EventProcessor;
import cz.incad.kramerius.gwtviewers.client.slider.SliderValue;
import cz.incad.kramerius.gwtviewers.client.slider.impl.GWTSliderValue;

public class JQuerySliderEventsHandler extends EventsHandler {

	
	public JQuerySliderEventsHandler(SliderValue sliderValue,
			EventProcessor eventProcessor) {
		super(sliderValue, eventProcessor);
		exportMethods(this);
	}
	

	public void jQuerySliderChange(int current) {
		MovingEvent event = new MovingEvent(new Double(current).doubleValue());
		EventBus.get().registerEvent(event);
		getEventProcessor().processEvent();
	}

	public void jQuerySliderMouseUp() {
		getEventProcessor().correction();
	}
	
	public native void exportMethods(JQuerySliderEventsHandler handler) /*-{
		$wnd.jQuerySliderChange = function(vr) {
			handler.@cz.incad.kramerius.gwtviewers.client.events.impl.JQuerySliderEventsHandler::jQuerySliderChange(I)(vr);
		};
		$wnd.jQuerySliderMouseUp =function() {
			handler.@cz.incad.kramerius.gwtviewers.client.events.impl.JQuerySliderEventsHandler::jQuerySliderMouseUp()();
		};

//		$wnd.jQuerySliderChange = handler.@cz.incad.kramerius.gwtviewers.client.events.impl.JQuerySliderEventsHandler::jQuerySliderChange(I);
//		$wnd.jQuerySliderMouseUp =handler.@cz.incad.kramerius.gwtviewers.client.events.impl.JQuerySliderEventsHandler::jQuerySliderMouseUp();
	}-*/;
}
