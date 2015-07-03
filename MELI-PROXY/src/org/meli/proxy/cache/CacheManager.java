package org.meli.proxy.cache;

import java.util.HashMap;

import net.spy.memcached.MemcachedClient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.meli.proxy.MeliConfig;

/**
 * Singleton controller.
 * 
 * @author kvannasaeng
 *
 */
public class CacheManager {
	private static final Logger logger = LogManager.getLogger(CacheManager.class);
	
	private static HashMap<String, MeliThread> meliConnectors = new HashMap<String, MeliThread>();

	private static MemcachedClient cache = MeliCache.getClient();
	
	private static CacheManager instance;
	
	private static int timeout = MeliConfig.DEFAULT_CACHE_TIMEOUT;
	
	private CacheManager() {
	}
	
	public static synchronized CacheManager getInstance() {
		if (instance == null) {
			instance = new CacheManager();
		}
		
		return instance;
	}
	
	public static void flushCache() {
		cache.flush();
		System.out.println("Cache has been flushed!!!!!!");
	}
	
	/**
	 * Get data from cache or call MELI service
	 * 
	 * @param uri
	 * @return
	 */
	public String getResponse(final String uri) {
		
		String localUri = new String(uri);

		String result = null;
		synchronized (localUri) {
			//is in cache?
			result = (String) cache.get(localUri);
			
			if (result != null) {
				addHitCount(localUri);
				
				logger.info("Found key: '" + uri);
			} 
		}
		addTotalCallCount(localUri);		
		if (result == null) {
			//Not Found in cache
			logger.info("Not Found in cache: " + localUri);
			
			MeliThread meliThread = (MeliThread) meliConnectors.get(localUri);
			if (meliThread != null && meliThread.getStatus() == 0) {
				//Existing MeliThread and Free
				synchronized (meliThread) {
					meliThread.setStatus(1);
					meliThread.notify();
				}
				
			} if (meliThread == null) {
				//Add new melithread
				meliThread = new MeliThread(uri, cache);
				meliThread.start();
				
				logger.info("Adding Melithread to map: " + meliThread.getName());
				
				//Save to map
				meliConnectors.put(uri, meliThread);

				try {
					synchronized (meliThread) {
						logger.info("I need to wait :(");
						meliThread.wait();
					}
					
					logger.info("Read loaded response from MELI: " + localUri);
					result = (String) cache.get(localUri);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} if (meliThread != null && meliThread.getStatus() == 1) {
				try {
					synchronized (meliThread) {
						while ((meliThread != null && meliThread.getStatus() == 1)) {
							logger.info("I need to wait :(");
							meliThread.wait();
						}
					}
					
					logger.info("Read loaded response from MELI: " + localUri);
					result = (String) cache.get(localUri);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		return result;
	}

	private synchronized void addHitCount(final String uri) {
		// Save hit counter
		Long counter = (Long) cache.incr(uri + MeliConfig.STAT_HIT_COUNTER, 1, 1L);
	}
	
	private synchronized void addTotalCallCount(final String uri) {
		// Save hit counter
		Long counter = (Long) cache.incr(uri + MeliConfig.STAT_TOTAL_COUNTER, 1, 1L);
	}

	public static synchronized String getHit(final String uri) {
		String returnValue = (String) MeliCache.get(uri + MeliConfig.STAT_HIT_COUNTER);
		if (returnValue == null) {
			return "0";
		}
		return returnValue;
	}
	
	public static synchronized String getReq() {
		String returnValue = (String) MeliCache.get(MeliConfig.SERVER_CURRENT_REQUESTS_COUNTER);
		if (returnValue == null) {
			return "0";
		}
		return returnValue;
	}

	public static synchronized String getTotalCounter(final String uri) {
		String returnValue = (String) MeliCache.get(uri + MeliConfig.STAT_TOTAL_COUNTER);
		if (returnValue == null) {
			return "0";
		}
		return returnValue;
	}
	
	public static synchronized String getCallCounter(final String uri) {
		String returnValue = (String) MeliCache.get(uri + MeliConfig.STAT_CALL_COUNTER);
		if (returnValue == null) {
			return "0";
		}
		return returnValue;
	}
	
	/**
	 * Returns an HTML formated string from cache entries.
	 * 
	 * @return
	 */
	public static String printCacheToHTML() {
		StringBuilder returnString = new StringBuilder();
				
		returnString.append("<table><tr>Meli Connectors: " + meliConnectors.keySet().size() + "</tr><br><br>");
		returnString.append("<tr><th>URI</th><th>Thread Counter</th><th>Hit</th><th>Total</th><th><font color='#FF0000'>%</font></th></tr>");
		
		for (String uri : meliConnectors.keySet()) {
			long total = Long.parseLong(getTotalCounter(uri));
			long totalHit = Long.parseLong(getHit(uri));
			double  percent = (total > 0)?((totalHit * 100) / total):0;
			
			returnString.append( 
					"<tr>" 
					+ "<td color='blue'>" + uri + "</td>" 
					+ "<td>" + getCallCounter(uri)  + "</td>"
					+ "<td>" + totalHit  + "</td>" 
					+ "<td>" + total + "</td>"
					+ "<td><font color='#FF0000'>" + percent + "</font></td>"
					+ "</tr>");
		}
		
		returnString.append("</table>");
		
		return returnString.toString();
	}
	
	public static void setTimeout(int value) {
		timeout = value;
	}
	
	public static  int  getTimeout() {
		return timeout;
	}
	
	public static HashMap<String, MeliThread> getMeliConnectors() {
		return meliConnectors;
	}
}