package org.bailey.taskfront.client;

import com.google.gwt.user.client.ui.HorizontalPanel;

public class CompoundEditPanel extends EditPanel {
	EditPanel[] subpanels;

	public CompoundEditPanel(EditPanel... panels) {
		super(panels[0].itemID);
		this.subpanels=panels;
		HorizontalPanel hpanel = new HorizontalPanel();
		this.add(hpanel);
		for(EditPanel p : panels)hpanel.add(p);
	}

	public void update() {
		for(EditPanel p : subpanels) p.update();
	}

	public void save() {
		for(EditPanel p : subpanels) p.save();
	}

	public void setFocus() {
		subpanels[0].setFocus();
	}

}
