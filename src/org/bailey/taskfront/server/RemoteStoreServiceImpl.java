package org.bailey.taskfront.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bailey.taskfront.client.RemoteStoreService;
import org.bailey.taskfront.shared.DatabaseEvent;
import org.bailey.taskfront.shared.DeleteEvent;
import org.bailey.taskfront.shared.FullSaveEvent;
import org.bailey.taskfront.shared.ItemsWithTime;
import org.bailey.taskfront.shared.LoginInfo;
import org.bailey.taskfront.shared.ModifyEvent;
import org.bailey.taskfront.shared.MoveChildEvent;
import org.bailey.taskfront.shared.NotLoggedIn;
import org.bailey.taskfront.shared.RemoveChildEvent;
import org.bailey.taskfront.shared.SerializedComponents;
import org.bailey.taskfront.shared.SetContentEvent;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class RemoteStoreServiceImpl extends RemoteServiceServlet implements
		RemoteStoreService {
	
	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	UserService userService = UserServiceFactory.getUserService();
	
	public LoginInfo getLoginInfo(String destination) {
		LoginInfo loginInfo = new LoginInfo(userService.createLoginURL(destination),userService.createLogoutURL(destination)); 
		User user = userService.getCurrentUser();
		if(user!=null) loginInfo.userId=user.getUserId();
		return loginInfo;
	}

	// deal with failures and exceptions
	// Deprecated:  remove?
	public double save(Map<String,String> saveStrings, double callTime) throws NotLoggedIn {
		//System.out.println("save (server)");
		Key userKey = getUserKey();
		for(String uid : saveStrings.keySet()){
			Entity e = new Entity("Item",uid,userKey);
			e.setProperty("saveString", saveStrings.get(uid));
			e.setProperty("lastUpdate",callTime);
			//System.out.println("  saving " + e.toString());
			datastore.put(e);
		}
		return callTime;
	}
	
	Key getUserKey() throws NotLoggedIn {
		User user = userService.getCurrentUser();
		if(user==null) throw new NotLoggedIn(); // really, throw a user exception!
		Key userKey = KeyFactory.createKey("User",user.getUserId());
		try{
			datastore.get(userKey);
		} catch (EntityNotFoundException e) {
			return datastore.put(new Entity(userKey));
		}
		return userKey;
	}	
	
	public ItemsWithTime load(Collection<String> uids) throws NotLoggedIn {
		//System.out.println("load (server)");
		Key userKey = getUserKey();
		Collection<SerializedComponents> result = new ArrayList<SerializedComponents>();
		double loadTime = (new Date()).getTime();
		for(String uid : uids)
			try {
				//System.out.println("Server looking for " + uid);
				Entity e = datastore.get(KeyFactory.createKey(userKey,"Item",uid));
				// what happens if the content and list are not present, or empty?
				// check: if they return null, we're fine
				result.add(new SerializedComponents(e));
				//System.out.println("  loading " + uid + " with content " + (String)e.getProperty("content"));
			} catch (EntityNotFoundException ex) {
				// Automatically add this to the database if it's not there already
				// ***It doesn't really make sense to add a dummy to the database and then return it
				/*Entity e = new Entity("Item",uid,userKey);
				e.setProperty("content", null);
				e.setProperty("lastUpdate", loadTime);
				datastore.put(e);
				result.add(new SerializedComponents(uid,null,null));*/
			}
		return new ItemsWithTime(result,loadTime);
	}
	
	// this should somehow return the time of the load???
	// or the client can just read the incoming objects and see which one was modified most recently???
	public ItemsWithTime loadLaterThan(double time) throws NotLoggedIn {
		Key userKey = getUserKey();
		double loadTime = (new Date()).getTime();
		//System.out.println("loadLaterThan(" + time + ") with current loadTime " + loadTime);
		Collection<SerializedComponents> result = new ArrayList<SerializedComponents>();
		Query query = new Query("Item",userKey);
		query.addFilter("lastUpdate", Query.FilterOperator.GREATER_THAN, time);
		for(Entity e : datastore.prepare(query).asIterable()){
			//System.out.println("loading " + e.getKey().getName() + " last updated at" + e.getProperty("lastUpdate"));
			result.add(new SerializedComponents(e));
		}
		return new ItemsWithTime(result,loadTime);
	}
	
	// should I check the database event times against the entity lastupdate times?  there's an issue here!
	// this will return recent updates
	public ItemsWithTime applyDatabaseEvents(ArrayList<DatabaseEvent> events, double time) throws NotLoggedIn{
		Key userKey = getUserKey();
		ItemsWithTime result = loadLaterThan(time);  // Get updates to send back first, so we don't duplicate the new updates
		for(DatabaseEvent e : events){
			if(e instanceof ModifyEvent){
				ModifyEvent event = (ModifyEvent)e;
				Entity entity;
				Key key = KeyFactory.createKey(userKey, "Item", event.uid);
				try{
					entity = datastore.get(key);
				} catch(EntityNotFoundException x) {
					entity = new Entity("Item",event.uid,userKey);
				}
				if(event instanceof MoveChildEvent || event instanceof RemoveChildEvent){
					// hoping i understand list properties...
					Object o = entity.getProperty("list");
					List<String> list;
					if(o instanceof List<?>){
						list = (List<String>)o;
					}else{
						list = new ArrayList<String>();
					}
					if(event instanceof MoveChildEvent){
						list.remove(((MoveChildEvent)event).childUID);
						int i = ((MoveChildEvent)event).targetIndex;
						if(i >= 0 && i <= list.size()) list.add(((MoveChildEvent)event).targetIndex, ((MoveChildEvent)event).childUID);
						else list.add(((MoveChildEvent)event).childUID);
					}
					else if(event instanceof RemoveChildEvent) list.remove(((RemoveChildEvent)event).childUID);
					if(list.isEmpty()) entity.removeProperty("list");
					else entity.setProperty("list", list);
				}else if(event instanceof SetContentEvent){
					SetContentEvent contentEvent = (SetContentEvent)event;
					entity.setProperty("content", contentEvent.contentString);
					if(contentEvent.contentCacheString != null) entity.setProperty("contentCache", contentEvent.contentCacheString);
					//System.out.println("Setting content " + ((SetContentEvent)event).contentString);
				}else if(event instanceof FullSaveEvent){
					FullSaveEvent FSevent = (FullSaveEvent)event;
					entity.setProperty("type", FSevent.type);
					entity.setProperty("content", FSevent.contentString);
					if(FSevent.contentCacheString != null) entity.setProperty("contentCache", FSevent.contentCacheString);
					entity.setProperty("list", FSevent.uidList);
					//System.out.println("Setting content " + ((FullSaveEvent)event).contentString);
				}
				if(entity.hasProperty("lastUpdate") && (double)entity.getProperty("lastUpdate") > time){
					//System.out.println("A repeat added to result...");
					result.list.add(new SerializedComponents(entity));
				}
				entity.setProperty("lastUpdate",result.time);
				datastore.put(entity);
			}else if(e instanceof DeleteEvent){
				DeleteEvent event = (DeleteEvent)e;
				datastore.delete(KeyFactory.createKey(userKey, "Item", event.uid));
			}
		}
		return result;
	}
	
	public Void clear() throws NotLoggedIn {
		Query query = new Query("Item",getUserKey()).setKeysOnly();
		Iterable<Entity> ei = datastore.prepare(query).asIterable();
		for(Entity e : ei){
			datastore.delete(e.getKey());
		}
		return null;		
	}

}
