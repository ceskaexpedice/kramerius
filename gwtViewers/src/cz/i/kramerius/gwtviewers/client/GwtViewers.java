
package cz.i.kramerius.gwtviewers.client;


import java.util.ArrayList;
import java.util.HashMap;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.widgetideas.client.SliderBar;

import cz.i.kramerius.gwtviewers.client.panels.CoreConfiguration;
import cz.i.kramerius.gwtviewers.client.panels.ImageMoveWrapper;
import cz.i.kramerius.gwtviewers.client.panels.MoveEffectsPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class GwtViewers implements EntryPoint {
	
	public static GwtViewers _sharedInstance = null;
	/**
	 * The message displayed to the user when the server cannot be reached or
	 * returns an error.
	 */
	private static final String SERVER_ERROR = "An error occurred while "
			+ "attempting to contact the server. Please check your network "
			+ "connection and try again.";

	

	// pohyb doleva doprava
	private MoveEffectsPanel fxPane;
	private final PageServiceAsync pageService = GWT.create(PageService.class);
	private VerticalPanel _emptyVerticalPanel = new VerticalPanel();
	
	private Label nLabel = new Label();
	private boolean initialized = false;
	
	
	private SliderBar sliderBar = new SliderBar(1, 10); {
		sliderBar.setStepSize(1.0);
		sliderBar.setCurrentValue(1);
	    sliderBar.setNumLabels(10);
	    sliderBar.addChangeListener(new ChangeListener() {
			
			@Override
			public void onChange(Widget sender) {
				//fxPane.rollToPage(sliderBar.getCurrentValue());
				rollToPage(sliderBar.getCurrentValue());
			}
		});
	}

	private void doInitImages() {
		String pid = getViewersUUID();
		String uuid = pid.substring("uuid:".length());
		Window.alert("pageService is "+pageService);
		pageService.getNumberOfPages(uuid, new AsyncCallback<Integer>() {

			@Override
			public void onFailure(Throwable caught) {
				Window.alert(caught.getMessage());
				GWT.log(caught.getMessage(), caught);
			}

			@Override
			public void onSuccess(Integer result) {
				System.out.println("Result is "+result);
				Window.alert("Result is "+result);
				sliderBar.setMaxValue(result);
				sliderBar.setNumLabels(result);
				sliderBar.setCurrentValue(1);
			}
		});
		pageService.getPagesSet(uuid, new AsyncCallback<ArrayList<SimpleImageTO>>() {

			@Override
			public void onFailure(Throwable caught) {
				Window.alert(caught.getMessage());
				GWT.log(caught.getMessage(), caught);
				initialized = false;
			}

			@Override
			public void onSuccess(ArrayList<SimpleImageTO> result) {
				System.out.println("SUCCESS - initializing images");
				simplePaneContent(result);
				initialized = true;
			}
		});
	}
	

	public void rollToPage(double currentValue) {
		if (initialized) {
			fxPane.rollToPage(sliderBar.getCurrentValue());
		}
	}

	private void simplePaneContent(ArrayList<SimpleImageTO> itos) {
		RootPanel.get("container").remove(_emptyVerticalPanel);
		createSimpleEffectsPanel(itos);
		RootPanel.get("container").add(this.fxPane);
	}

	
	
	
	private void createSimpleEffectsPanel(ArrayList<SimpleImageTO> itos) {
		final HashMap<String, Object> resultMap = new HashMap<String, Object>();
		
		
		CoreConfiguration conf = new CoreConfiguration();
		{
			conf.setImgDistances(5);
			conf.setViewPortHeight(447);
			conf.setViewPortWidth(700);
		}

		ImageMoveWrapper[] viewPortImages = new ImageMoveWrapper[itos.size()];
		for (int i = 0; i < viewPortImages.length; i++) {
			SimpleImageTO ito = itos.get(i);
			viewPortImages[i] = new ImageMoveWrapper(0,0, ito.getWidth(), ito.getHeight(), ito.getUrl(),ito.getIdentification());
		}
		ImageMoveWrapper[] noVisibleImages = new ImageMoveWrapper[itos.size()];
		for (int i = 0; i < noVisibleImages.length; i++) {
			SimpleImageTO ito = itos.get(i);
			noVisibleImages[i] = new ImageMoveWrapper(0,0, ito.getWidth(), ito.getHeight(), ito.getUrl(),ito.getIdentification());
		}
		ImageMoveWrapper lcopy = new ImageMoveWrapper(0,0, 301, 401, "data/L.png","L.png");
		ImageMoveWrapper rcopy = new ImageMoveWrapper(0,0, 301, 401, "data/R.png","R.png");
		
		this.fxPane = new MoveEffectsPanel( viewPortImages, noVisibleImages, lcopy, rcopy, conf);
	}

	public static void gwtViewers() {
		System.out.println("Callled .... ");
		RootPanel.get("container").add(_sharedInstance._emptyVerticalPanel);	
		_sharedInstance.doInitImages();
		RootPanel.get("slider").add(_sharedInstance.sliderBar);
	}
	
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		_sharedInstance = this;
		Window.alert("Jsem v onModuleLoad ... ");
		
//		RootPanel.get("label").add(nLabel);
//		nLabel.setText(getVariable());
//		final PushButton left = new PushButton(new Image("small_left.png"));
//		left.addClickHandler(new ClickHandler() {
//			
//			@Override
//			public void onClick(ClickEvent event) {
//				fxPane.moveLeft();
//			}
//		});
//		RootPanel.get("left").add(left);
		
//		PushButton right = new PushButton(new Image("small_right.png"));
//		right.addClickHandler(new ClickHandler() {
//			
//			@Override
//			public void onClick(ClickEvent event) {
//				fxPane.moveRight();
//			}
//		});
//		RootPanel.get("right").add(right);

		RootPanel.get("container").add(this._emptyVerticalPanel);	
		doInitImages();
		RootPanel.get("slider").add(sliderBar);
		
		exportMethod();
	}
	
	/// ========= Nativni metody =========
	public native String getViewersUUID() /*-{
		return $wnd.__gwtViewersUUID;
	}-*/;

	public static native void exportMethod() /*-{
	   $wnd.loadMyBusinessWidget = @cz.i.kramerius.gwtviewers.client.GwtViewers::gwtViewers();
	}-*/;}
