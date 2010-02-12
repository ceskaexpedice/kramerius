package cz.i.kramerius.gwtviewers.client.panels;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;

import cz.i.kramerius.gwtviewers.client.panels.utils.ImageRotatePool;
import cz.i.kramerius.gwtviewers.client.selections.SelectionDecorator;
import cz.i.kramerius.gwtviewers.client.selections.Selector;

public class MovePointerPanel extends Composite implements MoveListener {

	private AbsolutePanel absolutePanel = new AbsolutePanel();
	private ViewConfiguration configuration;
	private Selector selector;
	
	public MovePointerPanel(ViewConfiguration configuration, Selector selector) {
		super();
		this.configuration = configuration;
		this.selector = selector;
		
		this.absolutePanel.setWidth(this.configuration.getViewPortWidth()+"px");
		this.absolutePanel.setHeight("40px");
		initWidget(this.absolutePanel);
	}

	@Override
	public void onMoveLeft(ImageRotatePool pool, boolean effectsPlayed) {
		decorateSelection(pool);
	}

	@Override
	public void onMoveRight(ImageRotatePool pool, boolean effectsPlayed) {
		this.decorateSelection(pool);
	}
	
	private void decorateSelection(ImageRotatePool pool) {
		selector.getSelectionDecorator().decoratePanel(this.absolutePanel, pool);
	}

	
}
