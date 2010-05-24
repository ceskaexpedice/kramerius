package cz.incad.kramerius.ngwt.client.panels;

import static cz.incad.kramerius.ngwt.client.utils.ElementIDs.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;


public class ImageContainer extends Composite implements ClickHandler {

	private static final String IMG_SELECTED = "img_selected";
	private static final String CELL_SELECTED = "cell_selected";

	private static final String IMG_NOT_SELECTED ="img_not_selected"; 
	
	private final HorizontalPanel grid; 
	private Element previousSelected =  null;
	
	public ImageContainer(JsArrayString jsArrayString) {
		super();
		final int length = jsArrayString.length();
		this.grid = new HorizontalPanel();
		this.grid.getElement().getStyle().setPosition(Position.RELATIVE);
		this.grid.setSpacing(3);
		
		for (int i = 0,ll = jsArrayString.length(); i < ll; i++) {
			Image img = new Image();
			img.addClickHandler(this);
			img.getElement().setId(createElementID(jsArrayString.get(i)));
			img.getElement().addClassName(IMG_NOT_SELECTED);
			img.setUrl(jsniGetImageUrl()+jsArrayString.get(i));
			grid.add(img);
			
		}
		this.initWidget(this.grid);
	}

	@Override
	public void onClick(ClickEvent event) {
		Image img = (Image) event.getSource();
		String uuid = getUUID(img.getElement());
		changeSelection(uuid,img.getElement());
	}

	public void changeSelection(String selId, Element elm) {
		if (this.previousSelected !=  null) {
			this.previousSelected.getParentElement().removeClassName(CELL_SELECTED);
			this.previousSelected.removeClassName(IMG_SELECTED);
			this.previousSelected.addClassName(IMG_NOT_SELECTED);
		}
		elm.removeClassName(IMG_NOT_SELECTED);
		elm.addClassName(IMG_SELECTED);
		elm.getParentElement().addClassName(CELL_SELECTED);
		this.jsnionSelectionChanged(selId);
		this.previousSelected = elm;
	}
	
	
	public int findIndex(String uuid) {
		NodeList<com.google.gwt.dom.client.Element> tds = this.grid.getElement().getElementsByTagName("td");
		for (int i = 0,ll=tds.getLength(); i < ll; i++) {
			com.google.gwt.dom.client.Element item = tds.getItem(i);
			NodeList<com.google.gwt.dom.client.Element> imgs = item.getElementsByTagName("img");
			if (imgs.getLength()  > 0) {
				Element img = (Element) imgs.getItem(0);
				if (containsElementGivenUUID(uuid, img)) {
					return i;
				}
			}
		}
		return -1;
	}

	
	
	public static native String jsniGetImageUrl() /*-{
		return $wnd.getImageURL();
	}-*/;

	public native String jsnionSelectionChanged(String uuid) /*-{
		return $wnd.selectPage(uuid);
	}-*/;
}
