#!/bin/sh
echo "java -mx512M -classpath ./src:./bin:./lib/xercesImpl.jar:./lib/postgresql-8.4-701.jdbc4.jar:./lib/parboiled4j-0.9.9.0.jar org.dbwiki.main.DatabasePackageExport ./resources/configuration/server/config $1 $2 $3 $4 $5 $6 $7 $8 $9 "
java -mx512M -classpath ./src:./bin:./lib/xercesImpl.jar:./lib/postgresql-8.4-701.jdbc4.jar:./lib/parboiled4j-0.9.9.0.jar org.dbwiki.main.DatabasePackageExport ./resources/configuration/server/config $1 $2 $3 $4 $5 $6 $7 $8 $9 
