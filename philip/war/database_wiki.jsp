<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.sql.*" %>
<%@ page import="com.google.appengine.api.rdbms.AppEngineDriver" %>
<%@ page import="javax.servlet.http.HttpServletRequest" %>

<html>
 <title>Database Wiki control panel</title>
 
 <body>
 <p><a href="/server.jsp">Server Home</a> &nbsp <a href="/databases.jsp"><strong>Databases</strong></a>
 
 <p ><strong>Load data to database:<code><big> <%= request.getParameter("dbn") %> </big></code> <%= request.getParameter("dbt") %></strong></p>
  <form action="/importData" method="post">
   <div><font color=#FF4500>XML file: </font><input type="text" name="file" placeholder="http://"></input></div>
   <div>Path: <input type="text" name="path" placeholder="/root/data node/"></input></div>
   <input type="hidden" name="name" value=<%= request.getParameter("dbn") %> />
   <input type="hidden" name="title" value=<%= request.getParameter("dbt") %> />
   <div><input type="submit" value="Upload" /></div>
   <input type="hidden" name="importData" />
  </form>
 
 </body>