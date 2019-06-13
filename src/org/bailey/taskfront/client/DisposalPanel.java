package org.bailey.taskfront.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/*
 *  Contains a BareItemWidget containing a DisposalListWidget
 */
public class DisposalPanel extends PopupPanel {
	BareItemWidget itemWidget;

	public DisposalPanel(String parentID){
		super(false);
		setGlassEnabled(true);
		VerticalPanel vpanel = new VerticalPanel();
		itemWidget = new BareItemWidget(parentID,true,ItemWidget.ListType.DISPOSAL_LIST);
		vpanel.add(itemWidget);
		Button closebutton = new Button("X",new ClickHandler(){
			public void onClick(ClickEvent event) {
				Taskfront.disposalPanel=null;
				DisposalPanel.this.hide();
			}			
		});
		closebutton.setStyleName("actionButton");
		closebutton.addStyleName("floatRight");
		vpanel.add(closebutton);
		this.setWidget(vpanel);
	}

}
