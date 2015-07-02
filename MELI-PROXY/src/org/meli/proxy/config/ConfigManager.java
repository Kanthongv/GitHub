package org.meli.proxy.config;

import org.meli.proxy.MeliConfig;
import org.meli.proxy.cache.CacheManager;
import org.meli.proxy.cache.MeliCache;

public class ConfigManager {

	public static String SERVER_NAME = null;
	
	public static void setTimeout(int timeout) {
		CacheManager.setTimeout(timeout);
	}
	
	public static int getTimeout() {
		return CacheManager.getTimeout();
	}
	
	public static void flushCache() {
		CacheManager.flushCache();
		
		//Init counter
        System.out.println("Request counter reset");
        MeliCache.set(MeliConfig.SERVER_CURRENT_REQUESTS_COUNTER, 3600, "0");
	}
}
