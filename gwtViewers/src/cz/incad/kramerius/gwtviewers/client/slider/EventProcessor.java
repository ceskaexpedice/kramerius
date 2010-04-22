package cz.incad.kramerius.gwtviewers.client.slider;

import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.Widget;

import cz.incad.kramerius.gwtviewers.client.events.MovingEvent;
import cz.incad.kramerius.gwtviewers.client.events.EventBus;
import cz.incad.kramerius.gwtviewers.client.events.EventCollectors;
import cz.incad.kramerius.gwtviewers.client.panels.MoveEffectsPanel;

public class EventProcessor   {

	private int previousSliderMove = -5;
	private int previousSliderUp = -5;

	private int modulo = 1;
	private double duration = 0.3;
	private MoveEffectsPanel fxPane;

	private SliderDirection direction = SliderDirection.RIGHT;
	//private SliderBar sliderBar;
	private SliderValue sliderValue;
	
	
	public EventProcessor(int modulo, double duration/*,SliderBar sliderBar*/, SliderValue sliderValue,
			MoveEffectsPanel fxPane) {
		super();
		//this.sliderBar = sliderBar;
		this.modulo = modulo;
		this.duration = duration;
		this.fxPane = fxPane;
		this.sliderValue = sliderValue;
	}




	/**
	 * Zpracovani udalosti
	 */
	public void processEvent() {
		if (EventBus.get().isInProcess()) {
			EventBus.get().popEvent();
			return;
		}
		while(EventBus.get().somethingHappen()) {
			MovingEvent evt = EventBus.get().popEvent();
			EventBus.get().changeRunningState(true);
			//EventCollectors.get().eventStarted(evt);
			double currentValue = evt.getSliderPosition();

			int round = (int) Math.round(currentValue);
			if (round != previousSliderMove) {
				if (round > previousSliderMove) this.direction = SliderDirection.RIGHT;
				else this.direction = SliderDirection.LEFT;
				previousSliderMove = round;
				int calclulatedModulo = round % modulo;
				boolean acceptEvent = calclulatedModulo == 0;
				if (acceptEvent) {
					// Tohle jsem uz jednou zakmentovaval... ale uz nevim proc? 
					// Ted odkomentovano, protoze na drobnustkach blbne nasledujici scenar:
					// Krok Doleva -> Krok zpet
					if (modulo != 1) {
						fxPane.modifyImagesToPointer();
					}
					rollToPage(round, duration, true, evt);
				} else {
					rollToPage(round, 0.0, false, evt);
				}
			}
			EventBus.get().changeRunningState(false);
		}

		if (fxPane == null) return;
	}
	
	
	/**
	 * Correct move 
	 */
	public void correction() {
		// if modulo equals 1 no correction needed
		if (modulo == 1) return;
		double currentValue = this.sliderValue.getValue();
		MovingEvent mevt = new MovingEvent(currentValue);
		int round = (int) Math.round(currentValue);
		if (round != previousSliderUp) {
			if ((round % modulo) != 0) {
				if (fxPane == null) Window.alert("fxPane cannot be null !");
				fxPane.modifyImagesToPointer();
				//fxPane.getRotatePool().debugPool();
				animateOneJump(round, mevt);
				previousSliderUp = round;
			} else {}
		} else {}
	}
		
	private void animateOneJump(int round, MovingEvent evt) {
		if (direction == SliderDirection.LEFT) {
			fxPane.getRotatePool().rollLeft(); 
			fxPane.calulateNextPositions();
			fxPane.storeCalculatedPositions();
		} else {
			fxPane.getRotatePool().rollRight(); 
			fxPane.calulateNextPositions();
			fxPane.storeCalculatedPositions();
		}
		rollToPage(round,duration, true, evt);
	}

	
	public void rollToPage(int currentValue, double duration, boolean playEffect, MovingEvent evt) {
		fxPane.rollToPage(currentValue,duration, playEffect,evt);
	}

	public int getModulo() {
		return modulo;
	}


	public void setModulo(int modulo) {
		this.modulo = modulo;
	}
	
}
