package org.bailey.taskfront.client;

import org.bailey.taskfront.client.ItemWidget.ListType;
import org.bailey.taskfront.shared.Item;
import org.bailey.taskfront.shared.TimeSpanItem;

import com.google.gwt.user.client.ui.HorizontalPanel;

// creates a new time sequence deriving from the given item
public class TimesPanel extends MainPanel {

	public TimesPanel(){
		super();
	}
	
	public TimesPanel(String itemID){
		this();
		this.itemID = itemID;
		Item sequence = TimeSpanItem.timeSpanSequence(itemID);
		boolean showTitle = true;
		if(itemID.equals("mainList")) showTitle=false;
		ItemWidget timesWidget = new ImmutableItemWidget(TimeSpanItem.timeSpanSequence(itemID).uid,showTitle,ListType.HEADER_LIST);
		HorizontalPanel hpanel = new HorizontalPanel();
		hpanel.add(timesWidget);
		ItemWidget unscheduled = new HeaderItemWidget(TimeSpanItem.unscheduledList(itemID).uid);
		hpanel.add(unscheduled);
		hpanel.setWidth("100%");
		hpanel.setCellWidth(timesWidget,"50%");
		//timesWidget.setWidth("50%");
		this.add(hpanel);
		setMainItemObserver();
		setTitle(Database.getItem(itemID).toString());
	}
	
	public String getToken(){
		return itemID + "/Times";
	}
}
