package cz.incad.kramerius.ngwt.client;

import static cz.incad.kramerius.ngwt.client.utils.ElementIDs.*;

import java.util.ArrayList;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import cz.incad.kramerius.ngwt.client.panels.ImageContainer;
import cz.incad.kramerius.ngwt.client.panels.ImageContainerParent;
import cz.incad.kramerius.ngwt.client.panels.fx.CustomMove;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class GwtN implements EntryPoint {

	private ImageContainerParent icp;
	
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		jsniExportMethods(this);
	}
	
	public void doInit() {
		JsArrayString jsarray = jsniJQueryFindPages();

		if (this.icp != null) {
			RootPanel container = RootPanel.get("container");
			container.remove(this.icp);
		}
		this.icp = new ImageContainerParent(jsarray);
		RootPanel container = RootPanel.get("container");
		container.add(icp);

		jsniJQuerySliderFactoryMethod(0, jsarray.length() - jsniGetNumberOfImages()+1);
	}
	
	public void gwtInitialized() {}

	
	public int findIndex(String uuid) {
		if (this.icp != null) {
			int localVar = this.icp.getImageContainer().findIndex(uuid);
			return localVar;
		}
		return -1;
	}
	
	
	public void onSliderChange(double val) {
		int iVal = (int) Math.round(val);
		String id = this.icp.getId(iVal);
		if (id != null) {
			com.google.gwt.dom.client.Element elm = Document.get().getElementById(createElementID(id));
			if (elm != null) {
				int offsetLeft = elm.getParentElement().getOffsetLeft();
				this.icp.animatePosition(-offsetLeft);
			}
		} else {
			Window.alert("Error in data '"+id+"'");
		}
	}
	
	public void select(String id) {
		if (this.icp != null) {
			com.google.gwt.user.client.Element elm = DOM.getElementById(createElementID(id));
			if (elm != null) {
				this.icp.getImageContainer().changeSelection(id,elm);
			}
		}
	}
	
	public native void jsniExportMethods(GwtN gwt) /*-{
		$wnd.onSlider = function (val) {
			gwt.@cz.incad.kramerius.ngwt.client.GwtN::onSliderChange(D)(val);
		};

		$wnd.initialize = function () {
			gwt.@cz.incad.kramerius.ngwt.client.GwtN::doInit()();
		};
		

		$wnd.select = function (sel) {
			gwt.@cz.incad.kramerius.ngwt.client.GwtN::select(Ljava/lang/String;)(sel);
		};

		$wnd.index = function (uuid) {
			return gwt.@cz.incad.kramerius.ngwt.client.GwtN::findIndex(Ljava/lang/String;)(uuid);
		};
	
		// flag function -- everything is initialized
		$wnd.initialized = function () {
			gwt.@cz.incad.kramerius.ngwt.client.GwtN::gwtInitialized()();
		};
		
	}-*/;

	public native void jsniJQuerySliderFactoryMethod(double min, double max) /*-{
		$wnd.createSlider(min, max, min);
	}-*/;
	
	
	public native int jsniGetNumberOfImages() /*-{
		return $wnd.__confNumberOfImages;
	}-*/;
	
	public native JsArrayString jsniJQueryFindPages() /*-{
		return $wnd.findPages();
	}-*/;
	
	public static native void jsniSelectionChanged(String uuid) /*-{
		$wnd.selectPage(uuid);
	}-*/;
}
