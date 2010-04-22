package cz.incad.kramerius.gwtviewers.client.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventCollectors {
	
	private StringBuffer finishedBuffer = new StringBuffer();
	private List<MovingEvent> playing = new ArrayList<MovingEvent>();
	private Map<Long, MovingEvent> mapping = new HashMap<Long, MovingEvent>();
	
	public void eventStarted(MovingEvent evt) {
		playing.add(evt);
	}
	
	public void eventFinished(MovingEvent evt) {
		playing.remove(evt);
		finishedBuffer.append("<p>Started and finished event :"+evt.getEvent()+"</p>");

//		StringBuffer buffer = new StringBuffer();
//		buffer.append("<h3>Finished events</h3>");
//		buffer.append(finishedBuffer.toString());
//		buffer.append("<hr>");
//		buffer.append("<h3>Not finished events</h3>");
//		for (Event ev : this.playing) {
//			buffer.append("<p>Started and not finished event :"+ev.getEvent());
//		}
//		colMessage(buffer.toString());
	}



	private static EventCollectors _COLL = new EventCollectors();

	public static EventCollectors get() {
		return _COLL;
	}
}
