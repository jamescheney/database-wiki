#!/bin/sh
echo "Initialising local database..."
java -mx512M -classpath ./src:./war/WEB-INF/classes:./war/WEB-INF/lib/xercesImpl.jar:./war/WEB-INF/lib/parboiled4j-0.9.9.0.jar:./war/WEB-INF/lib/mysql-connector-java-5.1.13-bin.jar org.dbwiki.main.CreateServer ./war/resources/configuration/server/config.local ./war/resources/configuration/server/users
