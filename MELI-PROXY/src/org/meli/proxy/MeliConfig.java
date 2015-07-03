package org.meli.proxy;

/**
 * Configuration data.
 * 
 * @author kvannasaeng
 *
 */
public interface MeliConfig {
	String MELI_BASE_URI = "https://api.mercadolibre.com/";
	//String MELI_BASE_URI = "http://localhost:9500/MELI-MOCK/MockServlet";
	
	String WEB_CONTEXT = "/MELI-PROXY/";
	
	//TTL for objects in the cache.
	int DEFAULT_CACHE_TIMEOUT = 30;
	
	//Max request for all proxy intances
	long MAX_REQUESTS = 1500;  //TODO: Must be configurable in the DB
	
	//Cache
	String CACHE_IP = "127.0.0.1";
	Integer CACHE_PORT =  11211;
	
	//Db
	String DB_IP = "127.0.0.1";
	Integer DB_PORT = 9042;
	
	//Config	
	String CONFIG_CACHE_TIMEOUT = "cache-timeout";
	String CONFIG_FLUSH_CACHE = "flush-cache";
	
	//Stats
	String STAT_HIT_COUNTER = "_HIT_COUNTER";
	String STAT_TOTAL_COUNTER = "_TOTAL_COUNTER";
	String STAT_CALL_COUNTER = "_CALL_COUNTER";

	//Call server current requests
	String SERVER_CURRENT_REQUESTS_COUNTER = "REQUEST_COUNTER";
}

