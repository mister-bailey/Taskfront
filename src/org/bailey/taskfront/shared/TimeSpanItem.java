package org.bailey.taskfront.shared;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.bailey.taskfront.client.Database;
import org.bailey.taskfront.client.JsArrays;
import org.bailey.taskfront.client.JsObject;
import org.bailey.taskfront.shared.RecursiveFilterItem.RecursiveInclusionFilter;

import com.google.gwt.i18n.client.DateTimeFormat;

public class TimeSpanItem extends FilteredListItem {
	/*
	 * This is kind of a proof of concept of the whole "sorted criterial view" idea.
	 * Later, I may refactor into this generalization.
	 */
	
	public TimeSpanItem(String text, String filterID, double startTime, double endTime) {
		super(filterID);
		setText(text);
		content.set("type","TimeSpanItem");
		content.set("startTime",startTime);
		content.set("endTime",endTime);
		populateUIDlist();
	}
	// Default constructor doesn't set type, since this should be set during deserialization??
	public TimeSpanItem(){super();}
    public static TimeSpanItem pastList(String filterID){
        return new TimeSpanItem("Past", filterID, DT.ancientPast, DT.todayStart);
    }
    public static TimeSpanItem todayList(String filterID){
    	return new TimeSpanItem("Today", filterID, DT.todayStart, DT.tomorrowStart);
    }
    public static TimeSpanItem thisWeekList(String filterID){
    	return new TimeSpanItem("This Week", filterID, DT.todayStart, DT.todayStart + DT.weekLength);
    }
    public static TimeSpanItem thisWeekMinusTodayList(String filterID){
        return new TimeSpanItem("This Week", filterID, DT.tomorrowStart, DT.todayStart + DT.weekLength);
    }
    public static TimeSpanItem thisMonthList(String filterID){
    	return new TimeSpanItem("30 Days", filterID, DT.todayStart, DT.todayStart + 30 * DT.dayLength);
    }
    public static TimeSpanItem thisMonthMinusThisWeekList(String filterID){
    	return new TimeSpanItem("30 days", filterID, DT.todayStart + DT.weekLength, DT.todayStart + 30 * DT.dayLength);        
    }
    public static TimeSpanItem sixMonthsList(String filterID){
    	return new TimeSpanItem("6 Months", filterID, DT.todayStart, DT.todayStart + DT.yearLength / 2);
    }
    public static TimeSpanItem sixMonthsMinusThisMonthList(String filterID){
    	return new TimeSpanItem("6 Months", filterID, DT.todayStart + 30 * DT.dayLength, DT.todayStart + DT.yearLength / 2);
    }
    // Watch out! This probably has bad insertion behaviour: TODO
    public static TimeSpanItem farFutureList(String filterID){
    	return new TimeSpanItem("Future", filterID, DT.todayStart + DT.yearLength / 2, DT.farFuture);
    }
    public static HasPropertyItem unscheduledList(String filterID){
    	return new HasPropertyItem("Unscheduled",filterID,"time",false);
    }
    public static Item timeSpanSequence(String filterID){
    	TimeSpanItem todayList = todayList(filterID);
    	TimeSpanItem thisWeekMinusTodayList = thisWeekMinusTodayList(filterID);
    	todayList.setProperty("demotionTarget",todayList.uid);
    	TimeSpanItem thisMonthMinusThisWeekList = thisMonthMinusThisWeekList(filterID);
    	thisWeekMinusTodayList.setProperty("demotionTarget",thisMonthMinusThisWeekList.uid);
    	TimeSpanItem sixMonthsMinusThisMonthList = sixMonthsMinusThisMonthList(filterID);
    	thisMonthMinusThisWeekList.setProperty("demotionTarget",sixMonthsMinusThisMonthList.uid);
    	TimeSpanItem farFutureList = farFutureList(filterID);
    	sixMonthsMinusThisMonthList.setProperty("demotionTarget",farFutureList.uid);
    	
    	Item sequence = new Item(Database.getItem(filterID).toString(),false);
    	sequence.addChild(todayList.uid);
    	sequence.addChild(thisWeekMinusTodayList.uid);
    	sequence.addChild(thisMonthMinusThisWeekList.uid);
    	sequence.addChild(sixMonthsMinusThisMonthList.uid);
    	sequence.addChild(farFutureList.uid);
    	return sequence;
    }
    public static ArrayList<Item> calendarBinaryTree(String filterID, double startTime, int numDays){
    	startTime = DT.getDayStart(startTime);
    	if(numDays<=1){
    		String text = DateTimeFormat.getFormat("d\t EEE").format(new Date((long) startTime));
    		//String text = DateTimeFormat.getFormat("M/d/H:mm").format(new Date((long) startTime)) + "--" +
    				DateTimeFormat.getFormat("M/d/H:mm").format(new Date((long) (startTime + numDays * DT.dayLength)));
    		ArrayList<Item> r = new ArrayList<Item>();
    		r.add(new TimeSpanItem(text,filterID,startTime,startTime + numDays * DT.dayLength));
    		return r;
    	} else {
    		TimeSpanItem interval = new TimeSpanItem("", filterID, startTime, startTime + numDays * DT.dayLength);
    		int p = greatestPowerOf2LessThan(numDays);
    		ArrayList<Item> leftBranch = calendarBinaryTree(interval.uid, startTime, p);
    		ArrayList<Item> rightBranch = calendarBinaryTree(interval.uid, startTime + p * DT.dayLength, numDays-p);
    		ArrayList<Item> r = new ArrayList<Item>();
    		r.addAll(leftBranch);
    		r.addAll(rightBranch);
    		return r;
    	}		
    }
    public static int greatestPowerOf2LessThan(int num){
    	int i=1;
    	while(2*i < num) i=2*i;
    	return i;
    }
    
    public class TimeSpanFilter extends PropertyRangeFilter{
		public TimeSpanFilter(double start, double end) {super(start, end);}
		public String getFilterProperty(){return "time";}
    }
    public class RecursiveTimeSpanFilter extends TimeSpanFilter implements RecursiveInclusionFilter {
		public RecursiveTimeSpanFilter(double start, double end) {super(start, end);}
		@Override
		public RecursiveTimeSpanFilter createRecursiveFilter(Item objectItem) {
			// TODO Auto-generated method stub
			return null;
		}
    	
    }
    public class TimeSorter extends PropertySorter {
		public String getSorterProperty() {return "time";}
    }
	public static final TimeSorter standardTimeSorter = new TimeSorter();

  

	//private static double nowUTC=DT.getTime();
	// These don't repopulate the list.  You must do that yourself (by calling populatelist etc.)
    // Nor do they save or update anything
	public void setStartTime(double t){content.set("startTime",t);} // TODO update observers?
	public double getStartTime(){return content.getDouble("startTime");}
	public void setEndTime(double t){content.set("endTime",t);}
	public double getEndTime(){return content.getDouble("endTime");}
	
	public double getStartOrder(){return getStartTime();}
	public double getEndOrder(){return getEndTime();}
	public void setChildOrder(Item item, double order){
		item.setTime(order);
	}
	public boolean include(Item item){
		if(!item.content.has("time")) return false;
		double time = item.getTime();
		return time >= getStartTime() && time < getEndTime();
	}

	public double childOrder(Item child) {
		return child.getTime();
	}	
/*	public static int compare(Item x, Item y){  // -1 if x<y, 0 if x==y and 1 if x>y
		// assuming for now that x and y have both been checked to see that have "time"
		double tx = x.getTime(), ty = y.getTime();
		if (tx<ty) return -1;
		if (tx>ty) return 1;
		return 0;  //  How likely is this???
	}
	
	@Override
	protected void filterMove(int indexTo, int indexFrom) {
		// Do nothing, unless I wanted to use the index information from the bin, but that's not directly
		// related to my UIDlist.
	}
	protected void filterRemove(String uid) {
		Item item = Database.getItem(uid);
		if(UIDlist.contains(item.uid)){
			for(Observer obs : observers)obs.listObserver().remove(uid);
			UIDlist.remove(uid);
			//item.parentObservers.remove(this);
		}
	}
	// item may or may not already be in UIDlist
	public void updateChild(Item item) {
		if(UIDlist.contains(item.uid)){
			if(!include(item)){
				filterRemove(item.uid);
			} else {
				int size = UIDlist.size()-1;
				if(size==0)return;
				int indexFrom = UIDlist.indexOf(item.uid);
				if(indexFrom >= size) return;
				int indexTo = indexFrom < size ? indexFrom : indexFrom - 1;
				UIDlist.remove(indexFrom);
				if(indexTo == 0 || compare(item, Database.getItem(UIDlist.get(indexTo))) > 0){
					while(indexTo<size && compare(item, Database.getItem(UIDlist.get(indexTo))) > 0) indexTo++;
				} else {
					while(indexTo>0 && compare(item, Database.getItem(UIDlist.get(indexTo-1))) < 0) indexTo--;
				}
				UIDlist.add(indexTo,item.uid);
				if(indexTo>indexFrom)indexTo++;
				for(Observer obs : observers){
                    obs.listObserver().move(indexTo,indexFrom);
                    if(obs instanceof ParentObserver) ((ParentObserver)obs).updateChild(item);
                }
			}
		} else {
			filterAdd(UIDlist.size(),item.uid);
		}
	}
	@Override
	public void add(int index, String itemID) {
		int indexFrom=UIDlist.indexOf(itemID);
		if(indexFrom >= 0){
			move(index,indexFrom);
			return;
		}
		interpolateTime(index, itemID);
		Item filterItem = Database.getItem(getFilter());
		if(filterItem.UIDlist==null)filterItem.UIDlist=new ArrayList<String>();
		if(!filterItem.UIDlist.contains(itemID)){ 
			int i = (index<=0 || index>=UIDlist.size()) ? 0 : filterItem.UIDlist.indexOf(UIDlist.get(index));
			filterItem.add(i,itemID);
		}
	}*/
	/*
	public void remove(int index) {
		Database.getItem(UIDlist.get(index)).deleteProperty("time");
	}
	@Override
	public String move(int indexTo, int indexFrom) {
		String uid = UIDlist.get(indexFrom);
		//for(Observer obs : observers)obs.listObserver().remove(indexFrom);
		//UIDlist.remove(indexFrom);
		interpolateTime(indexTo,uid);
		return uid;
	}*/
	
	//The following is all wrong:
	@Override
	public void setInclude(Item item, boolean include) {
		if(include) item.setTime(DT.todayStart + DT.dayLength / 2);
		else item.deleteProperty("time");
	}
	@Override
	protected void filterUpdate() {
		// Do nothing, unless I want this item's text to depend on filterItem, eg.
	}

}
