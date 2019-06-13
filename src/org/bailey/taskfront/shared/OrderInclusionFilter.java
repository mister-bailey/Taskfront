package org.bailey.taskfront.shared;

import org.bailey.taskfront.client.Database;

import com.google.gwt.user.client.Random;

public abstract class OrderInclusionFilter implements InclusionFilter {
	public abstract double getStartOrder();
	public abstract double getEndOrder();
	public abstract void removeOrder(FilteredListItem parent,Item item);
	public boolean include(FilteredListItem parent,Item item) {
		double order = parent.sorter.childOrder(parent,item);
		return (order > getStartOrder()) && (order < getEndOrder());
	}
	public void setInclusionProperty(FilteredListItem parent, Item item, int index) { 
		double t0,t1;
		Sorter sorter = parent.sorter;
		if(index<=0){
			t0=getStartOrder();
		} else {
			t0=sorter.childOrder(parent,Database.getItem(parent.children.get(index-1)));
		}
		if(index>=parent.children.size()){
			t1=getEndOrder();
		} else {
			t1=sorter.childOrder(parent,Database.getItem(parent.children.get(index)));
		}
		if(!include(parent,item)) sorter.setChildOrder(parent,item, .5 * (t0 + t1));
		else {
			double t = sorter.childOrder(parent,item);
			if(t<t0 || t>t1) sorter.setChildOrder(parent,item, .5 * (t0 + t1)); // updates observers
		}
	}
	// Should this check for prior inclusion/exclusion??? ***********************
	// When do I ever actually use this?????
	public void setInclude(FilteredListItem parent,Item child, boolean include) {
		double t0 = getStartOrder(), t1 = getEndOrder();
		if(include) parent.sorter.setChildOrder(parent,child, t0 + Random.nextDouble() * (t1 - t0));
		else removeOrder(parent,child);// sorter.setChildOrder(child, t1 + Random.nextDouble() * (t1 - t0));
	}
}
