package org.meli.proxy.cache;

import java.io.IOException;

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
 * Meli http thread.
 * It makes the call to Meli rest services. Caches the response and handle call lifecicle.
 * 
 * @author kvannasaeng
 */
public class MeliThread extends Thread {
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
			cache.incr(getName() + MeliConfig.STAT_CALL_COUNTER, 1L,1L);

			// Free flag
			logger.info("Wake up pending threads for: " + getName());
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