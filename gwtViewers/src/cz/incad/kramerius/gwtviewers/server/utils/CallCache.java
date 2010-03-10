package cz.incad.kramerius.gwtviewers.server.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.WeakHashMap;


public class CallCache {
	
	public static final int MAX_CALL = 1000;
	
	private static HashMap<String, Object> callCache = new HashMap<String, Object>();
	private static List<String> identList = new ArrayList<String>();
	
	public static String parametersHash(Object...args) {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(args);
		return "0x"+Integer.toHexString(result);
	}
	

	public static String makeIdent(String className, String methodName, String parametersHash) {
		return className+"_"+methodName+"_"+parametersHash;
	}
	public static String makeIdent(String className, String methodName, Object...args) {
		return makeIdent(className, methodName, args);
	}

	public synchronized static void cacheValue(String ident, Object value) {
		deleteOldIdent();
		makeNew(ident);
		callCache.put(ident, value);
	}
	public static Object valueFromCache(String ident) {
		if (identList.contains(ident)) {
			makeNew(ident);
			return callCache.get(ident);
		} else return null;
	}

	public static boolean isInCache(String ident) {
		return identList.contains(ident);
	}

	private static void deleteOldIdent() {
		if (identList.size() > MAX_CALL) {
			String oldIdent = identList.remove(0);
			callCache.remove(oldIdent);
		}
	}
	


	private static void makeNew(String ident) {
		identList.remove(ident);
		identList.add(ident);
	}

	public static void dumpCache() {
		System.out.println(identList);
		System.out.println(callCache);
	}
}
