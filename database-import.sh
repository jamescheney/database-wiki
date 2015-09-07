#!/bin/sh
echo "java -mx512M -classpath ./src:./bin:./lib/xercesImpl.jar:./lib/postgresql-8.4-701.jdbc4.jar:./lib/parboiled4j-0.9.9.0.jar org.dbwiki.main.DatabaseImport ./resources/configuration/server/config $1 $2 $3 $4 $5"
java -mx512M -classpath ./src:./bin:./lib/xercesImpl.jar:./lib/postgresql-8.4-701.jdbc4.jar:./lib/asm-all-3.3.1.jar:./lib/parboiled-core-0.10.0.jar:./lib/parboiled-java-0.10.0.jar org.dbwiki.main.DatabaseImport ./resources/configuration/server/config $1 $2 $3 $4 $5
