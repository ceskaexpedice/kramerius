
package cz.i.kramerius.gwtviewers.client;


import java.util.ArrayList;
import java.util.HashMap;

import org.adamtacy.client.ui.effects.impl.SlideBase;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.gen2.logging.handler.client.PopupLogHandler;
import com.google.gwt.gen2.logging.handler.client.SimpleLogHandler;
import com.google.gwt.gen2.logging.shared.Level;
import com.google.gwt.gen2.logging.shared.Log;
import com.google.gwt.gen2.logging.shared.LogHandler;
import com.google.gwt.gen2.logging.shared.SmartLogHandler;
import com.google.gwt.gen2.widgetbase.client.Gen2CssInjector;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.widgetideas.client.SliderBar;

import cz.i.kramerius.gwtviewers.client.panels.CoreConfiguration;
import cz.i.kramerius.gwtviewers.client.panels.ImageMoveWrapper;
import cz.i.kramerius.gwtviewers.client.panels.MoveEffectsPanel;
import cz.i.kramerius.gwtviewers.client.panels.MoveListener;
import cz.i.kramerius.gwtviewers.client.panels.utils.ImageRotatePool;

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
		String pid = getViewersUUID();
		String uuid = pid.substring("uuid:".length());
		pageService.getNumberOfPages(uuid, new AsyncCallback<Integer>() {
			@Override
			public void onFailure(Throwable caught) {
				Window.alert(caught.getMessage());
				GWT.log(caught.getMessage(), caught);
			}

			@Override
			public void onSuccess(Integer result) {
				sliderBar.setMaxValue(result);
				sliderBar.setNumLabels(result);
				sliderBar.setNumTicks(result);
				sliderBar.setMinValue(0);
				sliderBar.setCurrentValue(0);
			}
		});
		pageService.getPagesSet(uuid, new AsyncCallback<ArrayList<SimpleImageTO>>() {

			@Override
			public void onFailure(Throwable caught) {
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
		// cele pole v pameti .. posuvatko v ramci pameti
		
		CoreConfiguration conf = new CoreConfiguration();
		{
			conf.setImgDistances(getConfigurationDistance());
			conf.setViewPortHeight(getConfigurationHeight());
			conf.setViewPortWidth(getConfigurationWidth());
		}
		
		ImageMoveWrapper[] viewPortImages = new ImageMoveWrapper[3];
		for (int i = 0; i < viewPortImages.length; i++) {
			SimpleImageTO ito = itos.get(i);
			ImageMoveWrapper wrapper = createImageMoveWrapper(ito,""+i);
			viewPortImages[i] = wrapper;
		}
		
		
		SimpleImageTO rito = itos.get(3);
		ImageMoveWrapper rcopy = createImageMoveWrapper(rito,"R");

		ImageMoveWrapper[] noVisibleImages = new ImageMoveWrapper[3];
		for (int i = 0; i < noVisibleImages.length; i++) {
			SimpleImageTO ito = itos.get(i+4);
			ImageMoveWrapper wrapper = createImageMoveWrapper(ito,"n"+i);
			noVisibleImages[i] = wrapper;
		}
		
		ImageMoveWrapper lcopy = createImageMoveWrapper(rito,"L");
		this.fxPane = new MoveEffectsPanel( viewPortImages, noVisibleImages, lcopy, rcopy, conf);
		
		MoveHandler handler = new MoveHandler(itos);
		this.fxPane.setMoveHandler(handler);
	}


	private ImageMoveWrapper createImageMoveWrapper(SimpleImageTO ito, String id) {
		ImageMoveWrapper wrapper = new ImageMoveWrapper(0,0, ito.getWidth(), ito.getHeight(), ito.getUrl(),ito.getIdentification());
		wrapper.setFirst(ito.isFirstPage());
		wrapper.setLast(ito.isLastPage());
		wrapper.getWidget().getElement().setAttribute("id", id);
		return wrapper;
	}
	
	private void modifyImageMoveWrapper(ImageMoveWrapper wrapper, SimpleImageTO ito, String id) {
		wrapper.setWidth(ito.getWidth());
		wrapper.setHeight(ito.getHeight());
		wrapper.setFirst(ito.isFirstPage());
		wrapper.setLast(ito.isLastPage());
		wrapper.setUrl(ito.getUrl());
		wrapper.setImageIdent(ito.getIdentification());
		wrapper.getWidget().getElement().setId(id);
	}
	

	public static void gwtViewers() {
		RootPanel.get("container").add(_sharedInstance._emptyVerticalPanel);	
		_sharedInstance.doInitImages();
		RootPanel.get("slider").add(_sharedInstance.sliderBar);
	}
	
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		_sharedInstance = this;
		RootPanel.get("container").add(this._emptyVerticalPanel);	
		doInitImages();
		RootPanel.get("slider").add(sliderBar);
	}
	
	class MoveHandler implements MoveListener {
		ArrayList<SimpleImageTO> sito;
		
		public MoveHandler(ArrayList<SimpleImageTO> sito) {
			super();
			this.sito = sito;
		}

		@Override
		public void onMoveLeft(ImageRotatePool pool) {
			rightNoVisible(pool);
			leftNoVisible(pool);
		}


		
		
		@Override
		public void onMoveRight(ImageRotatePool pool) {
			rightNoVisible(pool);
			leftNoVisible(pool);
		}
		

		private void leftNoVisible(ImageRotatePool pool) {
			// ukazovatko pozice 
			int current = pool.getPointer();
			// levy je pozice -1
			int left = current-1;
			ArrayList<ImageMoveWrapper> nvis = pool.getNoVisibleImages();
			int size = nvis.size();
			int maxIndex = size / 2;
			for (int i = 1; i <= maxIndex; i++) {
				int loadingIndex = left - i;
				int nvisPosition = i-1;
				if (loadingIndex >=0) {
					SimpleImageTO sit = sito.get(loadingIndex);
					modifyImageMoveWrapper(nvis.get(nvisPosition), sit, "_"+loadingIndex);
				} else {
					SimpleImageTO sit = new SimpleImageTO();
					sit.setFirstPage(false);
					sit.setLastPage(false);
					sit.setUrl("na.png");
					sit.setWidth(130);
					sit.setHeight(200);
					sit.setIdentification("NA");
					modifyImageMoveWrapper(nvis.get(nvisPosition), sit, "_na");
				}
			}
		}

		private void rightNoVisible(ImageRotatePool pool) {
			// ukazovatko pozice 
			int current = pool.getPointer();
			// levy je pozice -1
			int left = current-1;
			int right = pool.getPointer()+ pool.getViewPortSize();
			ArrayList<ImageMoveWrapper> nvis = pool.getNoVisibleImages();
			int size = nvis.size();
			int maxIndex = size / 2; maxIndex += size % 2;
			for (int i = 1; i <= maxIndex; i++) {
				int loadingIndex = right + i;
				int nvisPosition = size -i;
				if (loadingIndex < 15) {
					SimpleImageTO sit = sito.get(loadingIndex);
					modifyImageMoveWrapper(nvis.get(nvisPosition), sit, "_"+loadingIndex);
				} else {
					SimpleImageTO sit = new SimpleImageTO();
					sit.setFirstPage(false);
					sit.setLastPage(false);
					sit.setUrl("na.png");
					sit.setWidth(130);
					sit.setHeight(200);
					sit.setIdentification("NA");
					modifyImageMoveWrapper(nvis.get(nvisPosition), sit, "_na");
				}
			}
		}
	}
	

	/// ========= Nativni metody =========
	public native String getViewersUUID() /*-{
		return $wnd.__gwtViewersUUID;
	}-*/;


	public native int getConfigurationWidth() /*-{
		return $wnd.__confWidth;
	}-*/;

	public native int getConfigurationHeight() /*-{
		return $wnd.__confHeight;
	}-*/;

	public native int getConfigurationDistance() /*-{
		return $wnd.__confDistance;
	}-*/;

	
	public static native void exportMethod() /*-{
	   $wnd.loadMyBusinessWidget = @cz.i.kramerius.gwtviewers.client.GwtViewers::gwtViewers();
	}-*/;}
