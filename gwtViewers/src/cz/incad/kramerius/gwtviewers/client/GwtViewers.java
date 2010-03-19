
package cz.incad.kramerius.gwtviewers.client;


import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasAllMouseHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.widgetideas.client.SliderBar;

import cz.incad.kramerius.gwtviewers.client.data.DataHandler;
import cz.incad.kramerius.gwtviewers.client.panels.ConfigurationChanged;
import cz.incad.kramerius.gwtviewers.client.panels.ConfigurationPanel;
import cz.incad.kramerius.gwtviewers.client.panels.ImageMoveWrapper;
import cz.incad.kramerius.gwtviewers.client.panels.MoveEffectsPanel;
import cz.incad.kramerius.gwtviewers.client.panels.MoveListener;
import cz.incad.kramerius.gwtviewers.client.panels.ViewConfiguration;
import cz.incad.kramerius.gwtviewers.client.panels.utils.ImageRotatePool;
import cz.incad.kramerius.gwtviewers.client.slider.SliderBarFormatter;
import cz.incad.kramerius.gwtviewers.client.slider.SliderChangeListener;

public class GwtViewers implements EntryPoint, ClickHandler, ConfigurationChanged, MouseMoveHandler {
	
	public static GwtViewers _sharedInstance = null;

	// panel s efekty
	private MoveEffectsPanel fxPane;
	// konfigurace skoku
	private ConfigurationPanel confPanel;
	
	// sluzba pro podavani stranek
	private final PageServiceAsync pageService = GWT.create(PageService.class);
	private VerticalPanel _emptyVerticalPanel = new VerticalPanel();
	private VerticalPanel container = new VerticalPanel();
	private Label label = new Label();
	
	private boolean initialized = false;
	private int modulo = 1;
	private double duration = 0.3; 
	

	private ModuloCreator moduloCreator = GWT.create(ModuloCreator.class);
	private MoveHandler moveHandler = new MoveHandler();
	
	
	
	private SliderBar sliderBar = new SliderBar(1, 10);
	{
		sliderBar.setStepSize(1.0);
		sliderBar.setCurrentValue(0);
	    sliderBar.setNumLabels(0);
	    sliderBar.setLabelFormatter(null);

	}
	private SliderChangeListener sliderChangeListener;
	
	private void doInitImages() {
		message("Initializing images ...");
		String uuidPath = getUUIDPath();
		pageService.getNumberOfPages(uuidPath, new AsyncCallback<Integer>() {
			@Override
			public void onFailure(Throwable caught) {
				message("Initialization fails");
				initialized = false;
			}

			@Override
			public void onSuccess(Integer result) {
				message("received number of images");
				modulo = moduloCreator.createModulo(result);
			}

		});
		pageService.getPagesSet(uuidPath, new AsyncCallback<PagesResultSet>() {

			@Override
			public void onFailure(Throwable caught) {
				message("initialization failure");
				initialized = false;
			}

			@Override
			public void onSuccess(PagesResultSet result) {
				message("received page resulset");
				DataHandler.get().setData(result.data);
				DataHandler.get().setCurrentId(result.getCurrentSimpleImageTOId());
				DataHandler.get().setCurrentIndex(result.getCurrentSimpleImageTOIndex());
				// dosud v pohode
				simplePaneContent();
				initialized = true;
			}
		});
	}
	
	@Override
	public void onClick(ClickEvent event) {
		ImageMoveWrapper wrapper = this.fxPane.getRotatePool().getWrapper((Widget) event.getSource());
		changeSelection(wrapper);
	}

	private void changeSelection(ImageMoveWrapper wrapper) {
		fxPane.getImgSelector().markUnselect(fxPane.getRotatePool());
		fxPane.getImgSelector().changeSelection(wrapper, this);
		fxPane.getImgSelector().markSelect(fxPane.getRotatePool());
	}

	
	public void requestToChangeSelection(String uuid) {
		int dataPosition = -1;
		List<SimpleImageTO> data = DataHandler.get().getData();
		for (int i = 0; i < data.size(); i++) {
			SimpleImageTO sit = data.get(i);
			if (sit.getIdentification().equals(uuid)) {
				//this.sliderBar.setCurrentValue(i);
				dataPosition = i;
				break;
			}
		}
		if (dataPosition != -1) {
			int windowStart = this.fxPane.getRotatePool().getPointer();
			int windowStop = createWindowStop(windowStart, getNumberOfImages());
			message("windowStart, windowStop "+windowStart+","+windowStop);
			message("dataposition "+dataPosition);

			message("dataPosition <= windowStart"+(dataPosition <= windowStart));
			message("dataPosition >= windowStop"+(dataPosition >= windowStop));
			
			if ((dataPosition <= windowStart) || (dataPosition >= windowStop)) {
				this.sliderBar.setCurrentValue(dataPosition);
				message("data positon "+dataPosition);
				message("slider position "+this.sliderBar.getCurrentValue());
				if (modulo != 1) {
					this.sliderChangeListener.onMouseUp(null);
				}
			}	
			ImageMoveWrapper wrapper = fxPane.getRotatePool().getWrapper(uuid);
			changeSelection(wrapper);
		}
	}
	
	
	@Override
	public void onJumpChange(String to) {
//		int jump = Integer.parseInt(to)-1;
//		int translated = this.fxPane.getImgSelector().selectionToSliderPosition(this.fxPane.getRotatePool(), jump);
//		rollToPage(translated, 0.0, false);
//		animateOneJump(translated);
//		this.sliderBar.setCurrentValue(translated);
	}


	@Override
	public void onModuloStepChange(String step) {
		this.modulo = Integer.parseInt(step);
		this.sliderChangeListener.setModulo(this.modulo);
	}


	private void simplePaneContent() {
		RootPanel.get("container").remove(_emptyVerticalPanel);
		createSimpleEffectsPanel();

		this.container.add(this.fxPane);
		this.container.add(this.sliderBar);
		this.container.add(this.label);
		this.container.add(this.confPanel);
		this.confPanel.setHeight("50px");
		RootPanel.get("container").add(this.container);
		
		DeferredCommand.addCommand(new Command() { 
			public void execute() {
				modifySliderBar();
				sliderChangeListener = new SliderChangeListener(modulo, duration, sliderBar, fxPane);
				sliderBar.addChangeListener(sliderChangeListener);
				sliderBar.addMouseUpHandler(sliderChangeListener);
				
				ImageMoveWrapper wrapper = fxPane.getRotatePool().getWrapper(DataHandler.get().getCurrentId());
				if (wrapper != null) {
					fxPane.getImgSelector().changeSelection(wrapper, _sharedInstance);
					fxPane.getImgSelector().markSelect(fxPane.getRotatePool());
				}
				
				moveHandler.informAboutPagesRange();
			}
		});
	}


	private void modifySliderBar() {
		int maxImage = DataHandler.get().getMax()-2;
		sliderBar.setMaxValue(maxImage-getNumberOfImages()+2);
		sliderBar.setNumLabels(0);
		sliderBar.setNumTicks(0);
		// prvni pozice  na|0|1
		// urcuje, co bude nejvice vlevo
		sliderBar.setMinValue(0);
		sliderBar.setCurrentValue(createWindowStart());
		Double n = new Integer(DataHandler.get().getMax()).doubleValue();
		Double divider = new Double(20000);
		sliderBar.setStepSize(n/divider);
		
		sliderBar.setLabelFormatter(new SliderBarFormatter(0, maxImage));
		sliderBar.setEnabled(!(getNumberOfImages() >= maxImage));
	}

	
	private void createSimpleEffectsPanel() {
		// vytvoreni imagerotate pool -> v pohode
		ImageRotatePool rotatePool = createImageRotatePool();
		int width = getConfigurationDistance();
		for (ImageMoveWrapper imw : rotatePool.getVisibleImages()) {
			width +=  imw.getWidth();
			width += getConfigurationDistance();
		}
		ViewConfiguration conf = ViewConfiguration.getConfiguration(); {
			conf.setImgDistances(getConfigurationDistance());
			conf.setViewPortHeight(getConfigurationHeight());
			conf.setViewPortWidth(width);
		}
		// slide bar set width v pohde
		sliderBar.setWidth(""+conf.getViewPortWidth()+"px");

		appendClickHandler(rotatePool.getLeftSideImage());
		this.fxPane = new MoveEffectsPanel(rotatePool, conf);
		
		this.fxPane.addMoveListener(this.moveHandler);
		
		this.confPanel = new ConfigurationPanel();
		this.confPanel.initConfiguration("0",Integer.toString(this.modulo));
		this.confPanel.addConfigurationChanged(this);
	}


	private ImageRotatePool createImageRotatePool() {
		// zacatek
		ArrayList<ImageMoveWrapper> visibleImages = new ArrayList<ImageMoveWrapper>(getNumberOfImages());
		int indexStart = createWindowStart();
		int indexStop = createWindowStop(indexStart, getNumberOfImages());
		for (int i = indexStart, j=0; i < indexStop; i++,j++) {
			ImageMoveWrapper wrapper = createImageMoveWrapper(i,""+i);
			wrapper.getWidget().getElement().getStyle().setZIndex(ImageRotatePool.VIEW_IMAGES_Z_INDEX);
			visibleImages.add(wrapper);
			appendClickHandler(wrapper);
		}
		
		ImageMoveWrapper rcopy = createImageMoveWrapper(getNumberOfImages(),"R");
		rcopy.getWidget().getElement().getStyle().setZIndex(ImageRotatePool.LEFTRIGNT_IMAGES_Z_INDEX);
		appendClickHandler(rcopy);

		// neni dulezite ImageRotatePool si preusporada
		ArrayList<ImageMoveWrapper> noVisibleImages = new ArrayList<ImageMoveWrapper>(getNumberOfImages());
		for (int i = indexStart,j=0; i < indexStop; i++,j++) {
			ImageMoveWrapper wrapper = createImageMoveWrapper(i,"n"+i);
			wrapper.getWidget().getElement().getStyle().setZIndex(ImageRotatePool.NOVIEW_IMAGES_Z_INDEX);
			noVisibleImages.add(wrapper);
			//noVisibleImages[j] = wrapper;
			appendClickHandler(wrapper);
		}
		
		System.out.println("No visible images == "+noVisibleImages.size());
		ImageMoveWrapper lcopy = createImageMoveWrapper(DataHandler.get().getMax(),"L");
		lcopy.getWidget().getElement().getStyle().setZIndex(ImageRotatePool.LEFTRIGNT_IMAGES_Z_INDEX);
		ImageRotatePool rotatePool = new ImageRotatePool(visibleImages, noVisibleImages, lcopy, rcopy, indexStart);
		return rotatePool;
	}


	private int createWindowStop(int windowStart, int numberOfImages) {
		return Math.min(windowStart + numberOfImages, DataHandler.get().getMax());
	}

	private int createWindowStart() {
		int firstArg = Math.max(DataHandler.get().getMax()-getNumberOfImages(),0);
		int secondArg = getCurIndexFromData();
		int min = Math.min(firstArg, secondArg);
		System.out.println("createWindowStart("+firstArg+","+secondArg+") = "+min);
		return min;
	}

	private int getCurIndexFromData() {
		int indexFromData = Math.max(DataHandler.get().getCurrentIndex(), 0);
		System.out.println("Index from data :"+indexFromData);
		return indexFromData;
	}

	private void appendClickHandler(ImageMoveWrapper wrap) {
		HasClickHandlers comp = (HasClickHandlers) wrap.getWidget();
		comp.addClickHandler(this);
	}


	public static ImageMoveWrapper createImageMoveWrapper(int itoIndex, String id) {
		SimpleImageTO ito = itoIndex< DataHandler.get().getMax() ? DataHandler.get().getData().get(itoIndex) : DataHandler.get().getNaImage();
		ImageMoveWrapper wrapper = new ImageMoveWrapper(0,0, ito.getWidth(), ito.getHeight(), ito.getUrl(),ito.getIdentification());
		wrapper.setFirst(ito.isFirstPage());
		wrapper.setLast(ito.isLastPage());
		wrapper.getWidget().getElement().setAttribute("id", id);
		wrapper.setIndex(ito.getIndex());
		wrapper.modifyHeightAndWidth();
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
		message("Loading module ...");
		_sharedInstance = this;
		RootPanel.get("container").add(this._emptyVerticalPanel);	
		doInitImages();
		exportMethods(this);
	}


	class MoveHandler implements MoveListener {
		
		public MoveHandler() {
			super();
		}

		@Override
		public void onMoveLeft(ImageRotatePool pool, boolean effectsPlayed) {
			ImageMoveWrapper selection = fxPane.getRotatePool().getVisibleImages().get(0);
			confPanel.initConfiguration(""+(selection.getIndex()+1), ""+modulo);
			modifyIds(pool);
			informAboutPagesRange();
			//GwtViewers.this.direction = SliderDirection.LEFT;
		}

		public void informAboutPagesRange() {
			int from = fxPane.getRotatePool().getPointer();
			int to = from + fxPane.getRotatePool().getVisibleImageSize();
			pages(from+1, to);
		}

		private void modifyIds(ImageRotatePool pool) {
			ArrayList<ImageMoveWrapper> viewPortImages = pool.getVisibleImages();
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
			ImageMoveWrapper selection = fxPane.getRotatePool().getVisibleImages().get(0);
			confPanel.initConfiguration(""+(selection.getIndex()+1), ""+modulo);
			informAboutPagesRange();
			//GwtViewers.this.direction = SliderDirection.RIGHT;
		}

		@Override
		public void onPointerLeft(ImageRotatePool pool) {
			//GwtViewers.this.direction = SliderDirection.LEFT;
			informAboutPagesRange();
		}

		@Override
		public void onPointerRight(ImageRotatePool pool) {
			//GwtViewers.this.direction = SliderDirection.RIGHT;
			informAboutPagesRange();
		}
	}
	

	/// ========= Nativni metody =========
	public native String getUUIDPath() /*-{
		return $wnd.__gwtViewersUUIDPATH;
	}-*/;

	public native void setUUIDPath(String uuid) /*-{
		$wnd.__gwtViewersUUID = uuid;
	}-*/;

	
	public native int getConfigurationWidth() /*-{
		return $wnd.__confWidth;
	}-*/;

	public native void setConfigurationWidth(int width) /*-{
		$wnd.__confWidth = width;
	}-*/;
	
	public native int getConfigurationHeight() /*-{
		return $wnd.__confHeight;
	}-*/;

	public native void setConfigurationHeight(int height) /*-{
		$wnd.__confHeight  = height;
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
	

	public native void changeGWTSelection(String uuid) /*-{
		$wnd.selectPage(uuid);
	}-*/;

	public native void pages(int from, int to) /*-{
		$wnd.pages(from, to);
	}-*/;

	
	public native boolean debugEnabled() /*-{
		if (typeof($wnd.__debug) == "undefined") {
			return false;
		} else {
			return $wnd.__debug;
		}
	}-*/;

	/**
	 * Exports some methods to pure js
	 * @param gwtV
	 */
	public native void exportMethods(GwtViewers gwtV) /*-{
	   $wnd.requestToSelect = function (uuid) {
	       gwtV.@cz.incad.kramerius.gwtviewers.client.GwtViewers::requestToChangeSelection(Ljava/lang/String;)(uuid);
	   };
	   
	   $wnd.testExport = function (uuid) {
			alert("Exporeted function");
	   };
	   
	}-*/;
	
	public void message(String messge) {
		if (debugEnabled()) {
			Window.alert(messge);
		}
	}
	

	@Override
	public void onMouseMove(MouseMoveEvent event) {
	}
}
