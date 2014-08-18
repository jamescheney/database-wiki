#!/bin/sh
java -mx512M -classpath ./src:./war/WEB-INF/classes:./war/WEB-INF/lib/xercesImpl.jar:./war/WEB-INF/lib/parboiled4j-0.9.9.0.jar:./war/WEB-INF/lib/mysql-connector-java-5.1.13-bin.jar:./war/WEB-INF/lib/postgresql-8.4-701.jdbc4.jar org.dbwiki.main.ImportPresentationFiles ./war/resources/configuration/server/config.local $1 $2 $3
