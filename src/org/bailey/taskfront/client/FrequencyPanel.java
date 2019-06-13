package org.bailey.taskfront.client;

import org.bailey.taskfront.client.ItemWidget.ListType;
import org.bailey.taskfront.shared.DT;
import org.bailey.taskfront.shared.HasPropertyItem;
import org.bailey.taskfront.shared.Item;
import org.bailey.taskfront.shared.NeededActionListItem;
import org.bailey.taskfront.shared.SubtractionItem;

public class FrequencyPanel extends DoubleColumnPanel {
	double [] dayFrequencies = {5,10,15,20,30,60,120};
	String verb;

	public FrequencyPanel(){
		super();
	}
	
	public FrequencyPanel(String itemID, String verb, double [] dayFrequencies){
		super();
		this.itemID=itemID;
		if(verb==null || verb.equals("")){
			Item item = Database.getItem(itemID);
			if(item.content.has("defaultChildAction")) verb = item.content.getString("defaultChildAction");
			else return; // do something better here
		}
		this.verb=verb;
		
		if(dayFrequencies==null || dayFrequencies.length<2){
			dayFrequencies = this.dayFrequencies;
		} else this.dayFrequencies = dayFrequencies;
		
		double f0 = dayFrequencies[0] * DT.dayLength;
		double [] f = new double[dayFrequencies.length-1];
		for(int i=0; i<f.length; i++) f[i] = dayFrequencies[i+1] * DT.dayLength;
		Item frequencySequence = NeededActionListItem.frequencySequence(itemID,verb,f0,f);
		SubtractionItem unscheduled = new SubtractionItem("Unscheduled", itemID, frequencySequence.children);
		super.initialize(new MainPanel(new ImmutableItemWidget(frequencySequence.uid,true,ListType.HEADER_LIST)), 
				new MainPanel(unscheduled.uid));
	}
	
	public String getToken(){
		String freq = "";
		for(double f : dayFrequencies) freq += "/" + f;
		return itemID + "/Frequencies/" + verb + freq;
	}
}
