# Introduction #
Thanks to the servlet interface and improved MySQL backend, deploying the whole Database Wiki to the cloud was accomplishable.
This is a brief documentation on how to set the system up to be able to run locally, as well as remotely in the Google App Engine.

# Preparation #
Prerequisites to be installed:
  * JDK
  * subversion
  * App Engine SDK (java)
  * standards-compliant web browser
  * ant
Optionally (for running the server locally):
  * PostgreSQL or MySQL server
# Checking out the sources #
```
svn checkout https://database-wiki.googlecode.com/svn/branches/appengine dbwiki
cd dbwiki
```
# Basic configuration and compilation #
  1. If you intend to use the App Engine version (cloud or local), specify the location of your App Engine SDK as the `sdk.dir` property in the `build.xml` file.
  1. You can follow to compile the sources by executing:
```
ant compile
```
  1. Create and fill the initial user list file:
```
cd war/resources/configuration/server
cp users.sample users
```
# Local database configuration (optional) #
You need to set up either a MySQL or a PostgreSQL server. That includes creating a user with a password and a database owned by the user. After confirming the database is working properly, you can proceed with the following steps:
  1. Create a local configuration file
```
cd war/resources/configuration/server
cp config.sample config.local
```
  1. Set `'RDBMS_TYPE'` to `'MYSQL'` or `'PSQL'` depending on the server you have set up
  1. Set `'JDBC_URL'` to either `'jdbc:mysql://127.0.0.1/<database-name>'` or `'jdbc:postgresql:<database-name>'`
  1. Set `'JDBC_USER'` and `'JDBC_PASSWORD'` to match the database user credentials
  1. Initialise the local database by running
```
cd ../../../..
sh init-local.sh
```
# Cloud database configuration #
To use the cloud version of Database Wiki, you need to have an instance of a Google's Cloud SQL database that is accessible by the App Engine project used for deploying. You can specify which applications (projects) can access your database in the Cloud SQL online preferences. Following steps are needed for configuration:
  1. Create a cloud configuration file
```
cd war/resources/configuration/server
cp config.sample config.engine
```
  1. Set `'RDBMS_TYPE'` to `'CLOUDSQL'`
  1. Set `'JDBC_URL'` to `'jdbc:google:mysql://<parent-of-cloudsql-instance>:<cloudsql-instance>/<database-name>'`
  1. Set `'JDBC_USER'` to `root` and comment out the field `'JDBC_PASSWORD'`
# Remote cloud database management #
To initialise your cloud database and import local data into it, you need to configure remote access to the database. You need to assign an IP address and a root password in the Cloud SQL online preferences of your instance.
  1. Create a remote configuration file
```
cd war/resources/configuration/server
cp config.sample config.remote
```
  1. Set `'RDBMS_TYPE'` to `'MYSQL'`
  1. Set `'JDBC_URL'` to `'jdbc:mysql://<assigned-ip-address>/<database-name>'`
  1. Set `'JDBC_USER'` to `'root'` and `'JDBC_PASSWORD'` to match the root password you assigned to the database
  1. Initialise the cloud database by running
```
cd ../../../..
sh init-cloud.sh
```
# Importing data #
After configuring the database you can import some data. You can use either the script `'import-xml-local.sh'` or `'import-xml-cloud.sh'`.
Usage of this script is as following:
```
sh import-xml-*.sh <DB> <DatabaseName> /path/step foo.xml <userid>
```
For example, to import data to Cloud SQL from TEST.xml where the root node is `'TEST'` and step being `'COUNTRY'` you can execute the following:
```
sh import-xml-cloud.sh TEST Test /TEST/COUNTRY data/TEST/TEST.xml Admin
```
This will create a database with id TEST and name Test under user Admin. For information about the data format, see LoadingData.
Alternatively, you can also import presentation files to make the data more readable:
```
sh import-presentation-cloud.sh TEST data/TEST/presentation/ Admin
```
This will import the following presentation files: `TEST.css, TEST.layout, TEST.template, TEST.urldecoding.`
# Running locally #
To run the local App Engine server, run:
```
ant run-server
```
To run the old `HttpServer` version without App Engine, run:
```
ant run-server-old
```
# Deploying to App Engine #
To deploy the project to App Engine, you need to set your App Engine project id in the file `'war/WEB-INF/appengine-web.xml'` inside `<application>`. After that you can initiate the deployment by executing:
```
ant deploy
```
You will be asked for your Google account credentials to authenticate and start the deployment.
# Testing #
After starting the local server, you can test if it's running properly by visiting:
```
http://localhost:8080
```
If you deployed the project successfuly to App Engine, you can test it at:
```
http://<project-id>.appspot.com
```
# Logging in #
The App Engine version currently supports logging in with existing Google accounts.

(To be continued)