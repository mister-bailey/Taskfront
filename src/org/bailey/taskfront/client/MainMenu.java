package org.bailey.taskfront.client;

import java.util.Arrays;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;

public class MainMenu extends MenuBar {
	
	public MainMenu(){
		super(false);
		
		//"&#9816;"
		MenuBar top = new MenuBar(true);
		MenuItem horse = this.addItem("&#9816;",true,top);
		horse.addStyleName("rootMenuItem");
		
		// View options
		MenuBar views = new MenuBar(true);
		views.addItem("Timespan", new Command(){
			public void execute(){
				MainPanel.deferPanelLoad(new TimesPanel(Taskfront.mainPanel.itemID));
			}
		});
		views.addItem("Calendar", new Command(){
			public void execute(){
				MainPanel.deferPanelLoad(new CalendarPanel(Taskfront.mainPanel.itemID));
			}
		});
		views.addItem("Unfiltered", new Command(){
			public void execute(){
				MainPanel.deferPanelLoad(new MainPanel(Taskfront.mainPanel.itemID));
			}
		});
		views.addItem("Double column", new Command(){
			public void execute(){
				MainPanel.deferPanelLoad(new DoubleColumnPanel(Taskfront.mainPanel.itemID, null));
			}
		});
		views.addSeparator();
		views.addItem("Main list", new Command(){
			public void execute(){
				MainPanel.deferPanelLoad(new TimesPanel("mainList"));
			}
		});
		views.addItem("Completed", new Command(){
			public void execute(){
				MainPanel.deferPanelLoad(new MainPanel("completedList"));
			}
		});
		top.addItem("View",views);
		views.setStyleName("leftMenu");
		
		
		// Actions
		MenuBar actions = new MenuBar(true);
		actions.addItem("Backup to file",new Command(){
			public void execute(){Database.fileBackup();}
		});
		actions.addItem("Garbage collect",new Command(){
			public void execute() {Database.localGarbageCollect();}
		});
		actions.addItem("Disposal",new Command(){
			public void execute() {
				Taskfront.disposalPanel=new DisposalPanel(Taskfront.pastIncomplete.uid);
				Taskfront.disposalPanel.setPopupPosition(20,20);
				Taskfront.disposalPanel.show();
			}

		});
		actions.addItem("Clear all",new Command(){
			public void execute(){
				if(Window.confirm("Clear all?") && Window.confirm("Are you sure?")){
					 if(Database.storageMap != null) Database.storageMap.clear();
					 Database.remoteStoreService.clear(new AsyncCallback<Void>(){
							public void onSuccess(Void v) {
							}
							public void onFailure(Throwable caught) {
								Window.alert("Failed to reach remote database.");
							}
					 });
				}
			}
		});
		top.addItem("Actions",actions);
	}
	
}
