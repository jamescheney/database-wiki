The synchronization tool can be run in two ways. 
1. Java class:
   SynchronizeDatabaseWiki <config-file> <synchronize config-file>
   The first argument is a server configuration file. The second argument is a synchronization configuration file.
   
   Here is an example of <synchronize config-file>:
   remoteURL=http://172.20.96.71:8080/
   database=CIAWFB
   remoteEntry=31A5
   localEntry=31A5
   remoteAdded=true
   remoteChanged=true
   remoteDeleted=true
   deletedChanged=true
   changedDeleted=true
   changedChanged=true
   
2. Web page:
   On each displaying page of an entry, there is a "synchronize with..." menu that can be used to browse the synchronization page. Only the host address of remote DBWiki and a list of selections are required on this page.
   