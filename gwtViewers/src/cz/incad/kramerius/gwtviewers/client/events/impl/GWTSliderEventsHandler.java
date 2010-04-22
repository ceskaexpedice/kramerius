package cz.incad.kramerius.gwtviewers.client.events.impl;

import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.widgetideas.client.SliderBar;

import cz.incad.kramerius.gwtviewers.client.events.EventBus;
import cz.incad.kramerius.gwtviewers.client.events.EventsHandler;
import cz.incad.kramerius.gwtviewers.client.events.MovingEvent;
import cz.incad.kramerius.gwtviewers.client.slider.EventProcessor;
import cz.incad.kramerius.gwtviewers.client.slider.SliderValue;

public class GWTSliderEventsHandler extends EventsHandler implements ChangeListener, MouseUpHandler {

	
	
	public GWTSliderEventsHandler(SliderValue sliderValue,
			EventProcessor eventProcessor) {
		super(sliderValue, eventProcessor);
	}

	@Override
	public void onChange(Widget sender) {
		MovingEvent event = new MovingEvent(getSliderValue().getValue());
		EventBus.get().registerEvent(event);
		getEventProcessor().processEvent();
	}

	@Override
	public void onMouseUp(MouseUpEvent event) {
		System.out.println("On mouse up");
		getEventProcessor().correction();
	}

}
