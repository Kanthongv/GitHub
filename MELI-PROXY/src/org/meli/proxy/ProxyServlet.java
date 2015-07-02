package org.meli.proxy;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.meli.proxy.cache.CacheManager;
import org.meli.proxy.cache.MeliCache;
import org.meli.proxy.db.DBManager;
import org.meli.proxy.filter.MeliFilter;

/**
 * Servlet implementation class proxy
 */
@WebServlet("/")
public class ProxyServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final Logger logger = LogManager.getLogger(ProxyServlet.class);

	private static MeliFilter filter = new MeliFilter();

	/**
	 * Default constructor.
	 */
	public ProxyServlet() {
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doCachedMethod(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		PrintWriter pw = response.getWriter();

		// Filter
		try {
			filter.filter(request.getServerName(), request.getRemoteAddr());
		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);

			return;
		}

		pw.write("Success!");

		response.setStatus(HttpServletResponse.SC_OK);

		response.flushBuffer();
	}

	private void doCachedMethod(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter pw = response.getWriter();
		String responseString = null;
		String uri = getUri(request);
		
		try {
			// Filter
			try {
				filter.filter(request.getServerName(), request.getRemoteAddr());
				//throw new Exception("My exc");
			} catch (Exception e) {
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
				logger.info("Max connection reached!");
				return;
			}
			
			try {
				responseString = CacheManager.getInstance().getResponse(uri);
				if (responseString != null) {
					pw.write(responseString);
				}

				// Save to DB
				//DBManager.getInstance().insert(request.getRemoteAddr(), getType(request), response.getStatus(), responseString);

				response.flushBuffer();
			} catch (Exception e) {
				logger.error("Error getting Meli response: " + e.getMessage());
				e.printStackTrace();
			}
		} finally {
			// Save to DB
			DBManager.getInstance().insert(request.getRemoteAddr(), getType(request), response.getStatus(), responseString);
			
			Long counter = MeliCache.getClient().decr(MeliConfig.SERVER_CURRENT_REQUESTS_COUNTER, 1, 0);
			System.out.println("Decrement to: " + counter);
		}
	}

	private void doNonCachedMethod(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		PrintWriter pw = response.getWriter();

		// Filter
		try {
			filter.filter(request.getServerName(), request.getRemoteAddr());
		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);

			return; // e.printStackTrace();
		}

		final String responseString = CacheManager.getInstance().getResponse(
				getUri(request));
		if (responseString != null) {
			pw.write(responseString);
		}

		// Save to DB
		// DBManager.getInstance().insert(request.getRemoteAddr(),
		// getType(request), "");

		response.flushBuffer();

	}

	private String getUri(HttpServletRequest req) {
		String url = req.getRequestURI();
		// logger.debug("getRequestURL: " + url.toString());

		String queryString = req.getQueryString();
		if (queryString != null) {
			url = url + '?';
			url = url + queryString;
		}
		url = url.replace(MeliConfig.WEB_CONTEXT, "/");
		// logger.debug("uri: " + url.toString());
		return url.toString();
	}

	public String getType(HttpServletRequest req) {
		String url = req.getRequestURI();
		// System.out.println("Uri: " + url);

		String[] path = url.split("/");

		// System.out.println(path[2]);
		return path[2];
	}
}
