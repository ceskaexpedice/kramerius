package cz.incad.kramerius.gwtviewers.client.panels;


import org.adamtacy.client.ui.effects.NEffect;
import org.adamtacy.client.ui.effects.events.EffectCompletedEvent;
import org.adamtacy.client.ui.effects.events.EffectCompletedHandler;
import org.adamtacy.client.ui.effects.events.EffectStartingEvent;
import org.adamtacy.client.ui.effects.events.EffectStartingHandler;
import org.adamtacy.client.ui.effects.events.EffectSteppingEvent;
import org.adamtacy.client.ui.effects.events.EffectSteppingHandler;
import org.adamtacy.client.ui.effects.impl.Fade;
import org.adamtacy.client.ui.effects.transitionsphysics.LinearTransitionPhysics;
import org.adamtacy.client.ui.effects.transitionsphysics.TransitionPhysics;

import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import cz.incad.kramerius.gwtviewers.client.panels.fx.ChangeZIndex;
import cz.incad.kramerius.gwtviewers.client.panels.fx.CustomMove;
import cz.incad.kramerius.gwtviewers.client.panels.utils.Point;

/**
 * Wrap image core and adds other new properties and fuctions
 */
public class ImageMoveWrapper  implements EffectSteppingHandler, EffectStartingHandler, EffectCompletedHandler {

	private ImageWrapper imageCore;
	private int relative;
	private int absolute;
	
	private boolean first=false;
	private boolean last = false;
	
	private int zIndex = 1;
	
	
	public boolean isLast() {
		return last;
	}


	public void setLast(boolean last) {
		this.last = last;
	}


	public boolean isFirst() {
		return first;
	}


	public void setFirst(boolean first) {
		this.first = first;
	}


	public ImageMoveWrapper(int x, int y, int width, int height, String url, String imageIdent) {
		this.imageCore = new ImageWrapper(x, y, width, height, url, imageIdent);
		appendAlt(imageIdent);
		getWidget().setStyleName("page_not_selected");
	}


	private void appendAlt(String imageIdent) {
		this.imageCore.getImage().getElement().setAttribute("alt", imageIdent);
	}

	
	public void setImageIdent(String imageIdent) {
		imageCore.setImageIdent(imageIdent);
		this.appendAlt(imageIdent);
	}

	public int getX() {
		return this.imageCore.getX();
	}

	public Widget getWidget() {
		return this.imageCore.getWidget();
	}


	public void setX(int x) {
		this.imageCore.setX(x);
	}

	public int getY() {
		return this.imageCore.getY();
	}

	public void setY(int y) {
		this.imageCore.setY(y);
	}

	public int getWidth() {
		return this.imageCore.getWidth();
	}

	public void setWidth(int width) {
		this.imageCore.setWidth(width);
	}

	public int getHeight() {
		return this.imageCore.getHeight();
	}

	public void setHeight(int height) {
		this.imageCore.setHeight(height);
	}

	public String getUrl() {
		return this.imageCore.getUrl();
	}

	public void setUrl(String url) {
		this.imageCore.setUrl(url);
	}
	

	public void disable() {
	}
	
	public void debugXY() {
		String str = ""+this.getImageIdent()+" x,y = ["+this.getX()+","+this.getY()+"]";
		System.out.println(str);
	}

	public boolean isVisible(int viewPortWidth, int viewPortHeight) {
		// test left side
		int border = this.getX() + this.getWidth();
		if (border < 0) return false;
		// test right side
		if (getX()> viewPortWidth) return false;
		return true;
	}

	public int getVisibleRightRest(ViewConfiguration configuration) {
		return configuration.getViewPortWidth() - getX();
	}

	public int getVisibleLeftSideRest(ViewConfiguration configuration) {
		return getX() + getWidth();
	}

	public boolean isOnlyPartVisible(int viewPortWidth, int viewPortHeight) {
		if ((getX() < 0) && ((getX() + getWidth()) > 0)) {
			return true;
		}
		if ((getX() < viewPortWidth) && ((getX() + getWidth()) > viewPortWidth)) {
			return true;
		}
		return false;
	}

	public NEffect move(int x, int y) {
		CustomMove customMove = new CustomMove(this.getX(), this.getY(), x, y);
		customMove.addEffectElement(this.imageCore.getWidget().getElement());
		//customMove.setDuration(0.7);
		this.setX(x);
		this.setY(y);
		return customMove;
	}

	public NEffect move(Point point) {
		CustomMove customMove = new CustomMove(this.getX(), this.getY(), point.x, point.y);
		customMove.addEffectElement(this.imageCore.getWidget().getElement());
		customMove.setTransitionType(new LinearTransitionPhysics());
		customMove.addEffectStartingHandler(this);
		//customMove.setDuration(0.4);
		this.setX(this.getX() + this.getWidth());
		return customMove;
	}
	
	public NEffect changeZIndex(int zindex) {
		ChangeZIndex index = new ChangeZIndex(getzIndex(), zindex);
		index.addEffectElement(this.imageCore.getWidget().getElement());
		return	index;
	}

	public String getImageIdent() {
		return imageCore.getImageIdent();
	}

	public void setImage(Image image) {
		imageCore.setImage(image);
	}

	@Override
	public void onEffectStep(EffectSteppingEvent event) {
		String sposition = this.imageCore.getWidget().getElement().getStyle().getPosition();
		if (sposition.equals("absolute")) {
			absolute += 1;
		} else {
			relative += 1;
		}
	}

	@Override
	public void onEffectStarting(EffectStartingEvent event) {
		this.imageCore.getWidget().getElement().getStyle().setPosition(Position.ABSOLUTE);
	}

	@Override
	public void onEffectCompleted(EffectCompletedEvent event) {
		System.out.println("Relative :"+this.relative);
		System.out.println("Absolute:"+this.absolute);
	}
	
	
	public ImageMoveWrapper copy() {
		return new ImageMoveWrapper(this.getX(), this.getY(), this.getWidth(), this.getHeight(), this.getUrl(), getImageIdent());
	}

	public NEffect fade() {
		Fade fade = new Fade();
		System.out.println("Fading -"+getImageIdent());
		fade.addEffectElement(this.imageCore.getWidget().getElement());
		return fade;
	}


	public int getIndex() {
		return imageCore.getIndex();
	}


	public void setIndex(int index) {
		imageCore.setIndex(index);
	}


	public void propagateXToElement() {
		getWidget().getElement().getStyle().setLeft(getX(), Unit.PX);
	}


	public void propagateYToElement() {
		getWidget().getElement().getStyle().setRight(getY(), Unit.PX);
	}


	public int getzIndex() {
		return zIndex;
	}


	public void setzIndex(int zIndex) {
		this.zIndex = zIndex;
	}


	public void modifyHeightAndWidth() {
		this.imageCore.modifyHeightAndWidth();
	}
	
}
