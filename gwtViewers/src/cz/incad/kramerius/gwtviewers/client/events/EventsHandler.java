package cz.incad.kramerius.gwtviewers.client.events;

import cz.incad.kramerius.gwtviewers.client.slider.EventProcessor;
import cz.incad.kramerius.gwtviewers.client.slider.SliderValue;

public class EventsHandler {
	
	private SliderValue sliderValue;
	private EventProcessor eventProcessor;

	public EventsHandler(SliderValue sliderValue, EventProcessor eventProcessor) {
		super();
		this.sliderValue = sliderValue;
		this.eventProcessor = eventProcessor;
	}

	public SliderValue getSliderValue() {
		return sliderValue;
	}

	public EventProcessor getEventProcessor() {
		return eventProcessor;
	}

	
}
