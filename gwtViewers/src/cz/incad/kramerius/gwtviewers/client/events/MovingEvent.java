package cz.incad.kramerius.gwtviewers.client.events;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.management.RuntimeErrorException;

/**
 * This class represents event moving event
 * @author pavels
 */
public class MovingEvent {
	// Unique identifier of event
	private static long pocitadlo = 0;
	private double sliderPosition;
	
	public MovingEvent(double sliderPosition) {
		pocitadlo += 1;
		this.sliderPosition = sliderPosition;
	}
	
	public long getEvent() {
		return pocitadlo;
	}

	public double getSliderPosition() {
		return sliderPosition;
	}
}
