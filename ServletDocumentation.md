# Introduction #

The servlet interface for the Database Wiki was written mainly to get rid of the dependence on the non-standard _com.sun.net.httpserver_ framework the project has been using. This brings the possibility to run the whole system on a more standard servlet engine such as [Apache Tomcat](http://tomcat.apache.org), while also making it potentially easier to run on a cloud-based service, such as [Google App Engine](https://developers.google.com/appengine).


# Details #

The main difference between the old and current approach is that the system is now using only one handler (in `WikiServerServlet`) which propagates requests to specific `DatabaseWiki` instances, instead of using a pre-set context handler for every `DatabaseWiki` class instance. This way the system is able to run as a single servlet application.

The main introduced classes are following:

  * `org.dbwiki.main.ServletInterface` - the servlet entry-point; contains `HttpServlet` methods that handle initialization, as well as all GET/POST requests.
  * `org.dbwiki.web.server.WikiServerServlet` - subclass of the core `WikiServer` class with a servlet request handler. This is where the requests get either processed immediately or are propagated to a certain `DatabaseWikiServlet` instance if it is a `DatabaseWiki` request.
  * `org.dbwiki.web.server.DatabaseWikiServlet` - instance of a core `DatabaseWiki` class with a servlet-tuned request handler.
  * `org.dbwiki.web.server.ServletExchangeWrapper` - a wrapper around the core `org.dbwiki.web.request.Exchange` class that provides methods needed for proper http exchange (used in `RequestURL`).
  * `org.dbwiki.web.server.HtmlServletSender` - a basic html page sender through `HttpServletResponse`.
  * `org.dbwiki.web.security.WikiServletAuthenticator` - an authenticator class that checks whether a specific servlet request requires the user to be authenticated in order to provide a response. Every `DatabaseWikiServlet` instance uses its own authenticator.

See DatabaseWikiInternals for explanation of the internal Database Wiki classes.

# Installation #

### Prerequisites ###

  * subversion installation
  * javac/JDK (Java 1.6)
  * ant
  * postgreSQL (>= 8.4)
  * Apache Tomcat
  * a standards-compliant web browser

### Instructions (Tomcat on Unix/MacOS X) ###

Instructions differ only in the system deployment and Tomcat configuration:

  1. Check out sources:
```
svn checkout https://database-wiki.googlecode.com/svn/trunk/database-wiki dbwiki
cd dbwiki
```
  1. Install and configure PostgreSQL; create a PostgreSQL user and a database owned by that user. Give the user a password. Make sure the server is running and the password is correct before proceeding. If you want to use a remote database then you may need to ask its administrator to do this for you, if not, consult the PostgreSQL documentation.
  1. Create config and initial user list files:
```
  cd resources/configuration/server
  cp config.sample config
  cp users.sample users
```
  1. Edit config so that JDBC\_USER is the username and JDBC\_PASSWORD is that user's password. If the PostgreSQL server you want to use is remote, then change JDBC\_URL to point to the appropriate host instead of localhost.
  1. Change back to the root of the source tree and initialize the database. This should succeed with no output if everything is configured correctly. You can confirm this by checking that the three `'_*'` tables have been added to the dbwiki database.
```
cd ../../..
./create-server.sh
```
  1. Build and deploy the package:
```
ant build-war
cp dist/DatabaseWiki.war <tomcat>/webapps
```
  1. Configure Tomcat to make Database Wiki the root application. Add the following line under `<Host>` to _conf/server.xml_ in the Tomcat installation folder:
```
<Context path="" docBase="DatabaseWiki" reloadable="true"></Context>
```
  1. Restart Tomcat:
```
/etc/init.d/tomcat7 restart
```
  1. Test that the server is running correctly by visiting:
```
http://localhost:8080
```
  1. Once you've confirmed that the server is running correctly you can load some data. See LoadingData or QueryVisualizationTutorial.