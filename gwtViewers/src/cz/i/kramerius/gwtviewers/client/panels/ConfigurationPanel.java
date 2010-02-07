package cz.i.kramerius.gwtviewers.client.panels;

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
	
	private TextBox jumpTextBox;
	private TextBox stepTextBox;
	
	private String jump;
	private String step;
	
	private ArrayList<ConfigurationChanged> listeners = new ArrayList<ConfigurationChanged>();
	
	
	public ConfigurationPanel() {
		super();
		Widget jumpPanel = createJumbPanel();

		this.horizontalPanel = new HorizontalPanel();
		this.horizontalPanel.add(jumpPanel);
		this.horizontalPanel.setCellHorizontalAlignment(jumpPanel, HorizontalPanel.ALIGN_LEFT);
		this.horizontalPanel.setCellWidth(jumpPanel, "200px");
		
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

	private Widget createJumbPanel() {
		
		VerticalPanel vertPan = new VerticalPanel();
		vertPan.add(new Label("Skok na"));
		jumpTextBox = new TextBox();
		jumpTextBox.getElement().setId("jumbText");
		vertPan.add(jumpTextBox);
		
		Button but = new Button("Skok");
		but.addClickHandler(this);
		but.getElement().setId("jumpButton");
		vertPan.add(but);
		vertPan.setCellHorizontalAlignment(but, VerticalPanel.ALIGN_RIGHT);
		return vertPan;
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
		Widget wd = (Widget) event.getSource();
		String id = wd.getElement().getId();
		if (id.equals("jumpButton")) {
			this.jump = jumpTextBox.getText();
			this.fireJumpChanged();
		} else {
			this.step = stepTextBox.getText();
			this.fireModuleChnaged();
		}
	}

	public void initConfiguration(String currentPos, String step) {
		this.step = step;
		this.stepTextBox.setText(this.step);
		this.jump = currentPos;
		this.jumpTextBox.setText(this.jump);
	}
	
	public void addConfigurationChanged(ConfigurationChanged ch) {
		this.listeners.add(ch);
	}
	
	public void removeConfigurationChanged(ConfigurationChanged ch) {
		this.listeners.remove(ch);
	}
	
	public void fireJumpChanged() {
		for (ConfigurationChanged conf : this.listeners) {
			conf.onJumpChange(this.jumpTextBox.getText());
		}
	}
	
	public void fireModuleChnaged() {
		for (ConfigurationChanged conf : this.listeners) {
			conf.onModuleStepChange(this.stepTextBox.getText());
		}
	}
}
