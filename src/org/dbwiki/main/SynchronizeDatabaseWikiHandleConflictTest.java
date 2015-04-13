package org.dbwiki.main;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.dbwiki.data.database.DatabaseNode;
import org.dbwiki.data.database.Update;
import org.dbwiki.data.io.SAXCallbackInputHandler;
import org.dbwiki.data.io.SynchronizationInputHandler;
import org.dbwiki.data.resource.ResourceIdentifier;
import org.dbwiki.driver.rdbms.RDBMSDatabase;
import org.dbwiki.exception.WikiException;
import org.dbwiki.user.User;
import org.dbwiki.web.server.DatabaseWiki;
import org.easymock.EasyMock;
import org.junit.Test;
import org.xml.sax.SAXException;

public class SynchronizeDatabaseWikiHandleConflictTest {

    @Test
    public void testHandleConflicts() throws FileNotFoundException, IOException, SAXException, WikiException {
        // create mocks
        final User user = EasyMock.createMock(User.class);
        final RDBMSDatabase db = EasyMock.createMock(RDBMSDatabase.class);
        DatabaseWiki dbwiki = EasyMock.createMock(DatabaseWiki.class);

        // record behaviour
        expect(dbwiki.database()).andReturn(db).times(10);
        db.update(anyObject(ResourceIdentifier.class), anyObject(Update.class), EasyMock.eq(user));
        expectLastCall().times(10);
        EasyMock.replay(dbwiki);
        EasyMock.replay(db);

        // test
        final SynchronizeDatabaseWiki syncObject = new SynchronizeDatabaseWiki(dbwiki, user);
        // Required for comparisons
        syncObject.localPreviousVersionNumber = 2;
        syncObject.remotePreviousVersionNumber = 2;
        DatabaseNode changedNode = getNodeFromFile("14176-0-10-0-spread-home-10-0-0.xml");
        DatabaseNode originalNode = getNodeFromFile("14176-0-10-0-spread-dice-10-0-0.xml");
        syncObject.compare(changedNode, originalNode);

        assertEquals(10, syncObject.changedChangedNodes.size());
        syncObject.changedChanged = true;
        syncObject.handleConflicts();
        EasyMock.verify(dbwiki);
        EasyMock.verify(db);
     }


    private DatabaseNode getNodeFromFile(String fileName) throws FileNotFoundException,
            IOException, SAXException {
        FileInputStream stream = new FileInputStream(new File(
                String.format("/home/cata/dbwiki/catalina/resources/data/testnodes/%s", fileName)));
        SynchronizationInputHandler ioHandler = new SynchronizationInputHandler();
        ioHandler.setIsRootRequest(false);
        new SAXCallbackInputHandler(ioHandler, false).parse(stream, false, false);

        DatabaseNode remoteNode = ioHandler.getSynchronizeDatabaseNode();
        return remoteNode;
    }

}
