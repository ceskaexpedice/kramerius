package cz.i.kramerius.gwtviewers.client.selections;

import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

import cz.i.kramerius.gwtviewers.client.panels.ImageMoveWrapper;
import cz.i.kramerius.gwtviewers.client.panels.utils.ImageRotatePool;


public class MidleImgSelectDecorator implements SelectionDecorator {

	private MiddleImgSelector selector;
	private VerticalPanel verticalPanel  = null; //= new HorizontalPanel();
	private Label label;
	private Image image;
	private int center;
	
	public MidleImgSelectDecorator(MiddleImgSelector selector, int center) {
		super();
		this.selector = selector;
		this.center = center;
	}

	public VerticalPanel createVerticalPane() {
		VerticalPanel vert = new VerticalPanel();
		this.image = new Image("pointer.png");
		this.label = new Label();
		vert.add(image);
		vert.add(label);
		return vert;
	}


	@Override
	public void decoratePanel(HasWidgets widgets, ImageRotatePool pool) {
		ImageMoveWrapper selection = this.selector.getSelection(pool);
		if (verticalPanel == null) {
			verticalPanel = createVerticalPane();
			verticalPanel.getElement().getStyle().setPosition(Position.ABSOLUTE);
			verticalPanel.getElement().getStyle().setLeft(this.center-45, Unit.PX);
			verticalPanel.getElement().getStyle().setWidth(90, Unit.PX);
			verticalPanel.setCellHorizontalAlignment(label, VerticalPanel.ALIGN_CENTER);
			widgets.add(verticalPanel);
		}
		//Label label = new Label(""+(selection.getIndex()+1));
		label.setText(""+(selection.getIndex()+1));
	}
}
