package org.bailey.taskfront.client;

import java.util.ArrayList;
import java.util.Date;

import org.bailey.taskfront.shared.DT;
import org.bailey.taskfront.shared.Item;
import org.bailey.taskfront.shared.TimeSpanItem;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.datepicker.client.CalendarUtil;

public class CalendarPanel extends MainPanel {
	double start;
	int numDays=0;

	public CalendarPanel(){
		super();
	}
	
	public CalendarPanel(String itemID){
		this();
		initialize(itemID, DT.getNow() - 2 * DT.dayLength, 32);
	}
	
	public CalendarPanel(String itemID, double start, int numDays){
		this();
		this.start=DT.getDayStart(start);
		this.numDays=numDays;
		initialize(itemID, start, numDays);
	}
	
	public void initialize(String itemID, double start, int numDays){
		this.itemID=itemID;
		HorizontalPanel hpanel = new HorizontalPanel();
		this.add(hpanel);
		
		// calculate calendar dimensions
		start = DT.getDayStart(start);
		int dayOfWeekStart = DT.dayOfWeek(start);
		int dayOfWeekEnd = (dayOfWeekStart + numDays) % 7; // not inclusive!
		int cellsNeeded = numDays + dayOfWeekStart;
		int rowsNeeded = (int) Math.ceil(((double)cellsNeeded) / 7);
		
		Grid grid = new Grid(rowsNeeded,7);
		grid.setStyleName("calendar");
		CellFormatter cf = grid.getCellFormatter();
		
		// get list of day items
		ArrayList<Item> days = TimeSpanItem.calendarBinaryTree(itemID,start,numDays);
		
		// fill cells
		int currentRow=0;
		int currentColumn=dayOfWeekStart;
		for(Item dayItem : days){
			ItemWidget itemWidget = new HeaderItemWidget(dayItem.uid); // TODO: create CalendarDayWidget?
			SimplePanel spanel = new SimplePanel(itemWidget);
			spanel.setStyleName("dayContainer");
			itemWidget.setHeight("94%");
			itemWidget.setWidth("100%");
			grid.setWidget(currentRow,currentColumn,spanel);
			
			cf.setStyleName(currentRow,currentColumn,"calendarCell");
			double time = ((TimeSpanItem)dayItem).getStartTime();
			if(currentColumn==0 || isFirstDayOfMonth(time)) cf.addStyleName(currentRow,currentColumn,"calendarCellLeft");
			if(currentColumn==6 || isLastDayOfMonth(time)) cf.addStyleName(currentRow,currentColumn,"calendarCellRight");
			if(isFirstWeekOfMonth(time)) cf.addStyleName(currentRow,currentColumn,"calendarCellTop");
			if(isLastWeekOfMonth(time)) cf.addStyleName(currentRow,currentColumn,"calendarCellBottom");	
			if(isToday(time)) cf.addStyleName(currentRow,currentColumn,"calendarCellToday");
			
			currentColumn++;
			if(currentColumn>=7){
				currentRow++;
				currentColumn -= 7;
			}
		}
		
		hpanel.add(grid);
		hpanel.add(new HeaderItemWidget(TimeSpanItem.unscheduledList(itemID).uid));
	
		setMainItemObserver();
		setTitle(Database.getItem(itemID).toString());

		// TODO
		// format cells
		// --today special
		// month labels
		// weekend format
	}
	
	@SuppressWarnings("deprecation")
	public static boolean isFirstDayOfMonth(double t){
		return (new Date((long)t)).getDate()==1;
	}
	public static boolean isLastDayOfMonth(double t){ 
		return isFirstDayOfMonth(t + DT.dayLength);
	}
	@SuppressWarnings("deprecation")
	public static boolean isFirstWeekOfMonth(double t){
		return (new Date((long)t)).getDate() < (new Date((long)(t - 7*DT.dayLength))).getDate();
	}
	@SuppressWarnings("deprecation")
	public static boolean isLastWeekOfMonth(double t){
		return (new Date((long)t)).getDate() > (new Date((long)(t + 7*DT.dayLength))).getDate();
	}
	public static boolean isToday(double t){
		return CalendarUtil.isSameDate(new Date((long)t), new Date((long)DT.todayStart));
	}
	
	public String getToken(){
		String token = itemID + "/Calendar";
		if(numDays != 0) { // dates have been specified, rather than default
			token += "/" + start + "+" + numDays;
		}
		return token;
	}
}
