package org.meli.proxy.cache;

import java.io.IOException;
import java.util.HashMap;

import net.spy.memcached.MemcachedClient;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
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
	
	private static int hitCount = 0;
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
	 * Get data from cache or MELI
	 * 
	 * @param uri
	 * @return
	 */
	public String getResponse(final String uri) {
		
		String localUri = new String(uri);

		//String cacheSync =  new String();
		String result = null;
		synchronized (localUri) {
			//is in cache?
			result = (String) cache.get(localUri);
			
			if (result != null) {
				addHitCount(localUri);
				
				//System.out.println("HitCount: " + hitCount++);
				logger.info("Found key: '" + uri + "' count: " + hitCount);
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

	private static synchronized String getHit(final String uri) {
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
		
//		Long ret = (Long) MeliCache.get(MeliConfig.STAT_REQUEST_COUNTER);
//		if (ret == null) {
//			return 0L;
//		}
//		return ret;
	}


	private static synchronized String getTotalCounter(final String uri) {
		String returnValue = (String) MeliCache.get(uri + MeliConfig.STAT_TOTAL_COUNTER);
		if (returnValue == null) {
			return "0";
		}
		return returnValue;
	}
	
	private static synchronized String getCallCounter(final String uri) {
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
}

/**
 * Meli http thread.
 * It makes the call to Meli rest services.
 * 
 * @author kvannasaeng
 */
class MeliThread extends Thread {
	private static final Logger logger = LogManager.getLogger(MeliThread.class);
	
	private Integer status = 0; //0:free, 1: connecting
	
	private MemcachedClient cache;
	
	private Integer connectionRequestcounter = 0;
	
	/**
	 * 
	 * @param name=uri
	 */
	public MeliThread(String name, MemcachedClient cache) {
		super(name);
		this.cache = cache;
	}

	@Override
	public void run() {
		
		while (true) {
			logger.info("MeliThread: '" + getName() + "' woke up!");
			String response = callMELI(getName());

			// Save to cache
			MeliCache.set(getName(), CacheManager.getTimeout(), response);
			
			// Save counter
//			setConnectionRequestcounter(getConnectionRequestcounter() + 1);
//			MeliCache.getClient().set(getName() + "_COUNTER", 3600, getConnectionRequestcounter());
			
			cache.incr(getName() + MeliConfig.STAT_CALL_COUNTER, 1L,1L);

			// Free flag
			logger.info("Wake up pending threads!");
			synchronized (this) {
				setStatus(0);
				notifyAll();
			}
			
			try {
				logger.info("MeliThread: '" + getName() + "' go to WAIT.");
				
				synchronized(this){
				    while (getStatus() == 0){
				        this.wait();
				    }
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	protected String callMELI(final String url) {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpget = new HttpGet(MeliConfig.MELI_BASE_URI + url);
		CloseableHttpResponse response = null;
		try {
			StopWatch sw = new StopWatch();
			
			logger.info("================> Calling MELI for: " + url );
			sw.start();
			response = httpclient.execute(httpget);
			sw.stop();
			
			logger.info("Time taken: " + sw.getTime());
			
			int server = response.getStatusLine().getStatusCode();
			logger.info("Response CODE: " + server);

			HttpEntity entity = response.getEntity();
			String responseString = EntityUtils.toString(entity, "UTF-8");
			
			return responseString;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (response != null) {
			  try {
				response.close();
			  } catch (IOException e) { }
			}
		}
		
		return "";
	}
	
	//Getters & Setters
	
	public synchronized int getStatus() {
		return status;
	}
	
	public synchronized void setStatus(Integer status) {
		this.status = status;
	}
	
	public synchronized Integer getConnectionRequestcounter() {
		return connectionRequestcounter;
	}

	public synchronized void setConnectionRequestcounter(Integer connectionRequestcounter) {
		this.connectionRequestcounter = connectionRequestcounter;
	}
}

	class CallStatus {
		boolean isHit = false;
		String response;
		
		public CallStatus(boolean isHit, String response) {
			this.isHit = isHit;
			this.response = response;
		}
	}
