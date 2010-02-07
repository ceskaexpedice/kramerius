package cz.i.kramerius.gwtviewers.client.selections;

import com.google.gwt.user.client.ui.HasWidgets;

import cz.i.kramerius.gwtviewers.client.panels.utils.ImageRotatePool;

public interface SelectionDecorator {

	public void decoratePanel(HasWidgets widgets, ImageRotatePool pool);
	
}
