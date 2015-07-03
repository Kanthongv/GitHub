package org.meli.proxy.filter;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.meli.proxy.MeliConfig;
import org.meli.proxy.cache.MeliCache;

import net.spy.memcached.MemcachedClient;

/**
 * Filter for all instances requests.
 * 
 * @author kvannasaeng
 *
 */
public class MeliFilter {
	private MemcachedClient cache;
	
	private static ArrayList<String> blackList = new ArrayList<String>();
	
	public MeliFilter() {
		this.cache = MeliCache.getClient();
	}
	
	/**
	 * Filter request
	 * @throws Exception
	 */
	//TODO: Filter by ip, path, etc
	public synchronized void filter(HttpServletRequest servletRequest) throws Exception {
		Long current = cache.incr(MeliConfig.SERVER_CURRENT_REQUESTS_COUNTER, 1, 1);
		System.out.println("Count: " + current);
		
		if (current > MeliConfig.MAX_REQUESTS) {
			throw new Exception("Limit reached");
		}
	}
	
	public synchronized void decrement(String serverName) {
		Long counter = MeliCache.getClient().decr(serverName + MeliConfig.SERVER_CURRENT_REQUESTS_COUNTER, 1, 0);
		System.out.println("Decrement to: " + counter);
	}
}
