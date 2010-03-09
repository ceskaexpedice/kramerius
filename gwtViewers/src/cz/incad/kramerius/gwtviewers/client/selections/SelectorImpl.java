package cz.incad.kramerius.gwtviewers.client.selections;

import java.util.ArrayList;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;

import cz.incad.kramerius.gwtviewers.client.GwtViewers;
import cz.incad.kramerius.gwtviewers.client.panels.ImageMoveWrapper;
import cz.incad.kramerius.gwtviewers.client.panels.MoveListener;
import cz.incad.kramerius.gwtviewers.client.panels.utils.ImageRotatePool;

public class SelectorImpl implements Selector, MoveListener  {

	public static String SELECTED_CLASS = "page_selected";
	public static String NOT_SELECTED_CLASS = "page_not_selected";
	
	private String selection;
	
	@Override
	public ImageMoveWrapper getSelection(ImageRotatePool rotatePool) {
		ArrayList<ImageMoveWrapper> viewPortImages = rotatePool.getVisibleImages();
		for (ImageMoveWrapper wrapper : viewPortImages) {
			if (wrapper.getImageIdent().equals(this.selection)) 
				return wrapper;
		}
		return null;
	}

	@Override
	public void changeSelection(ImageMoveWrapper wrapper, GwtViewers gwtViewers) {
		this.selection = wrapper.getImageIdent();
		gwtViewers.changeGWTSelection(this.selection);
	}

	@Override
	public boolean isSelected(ImageMoveWrapper wrapper, ImageRotatePool pool) {
		return this.selection != null ? (this.selection.equals(wrapper.getImageIdent())) : false;
	}

	@Override
	public void markSelect(ImageRotatePool pool) {
		if (this.selection != null) {
			ImageMoveWrapper foundWrapper = findWrapper(pool);
			setSelectedStyle(foundWrapper);
		}
	}

	private void setSelectedStyle(ImageMoveWrapper foundWrapper) {
		if (foundWrapper != null) {
			foundWrapper.getWidget().setStyleName(SELECTED_CLASS);
			foundWrapper.getWidget().setStylePrimaryName(SELECTED_CLASS);
		}
	}

	private ImageMoveWrapper findWrapper(ImageRotatePool pool) {
		ArrayList<ImageMoveWrapper> imgs = pool.getVisibleImages();
		for (ImageMoveWrapper wrapper : imgs) {
			if (wrapper.getImageIdent().equals(this.selection)) return wrapper;
		}
		return null;
	}

	@Override
	public void markUnselect(ImageRotatePool pool) {
		if (this.selection != null) {
			ImageMoveWrapper foundWrapper = findWrapper(pool);
			setUnselectStyle(foundWrapper);
		}
	}

	private void setUnselectStyle(ImageMoveWrapper foundWrapper) {
		if (foundWrapper != null) {
			foundWrapper.getWidget().setStyleName(NOT_SELECTED_CLASS);
			foundWrapper.getWidget().setStylePrimaryName(NOT_SELECTED_CLASS);
		}
	}

	public static void markUnselect(ImageMoveWrapper wrapper) {
		wrapper.getWidget().setStyleName(NOT_SELECTED_CLASS);
	}
	
	public static void markSelect(ImageMoveWrapper wrapper) {
		wrapper.getWidget().setStyleName(SELECTED_CLASS);
	}

	@Override
	public void onMoveLeft(ImageRotatePool pool, boolean effectsPlayed) {
		changeMarkSelection(pool);
	}

	@Override
	public void onMoveRight(ImageRotatePool pool, boolean effectsPlayed) {
		changeMarkSelection(pool);
	}

	private void changeMarkSelection(final ImageRotatePool pool) {
		DeferredCommand.addCommand(new Command() {
			@Override
			public void execute() {
				ArrayList<ImageMoveWrapper> viewPortImages = pool.getVisibleImages();
				for (ImageMoveWrapper wrapper : viewPortImages) {
					if (wrapper.getImageIdent().equals(selection)) {
						setSelectedStyle(wrapper);
					} else {
						setUnselectStyle(wrapper);
					}
				}
				ArrayList<ImageMoveWrapper> noVisibleImages = pool.getNoVisibleImages();
				for (ImageMoveWrapper nWrap : noVisibleImages) {
					setUnselectStyle(nWrap);
				}
				
			}
		});
	}

	@Override
	public void onPointerLeft(ImageRotatePool pool) {
	}

	@Override
	public void onPointerRight(ImageRotatePool pool) {
	}
}
