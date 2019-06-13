package org.bailey.taskfront.shared;

import java.util.HashMap;

import org.bailey.taskfront.client.Database;

// ****** Eventually, UID will not just be a static utility class, but will instantiate as the actual UIDs (or not??)
public class UID {
	private static double lastTime = 0;
	private static int msCount = 0;
	
	//private static Random rand = new Random((long) DT.getTime());
	
	//public native static double getTime() /*-{
	//	return .0 + (new Date()).getTime();
	//}-*/;
	
	public static String getUID () {
		double time = DT.getTime();
		if(time == lastTime){
			msCount++;
		} else {
			msCount=0;
			lastTime=time;
		}
		return Long.toString((long) time,Character.MAX_RADIX) + Integer.toString(msCount,Character.MAX_RADIX);
		//return time + "." + msCount;
	};
	public static String getUID(Item o){
		String uid = getUID();
		references.put(uid, o);
		return uid;
	}
	public static String getUIDextension(String baseID){
		return baseID + getUID();
	}
	public static String getUIDextension(Item o, String baseID){
		String uid = getUIDextension(baseID);
		references.put(uid, o);
		return uid;		
	}
	
	public static double UIDToTime(String uid){
		return Double.valueOf(uid.substring(0,uid.indexOf(".")));
	}
	// String.replace with substrings is native javascript
	
	private static HashMap<String,Item> references = new HashMap<String,Item>();
	public static Item getItem(String uid){return references.get(uid);}
	public static boolean hasUID(String uid){return references.containsKey(uid);}
	public static void putItem(String uid, Item item){
		references.put(uid, item);
	}
	public static void delete(String uid){
		references.remove(uid);
	}
	
}
