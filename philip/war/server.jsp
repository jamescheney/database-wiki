<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.sql.*" %>
<%@ page import="com.google.appengine.api.rdbms.AppEngineDriver" %>

<html>
<title>DBWiki Main control panel</title>
 
  <body>
<p><i><a href="https://appengine.google.com/">My GAE Applications</a></i>  
<p><a href="/server.jsp">Server Home</a> &nbsp <a href="/users.jsp"><strong>Users</strong></a> &nbsp <a href="/databases.jsp"><strong>Databases</strong></a></p>

 
  <p><strong>Create server</strong></p>
<form action="/create" method="post">
    <div><input type="submit" value="Create" /></div>
    <input type="hidden" name="create" />
  </form>
  
  <p><strong>Drop server</strong>: This will delete <b>all</b> data in the server!</p>
<form action="/drop" method="post">
    <div><input type="submit" value="Drop" /></div>
    <input type="hidden" name="drop" />
  </form>  
  
  </body>
</html>


