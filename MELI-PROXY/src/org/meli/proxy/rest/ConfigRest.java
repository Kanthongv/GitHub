package org.meli.proxy.rest;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.meli.proxy.MeliConfig;
import org.meli.proxy.cache.CacheManager;
import org.meli.proxy.config.ConfigManager;

@Path("/config")
public class ConfigRest {
	private static final Logger logger = LogManager.getLogger(ConfigRest.class);

	@GET
	@Produces("application/json")
	public String getClichedMessage() {
		JSONObject jsonResponse = new JSONObject();
		jsonResponse.put(MeliConfig.CONFIG_CACHE_TIMEOUT,
				CacheManager.getTimeout());

		return jsonResponse.toString();
	}

	@PUT
	@Produces("application/json")
	public void updateDefaultCacheTimeout(String json) {
		JSONObject jsonResponse = new JSONObject(json);
		System.out.println(jsonResponse.toString());

		int timeout = jsonResponse.getInt(MeliConfig.CONFIG_CACHE_TIMEOUT);
		System.out.println(MeliConfig.CONFIG_CACHE_TIMEOUT + timeout);
		
		ConfigManager.setTimeout(timeout);

		// Flush
		boolean flushCache = jsonResponse.getBoolean(MeliConfig.CONFIG_FLUSH_CACHE);

		System.out.println("flush-cache: " + flushCache);
		if (flushCache) {
			ConfigManager.flushCache();
		}
	}
}
