package org.dbwiki.main;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.dbwiki.data.database.DatabaseNode;
import org.dbwiki.data.database.DatabaseTextNode;
import org.dbwiki.data.io.SAXCallbackInputHandler;
import org.dbwiki.data.io.SynchronizationInputHandler;
import org.dbwiki.driver.rdbms.RDBMSDatabase;
import org.dbwiki.exception.WikiException;
import org.dbwiki.user.User;
import org.dbwiki.web.server.DatabaseWiki;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.xml.sax.SAXException;

@RunWith(JUnit4.class)
public class SynchronizeDatabaseWikiTest {
    DatabaseWiki dbwiki = EasyMock.createMock(DatabaseWiki.class);

    @Before
    public void setUp() throws WikiException {
//        Database db = EasyMock.createMock(Database.class);
//        expect(db.insertNode(EasyMock.anyObject(ResourceIdentifier.class),
//                EasyMock.anyObject(DocumentNode.class), EasyMock.anyObject(User.class)))
//                .andReturn(EasyMock.createMock(ResourceIdentifier.class)).anyTimes();
//        expect(dbwiki.database()).andReturn(db);
//        replay(dbwiki);
    }

    @Test
    public void testSynchronizeDatabaseWikiDatabaseWikiUser() {
        SynchronizeDatabaseWiki dbwiki = new SynchronizeDatabaseWiki(EasyMock.createMock(DatabaseWiki.class),
                new User(1, "dbwiki", "Admin", "dbwikipwd"));
        assertNotNull("SynchronizeDatabaseWiki constructor failed", dbwiki);
    }

    @Test
    public void testReading() throws IOException, SAXException {
        DatabaseNode node = getNodeFromFile("31A5.xml");
        assertNotNull("reading failed", node);
    }

    @Test
    public void testVersionNumbering() throws IOException, SAXException {
        DatabaseNode node = getNodeFromFile("31A5.xml");
        assertNotNull("reading failed", node);
        assertEquals("getting wrong timestamp", -1, node.getTimestamp().lastValue());
    }

    /*****************************
     * TESTING CHANGES DETECTION
     *****************************/

    /**
     * Detecting no change has happened.
     * @throws SAXException
     * @throws IOException
     * @throws FileNotFoundException
     */
    @Test
    public void testIsChanged() throws FileNotFoundException, IOException, SAXException {
        DatabaseNode node = getNodeFromFile("31A5.xml");
        assertTrue("testIsChanged", SynchronizeDatabaseWiki.isChanged(node, 2).isEmpty());
    }

    @Test
    public void testIsChanged2() throws FileNotFoundException, IOException, SAXException {
        int startVersion = 2;
        DatabaseNode originalNode = getNodeFromFile("31A5.xml");
        DatabaseNode changedNode = getNodeFromFile("31A5-11-0-0-spread-home.xml");
        System.out.println(changedNode.getTimestamp().lastValue());
        List<DatabaseTextNode> noChanges = SynchronizeDatabaseWiki.isChanged(originalNode, startVersion);
        List<DatabaseTextNode> withChanges = SynchronizeDatabaseWiki.isChanged(changedNode, startVersion);
        assertTrue("Code thinks given node changed when it didn't", noChanges.isEmpty());
        assertFalse("Code thinks given node did not change when it did" , withChanges.isEmpty());
     }

    /**
     * Testing whether {@code SynchronizeDatabaseWiki} correctly detects remote added nodes.
     * @throws FileNotFoundException
     * @throws IOException
     * @throws SAXException
     * @throws WikiException
     */
    @Test
    public void testCompare() throws FileNotFoundException, IOException, SAXException, WikiException {
        SynchronizeDatabaseWiki syncObject = new SynchronizeDatabaseWiki();
        // Required for comparisons
        syncObject.localPreviousVersionNumber = 2;
        syncObject.remotePreviousVersionNumber = 2;
        DatabaseNode changedNode = getNodeFromFile("31A5-11-0-0-spread-home.xml");
        DatabaseNode originalNode = getNodeFromFile("31A5.xml");
        syncObject.compare(originalNode, changedNode);

        assertEquals("getting wrong number of remote added nodes", 4, syncObject.remoteAddedNodes.size());

        assertTrue(syncObject.remoteChangedNodes.isEmpty());
        assertTrue(syncObject.remoteDeletedNodes.isEmpty());
        assertTrue(syncObject.changedChangedNodes.isEmpty());
        assertTrue(syncObject.deletedChangedNodes.isEmpty());
        assertTrue(syncObject.changedDeletedNodes.isEmpty());
        assertTrue(syncObject.localAddedNodes.isEmpty());
        assertTrue(syncObject.localChangedNodes.isEmpty());
        assertTrue(syncObject.localDeletedNodes.isEmpty());
     }

    /**
     * Testing whether remote deletoed nodes are detected correctly.
     * @throws FileNotFoundException
     * @throws IOException
     * @throws SAXException
     * @throws WikiException
     */
    @Test
    public void testCompare2() throws FileNotFoundException, IOException, SAXException, WikiException {
        SynchronizeDatabaseWiki syncObject = new SynchronizeDatabaseWiki();
        // Required for comparisons
        syncObject.localPreviousVersionNumber = 2;
        syncObject.remotePreviousVersionNumber = 2;
        DatabaseNode changedNode = getNodeFromFile("32B54-0-0-11-compact-home.xml");
        DatabaseNode originalNode = getNodeFromFile("32B54.xml");
        syncObject.compare(originalNode, changedNode);

        assertEquals("remote deleted objects are not detected correctly", 1, syncObject.remoteDeletedNodes.size());

        assertTrue(syncObject.remoteAddedNodes.isEmpty());
        assertTrue(syncObject.remoteChangedNodes.isEmpty());
        assertTrue(syncObject.changedChangedNodes.isEmpty());
        assertTrue(syncObject.deletedChangedNodes.isEmpty());
        assertTrue(syncObject.changedDeletedNodes.isEmpty());
        assertTrue(syncObject.localAddedNodes.isEmpty());
        assertTrue(syncObject.localChangedNodes.isEmpty());
        assertTrue(syncObject.localDeletedNodes.isEmpty());
     }

    @Test
    public void testCompare3() throws FileNotFoundException, IOException, SAXException, WikiException {
        SynchronizeDatabaseWiki syncObject = new SynchronizeDatabaseWiki();
        // Required for comparisons
        syncObject.localPreviousVersionNumber = 2;
        syncObject.remotePreviousVersionNumber = 2;
        DatabaseNode changedNode = getNodeFromFile("33EB1-0-10-0-spread-dice.xml");
        DatabaseNode originalNode = getNodeFromFile("33EB1.xml");
        syncObject.compare(originalNode, changedNode);

        assertEquals("remote changed objects are not detected correctly", 10, syncObject.remoteChangedNodes.size());

        assertTrue(syncObject.remoteAddedNodes.isEmpty());
        assertTrue(syncObject.remoteDeletedNodes.isEmpty());
        assertTrue(syncObject.changedChangedNodes.isEmpty());
        assertTrue(syncObject.deletedChangedNodes.isEmpty());
        assertTrue(syncObject.changedDeletedNodes.isEmpty());
        assertTrue(syncObject.localAddedNodes.isEmpty());
        assertTrue(syncObject.localChangedNodes.isEmpty());
        assertTrue(syncObject.localDeletedNodes.isEmpty());
     }

    /**
     * Testing whether {@code SynchronizeDatabaseWiki} correctly detects remote added nodes.
     * @throws FileNotFoundException
     * @throws IOException
     * @throws SAXException
     * @throws WikiException
     */
    @Test
    public void testCompare4() throws FileNotFoundException, IOException, SAXException, WikiException {
        SynchronizeDatabaseWiki syncObject = new SynchronizeDatabaseWiki();
        // Required for comparisons
        syncObject.localPreviousVersionNumber = 2;
        syncObject.remotePreviousVersionNumber = 2;
        DatabaseNode changedNode = getNodeFromFile("31A5-11-0-0-spread-home.xml");
        DatabaseNode originalNode = getNodeFromFile("31A5.xml");
        syncObject.compare(changedNode, originalNode);

        assertEquals("getting wrong number of remote added nodes", 4, syncObject.localAddedNodes.size());

        assertTrue(syncObject.remoteAddedNodes.isEmpty());
        assertTrue(syncObject.remoteChangedNodes.isEmpty());
        assertTrue(syncObject.remoteDeletedNodes.isEmpty());
        assertTrue(syncObject.changedChangedNodes.isEmpty());
        assertTrue(syncObject.deletedChangedNodes.isEmpty());
        assertTrue(syncObject.changedDeletedNodes.isEmpty());
        assertTrue(syncObject.localChangedNodes.isEmpty());
        assertTrue(syncObject.localDeletedNodes.isEmpty());
     }

    /**
     * Testing whether remote deletoed nodes are detected correctly.
     * @throws FileNotFoundException
     * @throws IOException
     * @throws SAXException
     * @throws WikiException
     */
    @Test
    public void testCompare5() throws FileNotFoundException, IOException, SAXException, WikiException {
        SynchronizeDatabaseWiki syncObject = new SynchronizeDatabaseWiki();
        // Required for comparisons
        syncObject.localPreviousVersionNumber = 2;
        syncObject.remotePreviousVersionNumber = 2;
        DatabaseNode changedNode = getNodeFromFile("32B54-0-0-11-compact-home-false-endtime.xml");
        DatabaseNode originalNode = getNodeFromFile("32B54.xml");
        syncObject.compare(changedNode, originalNode);

        assertEquals("local deleted objects are not detected correctly", 1, syncObject.localDeletedNodes.size());

        assertTrue(syncObject.remoteAddedNodes.isEmpty());
        assertTrue(syncObject.remoteChangedNodes.isEmpty());
        assertTrue(syncObject.remoteDeletedNodes.isEmpty());
        assertTrue(syncObject.changedChangedNodes.isEmpty());
        assertTrue(syncObject.deletedChangedNodes.isEmpty());
        assertTrue(syncObject.changedDeletedNodes.isEmpty());
        assertTrue(syncObject.localAddedNodes.isEmpty());
        assertTrue(syncObject.localChangedNodes.isEmpty());
     }

    @Test
    public void testCompare6() throws FileNotFoundException, IOException, SAXException, WikiException {
        SynchronizeDatabaseWiki syncObject = new SynchronizeDatabaseWiki();
        // Required for comparisons
        syncObject.localPreviousVersionNumber = 2;
        syncObject.remotePreviousVersionNumber = 2;
        DatabaseNode changedNode = getNodeFromFile("33EB1-0-10-0-spread-dice.xml");
        DatabaseNode originalNode = getNodeFromFile("33EB1.xml");
        syncObject.compare(changedNode, originalNode);

        assertEquals("remote changed objects are not detected correctly", 10, syncObject.localChangedNodes.size());

        assertTrue(syncObject.remoteAddedNodes.isEmpty());
        assertTrue(syncObject.remoteChangedNodes.isEmpty());
        assertTrue(syncObject.remoteDeletedNodes.isEmpty());
        assertTrue(syncObject.changedChangedNodes.isEmpty());
        assertTrue(syncObject.deletedChangedNodes.isEmpty());
        assertTrue(syncObject.changedDeletedNodes.isEmpty());
        assertTrue(syncObject.localAddedNodes.isEmpty());
        assertTrue(syncObject.localDeletedNodes.isEmpty());
     }

    @Test
    public void testCompare7() throws FileNotFoundException, IOException, SAXException, WikiException {
        SynchronizeDatabaseWiki syncObject = new SynchronizeDatabaseWiki();
        // Required for comparisons
        syncObject.localPreviousVersionNumber = 2;
        syncObject.remotePreviousVersionNumber = 2;
        DatabaseNode changedNode = getNodeFromFile("14176-0-10-0-spread-home-10-0-0.xml");
        DatabaseNode originalNode = getNodeFromFile("14176-0-10-0-spread-dice-10-0-0.xml");
        syncObject.compare(changedNode, originalNode);

        assertEquals("changed-changed objects are not detected correctly", 10, syncObject.changedChangedNodes.size());

        assertTrue(syncObject.remoteAddedNodes.isEmpty());
        assertTrue(syncObject.remoteChangedNodes.isEmpty());
        assertTrue(syncObject.remoteDeletedNodes.isEmpty());
        assertTrue(syncObject.deletedChangedNodes.isEmpty());
        assertTrue(syncObject.changedDeletedNodes.isEmpty());
        assertTrue(syncObject.localAddedNodes.isEmpty());
        assertTrue(syncObject.localChangedNodes.isEmpty());
        assertTrue(syncObject.localDeletedNodes.isEmpty());
     }

    // TODO: check this test case again -- maybe re-export the nodes
    @Test
    public void testCompare8() throws FileNotFoundException, IOException, SAXException, WikiException {
        SynchronizeDatabaseWiki syncObject = new SynchronizeDatabaseWiki();
        // Required for comparisons
        syncObject.localPreviousVersionNumber = 2;
        syncObject.remotePreviousVersionNumber = 2;
        DatabaseNode localNode = getNodeFromFile("7AF2-0-10-0-spread-home-0-10-0.xml");
        DatabaseNode remoteNode = getNodeFromFile("7AF2-0-0-16-spread-dice-0-10-0.xml");
        syncObject.compare(localNode, remoteNode);

        // 8 because we actually have 2 group nodes (2x2)
        assertEquals("changed-deleted objects are not detected correctly", 7, syncObject.changedDeletedNodes.size());

        assertTrue(syncObject.remoteAddedNodes.isEmpty());
        assertTrue(syncObject.remoteChangedNodes.isEmpty());
//        assertTrue(syncObject.remoteDeletedNodes.isEmpty());
        assertTrue(syncObject.changedChangedNodes.isEmpty());
        assertTrue(syncObject.deletedChangedNodes.isEmpty());
        assertTrue(syncObject.localAddedNodes.isEmpty());
//        assertTrue(syncObject.localChangedNodes.isEmpty());
        assertTrue(syncObject.localDeletedNodes.isEmpty());
     }

    @Test
    public void testCompare9() throws FileNotFoundException, IOException, SAXException, WikiException {
        SynchronizeDatabaseWiki syncObject = new SynchronizeDatabaseWiki();
        // Required for comparisons
        syncObject.localPreviousVersionNumber = 2;
        syncObject.remotePreviousVersionNumber = 2;
        DatabaseNode localNode = getNodeFromFile("4EAE-0-0-11-spread-home-0-11-0-fakeTimestamps.xml");
        DatabaseNode remoteNode = getNodeFromFile("4EAE-0-11-0-spread-dice-0-11-0.xml");
        syncObject.compare(localNode, remoteNode);

        // 5 because all nodes that were part of 5 group nodes.
        assertEquals("deleted-changed objects are not detected correctly", 5, syncObject.deletedChangedNodes.size());

        assertTrue(syncObject.remoteAddedNodes.isEmpty());
        assertTrue(syncObject.remoteChangedNodes.isEmpty());
        assertTrue(syncObject.remoteDeletedNodes.isEmpty());
        assertTrue(syncObject.changedChangedNodes.isEmpty());
        assertTrue(syncObject.changedDeletedNodes.isEmpty());
        assertTrue(syncObject.localAddedNodes.isEmpty());
        assertTrue(syncObject.localChangedNodes.isEmpty());
        assertTrue(syncObject.localDeletedNodes.isEmpty());
     }

    @Test
    public void testCompare10() throws FileNotFoundException, IOException, SAXException, WikiException {
        SynchronizeDatabaseWiki syncObject = new SynchronizeDatabaseWiki();
        // Required for comparisons
        syncObject.localPreviousVersionNumber = 2;
        syncObject.remotePreviousVersionNumber = 2;
        DatabaseNode localNode = getNodeFromFile("9C20-11-0-0-spread-home-0-0-11.xml");
        DatabaseNode remoteNode = getNodeFromFile("9C20-11-0-0-spread-dice-0-0-11.xml");
        syncObject.compare(localNode, remoteNode);

        /**
         * Added-added nodes are not detected per se, but when tryong to add a remoteAdded node to the local dbWiki,
         * we skip adding it, and add a record in the align log file to show that the two nodes are one.
         */
//        assertEquals("added-added objects are not detected correctly", 10, syncObject.changedChangedNodes.size());

        assertTrue(syncObject.remoteChangedNodes.isEmpty());
        assertTrue(syncObject.remoteDeletedNodes.isEmpty());
        assertTrue(syncObject.changedChangedNodes.isEmpty());
        assertTrue(syncObject.deletedChangedNodes.isEmpty());
        assertTrue(syncObject.changedDeletedNodes.isEmpty());
        assertTrue(syncObject.localChangedNodes.isEmpty());
        assertTrue(syncObject.localDeletedNodes.isEmpty());
     }

    @Test
    public void testSameParent() throws FileNotFoundException, IOException, SAXException {
        SynchronizeDatabaseWiki syncObject = new SynchronizeDatabaseWiki();
        DatabaseNode remoteNode = getNodeFromFile("31A5-11-0-0-spread-home.xml");
        DatabaseNode localNode = getNodeFromFile("31A5.xml");
        assertTrue(syncObject.sameParent(localNode, remoteNode));
    }

    @Test
    public void testHandleConflicts() throws FileNotFoundException, IOException, SAXException, WikiException {
        DatabaseWiki dbwiki = EasyMock.createMock(DatabaseWiki.class);
        User user = EasyMock.createMock(User.class);
        RDBMSDatabase db = EasyMock.createStrictMock(RDBMSDatabase.class);
        expect(dbwiki.database()).andReturn(db).times(5);
        replay(db);
        replay(dbwiki);

        SynchronizeDatabaseWiki syncObject = new SynchronizeDatabaseWiki(dbwiki, user);
        // Required for comparisons
        syncObject.localPreviousVersionNumber = 2;
        syncObject.remotePreviousVersionNumber = 2;
        DatabaseNode changedNode = getNodeFromFile("14176-0-10-0-spread-home-10-0-0.xml");
        DatabaseNode originalNode = getNodeFromFile("14176-0-10-0-spread-dice-10-0-0.xml");
        syncObject.compare(changedNode, originalNode);

        syncObject.handleConflicts();
        verify(db);
     }

    @Test
    public void testCleanRemoteServerReport() {
        assertEquals("preprocessing of remote report is broken", "REMOTE-ADDED#c-a#d-a\nREMOTE-DELETED#c-b#d-b",
                SynchronizeDatabaseWiki.cleanRemoteServerReport("ADDED#a-c#a-d\nDELETED#b-c#b-d"));
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
