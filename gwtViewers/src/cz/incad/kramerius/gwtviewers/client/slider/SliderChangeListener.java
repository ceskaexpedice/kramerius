package cz.incad.kramerius.gwtviewers.client.slider;

import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.widgetideas.client.SliderBar;

import cz.incad.kramerius.gwtviewers.client.panels.MoveEffectsPanel;

public class SliderChangeListener implements ChangeListener, MouseUpHandler  {

	private int previousSliderMove = -5;
	private int previousSliderUp = -5;

	private int modulo = 1;
	private double duration = 0.3;
	private MoveEffectsPanel fxPane;

	private SliderDirection direction = SliderDirection.RIGHT;

	
	public SliderChangeListener(int modulo, double duration,
			MoveEffectsPanel fxPane) {
		super();
		this.modulo = modulo;
		this.duration = duration;
		this.fxPane = fxPane;
	}


	@Override
	public void onChange(Widget sender) {
		if (fxPane == null) return;
		double currentValue = ((SliderBar)sender).getCurrentValue();
		int round = (int) Math.round(currentValue);
		if (round != previousSliderMove) {
			if (round > previousSliderMove) this.direction = SliderDirection.RIGHT;
			else this.direction = SliderDirection.LEFT;
			previousSliderMove = round;
			if ((round % modulo) == 0) {
				// Tohle jsem uz jednou zakmentovaval... ale uz nevim proc? 
				// Ted odkomentovano, protoze na drobnustkach blbne nasledujici scenar:
				// Krok Doleva -> Krok zpet
				if (modulo != 1) {
					fxPane.rollToPointer();
				}
				rollToPage(round, duration, true);
				//rollToPage(round, duration, false);
//				label.setText("strana:"+round+" - posun");
			} else {
				rollToPage(round, 0.0, false);
//				label.setText("strana:"+round +"-zadny posun");
			}
		}
	}
	
	
	
	
	@Override
	public void onMouseUp(MouseUpEvent event) {
		if (modulo == 1) return;
		double currentValue = ((SliderBar)event.getSource()).getCurrentValue();
		int round = (int) Math.round(currentValue);
		if (round != previousSliderUp) {
			if ((round % modulo) != 0) {
				fxPane.rollToPointer();
				//fxPane.getRotatePool().debugPool();
				animateOneJump(round);
				previousSliderUp = round;
			} else {}
		} else {}
	}
		
	private void animateOneJump(int round) {
		if (direction == SliderDirection.LEFT) {
			fxPane.getRotatePool().rollLeft(); 
			fxPane.calulateNextPositions();
			fxPane.storeCalculatedPositions();
		} else {
			fxPane.getRotatePool().rollRight(); 
			fxPane.calulateNextPositions();
			fxPane.storeCalculatedPositions();
		}
		rollToPage(round,duration, true);
	}



	public void rollToPage(int currentValue, double duration, boolean playEffect) {
		fxPane.rollToPage(currentValue,duration, playEffect);
	}


	public int getModulo() {
		return modulo;
	}


	public void setModulo(int modulo) {
		this.modulo = modulo;
	}
	
	
}
