package cz.i.kramerius.gwtviewers.client.selections;

import cz.i.kramerius.gwtviewers.client.panels.ImageMoveWrapper;

public interface SelectionListener {

	public void selectionChanged(ImageMoveWrapper oldW, ImageMoveWrapper newW);
}
