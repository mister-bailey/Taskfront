package org.bailey.taskfront.client;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class DoubleColumnPanel extends MainPanel {
	MainPanel p1, p2;
	
	public DoubleColumnPanel(){
		super();
	}
	
	public DoubleColumnPanel(String item1, String item2){
		super();
		initialize(new SwappableItemPanel(item1), new SwappableItemPanel(item2));
	}
	
	public DoubleColumnPanel(MainPanel p1, MainPanel p2){
		super();
		initialize(p1,p2);
	}
	
	public void initialize(MainPanel p1, MainPanel p2){
		p1.parent=p2.parent=this;
		HorizontalPanel hpanel = new HorizontalPanel();
		ScrollPanel spanel1 = new ScrollPanel(this.p1 = p1);
		ScrollPanel spanel2 = new ScrollPanel(this.p2 = p2);
		hpanel.add(spanel1);
		hpanel.add(spanel2);
		hpanel.setWidth("100%");
		hpanel.setCellWidth(p1,"50%");
		this.add(hpanel);
		
		setMainItemObserver(p1.itemID);
		setTitle(Database.getItem(p1.itemID).toString());
	}
		
	public String getToken(){
		return p1.getToken() + "/Column/" + p2.getToken();
	}
	
	public void cleanUp(){
		p1.cleanUp();
		p2.cleanUp();
	}
	

}
