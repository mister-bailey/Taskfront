package org.bailey.taskfront.client;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bailey.taskfront.shared.DatabaseEvent;
import org.bailey.taskfront.shared.DeleteEvent;
import org.bailey.taskfront.shared.FullSaveEvent;
import org.bailey.taskfront.shared.ItemsWithTime;
import org.bailey.taskfront.shared.LoginInfo;
import org.bailey.taskfront.shared.Item;
import org.bailey.taskfront.shared.Parent;
import org.bailey.taskfront.shared.SerializedComponents;
import org.bailey.taskfront.shared.UID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.storage.client.StorageMap;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

// ***ISSUE*** calling queueServerEvent from outside the class is bad---no local storage!
// Must also deal with the case where there's no local storage
// probably want connected field to have a setter method so we can change things
// I think I'll do change histories passed both ways, ultimately making things easier
public class Database {
	private static double lastServerUpdate=0;
	public static StorageMap storageMap;
	{
		Storage ls = Storage.getLocalStorageIfSupported();
		storageMap = ls==null ? null : new StorageMap(ls);
	}
	static RemoteStoreServiceAsync remoteStoreService  = GWT.create(RemoteStoreService.class);
	
	private static AsyncCallback<LoginInfo> callback;
	public static void tryConnect(AsyncCallback<LoginInfo> callback){
		Database.callback=callback;
		remoteStoreService.getLoginInfo(GWT.getHostPageBaseURL(),
				new AsyncCallback<LoginInfo>(){
			public void onSuccess(LoginInfo loginInfo){
				connected=true;
				AsyncCallback<LoginInfo> cb=Database.callback;
				Database.callback=null;
				applyServerEventQueue();
				executeUIDLoad();
				cb.onSuccess(loginInfo);
			}
			public void onFailure(Throwable caught){
				connected=false;
				AsyncCallback<LoginInfo> cb=Database.callback;
				Database.callback=null;
				cb.onFailure(caught);
			}
		});		
	}

	
	public static Item localLoadItem(String uid){
		//System.out.println("Trying to locally load " + uid);
		if(storageMap != null && storageMap.containsKey(uid)) {
			JsObject jso = JsObject.eval(storageMap.get(uid));
			ArrayList<String> list = jso.has("list") ? new ArrayList<String>(Arrays.asList(jso.getStringArray("list"))) : null;
			return Item.fromSerializedComponents(
					new SerializedComponents(uid,jso.getString("content"),list));
		} else return null;
	}
	public static void localSaveItem(Item x){
		// this method is ludicrous.
		if(storageMap == null) return;
		SerializedComponents c = x.serialize();
		JsObject jso = JsObject.newJsObject();
		jso.set("content",c.contentString);
		if(c.list != null) jso.set("list", JsArrays.fromList(c.list));
		storageMap.put(x.uid, jso.JSON());
	}
	public static boolean getBoolean(String key){ // for checking "noconnect" and so on.
		return (storageMap != null) && Boolean.parseBoolean(storageMap.get(key));
	}
	
	public static ArrayList<DatabaseEvent> pendingServerEvents;
	// something to think about: how do i avoid running this whole routine
	// (in particular, getSaveString) every time a field is updated, rather
	// than when a sequence of updates completes?  maybe getSaveString
	// should be processed when the remoteSave is occurring.
	public static void queueServerEvent(DatabaseEvent e){
		//System.out.println("Queueing server event; UID = " + e.uid);
		//if(Database.getItem(e.uid).shouldSave==false)return;
		if(pendingServerEvents==null) pendingServerEvents = new ArrayList<DatabaseEvent>();
		pendingServerEvents.add(e);
		// Incremental RPCs processing will send the remote saves to the server
		if(connected && remoteSaveTimer==null){
			remoteSaveTimer = new Timer () {
				public void run() {
					if(connected)applyServerEventQueue();
					remoteSaveTimer = null;
				}
			};
			remoteSaveTimer.schedule(remoteSaveDelay);
		}
	}

	public static void applyServerEventQueue() {
		if(pendingServerEvents==null || pendingServerEvents.isEmpty())return;
		//System.out.println("applyDatabaseEvents...");
		remoteStoreService.applyDatabaseEvents(pendingServerEvents, lastServerUpdate,
				new AsyncCallback<ItemsWithTime>() {
					public List<DatabaseEvent> eventBatch = new ArrayList<DatabaseEvent>(pendingServerEvents);
					public void onSuccess(ItemsWithTime it){
						pendingServerEvents.removeAll(eventBatch);
						processUpdatesFromServer(it);
					}
					public void onFailure(Throwable caught) {
						// Do something?
						connected=false;
					}
				});
	}
	public static void loadNewUpdates(){
		loadLaterThan(lastServerUpdate);
	}
	public static void loadLaterThan(double time){
		remoteStoreService.loadLaterThan(time,
				new AsyncCallback<ItemsWithTime>(){
					public void onSuccess(ItemsWithTime it){
						processUpdatesFromServer(it);				
					}
					public void onFailure(Throwable caught){
						// Do something?
						connected=false;
					}
				});
	}
	private static void processUpdatesFromServer(ItemsWithTime it){
		//System.out.println("processUpdatesFromServer...");
		if(lastServerUpdate <= it.time) {
			lastServerUpdate = it.time;
			//System.out.println("updating lastServerUpdate = " + lastServerUpdate);
		}
		for(SerializedComponents c : it.list){
			uidLoadQueue.remove(c.uid);
			if(c.contentString==null){
				// Couldn't find c.uid on server, so sending back local version.
				Database.getItem(c.uid).save(); // redundant???
				continue;
			}
			
			//System.out.println("processing " + x.toString());
			JsObject content = JsObject.eval(c.contentString);
			Item i;
			if(UID.hasUID(c.uid)){
				i = UID.getItem(c.uid);
					// for special object types, just replace in database
					Item x = fromSerializedComponents(c);
					x.representatives = i.representatives;
					x.parents = i.parents;
					x.selfSave=true;
					i.representatives=null;
					i.parents=null;
					i.deleteFromMemory();
					UID.putItem(x.uid,x);
					x.updateObservers();
					x.updateListObservers();
					i=x;
			} else {
				i = fromSerializedComponents(c);
				UID.putItem(i.uid, i);
			}
			localSaveItem(i);
		}
	}
	
	public static boolean connected = false;
	public static boolean remotePolling = false;
	public static boolean activePolling = false;
	public static int remoteSaveDelay = 1000;
	public static int remotePollDelay = 10000;
	private static Timer remoteSaveTimer = null;
	
	// some process of getting from the server:
	// this should probably happen automatically if the
	// user checks in again after a while away.  and/or
	// the server could send updates to things 
	static List<String> uidLoadQueue
		= new ArrayList<String>();
	static boolean pendingUIDLoad=false;
	static Scheduler loadScheduler = Scheduler.get();
	public static void queueUIDLoad(String uid){
		deferUIDLoad();
		if(uidLoadQueue==null)uidLoadQueue = new ArrayList<String>();
		uidLoadQueue.add(uid);
	}
	private static void deferUIDLoad(){
		//System.out.println("deferUIDLoad; connected = " + connected + ",  pendingUIDLoad = " + pendingUIDLoad);
		if(connected && !pendingUIDLoad){
			//System.out.println("I'm here, so I should be scheduling a UID load!");
			loadScheduler.scheduleDeferred(new ScheduledCommand(){
				public void execute(){
					//System.out.println("BEEEEEEEEEEP");
					executeUIDLoad();					
				}
			});
			pendingUIDLoad=true;
		}/* else {
			System.out.println("Tried a deferred UID load, but not connected!");
		}*/
	}
	private static void executeUIDLoad(){
		pendingUIDLoad=false;
		if(uidLoadQueue.isEmpty())return;
		//System.out.println("Executing UID load...");
		remoteStoreService.load(uidLoadQueue,
				new AsyncCallback<ItemsWithTime>(){
					public void onSuccess(ItemsWithTime it) {
						//System.out.println("Returning from UID load!");
						processUpdatesFromServer(it);
					}
					public void onFailure(Throwable caught) {
						connected=false;
						caught.printStackTrace();
					}
		});
	}
	
	// Should I have incremental display loading?
	// This can probably be accomplished by creating a template Item,
	// displaying it, then merging in the loaded item.   --what???
	public static Item getItem(String uid){
		if(UID.hasUID(uid))	return UID.getItem(uid);
		Item result = localLoadItem(uid);
		if(result == null){
			result = new Item();
			result.uid = uid;
		} else {
			//System.out.println("successfully locally loaded "+ uid+", value: " + result.toString());
		}
		UID.putItem(uid, result);
		queueUIDLoad(uid);
		return result;
	}
	
	public static void delete(String itemID){
		getItem(itemID).delete();
	}
	
	public static void fileBackup(){
		// Generate file as string (probably from local database?)
		String data = "[\r\n";
		if(storageMap != null){
			Set<Map.Entry<String,String>> entries = storageMap.entrySet();
			for(Map.Entry<String,String> entry : entries){
				data = data + "{\"" + entry.getKey() + "\" : " + entry.getValue() + "},\r\n";
			}
		}
		data = data.substring(0,data.lastIndexOf(",")) + "\r\n]";
		FileSystem.save(data,"taskfront-backup-" + DateTimeFormat.getFormat("dd-MM-yy").format(new Date()));
	}
	
	public static void loadBackup(){
		
	}
	
	// This does a mark-and-sweep garbage collection for all the objects stored in local storage.
	// Delete events are triggered for garbage objects.
	// If local storage doesn't exist, or the garbage collection otherwise fails, returns false.
	// If the local storage is not up-to-date, this might delete the wrong things.  (Don't run
	// immediately after loading, for example, since server requests haven't returned yet!)
	public static boolean localGarbageCollect(List<String> roots){
		if(storageMap==null) return false;
		//System.out.println("Starting mark/sweep...");
		for(String rootID : roots)garbageMark(rootID);
		int size = storageMap.size();
		// Sweep:
		// copies storageMap.keySet() to make it safe for changing during iteration
		for(String uid : new ArrayList<String>(storageMap.keySet())){
			if(nonItemKeys.contains(uid)){
				//System.out.println("Skipping " + uid);
				continue;
			}
			Item item = getItem(uid);
			if(item.garbageCollect) {
				//System.out.println("Deleting " + item.toString() + " : " + uid);
				item.delete();
			} else {
				//System.out.println("Keeping  " + item.toString() + " : " + uid);
			}
			item.garbageCollect=true;
		}
		return true;
	}
	
	public static final List<String> nonItemKeys = Arrays.asList("userID","noConnect");
	public static List<String> collectionRoots = Arrays.asList("mainList","completedList");
	public static boolean localGarbageCollect(){
		return localGarbageCollect(collectionRoots);
	}
	
	private static void garbageMark(String rootID){
		Item root=getItem(rootID);
		if(root.garbageCollect==false)return;
		root.garbageCollect=false;
		for(String uid : root.children){
			garbageMark(uid);
		}
	}
	
	
	public interface DataItemBuilder {
		public Item buildItem(SerializedComponents components);
	}
	public static HashMap<String,DataItemBuilder> builders = new HashMap<String,DataItemBuilder>();
	public static Item fromSerializedComponents(SerializedComponents components){
		Item item;
		if(builders.containsKey(components.type)) item = (builders.get(components.type).buildItem(components));
		else item = builders.get(components.type).buildItem(components); // Raise error instead????
		item.selfSave = true;
		return item;
	}
	

}
