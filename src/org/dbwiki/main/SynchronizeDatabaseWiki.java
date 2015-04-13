package org.dbwiki.main;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import org.dbwiki.data.database.Database;
import org.dbwiki.data.database.DatabaseAttributeNode;
import org.dbwiki.data.database.DatabaseElementNode;
import org.dbwiki.data.database.DatabaseGroupNode;
import org.dbwiki.data.database.DatabaseNode;
import org.dbwiki.data.database.DatabaseTextNode;
import org.dbwiki.data.database.NodeUpdate;
import org.dbwiki.data.database.Update;
import org.dbwiki.data.io.SAXCallbackInputHandler;
import org.dbwiki.data.io.SynchronizationInputHandler;
import org.dbwiki.data.resource.DatabaseIdentifier;
import org.dbwiki.data.resource.NodeIdentifier;
import org.dbwiki.driver.rdbms.DatabaseConstants;
import org.dbwiki.driver.rdbms.RDBMSDatabase;
import org.dbwiki.driver.rdbms.RDBMSDatabaseAttributeNode;
import org.dbwiki.driver.rdbms.RDBMSDatabaseGroupNode;
import org.dbwiki.driver.rdbms.RDBMSDatabaseListing;
import org.dbwiki.driver.rdbms.RDBMSDatabaseTextNode;
import org.dbwiki.exception.WikiException;
import org.dbwiki.exception.WikiFatalException;
import org.dbwiki.user.User;
import org.dbwiki.web.log.FileServerLog;
import org.dbwiki.web.log.ServerLog;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.server.DatabaseWiki;
import org.dbwiki.web.server.WikiServer;
import org.dbwiki.web.ui.printer.data.ConflictResolutionFormPrinter;
import org.xml.sax.SAXException;

public class SynchronizeDatabaseWiki {

    /*
     * Constants
     */

    DatabaseWiki wiki;
    User user;
    boolean isRootRequest = false;
    boolean remoteAdded;
    boolean remoteDeleted;
    boolean remoteChanged;
    boolean localAdded;
    boolean localDeleted;
    boolean localChanged;
    boolean changedChanged;
    boolean deletedChanged;
    boolean changedDeleted;
    boolean addedAdded;
    HashMap<Integer, Integer> idMap = new HashMap<Integer, Integer>();
    StringBuffer syncReport;

    //data structures storing the differences and conflicts after synchronization
    List<ConflictPair> localAddedNodes = new ArrayList<ConflictPair>();
    List<DatabaseNode> localDeletedNodes = new ArrayList<DatabaseNode>();
    List<NodePair> localChangedNodes = new ArrayList<NodePair>();
    List<ConflictPair> remoteAddedNodes = new ArrayList<ConflictPair>();
    List<DatabaseNode> remoteDeletedNodes = new ArrayList<DatabaseNode>();
    List<NodePair> remoteChangedNodes = new ArrayList<NodePair>();
    List<NodePair> changedChangedNodes = new ArrayList<NodePair>();
    List<DeleteandChange> deletedChangedNodes = new ArrayList<DeleteandChange>();
    List<DeleteandChange> changedDeletedNodes = new ArrayList<DeleteandChange>();
    List<NodePair> differentNodes = new ArrayList<NodePair>();

    //node matching list
    List<NodePair> nodeMatch = new ArrayList<NodePair>();

    //version numbers of local and remote entries in last synchronization
    int localPreviousVersionNumber;
    int remotePreviousVersionNumber;

    public static String SyncInfoRemoteURL = "remoteURL";
    public static String SyncInfoDatabaseName = "database";
    public static String SyncInfoRemoteEntry = "remoteEntry";
    public static String SyncInfoLocalEntry = "localEntry";
    public static String SyncInfoRemoteAdded = "remoteAdded";
    public static String SyncInfoRemoteChanged = "remoteChanged";
    public static String SyncInfoRemoteDeleted = "remoteDeleted";
    public static String SyncInfoDeletedChanged = "deletedChanged";
    public static String SyncInfoChangedDeleted = "changedDeleted";
    public static String SyncInfoChangedChanged = "changedChanged";
    public static String SyncInfoAddedAdded = "addedAdded";
    public static String SyncInfoLocalAdded = "localAdded";
    public static String SyncInfoLocalChanged = "localChanged";
    public static String SyncInfoLocalDeleted = "localDeleted";

    //get the id of the remote node that is mapped to the local node with id localID
    int getRemoteMapID(int localID){
        if(idMap.containsKey(localID)){
            return idMap.get(localID);
        }
        else{
            return localID;
        }
    }

    //get the id of the local node that is mapped to the remote node with id remoteID
    int getLocalMapID(int remoteID){
        for(Entry<Integer, Integer> entry: idMap.entrySet()){
            if(entry.getValue() == remoteID){
                return entry.getKey();
            }
        }
        return remoteID;
    }

    //given a local group node and a remote group node, return a list of matched nodes
    List<NodePair> match(RDBMSDatabaseGroupNode localNode, RDBMSDatabaseGroupNode remoteNode) throws WikiException{
        List<NodePair> tempNodeMatch = new ArrayList<NodePair>();
        int remoteChildNum = 0;
        if(localNode != null && remoteNode != null){
            if(localNode.isElement() && remoteNode.isElement()){
                if(((DatabaseElementNode)localNode).isGroup() && ((DatabaseElementNode)remoteNode).isGroup()){
                    RDBMSDatabaseGroupNode localGroupNode = (RDBMSDatabaseGroupNode)localNode;
                    RDBMSDatabaseGroupNode remoteGroupNode = (RDBMSDatabaseGroupNode)remoteNode;
                    for(int i = 0; i < localGroupNode.children().size(); i++){
                        DatabaseElementNode localChild = localGroupNode.children().get(i);
                        if(localChild.getTimestamp().firstValue() > localPreviousVersionNumber){
                            localAddedNodes.add(new ConflictPair(remoteGroupNode.identifier().nodeID(), localChild));
                        }
                        else{
                            DatabaseElementNode remoteChild = null;
                            int localChildId = localChild.identifier().nodeID();
                            if(idMap.containsKey(localChildId)){
                                remoteChild = remoteGroupNode.findChild(new NodeIdentifier(idMap.get(localChildId)));
                            }
                            else{
                                remoteChild = remoteGroupNode.findChild(localChild.identifier());
                            }
                            if(remoteChild != null){
                                remoteChildNum++;
                                tempNodeMatch.add(new NodePair(localChild, remoteChild));
                            }
                            else{
                                if(localChild.getTimestamp().firstValue() > localPreviousVersionNumber){
                                    localAddedNodes.add(new ConflictPair(remoteGroupNode.identifier().nodeID(), localChild));
                                }
                                else{
                                    List<DatabaseTextNode> changes = this.isChanged(localChild, localPreviousVersionNumber);
                                    if(changes.isEmpty()){
                                        remoteDeletedNodes.add(localChild);
                                    }
                                    else{
                                        changedDeletedNodes.add(new DeleteandChange(new NodePair(localChild, remoteChild), changes));
                                    }
                                }
                            }
                        }

                    }
                    if(remoteChildNum < remoteGroupNode.children().size()){
                        // we have to look in the entire list of children because they follow the order in the xml received
                        // e.g. if we have a PROPERTY group node that contains a NOTE node, and we add a SUBPROP node, the new node
                        // will not be detected by  simply checking the last element of the children list,
                        // because the last element will be the NOTE node.
                        for(int i = 0; i < remoteGroupNode.children().size(); i++){
                            if(remoteGroupNode.children().get(i).getTimestamp().firstValue() > remotePreviousVersionNumber){
                                remoteAddedNodes.add(new ConflictPair(localGroupNode.identifier().nodeID(), remoteGroupNode.children().get(i)));
                            }
                        }
                    }
                }
            }
        }
        else{
            if(remoteNode == null && localNode != null){
                if(localNode.getTimestamp().firstValue() > localPreviousVersionNumber){
                    localAddedNodes.add(new ConflictPair(new NodeIdentifier(localNode.getparent()).nodeID(), localNode));
                }
                else{
                    List<DatabaseTextNode> changes = this.isChanged(localNode, localPreviousVersionNumber);
                    if(changes.isEmpty()){
                        remoteDeletedNodes.add(localNode);
                    }
                    else{
                        changedDeletedNodes.add(new DeleteandChange(new NodePair(localNode, remoteNode), changes));
                    }
                }
            }
        }
        nodeMatch.addAll(tempNodeMatch);
        return tempNodeMatch;
    }

    //compare two DatabaseNode
    void compare(DatabaseNode localNode, DatabaseNode remoteNode) throws WikiException{
        // TODO: investigate previous version numbers being set
        if(localNode != null && remoteNode != null){
            if(localNode.getTimestamp().isCurrent()){
                if(localNode.isElement() && remoteNode.isElement()){
                    if(((DatabaseElementNode)localNode).isGroup() && ((DatabaseElementNode)remoteNode).isGroup()){
                        this.compareGroupNodes((RDBMSDatabaseGroupNode)localNode, (RDBMSDatabaseGroupNode)remoteNode);
                    }
                    else if(((DatabaseElementNode)localNode).isAttribute() && ((DatabaseElementNode)remoteNode).isAttribute()){
                        this.compareAttributeNodes((RDBMSDatabaseAttributeNode)localNode, (RDBMSDatabaseAttributeNode)remoteNode);
                    }
                }
                else if(localNode.isText() && remoteNode.isText()){
                    this.compareTextNodes((RDBMSDatabaseTextNode)localNode, (RDBMSDatabaseTextNode)remoteNode);
                }
                else{
                    throw new WikiFatalException("Nodes compared are not of the same type");
                }
            }
            else{
                List<DatabaseTextNode> changes = this.isChanged(remoteNode, remotePreviousVersionNumber);
                if(changes.isEmpty()){
                    localDeletedNodes.add(localNode);
                }
                else{
                    deletedChangedNodes.add(new DeleteandChange(new NodePair(localNode, remoteNode), changes));
                }
            }
        }
        else if(localNode != null && remoteNode == null){
            int localParentID ;
            int remoteParentID = -1;
            if(localNode.parent() != null){
                localParentID = ((NodeIdentifier)localNode.parent().identifier()).nodeID();
                remoteParentID = this.getRemoteMapID(localParentID);
            }
            if(localNode.getTimestamp().firstValue() > localPreviousVersionNumber){
                localAddedNodes.add(new ConflictPair(remoteParentID, localNode));
            }
            else{
                if(localNode.getTimestamp().changedSince(localPreviousVersionNumber)){
                    localAddedNodes.add(new ConflictPair(remoteParentID, localNode));
                }
                else{
                    List<DatabaseTextNode> changes = this.isChanged(localNode, localPreviousVersionNumber);
                    if(changes.isEmpty()){
                        remoteDeletedNodes.add(localNode);
                    }
                    else{
                        changedDeletedNodes.add(new DeleteandChange(new NodePair(localNode, remoteNode), changes));
                    }
                }
            }
        }
        else if(localNode == null && remoteNode != null){
            if(remoteNode.getTimestamp().firstValue() > remotePreviousVersionNumber){
                int localParentID = -1;
                if(remoteNode.parent() != null){
                    localParentID = this.getLocalMapID(remoteNode.parent().identifier().nodeID());
                }
                remoteAddedNodes.add(new ConflictPair(localParentID, (DatabaseElementNode)remoteNode));
            } else {
                // Local deleted nodes should be recorded. But for fear of naming conflicts with
                // existing local nodes, we will not to this.
            }
        }
    }

    //compare two text nodes
    void compareTextNodes(RDBMSDatabaseTextNode localTextNode, RDBMSDatabaseTextNode remoteTextNode){
        if(!localTextNode.value().equals(remoteTextNode.value())){
            boolean localChanged = localTextNode.getTimestamp().changedSince(localPreviousVersionNumber);
            boolean remoteChanged = remoteTextNode.getTimestamp().changedSince(remotePreviousVersionNumber);
            if(localChanged && !remoteChanged){
                localChangedNodes.add(new NodePair(localTextNode, remoteTextNode));
            }
            else if(!localChanged && remoteChanged){
                remoteChangedNodes.add(new NodePair(localTextNode, remoteTextNode));
            }
            else if(localChanged && remoteChanged){
                changedChangedNodes.add(new NodePair(localTextNode, remoteTextNode));
            }
            else{
                differentNodes.add(new NodePair(localTextNode, remoteTextNode));
            }
        }
    }

    //compare two attribute nodes
    void compareAttributeNodes(RDBMSDatabaseAttributeNode localAttributeNode, RDBMSDatabaseAttributeNode remoteAttributeNode) throws WikiException{
        RDBMSDatabaseTextNode lt = (RDBMSDatabaseTextNode)localAttributeNode.value().getMostRecent();
        RDBMSDatabaseTextNode rt = (RDBMSDatabaseTextNode)remoteAttributeNode.value().getMostRecent();
        this.compare(lt, rt);
    }

    //compare two group nodes
    void compareGroupNodes(RDBMSDatabaseGroupNode localGroupNode, RDBMSDatabaseGroupNode remoteGroupNode) throws WikiException{
        List<NodePair> nodePairs = this.match(localGroupNode, remoteGroupNode);
        for(NodePair np: nodePairs){
            this.compare(np.get_localNode(), np.get_remoteNode());
        }
    }

    public SynchronizeDatabaseWiki(){}

    public SynchronizeDatabaseWiki(DatabaseWiki wiki, User user){
        this.wiki = wiki;
        this.user = user;
    }

    //the interface that responses to the synchronization request from the command line
    public void responseToSynchronizeRequest(File configFile, File syncFile) throws WikiException{
        WikiServer server;
        try {
            Properties syncProperties = org.dbwiki.lib.IO.loadProperties(syncFile);
            Properties properties = org.dbwiki.lib.IO.loadProperties(configFile);
            String remoteURL = syncProperties.getProperty(SyncInfoRemoteURL);
            String database = syncProperties.getProperty(SyncInfoDatabaseName);
            String remoteCollection = syncProperties.getProperty(SyncInfoRemoteEntry);
            String localCollection = syncProperties.getProperty(SyncInfoLocalEntry);
            this.remoteAdded = Boolean.parseBoolean(syncProperties.getProperty(SyncInfoRemoteAdded));
            this.remoteChanged = Boolean.parseBoolean(syncProperties.getProperty(SyncInfoRemoteChanged));
            this.remoteDeleted = Boolean.parseBoolean(syncProperties.getProperty(SyncInfoRemoteDeleted));
            this.deletedChanged = Boolean.parseBoolean(syncProperties.getProperty(SyncInfoDeletedChanged));
            this.changedDeleted = Boolean.parseBoolean(syncProperties.getProperty(SyncInfoChangedDeleted));
            this.changedChanged = Boolean.parseBoolean(syncProperties.getProperty(SyncInfoChangedChanged));
            this.addedAdded = Boolean.parseBoolean(syncProperties.getProperty(SyncInfoAddedAdded, "false"));
            this.localAdded = Boolean.parseBoolean(syncProperties.getProperty(SyncInfoLocalAdded, "false"));
            this.localChanged = Boolean.parseBoolean(syncProperties.getProperty(SyncInfoLocalChanged, "false"));
            this.localDeleted = Boolean.parseBoolean(syncProperties.getProperty(SyncInfoLocalDeleted, "false"));


            String url = remoteURL + database + DatabaseIdentifier.PathSeparator;
            server = new WikiServer(properties);
            wiki = server.get(database);
            int localID = 0;
            if(remoteCollection == null && localCollection == null){
                isRootRequest = true;
            }
            else{
                localID = Integer.parseInt(localCollection, 16);
            }
            this.responseToSynchronizeRequest(url, localID, isRootRequest);
        } catch (WikiException e) {
            throw new WikiFatalException(e);
        } catch (MalformedURLException e) {
            throw new WikiFatalException(e);
        } catch (IOException e) {
            throw new WikiFatalException(e);
        }
    }

    //the interface that responses to the synchronization request from the web page
    public void responseToSynchronizeRequest(String url, int localID, boolean isRootRequest) throws WikiException{
        responseToSynchronizeRequest(url, localID, isRootRequest, RequestParameter.ParameterSynchronizeExport, null);
    }

    //the interface that responses to the synchronization request from the web page
    public String responseToSynchronizeRequest(String url, int localID, boolean isRootRequest,
            String xmlRequestType, String port) throws WikiException {
        syncReport = new StringBuffer();
        try {
            //read the id maps from the alignment log file
            File alignFile = new File("align_log");
            if(alignFile.exists()){
                BufferedReader br = new BufferedReader(new FileReader(alignFile));
                String tmp = br.readLine();
                while(tmp != null && tmp.contains(",")){
                    idMap.put(Integer.valueOf(tmp.split(",")[0]), Integer.valueOf(tmp.split(",")[1]));
                    tmp = br.readLine();
                }
                br.close();
            }

            //request and parse the remote entry/entries
            this.isRootRequest = isRootRequest;
            String sourceURL;
            if (url.contains("?")) {
                sourceURL = url.substring(0, url.lastIndexOf("?"));
            } else {
                sourceURL = url;
            }
            if(!isRootRequest){
                if(idMap.containsKey(localID)){
                    sourceURL += Integer.toHexString(idMap.get(localID));
                }
                else{
                    sourceURL += Integer.toHexString(localID);
                }
            }
            if (url.contains("?")) {
                sourceURL += url.substring(url.lastIndexOf("?"));
            }

            //get the version information when the synchronization happens
            int new_remoteVersion = 0;
            int new_localVersion = 0;

            //obtain version information of last synchronization from the version log file
            //if the version log file does not exist or is empty, then the version of last synchronization is set to be -1
            File syncFile = new File("synchronize_log");
            if(!syncFile.exists()){
                syncFile.createNewFile();
            }

            extractVersionNumbers(syncFile);
            //compare entries from two DBWiki instances that is to be synchronized
            SynchronizationInputHandler ioHandler = new SynchronizationInputHandler();
            SAXCallbackInputHandler saxCallbackInputHandler = new SAXCallbackInputHandler(ioHandler, false);
            if(!isRootRequest){
                sourceURL = invertAndAddParameters(sourceURL, xmlRequestType, port);
                ioHandler.setIsRootRequest(isRootRequest);
                InputStream inputStreamFromRemote = new URL(sourceURL).openStream();
                saxCallbackInputHandler.parse(inputStreamFromRemote, false, false);

                //get the version information when the synchronization happens
                //ioHandler;
                new_remoteVersion = ioHandler.getVersionNumber();
                new_localVersion = getDatabase().versionIndex().getLastVersion().number();
                DatabaseNode remoteNode = ioHandler.getSynchronizeDatabaseNode();
                DatabaseNode localNode = getDatabase().get(new NodeIdentifier(localID));
                this.compare(localNode, remoteNode);
            }
            else{
                //List<DatabaseNode> nodeList = ioHandler.getSynchronizeDatabaseNodeList();
                RDBMSDatabaseListing entries = ((RDBMSDatabase)getDatabase()).content();
                for(int i = 0; i < entries.size(); i++){
                    String newurl;
                    ioHandler.setIsRootRequest(false);

                    if(idMap.containsKey(entries.get(i).identifier().nodeID())){
                        newurl = sourceURL + Integer.toHexString(idMap.get(entries.get(i).identifier().nodeID()));
                    }
                    else{
                        newurl = sourceURL + Integer.toHexString(entries.get(i).identifier().nodeID());
                    }
                    newurl = invertAndAddParameters(newurl, xmlRequestType, port);
                    InputStream inputStreamFromRemote = new URL(newurl).openStream();
                    saxCallbackInputHandler.parse(inputStreamFromRemote, false, false);

                    new_remoteVersion = ioHandler.getVersionNumber();
                    new_localVersion = getDatabase().versionIndex().getLastVersion().number();
                    DatabaseNode localNode = getDatabase().get(entries.get(i).identifier());
                    DatabaseNode remoteNode = ioHandler.getSynchronizeDatabaseNode();
                    this.compare(localNode, remoteNode);
                }
            }
            if (xmlRequestType == RequestParameter.ParameterSynchronizeThenExport) {
                syncReport = cleanRemoteServerReport(saxCallbackInputHandler.getReport());

            }
            // reconcile the differences and conflicts
            this.handleDifferences();
//            this.handleConflicts();
            this.reportConflicts();
            //System.out.println("reconcile time: " + (System.currentTimeMillis()-begin));

            // write new version information of this synchronization into the version log file
            ServerLog versionLog = new FileServerLog(syncFile);
            versionLog.openLog();
            versionLog.writeln("LOCALVERSION=" + new_localVersion);
            versionLog.writeln("REMOTEVERSION=" + new_remoteVersion);
            versionLog.closeLog();

            // write new id map information into the alignment log file
            ServerLog matchLog = new FileServerLog(alignFile);
            matchLog.openLog();
            for(Entry<Integer, Integer> entry: idMap.entrySet()){
                matchLog.writeln(entry.getKey() + "," + entry.getValue());
            }
            matchLog.closeLog();
            // Write report log to file
            File reportFile = new File(String.format("%d_sync_log_v%d.txt", localID, new_localVersion));
            if(!reportFile.exists()){
                reportFile.createNewFile();
            }
            ServerLog servLog = new FileServerLog(reportFile);
            servLog.openLog();
            servLog.writeln(syncReport.toString());
            servLog.closeLog();
            return syncReport.toString();
        } catch (WikiException e) {
            // TODO Auto-generated catch block
            throw new WikiFatalException(e);
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            throw new WikiFatalException(e);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            throw new WikiFatalException(e);
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            throw new WikiFatalException(e);
        }
    }

    /**
     * Takes one-sided report arrived from remote-server, updates it's labels and swaps
     * node IDs of the updates described. This is done to maintain consistency with the
     * expected format of naming nodes in the sync reports(i.e. localID + "-" + remoteID)
     */
    static StringBuffer cleanRemoteServerReport(String report) {
        if(report.length() > 1 && report.charAt(0) == '\n') {
            report = report.substring(1);
        }
        StringBuffer cleanReport = new StringBuffer();
        report = report.replaceFirst("LOCAL-ADDED", "REMOTE-ADDED")
                .replaceFirst("LOCAL-CHANGED", "REMOTE-CHANGED")
                .replaceFirst("LOCAL-DELETED", "REMOTE-DELETED");
        String[] lines = report.split("\n");
        // we only care about differences handled
        for (int j = 0; j < Math.min(3, lines.length); j++) {
            String[] idPairs = lines[j].split("#");
            if(idPairs[0].equals("ADDED-ADDED")) {
                // Added-added nodes will be identified again locally.
                continue;
            }
            cleanReport.append(idPairs[0]);	// line tag
            for (int i = 1; i < idPairs.length; i++) {
                String[] ids = idPairs[i].split("-");
                cleanReport.append('#').append(ids[1]).append('-').append(ids[0]);
            }
            cleanReport.append('\n');
        }
        return cleanReport.deleteCharAt(cleanReport.length() - 1);
    }

    String addLocalPortParameter(String sourceURL, String port) {
        try {
            sourceURL += "&localport=" + URLEncoder.encode(port, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return sourceURL;
    }

    /**
     * @param syncFile
     * @throws IOException
     */
    void extractVersionNumbers(File syncFile) throws IOException {
        Properties pros = org.dbwiki.lib.IO.loadProperties(syncFile);
        String lv = pros.getProperty("LOCALVERSION");
        String rv = pros.getProperty("REMOTEVERSION");

        if(lv != null){
            localPreviousVersionNumber = Integer.parseInt(lv);
            if(localPreviousVersionNumber < 2){
                localPreviousVersionNumber = 2;
            }
        }
        else{
            localPreviousVersionNumber = 2;
        }
        if(rv != null){
            remotePreviousVersionNumber = Integer.parseInt(rv);
            if(remotePreviousVersionNumber < 2){
                remotePreviousVersionNumber = 2;
            }
        }
        else{
            remotePreviousVersionNumber = 2;
        }
    }

    String invertAndAddParameters(String sourceURL, String xmlRequestType, String port) {
        if (xmlRequestType.equals(RequestParameter.ParameterSynchronizeThenExport2)) {
            sourceURL += "&";
        } else {
            sourceURL += "?";
        }
        sourceURL += xmlRequestType;
        System.out.print(sourceURL + "->");
        if (xmlRequestType == RequestParameter.ParameterSynchronizeExport) {
            return sourceURL;
        } else {
            sourceURL = addLocalPortParameter(sourceURL, port);
        }
        if (xmlRequestType == RequestParameter.ParameterSynchronizeThenExport) {
            sourceURL += String.format("&%s=%b", RequestParameter.parameterRemoteAdded, !this.remoteAdded);
            sourceURL += String.format("&%s=%b", RequestParameter.parameterRemoteChanged, !this.remoteChanged);
            sourceURL += String.format("&%s=%b", RequestParameter.parameterRemoteDeleted, !this.remoteDeleted);
        }
        if (xmlRequestType == RequestParameter.ParameterSynchronizeThenExport1) {
            sourceURL += "&" + RequestParameter.parameterRemoteAdded + "=" + true;
            sourceURL += "&" + RequestParameter.parameterRemoteChanged + "=" + true;
            sourceURL += "&" + RequestParameter.parameterRemoteDeleted + "=" + true;
            sourceURL += "&" + RequestParameter.parameterAddedAdded + "=" + this.addedAdded;
        }
        if (xmlRequestType == RequestParameter.ParameterSynchronizeThenExport2) {
            sourceURL += "&" + RequestParameter.parameterRemoteAdded + "=" + this.localAdded;
            sourceURL += "&" + RequestParameter.parameterRemoteChanged + "=" + this.localChanged;
            sourceURL += "&" + RequestParameter.parameterRemoteDeleted + "=" + this.localDeleted;
            sourceURL += "&" + RequestParameter.parameterAddedAdded + "=" + this.addedAdded;
            return sourceURL;
        }
        sourceURL += "&" + RequestParameter.parameterchangedChanged + "=" + !this.changedChanged;
        sourceURL += "&" + RequestParameter.parameterchangedDeleted + "=" + !this.changedDeleted;
        sourceURL += "&" + RequestParameter.parameterdeletedChanged + "=" + !this.deletedChanged;
        System.out.println(sourceURL);
        return sourceURL;
    }

    //handle the conflicts in the synchronization results
    void handleConflicts() throws WikiException{
        if(!changedChangedNodes.isEmpty() && changedChanged){
            for(NodePair nodePair: changedChangedNodes){
                Update update = new Update();
                NodeUpdate nodeupdate = new NodeUpdate(((RDBMSDatabaseTextNode)nodePair.get_localNode()).identifier(), ((RDBMSDatabaseTextNode)nodePair._remoteNode).value());
                update.add(nodeupdate);
                    this.getDatabase().update(nodePair.get_localNode().identifier(), update, user);
            }
        }
        if(!deletedChangedNodes.isEmpty() && deletedChanged){
            for(DeleteandChange pair: deletedChangedNodes){
                if(pair.getList() == null){
                    getDatabase().activate(pair.getDeletedChangedNode().get_localNode().identifier(), user);
                    Update update = new Update();
                    NodeUpdate nodeupdate = new NodeUpdate(pair.getDeletedChangedNode().get_localNode().identifier(), ((RDBMSDatabaseTextNode)pair.getDeletedChangedNode().get_remoteNode()).getValue());
                    update.add(nodeupdate);
                    getDatabase().update(pair.getDeletedChangedNode().get_localNode().identifier(), update, user);
                }
                else{
                    getDatabase().activate(pair.getDeletedChangedNode().get_localNode().identifier(), user);
                    for(int i = 0; i < pair.getList().size(); i++){
                        Update update = new Update();
                        NodeUpdate nodeupdate = new NodeUpdate(new NodeIdentifier(idMap.get(((NodeIdentifier)pair.getList().get(i).identifier()).nodeID())), pair.getList().get(i).getValue());
                        update.add(nodeupdate);
                        getDatabase().update(new NodeIdentifier(idMap.get(((NodeIdentifier)pair.getList().get(i).identifier()).nodeID())), update, user);
                    }
                }

            }
        }
        if(!changedDeletedNodes.isEmpty() && changedDeleted){
            for(DeleteandChange pair: deletedChangedNodes){
                getDatabase().delete(pair.getDeletedChangedNode().get_localNode().identifier(), user);
            }
        }
    }

    void reportConflicts() {
        syncReport.append("\nCHANGED-CHANGED");
        for (NodePair pair : changedChangedNodes) {
            syncReport.append(String.format("#%s-%s", ((NodeIdentifier) pair.get_localNode().identifier()).nodeID(),
                    ((NodeIdentifier) pair.get_remoteNode().identifier()).nodeID()));
        }
        syncReport.append("\nCHANGED-DELETED");
        for (DeleteandChange cdPair : changedDeletedNodes) {
            NodePair pair = cdPair.deletedChangedNode;
            syncReport.append(String.format("#%s-%s", ((NodeIdentifier) pair.get_localNode().identifier()).nodeID(),
                    ((NodeIdentifier) pair.get_remoteNode().identifier()).nodeID()));
        }
        syncReport.append("\nDELETED-CHANGED");
        for (DeleteandChange dcPair : deletedChangedNodes) {
            NodePair pair = dcPair.deletedChangedNode;
            syncReport.append(String.format("#%s-%s", ((NodeIdentifier) pair.get_localNode().identifier()).nodeID(),
                    ((NodeIdentifier) pair.get_remoteNode().identifier()).nodeID()));
        }
    }

    //handle the differences in the synchronization results
    void handleDifferences() throws WikiException{
        System.out.println("Number of remote added nodes: " + remoteAddedNodes.size());
        System.out.println("Number of local added nodes: " + localAddedNodes.size());
        StringBuffer addedAddedReportBuffer = new StringBuffer();
        syncReport.append("\nLOCAL-ADDED");
        if (remoteAddedNodes.isEmpty()) {
            // do nothing
        } else  if (!remoteAdded) {
            syncReport.append(" -- you chose not to copy new nodes from the remote server");
        } else if(!remoteAddedNodes.isEmpty() && remoteAdded){
            for(ConflictPair pair: remoteAddedNodes){
                boolean skip = false;
                String localEquvalentId = null;
                int insertNodeID;
                // Checking if remote added node already exists.
                for(ConflictPair localPair: localAddedNodes) {
                    DatabaseNode local = localPair.getExistNode();
                    DatabaseNode remote = pair.getExistNode();
                    if (sameParent(local, remote)
                            && local.annotation().sameTexts(remote.annotation())) {
                        if (local.isElement() == remote.isElement()
                                ) {
                            DatabaseElementNode localElement = (DatabaseElementNode) local;
                            DatabaseElementNode remoteElement = (DatabaseElementNode) remote;
                            if (localElement.isSimilarTo(remoteElement)) {
                                skip = true;
                                localEquvalentId = localElement.identifier().toParameterString();
                                if(!local.identifier().equals(remote.identifier())) {
                                    this.map(local, remote);
                                }
                            }
                        }else if (local.isText() && remote.isText()) {
                            // Can be attribute node
                            DatabaseTextNode localText = (DatabaseTextNode) local;
                            DatabaseTextNode remoteText = (DatabaseTextNode) remote;
                            if (localText.getValue().equals(remoteText.getValue())) {
                                skip = true;
                                localEquvalentId = localText.identifier().toParameterString();
                                if(!local.identifier().equals(remote.identifier())) {
                                    this.map(local, remote);
                                }
                            }
                        }
                    }
                }
                /*********/
                if (skip) {
                    System.out.println("Not adding node " + pair.getNodeID());
                    addedAddedReportBuffer.append(String.format("#%s-%s", localEquvalentId, pair.getNodeID()));
                } else if(pair.getExistNode().isElement()){
                    if(isRootRequest){
                        if(pair.getNodeID() == -1){
                            insertNodeID = ((NodeIdentifier)getDatabase().insertNode(new NodeIdentifier(), ((DatabaseElementNode)pair.getExistNode()).toDocumentNode(), user)).nodeID();
                        }
                        else{
                            insertNodeID = ((NodeIdentifier)getDatabase().insertNode(new NodeIdentifier(pair.getNodeID()), ((DatabaseElementNode)pair.getExistNode()).toDocumentNode(), user)).nodeID();
                        }
                    }
                    else{
                        insertNodeID = ((NodeIdentifier)getDatabase().insertNode(new NodeIdentifier(pair.getNodeID()),
                                ((DatabaseElementNode)pair.getExistNode()).toDocumentNode(), user)).nodeID();
                    }
                    DatabaseNode insertedNode = getDatabase().get(new NodeIdentifier(insertNodeID));
                    this.map(insertedNode, pair.getExistNode());
                    syncReport.append(String.format("#%s-%s", insertNodeID,
                            pair.getNodeID()));
                }
            }
        }
        syncReport.append("\nLOCAL-CHANGED");
        if (!remoteChanged) {
            syncReport.append(" -- you chose to not update changed nodes");
        } else if (remoteChangedNodes.isEmpty()) {
            // do nothing
        } else if (!remoteChangedNodes.isEmpty() && remoteChanged){
            for(NodePair nodePair: remoteChangedNodes){
                Update update = new Update();
                NodeUpdate nodeupdate = new NodeUpdate(((RDBMSDatabaseTextNode)nodePair.get_localNode()).identifier(),
                        ((RDBMSDatabaseTextNode)nodePair.get_remoteNode()).value());
                update.add(nodeupdate);
                getDatabase().update(nodePair.get_localNode().identifier(), update, user);
                syncReport.append(String.format("#%s-%s",((NodeIdentifier) nodePair.get_localNode().identifier()).nodeID(),
                        ((NodeIdentifier) nodePair.get_remoteNode().identifier()).nodeID()));
            }
        }
        syncReport.append("\nLOCAL-DELETED");
        if (!remoteDeleted) {
            syncReport.append(" -- you chose to not remove remote deleted nodes");
        } else if (remoteDeletedNodes.isEmpty()) {
            // do nothing
        } else if (!remoteDeletedNodes.isEmpty() && remoteDeleted) {
            for(DatabaseNode node: remoteDeletedNodes){
                getDatabase().delete(node.identifier(), user);
                syncReport.append(String.format("#%s-%s",((NodeIdentifier) node.identifier()).nodeID(),
                        this.getRemoteMapID(((NodeIdentifier) node.identifier()).nodeID())));
            }
        }
        syncReport.append("\nADDED-ADDED" + addedAddedReportBuffer.toString());
        System.out.println("Reporting differences:\n" + syncReport.toString());
    }

    /**
     * Using a getter for the database. This allows to mock the database during testing.
     */
    Database getDatabase() {
        return wiki.database();
    }

    //map the localNode to the remoteNode
    void map(DatabaseNode localNode, DatabaseNode remoteNode){
        if(((NodeIdentifier)localNode.identifier()).nodeID() != ((NodeIdentifier)remoteNode.identifier()).nodeID()){
            idMap.put(((NodeIdentifier)localNode.identifier()).nodeID(), ((NodeIdentifier)remoteNode.identifier()).nodeID());
        }
        if(localNode.isElement() && remoteNode.isElement()){
            DatabaseElementNode localElement = (DatabaseElementNode)localNode;
            DatabaseElementNode remoteElement = (DatabaseElementNode)remoteNode;
            if(localElement.isGroup() && remoteElement.isGroup()){
                RDBMSDatabaseGroupNode lg = (RDBMSDatabaseGroupNode)localElement;
                RDBMSDatabaseGroupNode rg = (RDBMSDatabaseGroupNode)remoteElement;
                for(int i = 0; i < lg.children().size(); i++){
                    int lid = lg.children().get(i).identifier().nodeID();
                    int rid = rg.children().get(i).identifier().nodeID();
                    if(lid != rid){
                        idMap.put(lid, rid);
                    }
                    this.map(lg.children().get(i), rg.children().get(i));
                }
            }
            else{
                int lid = localElement.identifier().nodeID();
                int rid = remoteElement.identifier().nodeID();
                if(lid != rid){
                    idMap.put(lid, rid);
                }
                int ltid = ((NodeIdentifier)((RDBMSDatabaseAttributeNode)localElement).value().getCurrent().identifier()).nodeID();
                int rtid = ((NodeIdentifier)((RDBMSDatabaseAttributeNode)remoteElement).value().getCurrent().identifier()).nodeID();
                if(ltid != rtid){
                    idMap.put(ltid, rtid);
                }
            }
        }
    }

    //check whether the node is changed since the given version
    static List<DatabaseTextNode> isChanged(DatabaseNode node, int version){
        List<DatabaseTextNode> list = new ArrayList<DatabaseTextNode>();
        if(node.isElement()){
            DatabaseElementNode element = (DatabaseElementNode)node;
            if(element.isGroup()){
                DatabaseGroupNode group = (DatabaseGroupNode)element;
                for(int i = 0; i < group.children().size(); i++){
                    List<DatabaseTextNode> sublist = isChanged(group.children().get(i), version);
                    if(sublist != null && !sublist.isEmpty()){
                        list.addAll(sublist);
                    }
                }
            }
            else{
                DatabaseAttributeNode attribute = (DatabaseAttributeNode) element;
                DatabaseTextNode text = attribute.value().getCurrent();
                if(text != null){
                    if(text.getTimestamp().changedSince(version)){
                        list.add(text);
                    }
                }
            }
        }
        else{
            DatabaseTextNode text = (DatabaseTextNode)node;
            if(text.getTimestamp().changedSince(version)){
                list.add(text);
            }
        }
        return list;
    }

    public class NodePair{
        DatabaseNode _localNode;
        DatabaseNode _remoteNode;
        public NodePair(DatabaseNode localNode, DatabaseNode remoteNode){
            this._localNode = localNode;
            this._remoteNode = remoteNode;
        }
        public DatabaseNode get_localNode() {
            return _localNode;
        }
        public DatabaseNode get_remoteNode() {
            return _remoteNode;
        }

    }

    class ConflictPair{
        int nodeID;
        DatabaseNode existNode;
        public ConflictPair(int nodeID, DatabaseNode existNode){
            this.nodeID = nodeID;
            this.existNode = existNode;
        }
        public int getNodeID() {
            return nodeID;
        }
        public DatabaseNode getExistNode() {
            return existNode;
        }
    }

    class DeleteandChange{
        NodePair deletedChangedNode;
        List<DatabaseTextNode> list;
        public DeleteandChange(NodePair deletedChangedNode, List<DatabaseTextNode> list){
            this.deletedChangedNode = deletedChangedNode;
            this.list = list;
        }
        public NodePair getDeletedChangedNode() {
            return deletedChangedNode;
        }
        public List<DatabaseTextNode> getList() {
            return list;
        }
    }


    public int getRemotePreviousVersionNumber(){
        return remotePreviousVersionNumber;
    }

    public int getLocalPreviousVersionNumber(){
        return localPreviousVersionNumber;
    }

    public void setSynchronizeParameters(boolean remoteAdded, boolean remoteDeleted, boolean remoteChanged,
            boolean changedChanged, boolean deletedChanged, boolean changedDeleted, boolean addedAdded){
        setSynchronizeParameters(remoteAdded, remoteDeleted, remoteChanged, changedChanged, deletedChanged,
                changedDeleted, addedAdded, false, false, false);
    }

    public void setSynchronizeParameters(boolean remoteAdded, boolean remoteDeleted, boolean remoteChanged,
            boolean changedChanged, boolean deletedChanged, boolean changedDeleted, boolean addedAdded,
            boolean localAdded, boolean localChanged, boolean localDeleted){
        this.remoteAdded = remoteAdded;
        this.remoteDeleted = remoteDeleted;
        this.remoteChanged = remoteChanged;
        this.changedChanged = changedChanged;
        this.deletedChanged = deletedChanged;
        this.changedDeleted = changedDeleted;
        this.addedAdded = addedAdded;
        this.localChanged = localChanged;
        this.localAdded = localAdded;
        this.localDeleted = localDeleted;
    }

    public static void main(String args[]) throws org.dbwiki.exception.WikiException{
        String commandLine = "SynchronizeDatabaseWiki <config-file> <synchronize config-file>";
        if(args.length == 2){
            File configFile = new File(args[0]);
            File syncFile = new File(args[1]);
            if(configFile.exists() && syncFile.exists()){
                new SynchronizeDatabaseWiki().responseToSynchronizeRequest(configFile, syncFile);
            }
            else if(!configFile.exists()){
                System.out.println("File " + args[0] + " does not exist");
            }
            else{
                System.out.println("File " + args[1] + " does not exist");
            }
        }
        else {
            System.out.println(commandLine);
        }
    }

    boolean sameParent(DatabaseNode local, DatabaseNode remote) {
        if (!(local.getparent() == DatabaseConstants.RelDataColParentValUnknown)
                && !(remote.getparent() == DatabaseConstants.RelDataColParentValUnknown)
                && idMap.containsKey(local.getparent())) {
            return idMap.get(local.getparent()) == remote.getparent();
        }
        return local.getparent() == remote.getparent();
    }

    public ConflictResolutionFormPrinter getConflictResolutionFormPrinter() {
        return new ConflictResolutionFormPrinter(this.changedChangedNodes, changedChangedNodes, changedChangedNodes);
    }

    //
    // METHOD USED FOR TESTING CODE
    //

    List<ConflictPair> getRemoteAddedNodes() {
        return remoteAddedNodes;
    }

    List<ConflictPair> getLocalAddedNodes() {
        return localAddedNodes;
    }

    List<DatabaseNode> getRemoteDeletedNodes() {
        return remoteDeletedNodes;
    }

    List<DatabaseNode> getLocalDeletedNodes() {
        return localDeletedNodes;
    }

    List<NodePair> getRemoteChangedNodes() {
        return remoteChangedNodes;
    }

    List<NodePair> getLocalChangedNodes() {
        return localChangedNodes;
    }

    List<NodePair> getChangedChangedNodes() {
        return changedChangedNodes;
    }

    List<DeleteandChange> getChangedDeletedNodes() {
        return changedDeletedNodes;
    }

    List<DeleteandChange> getDeletedChangedNodes() {
        return deletedChangedNodes;
    }
}
