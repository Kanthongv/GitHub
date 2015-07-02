package org.meli.proxy.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.meli.proxy.MeliConfig;
import org.meli.proxy.cache.CacheManager;
import org.meli.proxy.cache.MeliCache;
import org.meli.proxy.config.ConfigManager;

@WebListener
public class InitializeListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("On start web app");
        
        //Load configuration data
        //ConfigManager.SERVER_NAME = sce.getServletContext().getServerInfo().
        
        //Connect to memcached
        MeliCache.getClient();
        
        System.out.println("Request counter reset");
        MeliCache.set(MeliConfig.SERVER_CURRENT_REQUESTS_COUNTER, 3600, "0");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("On shutdown web app");
    }

}
