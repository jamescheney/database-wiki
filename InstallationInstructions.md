# Introduction #

Installation instructions for Database Wiki.


# Details #

### Eclipse-based (Unix, Windows, or Mac OS X) ###

Database Wiki install instructions - Windows

These instructions will get you started with running Database Wiki using Eclipse for development and Subclipse to get the source code.

1.  Install Eclipse from:

If Eclipse  won't start up it is possible that Java is not installed
on your system, so if necessary, Java 6 JDK e.g. from

http://www.oracle.com/technetwork/java/javase/downloads/index.html

2.  Start Eclipse and go to "Install new software" under Help.

3.  Put the URL for Subclipse into the appropriate text box.  I used:

http://subclipse.tigris.org/update_1.6.x

but you can check for an updated URL at http://subclipse.tigris.org.

4. Install the following:

Subclipse
Subversion Client Adapter
Subversion JavaHL Native Library Adapter
Core SVNKit Library

and if you wish other related packages.  Also, follow the instructions at:

http://subclipse.tigris.org/wiki/JavaHL

to install JavaHL, if you are running anything other than 32-bit Windows.

5.  Now when you create a project there should be an option to "create from
SVN repository".  Do that, and enter the URL:

http://database-wiki.googlecode.com/svn/

Then, select an appropriate subdirectory from the directory tree, such
as "trunk/database-wiki" or "tags/database-wiki-0.9.0".

Once you've checked out the project, you should be able to build it immediately
since all necessary libraries are included.

You should then be able to follow the instructions below to configure and
start DatabaseWiki.


### Unix/Cygwin/MacOS X command line: ###

Prerequisites:
  * subversion installation
  * javac/JDK (Java 1.6)
  * ant
  * postgreSQL (>= 8.4)
  * Firefox web browser (v3.6+), other standards-compliant browsers should also work

1.  Check out sources:
```
  svn checkout https://database-wiki.googlecode.com/svn/trunk/database-wiki
```

2.  Build the sources to class files:
```
  cd database-wiki/trunk/database-wiki
  ant compile
```

3.  Create a PostgreSQL user (e.g. `dbwiki`) and a database owned by that user (e.g. also `dbwiki`).  Give the user a password.  Make sure the server is running and the password is correct before proceeding.  If you want to use a remote database then you may need to ask its administrator to do this for you, if not, consult the PostgreSQL documentation.  (MySQL should also work, but is not yet tested/documented).

4.  Create config and initial user list files:
```
  cd resources/configuration/server
  cp config.sample config
  cp users.sample users
```
Edit `config` so that `JDBC_USER` is the username (e.g. dbwiki) and
`JDBC_PASSWORD` is that user's password.  If the PostgreSQL server
you want to use is remote, then change `JDBC_URL` to point to the
appropriate host instead of `localhost`.

5.  Change back to the root of the source tree and initialize the
> database:
```
  cd ../../..
  sh create-server.sh
```

This should succeed with no output if everything is configured correctly.  You can confirm this by checking that the three `wiki_server_*` tables have been added to the `dbwiki` database.

6.  The exciting part: Start the server!
```
  sh start-server.sh
```
> This will not work if the PostgreSQL user/password settings are incorrect, or if the core server tables weren't created successfully.

It may also fail if port 8080 is already used for something else on your machine.  If so, change `PORT` in `config` to an unused port, e.g. 8081.

On success, start-server.sh should print out something like:
```
START SERVER ON ADDRESS /0.0.0.0:8080 AT Wed Feb 23 13:20:25 GMT 2011
```

7.  Test that the server is running correctly by visiting
```
  http://localhost:8080
```
using Firefox (other web browsers may work but have sometimes
had incompatibilities).

8.  Once you've confirmed that the server is running correctly, you need to load some data.  See LoadingData.

=== Windows command line:

If you want to do this, you should be able to figure out how from the instructions above.