package org.bailey.taskfront.client;

import org.bailey.taskfront.client.ItemWidget.ListType;
import org.bailey.taskfront.client.ListenerItem.ItemEventHandler;
import org.bailey.taskfront.shared.*;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Taskfront implements EntryPoint {
	
	protected Item mainList;
	protected Item completedList;
	private ItemWidget mainItemWidget;
	private LoginInfo loginInfo;
	private String userId;
	
	/**
	 * This is the entry point method.
	 */
	// Really, I should have asynchronous setup, so that we can setup the interface and
	// load from local storage, and only after connectivity is confirmed do we execute remoteloads etc.
	public void onModuleLoad() {
		if(Database.storageMap != null){
			//if(Window.confirm("Clear local storage?")) Database.storageMap.clear();
			userId=Database.storageMap.get("userId");
//			Window.alert("userId = " + userId);
		}
		if(!Database.getBoolean("noConnect")) {
//			Window.alert("Trying to connect.");
			Database.tryConnect(new AsyncCallback<LoginInfo>(){
			public void onSuccess(LoginInfo loginInfo){
//				Window.alert("Connected to remote database.");
				loadUser(loginInfo);
				/*if(Window.confirm("Clear server database?")) Database.remoteStoreService.clear(new 
						AsyncCallback<Void>(){
					public void onSuccess(Void v) {
						setup();
					}
					public void onFailure(Throwable caught) {
						System.out.println("Failed to connect!");
						caught.printStackTrace();
						// can't connect to the server, probably
						setup();
					}
				});
				else*/ setup();
			}
			public void onFailure(Throwable caught){
				Window.alert("Failed to connect to remote database.");
				setup();
			}
		});}
		else setup();
		
	}
	
	// in many cases, I will want to display lists other than mainList
	// as the primary list.  In those cases, I will probably want to display
	// the containing Item in a "maximal" form.
	private void setup() {
		// Set up an event handler on the root panel to catch
		// events (esp. keypresses) which would normally do something
		// in the browser, but have a different meaning for us.
		// TODO
		// RootPanel.get().addDomHandler(...);
		
		mainList = Database.getItem("mainList");
		mainList.setText("Main list");

		// TODO: create an item type that doesn't automatically load its children.
		completedList = Database.getItem("completedList");
		completedList.setText("Completed");

		History.addValueChangeHandler(new ValueChangeHandler<String>(){
			public void onValueChange(ValueChangeEvent<String> event) {
				MainPanel m = MainPanel.createFromToken(event.getValue());
				if(m!=null) setMainPanel(m);
			}
		});
		
		//RootPanel superPanel = RootPanel.get("mainList");
		//superPanel.add(new CalendarPanel("mainList"));
		//superPanel.add(new TimesPanel("mainList"));
		
		MainMenu menu = new MainMenu();
		RootPanel.get("mainList").add(menu);
		menu.setStyleName("mainMenu");
		
		createPastDisposal("mainList","completedList");
		
		String token = History.getToken();
		if(token.equals("")){
			mainPanel = new TimesPanel("mainList");
			RootPanel.get("mainList").add(mainPanel);
		} else {
			setMainPanel(MainPanel.createFromToken(token));
		}
	}
	
	public static MainPanel mainPanel=null;
	public static void setMainPanel(MainPanel m){
		if(mainPanel != null){
			mainPanel.removeFromParent();
			mainPanel.cleanUp();
		}
		// TODO: keep some recent panels in a buffer
		
		mainPanel = m;
		RootPanel.get("mainList").add(m);
		
		// History modification:
		History.newItem(m.getToken(),false);
	}
	
	// method for making "disposal lists"
	public HasPropertyItem pastCompleted;
	public static HasPropertyItem pastIncomplete;
	public String completeTargetID;
	public static DisposalPanel disposalPanel=null; // TODO static??? should they all be?
	public void createPastDisposal(String parentID, String completeTargetID){
		// TODO destroy any previous listeners!
		this.completeTargetID = completeTargetID;
		TimeSpanItem pastList = TimeSpanItem.pastList(parentID);
		pastIncomplete = new IsPropertyItem("Past incomplete",pastList.uid,"completed",false);
		pastIncomplete.selfSave=false;
		Taskfront.disposalPanel=new DisposalPanel(Taskfront.pastIncomplete.uid);
		disposalPanel.hide();
		pastCompleted = new IsPropertyItem("Past complete",pastList.uid,"completed",true);
		pastCompleted.selfSave=false;
		// Temporarily watching these lists to see if they're working???
		//RootPanel.get("mainList").add(new BareItemWidget(pastIncomplete.uid,true));
		//RootPanel.get("mainList").add(new BareItemWidget(pastCompleted.uid,true));
		
		if(completeTargetID != null){
			new ListenerItem(pastCompleted.uid, new ItemEventHandler(){
				public void onUpdate() {}
				public void onAdd(String itemID) {
					pastCompleted.hardRemove(itemID);
					Database.getItem(Taskfront.this.completeTargetID).add(itemID);
				}
				public void onMove(int indexTo, int indexFrom) {}
				public void onRemove(String itemID) {}
			});
		}
		
		new ListenerItem(pastIncomplete.uid, new ItemEventHandler(){
			public void onUpdate() {}
			public void onAdd(String itemID) {
				if(Taskfront.disposalPanel==null) Taskfront.disposalPanel=new DisposalPanel(Taskfront.pastIncomplete.uid);
				if(!Taskfront.disposalPanel.isShowing()){
					Taskfront.disposalPanel.setPopupPosition(20,20);
					Taskfront.disposalPanel.show();
				}
			}
			public void onMove(int indexTo, int indexFrom) {}
			public void onRemove(String itemID) {}			
		});	
	}

	
	private void loadUser(LoginInfo loginInfo){
		if(loginInfo.userId==null) {
			redirect(loginInfo.loginUrl);
		} else {
			this.loginInfo=loginInfo;
			if(userId!=null && userId!=loginInfo.userId){
				// do a complete reload of the data
				// !!!!!!!!!!!!!!!!!!!!!!!!!!!!
				// ask about saving old data! (or associating with new user)
				// !!!!!!!!!!!!!!!!!!!!!!!!!!!!
				if(Database.storageMap != null){
					Database.storageMap.clear();  // This is prob. a bad idea!
					Database.storageMap.put("userId", loginInfo.userId);
					Database.storageMap.put("noConnect", Boolean.toString(false));
				}
				redirect(GWT.getHostPageBaseURL());
			} else {
				userId=loginInfo.userId;
				if(Database.storageMap != null) Database.storageMap.put("userID", loginInfo.userId);
			}
		}
	}
	//redirect the browser to the given url
	public static native void redirect(String url)/*-{
		$wnd.location.replace(url);
	}-*/;
}



