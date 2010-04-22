package cz.incad.kramerius.gwtviewers.client.events;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;

import cz.incad.kramerius.gwtviewers.client.GwtViewers;
import cz.incad.kramerius.gwtviewers.client.ModuloCreator;

public class EventBus {
	

	private ModuloCreator moduloCreator = GWT.create(ModuloCreator.class);

	private long eventId = 0;
	private static EventBus _BUS = new EventBus();
	
	private EventBus() {}

	private List<MovingEvent> events = new ArrayList<MovingEvent>();
	
	private int pocitadlo = 1;

	private boolean processing = false;
	
	public void registerEvent(MovingEvent evt) {
		if ((pocitadlo%2) == 0) {
			pocitadlo = 1;
			pocitadlo =1;
		}
		events.add(evt);
		pocitadlo += 1;
	}
	
	public MovingEvent popEvent() {
		MovingEvent removed = this.events.remove(0);
		this.eventId = removed.getEvent();
		return removed;
	}
	
	public boolean somethingHappen() {
		return !this.events.isEmpty();
	}
	
	public static EventBus get() {
		return _BUS;
	}
	
	
	public boolean isInProcess() {
		return this.processing;
	}
	
	public long getEventId() {
		return eventId;
	}


	public MovingEvent peekEvent() {
		return this.events.get(0);
	}


	
	public void changeRunningState(boolean st) {
		this.processing = st;
	}
	
	public void consumeEvent(Long id){
		//this.standardWayEvents.remove(id);
	}

}
