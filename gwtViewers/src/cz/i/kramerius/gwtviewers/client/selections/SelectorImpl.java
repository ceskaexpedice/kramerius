package cz.i.kramerius.gwtviewers.client.selections;

import com.google.gwt.user.client.Window;

import cz.i.kramerius.gwtviewers.client.panels.ImageMoveWrapper;
import cz.i.kramerius.gwtviewers.client.panels.utils.ImageRotatePool;

public class SelectorImpl implements Selector {

	public static String SELECTED_CLASS = "page_selected";
	public static String NOT_SELECTED_CLASS = "page_not_selected";
	
	private ImageMoveWrapper selection;
	
	
	@Override
	public ImageMoveWrapper getSelection(ImageRotatePool rotatePool) {
		return this.selection;
	}

	@Override
	public void changeSelection(ImageMoveWrapper wrapper) {
		this.selection = wrapper;
		//Window.alert("Selected:"+this.selection);
	}

	@Override
	public boolean isSelected(ImageMoveWrapper wrapper, ImageRotatePool pool) {
		return wrapper.getImageIdent().equals(this.selection.getImageIdent());
	}

	@Override
	public void markSelect() {
		if (this.selection != null) {
			this.selection.getWidget().setStyleName(SELECTED_CLASS);
			this.selection.getWidget().setStylePrimaryName(SELECTED_CLASS);
			System.out.println(this.selection.getWidget().getStyleName());
		}
	}

	@Override
	public void markUnselect() {
		if (this.selection != null) {
			this.selection.getWidget().setStyleName(NOT_SELECTED_CLASS);
		}
	}

	public static void markUnselect(ImageMoveWrapper wrapper) {
		wrapper.getWidget().setStyleName(NOT_SELECTED_CLASS);
	}
	
	public static void markSelect(ImageMoveWrapper wrapper) {
		wrapper.getWidget().setStyleName(SELECTED_CLASS);
	}
}
