package cz.incad.kramerius.ngwt.client.panels;

import java.util.HashMap;
import java.util.Hashtable;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

import cz.incad.kramerius.ngwt.client.panels.fx.CustomMove;

public class ImageContainerParent extends Composite{

	private FlowPanel fPanel = new FlowPanel();
	private ImageContainer imageContainer = null;
	
	private int left;

	private HashMap<String, Integer> id2Index =  new HashMap<String, Integer>();
	private HashMap<Integer, String> index2Id =  new HashMap<Integer, String>();
	
	public ImageContainerParent(JsArrayString jsArrayString) {
		super();
		if (jsArrayString == null) Window.alert("error in data !");
		if (jsArrayString.length() == 0) Window.alert("error in data !");
		this.imageContainer = new ImageContainer(jsArrayString);
		for (int i = 0; i < jsArrayString.length(); i++) { 
			id2Index.put(jsArrayString.get(i), new Integer(i)); 
			index2Id.put(new Integer(i),jsArrayString.get(i)); 
		}
		this.fPanel.add(this.imageContainer);
		this.fPanel.getElement().setId("container_view");
		this.fPanel.setWidth(jsniGetWidth());
		this.fPanel.getElement().getStyle().setOverflow(Overflow.HIDDEN);
		initWidget(this.fPanel);
	}

	public ImageContainer getImageContainer() {
		return imageContainer;
	}
	
	public FlowPanel getContent() {
		return this.fPanel;
	}
	
	public void position(int left) {
		this.left = left;
		this.imageContainer.getElement().getStyle().setLeft(this.left, Unit.PX);
	}

	public String getId(Integer index) {
		return this.index2Id.get(index);
	}
	
	
	public void animatePosition(int left) {
		CustomMove custMove = new CustomMove(this.left, 0, left, 0);
		custMove.addEffectElement(this.imageContainer.getElement());
		custMove.setDuration(0.3);
		custMove.play();
		this.left = left;
	}
	
	public static native String jsniGetWidth() /*-{
		return $wnd.getImgContainerWidth();
	}-*/;

}
