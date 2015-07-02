package org.meli.proxy.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.meli.proxy.MeliConfig;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.eaio.uuid.UUID;

public class DBManager {
	private static final Logger logger = LogManager.getLogger(DBManager.class);
	
	private static final String INSERT_REQUEST = "insert into meli.requests (id, ip, path, http_code, response, created_date) values (?, ?, ?, ?, ?, dateof(now()));";
	
	private static PreparedStatement statement;
	private static BoundStatement boundStatement;
	
	/** Cassandra Cluster. */
	private Cluster cluster;

	/** Cassandra Session. */
	private Session session;
	
	private static DBManager instance;
	
	private DBManager() {}
	
	public static DBManager getInstance() {
		if (instance == null) {
			instance = new DBManager();
		}
		return instance;
	}
	
	/**
	 * Connect to Cassandra Cluster specified by provided node IP address and
	 * port number.
	 * 
	 * @param node
	 *            Cluster node IP address.
	 * @param port
	 *            Port of cluster host.
	 */
	public void connect(final String node, final int port) {
		this.cluster = Cluster.builder().addContactPoint(node).withPort(port).build();
		final Metadata metadata = cluster.getMetadata();
		
		logger.info("Connected to cluster: " + metadata.getClusterName());
		
		for (final Host host : metadata.getAllHosts()) {
			logger.info("Datacenter: " + host.getDatacenter() + " Host: " + host.getAddress() + " Rack: " + host.getRack());
		}
		session = cluster.connect();
		
		statement = getSession().prepare(INSERT_REQUEST);		 
		boundStatement = new BoundStatement(statement);	
	}
	
	/**
	 * Provide my Session.
	 * 
	 * @return My session.
	 */
	public Session getSession() {
		if (session == null) {
			connect(MeliConfig.DB_IP, MeliConfig.DB_PORT);
		}
		return session;
	}

	/** Close cluster. */
	public void close() {
		cluster.close();
	}
	
	public synchronized void insert(String ip, String path, int httpCode,  String response) {
		UUID uuid = new UUID();
		
		// Insert one record into the users table
		//String sql = "insert into meli.requests (id, ip, path, http_code, response, created_date) values ('" + uuid + "', '" + ip + "', '" + path + "', " + String.valueOf(httpCode) + ", '" + response + "', dateof(now()) );";
		try {
			//System.out.println("Inserted: " + sql);
			getSession().execute(boundStatement.bind(uuid.toString(), ip, path, httpCode, response));
			
			//getSession().execute(sql);
		} catch (Exception e) {
			//logger.error("SQL: " + sql);
			logger.error("Error: " + e.getMessage());
		}
				
		//logger.info("Register added: " + uuid);
	}
	
	
	/**
	 * Returns an HTML formated string from cache entries.
	 * 
	 * @return
	 */
	public String printCacheToHTML() {
		String returnString = "";
				
		returnString = returnString +	"<table style='width:100%;border: 1px solid black;'>";
		returnString = returnString + "<tr><td>Path</td><td>Total</td></tr>";
		
		ResultSet res = getSession().execute("select count(*) from meli.requests where path = 'search';");
		Row data = res.one();
		returnString = returnString + "<tr><td>Search</td><td>" + data.getLong(0) + "</td></tr>";
		
		res = getSession().execute("select count(*) from meli.requests where path = 'items';");
		data = res.one();
		returnString = returnString + "<tr><td>Items</td><td>" + data.getLong(0) + "</td></tr>";
		
		res = getSession().execute("select count(*) from meli.requests where path = 'sites';");
		data = res.one();
		returnString = returnString + "<tr><td>Sites</td><td>" + data.getLong(0) + "</td></tr>";
		
		return returnString + "</table>";
	}
}
