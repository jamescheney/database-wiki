#!/bin/sh
java -mx512M -classpath ./src:./bin:./lib/xercesImpl.jar:./lib/postgresql-8.4-701.jdbc4.jar:./lib/asm-all-3.3.1.jar:./lib/parboiled-core-0.10.0.jar:./lib/parboiled-java-0.10.0.jar org.dbwiki.main.DropServer resources/configuration/server/config
