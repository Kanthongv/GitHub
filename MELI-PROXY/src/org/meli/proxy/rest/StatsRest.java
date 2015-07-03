package org.meli.proxy.rest;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.json.JSONArray;
import org.json.JSONObject;
import org.meli.proxy.MeliConfig;
import org.meli.proxy.cache.CacheManager;
import org.meli.proxy.cache.MeliCache;
import org.meli.proxy.cache.MeliThread;
import org.meli.proxy.config.ConfigManager;

import com.sun.org.apache.bcel.internal.generic.NEW;


@Path("/stats")
public class StatsRest {
	
	@GET
	@Produces("application/json")
	public String getStats() {
		JSONObject jsonResponse = new JSONObject();
		
		JSONObject jsonEntry = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		
		jsonArray.put(jsonResponse);

		jsonEntry.put(ConfigManager.SERVER_NAME, jsonArray);
		
		jsonResponse.putOnce("Description", "Meli Proxy Stats");
		jsonResponse.putOnce(MeliConfig.CONFIG_CACHE_TIMEOUT, CacheManager.getTimeout());
		jsonResponse.putOnce("SERVER_MAX_REQUESTS", MeliConfig.MAX_REQUESTS);

		// Add threads pool
		HashMap<String, MeliThread> conn = CacheManager.getMeliConnectors();
		
		JSONObject jsonThread = new JSONObject();
		
		for (Iterator it = conn.entrySet().iterator(); it.hasNext();) {
			Entry<String, MeliThread> type = (Entry<String, MeliThread>) it.next();
			
			String name = type.getValue().getName();
			long total = Long.parseLong(CacheManager.getTotalCounter(name));
			long totalHit = Long.parseLong(CacheManager.getHit(name));
			double  percent = (total > 0)?((totalHit * 100) / total):0;
			
			JSONArray arr = new JSONArray();
			arr.put(new JSONObject().put("Hit", totalHit));
			arr.put(new JSONObject().put("Total", total));
			arr.put(new JSONObject().put("%", percent));
			
			jsonThread.put(type.getValue().getName(), arr);
		}
		
		jsonResponse.putOnce("Threads", jsonThread);

		return jsonEntry.toString();
	}
}
