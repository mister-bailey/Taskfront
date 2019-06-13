package org.bailey.taskfront.client;

//import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Map.Entry;

import org.bailey.taskfront.shared.CandidateObserver;
import org.bailey.taskfront.shared.Item;
import org.bailey.taskfront.shared.Representative;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.FlowPanel;

public class MainPanel extends FlowPanel {
	static ArrayList<Entry<String,MainPanel>> recentPanels = new ArrayList<Entry<String,MainPanel>>();
	static int numStoredPanels=4;
	String itemID;
	public MainPanel parent;
	
	public MainPanel(){
		super();
	}
	
	public MainPanel(ItemWidget itemWidget){
		this();
		initialize(itemWidget);
	}
	
	public MainPanel(String itemID){
		this();
		ItemWidget itemWidget;
		if(itemID.equals("mainList")) itemWidget = new ImmutableItemWidget("mainList",false);
		else itemWidget = new HeaderItemWidget(itemID);
		initialize(itemWidget);
	}
	
	protected void initialize(ItemWidget itemWidget){
		//recentPanels.add(new SimpleEntry<String,MainPanel>(getToken(),this));
		this.add(itemWidget);
		this.itemID = itemWidget.itemID;
		itemWidget.setWidth("100%");
		setMainItemObserver();
		setTitle(Database.getItem(itemID).toString());
	}
	
	public static MainPanel createFromToken(String compoundToken){
		if(compoundToken.equals("")) compoundToken = "mainList/Times";
		String [] tokenList = compoundToken.split("/Column/",-1);
		if(tokenList.length>1) return new DoubleColumnPanel(createFromToken(tokenList[0]), 
				createFromToken(compoundToken.substring(tokenList[0].length() + "/Column/".length())));
		// Otherwise...
		String token = tokenList[0];
		int uidEnd = token.indexOf('/');
		String itemID;
		String panelType;
		if(uidEnd<0){
			itemID = token;
			panelType = "";
		}
		else{
			itemID = token.substring(0,uidEnd);
			panelType = token.substring(uidEnd+1);
		}
		
		if(panelType.equals("")){
			return new MainPanel(itemID);
		} else if(panelType.equals("Times")){
			return new TimesPanel(itemID);
		} else if(panelType.startsWith("Calendar")){
			if(panelType.length()>8){
				String [] dateInfo = panelType.substring(9).split("\\+");
				double start = Integer.parseInt(dateInfo[0]);
				int numDays;
				if(dateInfo.length > 1) numDays = Integer.parseInt(dateInfo[1]);
				else numDays = 32;
				return new CalendarPanel(itemID,start,numDays);
			} else return new CalendarPanel(itemID);
		} else if(panelType.equals("Swappable")){
			return new SwappableItemPanel(itemID);
		} else if(panelType.startsWith("Frequencies")){
			String suffix = panelType.substring("Frequencies".length());
			while(suffix.startsWith("/")) suffix = suffix.substring(1);
			String []  t = suffix.split("/");
			if(t.length < 1) return null;
			double [] f = new double[t.length-1];
			for(int i=0; i<f.length; i++) f[i] = Double.parseDouble(t[i+1]);
			return new FrequencyPanel(itemID,t[0],f);
		} // ....
		return null;
	}
	
	public String getToken(){
		return itemID;
	}
	
	public String getGlobalToken(){ 
		if(parent==null) return getToken();
		else return parent.getGlobalToken();
	}
	
	public void cleanUp(){
		// TODO delete some widgets and temporarily existing items?
		Item item = Database.getItem(itemID);
		if(!item.selfSave) item.delete();
	}

	public static void deferPanelLoad(MainPanel m){
		Scheduler.get().scheduleDeferred(new DeferredPanelLoad(m));
	}
	
	public static class DeferredPanelLoad implements ScheduledCommand {
		MainPanel m;
		public DeferredPanelLoad(MainPanel m){
			this.m = m;
		}
		public void execute(){
			Taskfront.setMainPanel(m);
		}
	}
	
	// this title will usually be displayed on the browser title bar
	public String getTitle(){
		return Database.getItem(itemID).toString();
	}
	public void setTitle(String title){
		this.title=title;
		Document.get().setTitle(title);
	}
	
	private String title="Taskfront";
	public void setMainItemObserver(){setMainItemObserver(itemID);}
	public void setMainItemObserver(String itemID){
		this.itemID = itemID;
		new Representative(){
			public CandidateObserver candidateObserver() {return Item.dummyCandidateObserver();}
			public void update() {
				String newTitle = Database.getItem(MainPanel.this.itemID).toString();
				if(!title.equals(newTitle)) setTitle(newTitle);
			}
			public void deleteRepresentative() {
				// TODO Auto-generated method stub
				
			}
			public void removeList() {}			
		};
	}

}
