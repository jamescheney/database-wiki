package org.dbwiki.web.ui.printer.data;

import java.util.List;

import org.dbwiki.data.database.DatabaseNode;
import org.dbwiki.data.database.DatabaseTextNode;
import org.dbwiki.main.SynchronizeDatabaseWiki.NodePair;
import org.dbwiki.web.html.HtmlLinePrinter;
import org.dbwiki.web.request.WikiDataRequest;
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


    /*
     * Constructors
     */

    public ConflictResolutionFormPrinter(List<NodePair> changedChangedNodes,
            List<NodePair> changedDeletedNodes,
            List<NodePair> deletedChangedNodes) {
        this.changedChangedNodes = changedChangedNodes;
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
            String name = pair.get_localNode().identifier().toURLString() + "-" + pair.get_localNode().identifier().toURLString();
            body.addRADIOBUTTON("Local value: " + ((DatabaseTextNode) pair.get_localNode()).getValue(), name, "REMOTE:" +((DatabaseTextNode) pair.get_localNode()).getValue(), true);
            body.addBR();
            body.addRADIOBUTTON("Remote value: " + ((DatabaseTextNode) pair.get_remoteNode()).getValue(), name, "LOCAL:" + ((DatabaseTextNode) pair.get_remoteNode()).getValue(), false);
            body.closeTD();
            body.closeTR();
        }

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
