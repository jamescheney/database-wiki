<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.sql.*" %>
<%@ page import="com.google.appengine.api.rdbms.AppEngineDriver" %>

<html>
  <body>

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
<td><%= id %></td>
<td><%= name %></td>
<td><%= title %></td>
</tr>

<% }
c.close(); %>

</tbody>
</table>
<br />

<p><strong>Add database</strong></p>
<form action="/adddatabase" method="post">
    <div>Name: <input type="text" name="name"></input></div>
    <div>Title:<input type="text" name="title"></input></div>
    <div><input type="submit" value="Add database" /></div>
    <input type="hidden" name="adddatabase" />
  </form>
  </body>
</body>
</html>


