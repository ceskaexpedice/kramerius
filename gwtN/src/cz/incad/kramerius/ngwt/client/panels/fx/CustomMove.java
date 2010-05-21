package cz.incad.kramerius.ngwt.client.panels.fx;

import java.util.Vector;

import org.adamtacy.client.ui.effects.core.NMorphStyle;
import org.adamtacy.client.ui.effects.impl.css.Rule;

/**
 * Custom move image
 * @author pavels
 */
public class CustomMove extends NMorphStyle {


	public CustomMove(int oldX, int oldY, int x, int y) {
		super();
		setNewStyles(new Rule("start{position: relative; left:"+oldX+"px; top: "+oldY+"px;}"), new Rule("end{position:relative; left: "+x+"px; top: "+y+"px;}"));
	}
 

	@Override
	public void tearDownEffect() {
		if (thePanel != null) {
			effectElement.getStyle().setProperty("top",
					thePanel.getElement().getStyle().getProperty("top"));
			effectElement.getStyle().setProperty("left",
					thePanel.getElement().getStyle().getProperty("left"));
		}
		
	}



	@Override
	public void run(int duration, double startTime) {
		super.run(duration, startTime);
	}



	@Override
	public void run(int duration) {
		super.run(duration);
	}




	@Override
	public void setUpEffect() {
		super.setUpEffect();

		if (this.propertyPairs == null) return;
		Vector<PropertyPair> propertyPairs2 = this.propertyPairs;
		for (PropertyPair propertyPair : propertyPairs2) {
			System.out.println(propertyPair);
		}

		
	}

}
