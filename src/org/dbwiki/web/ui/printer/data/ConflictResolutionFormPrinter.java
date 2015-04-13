package org.dbwiki.web.ui.printer.data;

import java.util.HashMap;
import java.util.List;

import org.dbwiki.data.database.DatabaseNode;
import org.dbwiki.data.database.DatabaseTextNode;
import org.dbwiki.data.database.NodeUpdate;
import org.dbwiki.data.database.Update;
import org.dbwiki.data.resource.NodeIdentifier;
import org.dbwiki.driver.rdbms.RDBMSDatabaseTextNode;
import org.dbwiki.main.SynchronizeDatabaseWiki.DeleteandChange;
import org.dbwiki.main.SynchronizeDatabaseWiki.NodePair;
import org.dbwiki.web.html.HtmlLinePrinter;
import org.dbwiki.web.request.WikiDataRequest;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.request.parameter.RequestParameterAction;
import org.dbwiki.web.ui.CSS;
import org.dbwiki.web.ui.printer.HtmlContentPrinter;

public class ConflictResolutionFormPrinter implements HtmlContentPrinter {
    /*
     * Private Variables
     */

    private String _actionParameterName;
    private WikiDataRequest<?> _request;
    private List<NodePair> changedChangedNodes;
    private List<DeleteandChange> changedDeletedNodes;
    private List<DeleteandChange> deletedChangedNodes;
    private String remoteUrl;
    private HashMap<Integer, Integer> idMap;


    /*
     * Constructors
     */

    public ConflictResolutionFormPrinter(List<NodePair> changedChangedNodes,
            List<DeleteandChange> changedDeletedNodes2,
            List<DeleteandChange> deletedChangedNodes2,
            HashMap<Integer, Integer> idMap,
            String remoteURL) {
        this.changedChangedNodes = changedChangedNodes;
        this.changedDeletedNodes = changedDeletedNodes2;
        this.deletedChangedNodes = deletedChangedNodes2;
        this.idMap = idMap;
        this.remoteUrl = remoteURL;
    }

    public void setRequest(WikiDataRequest<?> request) {
        this._request = request;
    }




    /*
     * Public Methods
     */

    @Override
    public void print(HtmlLinePrinter body) throws org.dbwiki.exception.WikiException {
        body.paragraph("Conflict resolution", CSS.CSSHeadline);

        body.addBR();
        body.addBR();

        String url = _request.wri().getURL();
        body.openFORM("frmInput", "POST", url);
        body.addHIDDEN("nodeBeingSynced", url.substring(url.lastIndexOf('/')));
        body.addHIDDEN("resolution", "LOCAL"); // only local prints this form
        body.addHIDDEN(RequestParameter.ParameterRemoteAddr, remoteUrl);

        body.openCENTER();
        body.openTABLE(CSS.CSSInputForm);

        body.openTR();
        body.openTD(CSS.CSSContentCellActive);
        body.text("All differences have been handled. You must now help the system resolve the conflicts it encourtered.");
        body.closeTD();
        body.closeTR();

        for (NodePair pair : this.changedChangedNodes){
            body.openTR();
            body.openTD(CSS.CSSInputForm);
            body.openPARAGRAPH(CSS.CSSInputForm);
            body.text(String.format("Node %s has been changed on both servers. Choose which version you want to keep.", getPathLabels(pair)));
            body.closePARAGRAPH();
            String name = pair.get_localNode().identifier().toURLString() + "-" + pair.get_remoteNode().identifier().toURLString();
            body.addRADIOBUTTON("Local value: " + ((DatabaseTextNode) pair.get_localNode()).getValue(), name, "REMOTE:" +((DatabaseTextNode) pair.get_localNode()).getValue(), true);
            body.addBR();
            body.addRADIOBUTTON("Remote value: " + ((DatabaseTextNode) pair.get_remoteNode()).getValue(), name, "LOCAL:" + ((DatabaseTextNode) pair.get_remoteNode()).getValue(), false);
            body.closeTD();
            body.closeTR();
        }

/*        for (DeleteandChange pair : this.deletedChangedNodes){
            NodePair pair2 = pair.getDeletedChangedNode();
            body.openTR();
            body.openTD(CSS.CSSInputForm);
            body.openPARAGRAPH(CSS.CSSInputForm);
            body.text(String.format("Node %s has been deleted on local server and changed on the remote server. Choose which update to keep.", getPathLabels(pair2)));
            body.closePARAGRAPH();
            String name;
            if (idMap.containsKey(((NodeIdentifier) pair2.get_localNode().identifier()).nodeID())) {
                name = pair2.get_localNode().identifier().toURLString() + "-"
                        + new NodeIdentifier(idMap.get(((NodeIdentifier) pair2.get_localNode().identifier()).nodeID())).toURLString();
            } else {
                name = pair2.get_localNode().identifier().toURLString() + "-" + pair2.get_localNode().identifier().toURLString();
            }
            if(pair.getList() == null){
                body.addRADIOBUTTON("Local value: " + "deleted", name, "REMOTE:DELETE:", true);
                body.addBR();
                body.addRADIOBUTTON("Remote value: " + ((DatabaseTextNode) pair2.get_remoteNode()).getValue(), name, "LOCAL:REACTIVATE+CHANGE:" + ((DatabaseTextNode) pair2.get_remoteNode()).getValue(), false);
            }
            else{
                for(int i = 0; i < pair.getList().size(); i++){
                    NodeUpdate nodeupdate = new NodeUpdate(new NodeIdentifier(idMap.get(((NodeIdentifier)pair.getList().get(i).identifier()).nodeID())), pair.getList().get(i).getValue());
                    body.addRADIOBUTTON("Local value: " + "deleted", name, "REMOTE:DELETE:", true);
                    body.addBR();
                    body.addRADIOBUTTON("Remote value: " + ((DatabaseTextNode) pair2.get_remoteNode()).getValue(), name, "LOCAL:REACTIVATE+CHANGE:" + ((DatabaseTextNode) pair2.get_remoteNode()).getValue(), false);

                }
            }
            body.closeTD();
            body.closeTR();
        }*/

/*        for (DeleteandChange wrappedPair : this.changedDeletedNodes){
            NodePair pair = wrappedPair.getDeletedChangedNode();
            body.openTR();
            body.openTD(CSS.CSSInputForm);
            body.openPARAGRAPH(CSS.CSSInputForm);
            body.text(String.format("Node %s has been changed on local server and deleted on the remote server. Choose which update to keep.", getPathLabels(pair)));
            body.closePARAGRAPH();
            String name;
            if (idMap.containsKey(((NodeIdentifier) pair.get_localNode().identifier()).nodeID())) {
                name = pair.get_localNode().identifier().toURLString() + "-"
                        + new NodeIdentifier(idMap.get(((NodeIdentifier) pair.get_localNode().identifier()).nodeID())).toURLString();
            } else {
                name = pair.get_localNode().identifier().toURLString() + "-" + pair.get_localNode().identifier().toURLString();
            }
            if (pair.get_localNode().isText()) {
                body.addRADIOBUTTON("Local value: " + ((DatabaseTextNode) pair.get_localNode()).getValue(), name, "REMOTE:REACTIVATE+CHANGE:" +((DatabaseTextNode) pair.get_localNode()).getValue(), true);
            } else {

            }
            body.addBR();
            body.addRADIOBUTTON("Remote value: " + "deleted", name, "LOCAL:DELETE", false);
            body.closeTD();
            body.closeTR();
        }*/

        body.closeTABLE();
        body.closeCENTER();

        body.openPARAGRAPH(CSS.CSSButtonLine);
        body.openCENTER();
        body.addBUTTON("image", "button", "/pictures/button_ok.gif");
        body.text("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
        body.addREALBUTTON("submit",
                "action", RequestParameterAction.ActionCancel, "<img src=\"/pictures/button_cancel.gif\">");
        body.closePARAGRAPH();
        body.closeFORM();
    }

    private String getPathLabels(NodePair pair) {
        StringBuffer path = new StringBuffer(256);
        DatabaseNode node = pair.get_localNode();
        if (node.isText()) {
            path.insert(0, " " + getPathString(pair.get_localNode()));
            node = node.parent();
        }
        while (node.parent() != null) {
            path.insert(0, " >");
            path.insert(0, node.parent().identifier().toURLString());
            node = node.parent();
        }

        return path.toString();
    }

    private String getPathString(DatabaseNode node) {
        return "<a target=\"_blank\" HREF=\"" + _request.wri().databaseIdentifier().linkPrefix() + node.identifier().toURLString()
                + "\">" + node.identifier().toURLString() + "</a>";
    }
}
