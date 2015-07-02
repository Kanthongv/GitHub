package org.meli.proxy.cache;

import net.spy.memcached.MemcachedClient;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.Future;

import org.meli.proxy.MeliConfig;

public class MeliCache {
    private static MemcachedClient MC_CLIENT = null; 
    
    private MeliCache() {}
    
    public static synchronized MemcachedClient getClient() {
    	if (MC_CLIENT == null) {
    		try {
				MC_CLIENT = new MemcachedClient(new InetSocketAddress(MeliConfig.CACHE_IP, MeliConfig.CACHE_PORT));
				System.out.println("Created memcached client!");
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    	
    	return MC_CLIENT;
    }
    
    public static synchronized void set(String key, int exp, Object obj) {
    	MeliCache.getClient().set(key, exp, obj);
    }
	
    public static synchronized Object get(String key) {
		return MeliCache.getClient().get(key);
	}
	
	
   
   public static void main(String[] args) {
      try{
         // Connecting to Memcached server on localhost
         MemcachedClient mcc = new MemcachedClient(new InetSocketAddress("127.0.0.1", 11211));
         System.out.println("Connection to server sucessful.");
         
         // now set data into memcached server
         Future fo = mcc.set("tutorialspoint", 90, "Free Education");
      
         // print status of set method
         System.out.println("set status:" + fo.get());
         
         // retrieve and check the value from cache
         System.out.println("tutorialspoint value in cache - " + mcc.get("tutorialspoint"));
         
         
         mcc.delete("search?q=ipod");

         // Shutdowns the memcached client
         mcc.shutdown();
         
      } catch(Exception ex){
         System.out.println( ex.getMessage() );
      } 
   }
}
