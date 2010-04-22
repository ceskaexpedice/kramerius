package cz.incad.kramerius.gwtviewers.client.panels.fx;

import java.util.ArrayList;
import java.util.List;

import org.adamtacy.client.ui.effects.NEffect;
import org.adamtacy.client.ui.effects.ParallellCompositeEffect;
import org.adamtacy.client.ui.effects.events.EffectCompletedEvent;
import org.adamtacy.client.ui.effects.events.EffectCompletedHandler;
import org.adamtacy.client.ui.effects.events.EffectInterruptedEvent;
import org.adamtacy.client.ui.effects.events.EffectInterruptedHandler;
import org.adamtacy.client.ui.effects.events.EffectPausedEvent;
import org.adamtacy.client.ui.effects.events.EffectPausedHandler;
import org.adamtacy.client.ui.effects.events.EffectResumedEvent;
import org.adamtacy.client.ui.effects.events.EffectResumedHandler;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;

import cz.incad.kramerius.gwtviewers.client.events.Barrier;
import cz.incad.kramerius.gwtviewers.client.events.MovingEvent;
import cz.incad.kramerius.gwtviewers.client.events.EventCollectors;
import cz.incad.kramerius.gwtviewers.client.panels.ImageMoveWrapper;
import cz.incad.kramerius.gwtviewers.client.panels.ViewConfiguration;
import cz.incad.kramerius.gwtviewers.client.panels.utils.ImageRotateCalculatedPositions;
import cz.incad.kramerius.gwtviewers.client.panels.utils.ImageRotatePool;
import cz.incad.kramerius.gwtviewers.client.panels.utils.Point;

/**
 * Parallel effect for moving pictures
 * @author pavels
 */
public class Rotate extends ParallellCompositeEffect implements EffectCompletedHandler, EffectInterruptedHandler, EffectPausedHandler, EffectResumedHandler {
	
	private Barrier barrier;
	private boolean called = false;	
	private MovingEvent evt;
	
	public Rotate(ViewConfiguration conf, ImageRotatePool rotatePool, ImageRotateCalculatedPositions calcPos,ArrayList<ImageMoveWrapper> oldViewPortImages, Barrier barrier, MovingEvent evt) {
		super();
		this.barrier = barrier;
		this.evt  = evt;
		
		for (int i = 0; i <rotatePool.getVisibleImageSize(); i++) {
			ImageMoveWrapper viewPortImage = rotatePool.getVisibleImage(i);
			ImageMoveWrapper calculated = calcPos.getViewPortCalulcatedPosition(i);
			NEffect move = viewPortImage.move(new Point(calculated.getX(), calculated.getY()));

			this.registerEffect(move);
			NEffect changeZIndex = viewPortImage.changeZIndex(ImageRotatePool.VIEW_IMAGES_Z_INDEX);
			this.registerEffect(changeZIndex);
		}
		
		ImageMoveWrapper left = rotatePool.getLeftSideImage();
		ImageMoveWrapper leftCalculated = calcPos.getLeft();
		NEffect lmove = left.move(new Point(leftCalculated.getX(), leftCalculated.getY()));
		// pouze na jednom prvku je handler
		registerCompleteHandler(lmove);

		this.registerEffect(lmove);
		NEffect changeZIndex = left.changeZIndex(ImageRotatePool.LEFTRIGNT_IMAGES_Z_INDEX);
		this.registerEffect(changeZIndex);
		
		ImageMoveWrapper right = rotatePool.getRightSideImage();
		ImageMoveWrapper rightCalculated = calcPos.getRight();
		NEffect rmove = right.move(new Point(rightCalculated.getX(), rightCalculated.getY()));

		//registerCompleteHandler(rmove);

		this.registerEffect(rmove);
		NEffect changeZIndexR = right.changeZIndex(ImageRotatePool.LEFTRIGNT_IMAGES_Z_INDEX);
		this.registerEffect(changeZIndexR);

		for (int i = 0; i < rotatePool.getVisibleImageSize(); i++) {
			ImageMoveWrapper nviewPortI = rotatePool.getNoVisibleImages().get(i);
			NEffect noVZIndex = nviewPortI.changeZIndex(ImageRotatePool.NOVIEW_IMAGES_Z_INDEX);
			this.registerEffect(noVZIndex);
		}
		
	}

	private void registerCompleteHandler(NEffect move) {
		move.addEffectCompletedHandler(this);
		move.addEffectInterruptedHandler(this);
	}

	@Override
	protected void onComplete() {
	}

	@Override
	public void onEffectCompleted(final EffectCompletedEvent event) {
		//barrierMessage("<p>effectComplete "+evt.getEvent()+", called="+called+"</p>");
		if (!called) {
			DeferredCommand.addCommand(new Command() {
				@Override
				public void execute() {
					//barrierMessage("<p>effectComplete  - executing deffered command"+evt.getEvent()+"</p>");
					barrier.bound();
					called = true;
					//barrierMessage("<p>effectComplete  - event finished "+evt.getEvent()+"</p>");
					EventCollectors.get().eventFinished(evt);
				}
			});
		} else {
			
		}
	}

	@Override
	public void onEffectInterrupt(EffectInterruptedEvent event) {
//		barrierMessage("<p>effectInterrupt "+System.currentTimeMillis()+"</p>");
		if (!called) {
			DeferredCommand.addCommand(new Command() {
				@Override
				public void execute() {
					barrier.bound();
					called = true;
					EventCollectors.get().eventFinished(evt);
				}
			});
		}
	}
	

	
	
	@Override
	public void onEffectResumed(EffectResumedEvent event) {
	}

	@Override
	public void onEffectPaused(EffectPausedEvent event) {
		
	}


}


