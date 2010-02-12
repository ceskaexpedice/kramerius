
package cz.i.kramerius.gwtviewers.client;

import static cz.i.kramerius.gwtviewers.client.panels.utils.NoVisibleFillHelper.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.adamtacy.client.ui.effects.impl.SlideBase;

import com.google.gwt.core.client.Duration;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.gen2.logging.handler.client.PopupLogHandler;
import com.google.gwt.gen2.logging.handler.client.SimpleLogHandler;
import com.google.gwt.gen2.logging.shared.Level;
import com.google.gwt.gen2.logging.shared.Log;
import com.google.gwt.gen2.logging.shared.LogHandler;
import com.google.gwt.gen2.logging.shared.SmartLogHandler;
import com.google.gwt.gen2.widgetbase.client.Gen2CssInjector;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.widgetideas.client.SliderBar;

import cz.i.kramerius.gwtviewers.client.panels.ViewConfiguration;
import cz.i.kramerius.gwtviewers.client.panels.ConfigurationChanged;
import cz.i.kramerius.gwtviewers.client.panels.ConfigurationPanel;
import cz.i.kramerius.gwtviewers.client.panels.ImageMoveWrapper;
import cz.i.kramerius.gwtviewers.client.panels.MoveEffectsPanel;
import cz.i.kramerius.gwtviewers.client.panels.MoveListener;
import cz.i.kramerius.gwtviewers.client.panels.MovePointerPanel;
import cz.i.kramerius.gwtviewers.client.panels.fx.Rotate;
import cz.i.kramerius.gwtviewers.client.panels.utils.CalculationHelper;
import cz.i.kramerius.gwtviewers.client.panels.utils.ImageRotatePool;
import cz.i.kramerius.gwtviewers.client.selections.SelectionDecorator;
import cz.i.kramerius.gwtviewers.client.selections.Selector;

public class GwtViewers implements EntryPoint, ClickHandler, ConfigurationChanged {
	
	public static GwtViewers _sharedInstance = null;

	private MoveEffectsPanel fxPane;
	private MovePointerPanel mvPane;
	private ConfigurationPanel confPanel;
	
	
	private final PageServiceAsync pageService = GWT.create(PageService.class);
	private VerticalPanel _emptyVerticalPanel = new VerticalPanel();
	private VerticalPanel container = new VerticalPanel();
	private Label label = new Label();
	
	private boolean initialized = false;
	private int modulo = 1;
	private double duration = 0.3; 
	
	private SliderDirection direction = SliderDirection.RIGHT;

	private SliderBar sliderBar = new SliderBar(1, 10);
	{
		sliderBar.setStepSize(1.0);
		sliderBar.setCurrentValue(0);
	    sliderBar.setNumLabels(0);
	    sliderBar.setLabelFormatter(null);
	    sliderBar.addMouseUpHandler(new MouseUpHandler() {
			private int previous = -5;
			
			@Override
			public void onMouseUp(MouseUpEvent event) {
				double currentValue = sliderBar.getCurrentValue();
				int round = (int) Math.round(currentValue);
				if (round != previous) {
					if ((round % modulo) != 0) {
						animateOneJump(round);
						previous = round;
					}
				}
			}
		});


	    sliderBar.addChangeListener(new ChangeListener() {
			private int previous = -5;
			@Override
			public void onChange(Widget sender) {
				
				double currentValue = sliderBar.getCurrentValue();
				int round = (int) Math.round(currentValue);
				if (round != previous) {
					previous = round;
					if ((round % modulo) == 0) {
						rollToPage(round, duration, true);
						rollToPage(round, duration, false);
						label.setText("strana:"+round+" - posun");
					} else {
						rollToPage(round, 0.0, false);
						label.setText("strana:"+round);
					}
				}
			}
		});
	}

	private void animateOneJump(int round) {
		if (direction == SliderDirection.LEFT) {
			fxPane.getRotatePool().rollRight(); 
			fxPane.calulateNextPositions();
			fxPane.storeCalculatedPositions();
		} else {
			fxPane.getRotatePool().rollLeft(); 
			fxPane.calulateNextPositions();
			fxPane.storeCalculatedPositions();
		}
		rollToPage(round,duration, true);
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
				// posledni pozice n-2|n-1|na
				// urcuje to co bude nejvice vlevo
				modifySliderBar(result);
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
				DataHandler.setData(result);
				simplePaneContent();
				initialized = true;
			}
		});
		
	}
	

	@Override
	public void onClick(ClickEvent event) {
		ImageMoveWrapper wrapper = this.fxPane.getRotatePool().getWrapper((Widget) event.getSource());
		ImageRotatePool rotatePool = this.fxPane.getRotatePool();
		int intMoveToSelect = this.fxPane.getImgSelector().moveToSelect(wrapper, rotatePool);
		if (intMoveToSelect != 0) {
			ImageMoveWrapper selection = this.fxPane.getImgSelector().getSelection(rotatePool);
			int index = selection.getIndex();
			int current = index + intMoveToSelect;
			int move = this.fxPane.getImgSelector().selectionToSliderPosition(rotatePool,current);
			if (move != 0) {
				rollToPage(move, duration, true);
				sliderBar.setCurrentValue(move);
			} else {
				//openDialog();
			}
		} else {
			//openDialog();
		}
	}



	public void rollToPage(int currentValue, double duration, boolean playEffect) {
		if (initialized) {
			fxPane.rollToPage(currentValue,duration, playEffect);
		}
	}

	
	
	@Override
	public void onJumpChange(String to) {
		int jump = Integer.parseInt(to)-1;
		int translated = this.fxPane.getImgSelector().selectionToSliderPosition(this.fxPane.getRotatePool(), jump);
		rollToPage(translated, 0.0, false);
		animateOneJump(translated);
		this.sliderBar.setCurrentValue(translated);
	}


	@Override
	public void onModuleStepChange(String step) {
		this.modulo = Integer.parseInt(step);
	}


	private void simplePaneContent() {
		RootPanel.get("container").remove(_emptyVerticalPanel);
		createSimpleEffectsPanel();
		this.container.add(this.fxPane);
		this.container.add(this.mvPane);
		this.container.add(this.sliderBar);

		//this.container.add(this.label);
		this.container.add(this.confPanel);
		this.confPanel.setHeight("50px");
		RootPanel.get("container").add(this.container);
		fxPane.getRotatePool().debugPool();
	}


	private void modifySliderBar(Integer result) {
		int maxImage = result-2;
		sliderBar.setMaxValue(maxImage);
		sliderBar.setNumLabels(0);
		sliderBar.setNumTicks(0);
		// prvni pozice  na|0|1
		// urcuje, co bude nejvice vlevo
		sliderBar.setMinValue(-1);
		sliderBar.setCurrentValue(0);
		Double n = result.doubleValue();
		Double divider = new Double(20000);
		sliderBar.setStepSize(n/divider);
		
		sliderBar.setLabelFormatter(new SliderBarFormatter(-1, maxImage));
		sliderBar.setEnabled(!(getNumberOfImages() >= maxImage));
	}

	
	private void createSimpleEffectsPanel() {
		// cele pole v pameti .. posuvatko v ramci pameti
		
		List<SimpleImageTO> itos = DataHandler.getData();
		ImageMoveWrapper[] viewPortImages = new ImageMoveWrapper[getNumberOfImages()];
		for (int i = 0; i < viewPortImages.length; i++) {
			SimpleImageTO ito = itos.get(i);
			ImageMoveWrapper wrapper = createImageMoveWrapper(ito,""+i);
			viewPortImages[i] = wrapper;
			appendClickHandler(viewPortImages[i]);
		}
		

		int width = getConfigurationDistance();
		for (ImageMoveWrapper imw : viewPortImages) {
			width +=  imw.getWidth();
			width += getConfigurationDistance();
		}
		ViewConfiguration conf = new ViewConfiguration();
		{
			conf.setImgDistances(getConfigurationDistance());
			conf.setViewPortHeight(getConfigurationHeight());
			conf.setViewPortWidth(width);
			
		}
		sliderBar.setWidth(""+conf.getViewPortWidth()+"px");
		
		// prava neviditelna strana
		//TODO: Zmenit
		ImageMoveWrapper rcopy = createImageMoveWrapper(DataHandler.getData().get(getNumberOfImages()),"R");
		appendClickHandler(rcopy);

		ImageMoveWrapper[] noVisibleImages = new ImageMoveWrapper[getNumberOfImages()];
		for (int i = 0; i < noVisibleImages.length; i++) {
			SimpleImageTO ito = itos.get(i+4);
			ImageMoveWrapper wrapper = createImageMoveWrapper(ito,"n"+i);
			noVisibleImages[i] = wrapper;
			appendClickHandler(noVisibleImages[i]);
		}
		
		ImageMoveWrapper lcopy = createImageMoveWrapper(DataHandler.getNaImage(),"L");
		appendClickHandler(lcopy);
		this.fxPane = new MoveEffectsPanel( viewPortImages, noVisibleImages, lcopy, rcopy, conf);
		
		MoveHandler handler = new MoveHandler();
		this.fxPane.addMoveListener(handler);
		
		this.mvPane = new MovePointerPanel(conf, this.fxPane.getImgSelector());
		this.fxPane.addMoveListener(this.mvPane);
		
		this.confPanel = new ConfigurationPanel();
		this.confPanel.initConfiguration("0",Integer.toString(this.modulo));
		this.confPanel.addConfigurationChanged(this);
	}


	private void appendClickHandler(ImageMoveWrapper wrap) {
		HasClickHandlers comp = (HasClickHandlers) wrap.getWidget();
		comp.addClickHandler(this);
	}


	public static ImageMoveWrapper createImageMoveWrapper(SimpleImageTO ito, String id) {
		ImageMoveWrapper wrapper = new ImageMoveWrapper(0,0, ito.getWidth(), ito.getHeight(), ito.getUrl(),ito.getIdentification());
		wrapper.setFirst(ito.isFirstPage());
		wrapper.setLast(ito.isLastPage());
		wrapper.getWidget().getElement().setAttribute("id", id);
		wrapper.setIndex(ito.getIndex());
		return wrapper;
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
	}


	class MoveHandler implements MoveListener {
		
		public MoveHandler() {
			super();
		}

		@Override
		public void onMoveLeft(ImageRotatePool pool, boolean effectsPlayed) {
			ImageMoveWrapper selection = fxPane.getImgSelector().getSelection(fxPane.getRotatePool());
			//TODO:
			confPanel.initConfiguration(""+(selection.getIndex()+1), ""+modulo);
			modifyIds(pool);
			GwtViewers.this.direction = SliderDirection.LEFT;
		}

		private void modifyIds(ImageRotatePool pool) {
			ArrayList<ImageMoveWrapper> viewPortImages = pool.getViewPortImages();
			ArrayList<ImageMoveWrapper> novisImages = pool.getNoVisibleImages();
			for (int i = 0; i < viewPortImages.size(); i++) {
				ImageMoveWrapper wrap = viewPortImages.get(i);
				wrap.getWidget().getElement().setAttribute("id", "vis_"+i+"("+wrap.getIndex()+")");
			}
			
			ImageMoveWrapper leftSideImage = pool.getLeftSideImage();
			leftSideImage.getWidget().getElement().setAttribute("id","left("+leftSideImage.getIndex()+")");
			ImageMoveWrapper rightSideImage = pool.getRightSideImage();
			rightSideImage.getWidget().getElement().setAttribute("id", "right("+rightSideImage.getIndex()+")");
			
			for (int i = 0; i < novisImages.size(); i++) {
				ImageMoveWrapper wrap = novisImages.get(i);
				wrap.getWidget().getElement().setAttribute("id", "novis_"+i+"("+wrap.getIndex()+")");
			}

		}



		
		
		@Override
		public void onMoveRight(ImageRotatePool pool, boolean effectsPlayed) {
			modifyIds(pool);
			ImageMoveWrapper selection = fxPane.getImgSelector().getSelection(fxPane.getRotatePool());
			//TODO:
			confPanel.initConfiguration(""+(selection.getIndex()+1), ""+modulo);
			GwtViewers.this.direction = SliderDirection.RIGHT;
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

	public native void openDialog() /*-{
		$wnd._jqueryDialog();
	}-*/;
	
	public native int getConfigurationDistance() /*-{
		return $wnd.__confDistance;
	}-*/;
	
	public native int getNumberOfImages() /*-{
		return $wnd.__confNumberOfImages;
	}-*/;
	
	public native void setNumberOfImages(int n) /*-{
		$wnd.__confNumberOfImages = n;
	}-*/;
	

	//TODO: Zrusit !!
	public native int getDialogWidth() /*-{
		return $wnd.__dialog_width;
	}-*/;
	
	public native void setDialogWidth(int width) /*-{
		$wnd.__dialog_width = width;
	}-*/;

	public native int getDialogHeight() /*-{
		return $wnd.__dialog_height;
	}-*/;

	public native void setDialogHeight(int height) /*-{
		$wnd.__dialog_height = height;
	}-*/;

	public native int getDialogTitle() /*-{
		return $wnd.__dialog_title;
	}-*/;

	public native void setDialogTitle(String title) /*-{
		$wnd.__dialog_title = title;
	}-*/;

	public native int getFullImageUrl() /*-{
		return $wnd.__dialog_full_image_url;
	}-*/;

	public native void setFullImageUrl(String url) /*-{
		$wnd.__dialog_full_image_url = url;
	}-*/;
}
