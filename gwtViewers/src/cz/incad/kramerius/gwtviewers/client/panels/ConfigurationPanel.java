package cz.incad.kramerius.gwtviewers.client.panels;

import java.util.ArrayList;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;

public class ConfigurationPanel extends Composite implements ClickHandler {
	
	private DisclosurePanel disclosurePanel;
	private HorizontalPanel horizontalPanel;
	
	private TextBox stepTextBox;
	
	private String step;
	
	private ArrayList<ConfigurationChanged> listeners = new ArrayList<ConfigurationChanged>();
	
	
	public ConfigurationPanel() {
		super();

		this.horizontalPanel = new HorizontalPanel();
		Widget moduloPane = createSetModuloPanel();
		this.horizontalPanel.add(moduloPane);
		this.horizontalPanel.setCellHorizontalAlignment(moduloPane, HorizontalPanel.ALIGN_RIGHT);

		this.disclosurePanel = new DisclosurePanel("Vice ... ");
		this.disclosurePanel.setContent(this.horizontalPanel);
		this.disclosurePanel.setVisible(true);
		this.disclosurePanel.setAnimationEnabled(true);
		this.disclosurePanel.setOpen(false);
		VerticalPanel vPanel = new VerticalPanel();
		vPanel.add(disclosurePanel);
		this.initWidget(vPanel);
	}

	private Widget createSetModuloPanel() {
		
		VerticalPanel vertPan = new VerticalPanel();
		vertPan.add(new Label("Krok:"));
		stepTextBox = new TextBox();
		stepTextBox.getElement().setId("moduleText");
		vertPan.add(stepTextBox);
		
		Button but = new Button("Nastav krok");
		but.getElement().setId("stepButton");
		but.addClickHandler(this);
		vertPan.add(but);
		vertPan.setCellHorizontalAlignment(but, HorizontalPanel.ALIGN_RIGHT);
		
		return vertPan;
	}

	@Override
	public void onClick(ClickEvent event) {
		this.step = stepTextBox.getText();
		this.fireModuleChnaged();
	}

	public void initConfiguration(String currentPos, String step) {
		this.step = step;
		this.stepTextBox.setText(this.step);
	}
	
	public void addConfigurationChanged(ConfigurationChanged ch) {
		this.listeners.add(ch);
	}
	
	public void removeConfigurationChanged(ConfigurationChanged ch) {
		this.listeners.remove(ch);
	}
	
	
	public void fireModuleChnaged() {
		for (ConfigurationChanged conf : this.listeners) {
			conf.onModuloStepChange(this.stepTextBox.getText());
		}
	}
}
