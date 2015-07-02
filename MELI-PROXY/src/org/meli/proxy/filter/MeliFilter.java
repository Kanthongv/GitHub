package org.meli.proxy.filter;

import java.util.ArrayList;

import org.meli.proxy.MeliConfig;
import org.meli.proxy.cache.MeliCache;

import net.spy.memcached.MemcachedClient;

public class MeliFilter {
	private MemcachedClient cache;
	
	private static ArrayList<String> blackList = new ArrayList<String>();
	
	public MeliFilter() {
		this.cache = MeliCache.getClient();
	}
	
	public synchronized void filter(String serverName, final String ip) throws Exception {
		//Works with the cache, must be saved to cache
//		System.out.println("IP: " + ip);
		Long current = cache.incr(MeliConfig.SERVER_CURRENT_REQUESTS_COUNTER, 1, 1);
		System.out.println("Count: " + current);
		
		if (current > 50000) {
			throw new Exception("Limit reached");
		}
	}
	
	public synchronized void decrement(String serverName) {
		Long counter = MeliCache.getClient().decr(serverName + MeliConfig.SERVER_CURRENT_REQUESTS_COUNTER, 1, 0);
		System.out.println("Decrement to: " + counter);
	}
}
