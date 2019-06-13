package org.bailey.taskfront.client;

import org.bailey.taskfront.shared.DT;

public class ActionPostpone extends ActionSetTime {
	public int magnitude;
	
	public ActionPostpone(String itemID, int magnitude){
		super(itemID, getPostponedTime(Database.getItem(itemID).getTime(),magnitude));
		this.magnitude=magnitude;
		if(magnitude<=1)setProgressColor("#ceebfd");
		else if(magnitude<=2)setProgressColor("#6cc3f9");
		else if(magnitude<=3)setProgressColor("#0a9bf5");
		else setProgressColor("#065d93");
	}

	public static double getPostponedTime(double oldtime, int magnitude){
		if(magnitude<=0)return oldtime;
		double time;
		if(oldtime >= DT.todayStart){
			if(DT.isToday(oldtime)){
				time = oldtime + DT.dayLength;
				//label.setText("Tomorrow");
			} else if (DT.isTomorrow(oldtime)){
				time = oldtime + 5 * DT.dayLength;
				//label.setText("This week");
			} else if (DT.isThisWeek(oldtime)){
				time = oldtime + 23 * DT.dayLength;
				//label.setText("This month");
			} else if (DT.isThisMonth(oldtime)){
				time = oldtime + DT.yearLength / 2 - 30 * DT.dayLength;
				//label.setText("Six months");
			} else if (DT.isSixMonths(oldtime)){
				time = oldtime + DT.yearLength;
				//label.setText("Far future");
			} else {
				return oldtime; // No lazy postponement beyond 6 month window
			}
		} else {
			if(DT.isYesterday(oldtime)){
				time = oldtime + 2 * DT.dayLength;
			} else {
				time = oldtime + 2 * (DT.todayStart - DT.getDayStart(oldtime));
			}
		}
		return getPostponedTime(time, magnitude-1);
	}


}
