package cz.i.kramerius.gwtviewers.client;


import java.util.ArrayList;
import java.util.HashMap;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
	
	
	private SliderBar sliderBar = new SliderBar(0, 110); {
		sliderBar.setStepSize(1.0);
		sliderBar.setCurrentValue(0);
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
		System.out.println("Trying get images... ");
		pageService.getPagesSet("any-master", new AsyncCallback<ArrayList<SimpleImageTO>>() {

			@Override
			public void onFailure(Throwable caught) {
				GWT.log(caught.getMessage(), caught);
			}

			@Override
			public void onSuccess(ArrayList<SimpleImageTO> result) {
				System.out.println("SUCCESS - initializing images");
				simplePaneContent(result);
			}
		});
	}
	
	public void rollToPage(double currentValue) {
		fxPane.rollToPage(sliderBar.getCurrentValue());
	}

	private void simplePaneContent(ArrayList<SimpleImageTO> itos) {
		RootPanel.get("container").remove(_emptyVerticalPanel);
		createSimpleEffectsPanel(itos);
		RootPanel.get("container").add(this.fxPane);
	}

	
	public native String getVariable() /*-{
		return $wnd.firstName;
	}-*/;
	
	
	private void createSimpleEffectsPanel(ArrayList<SimpleImageTO> itos) {
		final HashMap<String, Object> resultMap = new HashMap<String, Object>();
		
		
		CoreConfiguration conf = new CoreConfiguration();
		{
			conf.setImgDistances(5);
			conf.setViewPortHeight(447);
			conf.setViewPortWidth(700);
		}

		ImageMoveWrapper[] viewPortImages = new ImageMoveWrapper[6];
		for (int i = 0; i < viewPortImages.length; i++) {
			viewPortImages[i] = new ImageMoveWrapper(0,0, 301, 401, "data/"+(i+1)+".png",i+".png");
		}
		ImageMoveWrapper[] noVisibleImages = new ImageMoveWrapper[6];
		for (int i = 0; i < noVisibleImages.length; i++) {
			noVisibleImages[i] = new ImageMoveWrapper(0,0, 301, 401, "data/nv"+(i+1)+".png","nv"+i+".png");
		}
		ImageMoveWrapper lcopy = new ImageMoveWrapper(0,0, 301, 401, "data/L.png","L.png");
		ImageMoveWrapper rcopy = new ImageMoveWrapper(0,0, 301, 401, "data/R.png","R.png");
		
		this.fxPane = new MoveEffectsPanel( viewPortImages, noVisibleImages, lcopy, rcopy, conf);
	}

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {

//		RootPanel.get("label").add(nLabel);
//		nLabel.setText(getVariable());
		
		final PushButton left = new PushButton(new Image("small_left.png"));
		left.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				fxPane.moveLeft();
			}
		});
		RootPanel.get("left").add(left);
		
		
		
		PushButton right = new PushButton(new Image("small_right.png"));
		right.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				fxPane.moveRight();
			}
		});
		RootPanel.get("right").add(right);
		RootPanel.get("container").add(this._emptyVerticalPanel);	
		
		doInitImages();
		
		RootPanel.get("slider").add(sliderBar);

	}

	
}
