<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.sql.*" %>
<%@ page import="com.google.appengine.api.rdbms.AppEngineDriver" %>

<html>
 <title>DBWiki Users control panel</title>
  
  <body>
<p><a href="/server.jsp">Server Home</a></p>

<%
Connection c = null;
c = DriverManager.getConnection("jdbc:google:rdbms://database-wiki-cloudsql:test/dbwiki");
ResultSet rs = c.createStatement().executeQuery("SELECT id,full_name, login, password FROM _user"); %>

<table style="border: 1px solid black">
<tbody>
<tr>
<th style="background-color: #CCFFCC; margin: 5px">ID</th>
<th width="35%" style="background-color: #CCFFCC; margin: 5px">Name</th>
<th style="background-color: #CCFFCC; margin: 5px">Login</th>
<th style="background-color: #CCFFCC; margin: 5px">Password</th>
</tr> <%
while (rs.next()){
	int id = rs.getInt("id");
    String name = rs.getString("full_name");
    String username = rs.getString("login");
    String password = rs.getString("password"); %>

<tr>
<td><%= id %></td>
<td><%= name %></td>
<td><%= username %></td>
<td><%= password %></td>
</tr>

<% }
c.close(); %>

</tbody>
</table>
<br />

<p><strong>Add user</strong></p>
<form action="/adduser" method="post">
    <div>Name: <input type="text" name="name"></input></div>
    <div>Userid:<input type="text" name="username"></input></div>
    <div>Password:<input type="text" name="password"></input></div>
    <div>Confirm Password:<input type="text" name="password2"></input></div>
    <div><input type="submit" value="Add user" /></div>
    <input type="hidden" name="adduser" />
  </form>

<p><strong>Drop user</strong></p>
<form action="/dropuser" method="post">
    <div>ID: <input type="text" name="id"></input></div>
    <div><input type="submit" value="Drop user" /></div>
    <input type="hidden" name="dropuser" />
  </form>

  </body>
</body>
</html>


