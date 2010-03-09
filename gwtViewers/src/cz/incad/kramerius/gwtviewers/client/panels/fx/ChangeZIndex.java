package cz.incad.kramerius.gwtviewers.client.panels.fx;

import org.adamtacy.client.ui.effects.core.NMorphStyle;
import org.adamtacy.client.ui.effects.impl.css.Rule;

public class ChangeZIndex extends NMorphStyle {

	  public ChangeZIndex(int oldZIndex, int newZIndex) {
		    super(new Rule("startMove{z-index:"+(oldZIndex)+";}"), new Rule("endMove{z-index:"+(newZIndex)+";}"));
	  }

}
