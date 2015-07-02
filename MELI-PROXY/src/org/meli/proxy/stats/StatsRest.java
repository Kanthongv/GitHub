package org.meli.proxy.stats;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;

@Path("/stats")
public class StatsRest {
 	    // The Java method will process HTTP GET requests
	    @GET
	    // The Java method will produce content identified by the MIME Media
	    // type "text/plain"
	    @Produces("text/plain")
	    public String getClichedMessage() {
	        // Return some cliched textual content
	        return "Hello World";
	    }
	    
	    @PUT
	    @Produces("text/plain")
	    private void updateDefaultCacheTimeout() {
			// TODO Auto-generated method stub

		}
}
