package org.bailey.taskfront.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

// this class can't record individual transactions, which aren't tracked in the main database anyway.
// I could do this tracking, but it's a pain, so I'll leave it till later.
// currently, I don't really have ways of triggering deletions.
public class ItemsWithTime implements Serializable {
	public ArrayList<SerializedComponents> list;
	public double time;
	public ItemsWithTime(ArrayList<SerializedComponents> l, double t){
		list = l;
		time = t;
	}
	public ItemsWithTime(Collection<SerializedComponents> l, double t){
		list=new ArrayList<SerializedComponents>(l);
		time=t;
	}
	public ItemsWithTime(){}
}
