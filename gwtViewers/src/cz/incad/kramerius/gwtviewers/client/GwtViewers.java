
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

	private MoveEffectsPanel fxPane;
	private ConfigurationPanel confPanel;
	
	
	private final PageServiceAsync pageService = GWT.create(PageService.class);
	private VerticalPanel _emptyVerticalPanel = new VerticalPanel();
	private VerticalPanel container = new VerticalPanel();
	private Label label = new Label();
	
	private boolean initialized = false;
	private int modulo = 1;
	private double duration = 0.3; 
	

	private ModuloCreator moduloCreator = GWT.create(ModuloCreator.class);
	private Button leftButton;
	private Button rightButton;
	
	
	private SliderBar sliderBar = new SliderBar(1, 10);
	{
		sliderBar.setStepSize(1.0);
		sliderBar.setCurrentValue(0);
	    sliderBar.setNumLabels(0);
	    sliderBar.setLabelFormatter(null);

	}
	private SliderChangeListener sliderChangeListener;
	
	
	private void doInitImages() {
		System.out.println("Initializing images ... ");
		String uuidPath = getUUIDPath();
		pageService.getNumberOfPages(uuidPath, new AsyncCallback<Integer>() {
			@Override
			public void onFailure(Throwable caught) {
				System.out.println("False initialisation.. ");
				initialized = false;
			}

			@Override
			public void onSuccess(Integer result) {
				System.out.println("Got result ");
				modulo = moduloCreator.createModulo(result);
			}

		});
		pageService.getPagesSet(uuidPath, new AsyncCallback<PagesResultSet>() {

			@Override
			public void onFailure(Throwable caught) {
				initialized = false;
			}

			@Override
			public void onSuccess(PagesResultSet result) {
				DataHandler.get().setData(result.getData());
				DataHandler.get().setCurrentIndex(result.getCurrentSimpleImageTOIndex());
				DataHandler.get().setCurrentId(result.getCurrentSimpleImageTOId());
				System.out.println("Current selected index :"+result.getCurrentSimpleImageTOIndex());
				System.out.println("Current selected id :"+result.getCurrentSimpleImageTOId());
				simplePaneContent();
				initialized = true;
			}
		});
	}
	
	@Override
	public void onClick(ClickEvent event) {
		ImageMoveWrapper wrapper = this.fxPane.getRotatePool().getWrapper((Widget) event.getSource());
		fxPane.getImgSelector().markUnselect(fxPane.getRotatePool());
		fxPane.getImgSelector().changeSelection(wrapper, this);
		fxPane.getImgSelector().markSelect(fxPane.getRotatePool());
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
	public void onModuleStepChange(String step) {
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
				modifySliderBar(DataHandler.get().getMax());
				sliderChangeListener = new SliderChangeListener(modulo, duration, fxPane);
				sliderBar.addChangeListener(sliderChangeListener);
				sliderBar.addMouseUpHandler(sliderChangeListener);
			}
		});
	}


	private void modifySliderBar(Integer result) {
		int maxImage = result-2;
		sliderBar.setMaxValue(maxImage-getNumberOfImages()+2);
		sliderBar.setNumLabels(0);
		sliderBar.setNumTicks(0);
		// prvni pozice  na|0|1
		// urcuje, co bude nejvice vlevo
		sliderBar.setMinValue(0);
		sliderBar.setCurrentValue(0);
		Double n = result.doubleValue();
		Double divider = new Double(20000);
		sliderBar.setStepSize(n/divider);
		
		sliderBar.setLabelFormatter(new SliderBarFormatter(0, maxImage));
		sliderBar.setEnabled(!(getNumberOfImages() >= maxImage));
	}

	
	private void createSimpleEffectsPanel() {
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
		sliderBar.setWidth(""+conf.getViewPortWidth()+"px");
		appendClickHandler(rotatePool.getLeftSideImage());
		this.fxPane = new MoveEffectsPanel(rotatePool, conf);
		
		MoveHandler handler = new MoveHandler();
		this.fxPane.addMoveListener(handler);
		
		this.confPanel = new ConfigurationPanel();
		this.confPanel.initConfiguration("0",Integer.toString(this.modulo));
		this.confPanel.addConfigurationChanged(this);
	}


	private ImageRotatePool createImageRotatePool() {
		// zacatek
		ImageMoveWrapper[] viewPortImages = new ImageMoveWrapper[getNumberOfImages()];
		for (int i = 0; i < viewPortImages.length; i++) {
			ImageMoveWrapper wrapper = createImageMoveWrapper(i,""+i);
			wrapper.getWidget().getElement().getStyle().setZIndex(ImageRotatePool.VIEW_IMAGES_Z_INDEX);
			viewPortImages[i] = wrapper;
			appendClickHandler(viewPortImages[i]);
		}
		ImageMoveWrapper rcopy = createImageMoveWrapper(getNumberOfImages(),"R");
		rcopy.getWidget().getElement().getStyle().setZIndex(ImageRotatePool.LEFTRIGNT_IMAGES_Z_INDEX);
		appendClickHandler(rcopy);

		ImageMoveWrapper[] noVisibleImages = new ImageMoveWrapper[getNumberOfImages()];
		for (int i = 0; i < noVisibleImages.length; i++) {
			ImageMoveWrapper wrapper = createImageMoveWrapper(i+getNumberOfImages()+1,"n"+i);
			wrapper.getWidget().getElement().getStyle().setZIndex(ImageRotatePool.NOVIEW_IMAGES_Z_INDEX);
			noVisibleImages[i] = wrapper;
			appendClickHandler(noVisibleImages[i]);
		}
		
		ImageMoveWrapper lcopy = createImageMoveWrapper(DataHandler.get().getMax(),"L");
		lcopy.getWidget().getElement().getStyle().setZIndex(ImageRotatePool.LEFTRIGNT_IMAGES_Z_INDEX);
		ImageRotatePool rotatePool = new ImageRotatePool(viewPortImages, noVisibleImages, lcopy, rcopy);
		return rotatePool;
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
			ImageMoveWrapper selection = fxPane.getRotatePool().getVisibleImages().get(0);
			confPanel.initConfiguration(""+(selection.getIndex()+1), ""+modulo);
			modifyIds(pool);
			informAboutPagesRange();
			//GwtViewers.this.direction = SliderDirection.LEFT;
		}

		private void informAboutPagesRange() {
			int from = fxPane.getRotatePool().getPointer();
			int to = from + fxPane.getRotatePool().getVisibleImageSize();
			pages(from+1, to+1);
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


	@Override
	public void onMouseMove(MouseMoveEvent event) {
	}
}
