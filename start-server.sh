#!/bin/sh
java -mx512M -classpath ./src:./bin:./lib/xercesImpl.jar:./lib/postgresql-8.4-701.jdbc4.jar:./lib/parboiled4j-0.9.9.0.jar org.dbwiki.main.StartServer resources/configuration/server/config
