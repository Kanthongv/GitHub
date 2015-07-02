<%@page import="org.meli.proxy.config.ConfigManager"%>
<%@page import="org.meli.proxy.db.DBManager"%>
<%@page import="org.meli.proxy.cache.CacheManager"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<meta http-equiv='refresh' content='5' />
<title>Monitor</title>
</head>
<script language="javascript">
  var localTime = new Date();
  document.write("Updated time: " + localTime + "<br><br>");
</script>
<style>
table {
    border-collapse: collapse;
    width: 70%;
}

table, td, th {
    border: 1px solid black;
}
</style>
<body id="im">
	<h1 align="center">Mercadolibre Proxy Monitor</h1>
	<hr width="100%"/>
	<p>
		Cache timeout in seconds:
		<%= ConfigManager.getTimeout() %></p>
	<p>
		Active requests: <font color="RED"><%= CacheManager.getReq()%></font>
	</p>
	<table width="100%" border="0">
		<tr align="center"><%= CacheManager.printCacheToHTML() %></tr>
	</table>
	<hr>
	<!-- %= DBManager.getInstance().printCacheToHTML() %> -->
</body>
</html>