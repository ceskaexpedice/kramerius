package cz.incad.kramerius.gwtviewers.client.selections;

import cz.incad.kramerius.gwtviewers.client.panels.ImageMoveWrapper;

public interface SelectionListener {

	public void selectionChanged(ImageMoveWrapper oldW, ImageMoveWrapper newW);
}
