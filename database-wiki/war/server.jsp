<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.sql.*" %>
<%@ page import="com.google.appengine.api.rdbms.AppEngineDriver" %>

<html>
<title>Database Wiki control panel</title>
  <body>
<p><a href="/server.jsp">Server Home</a> <a href="/users.jsp">Users</a> <a href="/databases.jsp">Databases</a></p>

 
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


