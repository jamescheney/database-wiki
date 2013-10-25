<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.sql.*" %>
<%@ page import="com.google.appengine.api.rdbms.AppEngineDriver" %>

<html>
 <title>DBWiki Databases control panel</title>
  
  <body>
<p><a href="/server.jsp">Server Home</a></p>

<%
Connection c = null;
c = DriverManager.getConnection("jdbc:google:rdbms://database-wiki-cloudsql:test/dbwiki");
ResultSet rs = c.createStatement().executeQuery("SELECT id, name, title FROM _database"); %>

<table style="border: 1px solid black">
<tbody>
<tr>
<th width="35%" style="background-color: #CCFFCC; margin: 5px">ID</th>
<th style="background-color: #CCFFCC; margin: 5px">Name</th>
<th style="background-color: #CCFFCC; margin: 5px">Title</th>
</tr> <%
while (rs.next()){
    int id = rs.getInt("id");
    String name = rs.getString("name");
    String title = rs.getString("title"); %>

<tr>
<td align="center"><%= id %></td>
<td><a href="database_wiki.jsp?dbn=<%= name %>&dbt=<%= title %>"><%= name %></a></td>
<td><%= title %></td>
</tr>

<% }
c.close(); %>

</tbody>
</table>
<br />

 <p><strong>Add a database</strong></p>
  <form action="/adddatabase" method="post">
    <div>Name: <input type="text" name="name"></input></div>
    <div>Title: <input type="text" name="title"></input></div>
    <div><input type="submit" value="Add database" /></div>
    <input type="hidden" name="adddatabase" />
  </form>
 

 <p><strong>Drop a database</strong></p>
  <form action="/dropdatabase" method="post">
    <div>Name: <input type="text" name="name"></input><div>
    <div><input type="submit" value="Drop database" /></div>
    <input type="hidden" name="dropdatabase" />
  <form>
  
  </body>
 </body>
</html>
