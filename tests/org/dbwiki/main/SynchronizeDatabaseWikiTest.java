package org.dbwiki.main;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.dbwiki.data.database.DatabaseElementNode;
import org.dbwiki.data.database.DatabaseNode;
import org.dbwiki.data.database.DatabaseTextNode;
import org.dbwiki.data.io.SAXCallbackInputHandler;
import org.dbwiki.data.io.SynchronizationInputHandler;
import org.dbwiki.data.schema.AttributeSchemaNode;
import org.dbwiki.data.time.TimeSequence;
import org.dbwiki.driver.rdbms.RDBMSDatabaseAttributeNode;
import org.dbwiki.exception.WikiException;
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
	public void setUp() throws FileNotFoundException, IOException, SAXException {
//		DatabaseSchema schema = EasyMock.createMock(DatabaseSchema.class);
//		String schemaNodeRootPath = "";
//		ImportHandler importHandler = EasyMock.createMock(ImportHandler.class);
//		DocumentInputHandler handler = new DocumentInputHandler(schema, schemaNodeRootPath, importHandler);
//		SAXCallbackInputHandler saxHandler = new SAXCallbackInputHandler(handler, true);
//		saxHandler.parse(new FileInputStream(new File("/home/cata/Desktop/disertation_various/note.xml")), false, true);
	}
//
//	@Test
//	public void testGetRemoteMapID() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetLocalMapID() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testMatch() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testCompare() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testCompareTextNodes() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testCompareAttributeNodes() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testCompareGroupNodes() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testSynchronizeDatabaseWiki() {
//		SynchronizeDatabaseWiki dbwiki = new SynchronizeDatabaseWiki();
//		assertNotNull("0 parameter constructor failed", dbwiki);
//	}
//
//	@Test
//	public void testSynchronizeDatabaseWikiDatabaseWikiUser() {
//		SynchronizeDatabaseWiki dbwiki = new SynchronizeDatabaseWiki(EasyMock.createMock(DatabaseWiki.class),
//				EasyMock.createMock(User.class));
//		assertNotNull("SynchronizeDatabaseWiki constructor failed", dbwiki);
//	}
//
//	@Test
//	public void testResponseToSynchronizeRequestFileFile() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testResponseToSynchronizeRequestStringIntBoolean() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testResponseToSynchronizeRequestStringIntBooleanStringString() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testAddLocalPortParameter() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testExtractVersionNumbers() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testInvertAndAddParameters() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testHandleConflicts() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testHandleDifferences() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testMap() {
//		fail("Not yet implemented");
//	}

//	/**
//	 * For {@code DatabaseElementNode}.
//	 */
//	@Test
//	public void testIsChanged() throws WikiException {
//		DatabaseElementNode node = new RDBMSDatabaseAttributeNode(15, new AttributeSchemaNode(0, "", null), null,
//				new TimeSequence(0,  14), 0, 0);
//		assertEquals("testIsChanged", true, SynchronizeDatabaseWiki.isChanged(node, 12).isEmpty());
//	}

	/**
	 * For {@code ResultTextNode}.
	 */
	@Test
	public void testIsChanged2() throws WikiException {
		DatabaseTextNode node = EasyMock.createMock(DatabaseTextNode.class);
		expect(node.isElement()).andReturn(false);
		expect(node.getTimestamp()).andReturn(new TimeSequence(2, 8));
		replay(node);
		assertEquals("testIsChanged2", false, SynchronizeDatabaseWiki.isChanged(node, 7).isEmpty());
		verify(node);
	}

//	/**
//	 * For {@code ResultGroupNode}.
//	 */
//	@Test
//	public void testIsChanged3() throws WikiException {
//		ResultGroupNode mockNode = EasyMock.createNiceMock(ResultGroupNode.class);
//		mockNode.setTimestamp(new TimeSequence(2));
//
//		DatabaseElementList list = new DatabaseElementList();
//		list.add(new RDBMSDatabaseAttributeNode(1, new AttributeSchemaNode(0, "lbl", null, new TimeSequence(0,7)),
//				null, new  TimeSequence(0,8), -1, -1));
//		expect(mockNode.children()).andReturn(list).anyTimes();
//		expect(mockNode.isElement()).andReturn(true);;
//		expect(mockNode.isGroup()).andReturn(true);
//		replay(mockNode);
//		System.out.println(mockNode.children().size());
//		assertEquals("testIsChanged2", false, SynchronizeDatabaseWiki.isChanged(mockNode, 3).isEmpty());
//	}

	@Test
	public void testIsChanged4() throws FileNotFoundException, IOException, SAXException {
		DatabaseNode remoteNode = getNodeFromFile("31A5.xml");
		DatabaseNode localNode = getNodeFromFile("31A5edited.xml");
		List<DatabaseTextNode> changes = SynchronizeDatabaseWiki.isChanged(remoteNode, getVersionOfFile("31A5edited.xml"));
		List<DatabaseTextNode> changes2 = SynchronizeDatabaseWiki.isChanged(localNode, getVersionOfFile("31A5.xml"));
		assertEquals(true, changes.isEmpty());
		assertEquals(false, changes2.isEmpty());
 	}
//
//	@Test
//	public void testGetRemotePreviousVersionNumber() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetLocalPreviousVersionNumber() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testSetSynchronizeParametersBooleanBooleanBooleanBooleanBooleanBooleanBoolean() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testSetSynchronizeParametersBooleanBooleanBooleanBooleanBooleanBooleanBooleanBooleanBooleanBoolean() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testMain() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testSameParent() {
//		fail("Not yet implemented");
//	}

	@Test
	public void testReading() throws IOException, SAXException {
		DatabaseNode remoteNode = getNodeFromFile("31A5.xml");
		assertEquals("reading from file failed", "2-*", remoteNode.getTimestamp().toIntString());
	}


	private DatabaseNode getNodeFromFile(String fileName) throws FileNotFoundException,
			IOException, SAXException {
		FileInputStream stream = new FileInputStream(new File(
				String.format("/home/cata/Desktop/disertation_various/%s", fileName)));
		SynchronizationInputHandler ioHandler = new SynchronizationInputHandler();
		ioHandler.setIsRootRequest(false);
		new SAXCallbackInputHandler(ioHandler, false).parse(stream, false, false);

		DatabaseNode remoteNode = ioHandler.getSynchronizeDatabaseNode();
		return remoteNode;
	}

	private int getVersionOfFile(String fileName) throws IOException, SAXException {
		FileInputStream stream = new FileInputStream(new File(
				String.format("/home/cata/Desktop/disertation_various/%s", fileName)));
		SynchronizationInputHandler ioHandler = new SynchronizationInputHandler();
		ioHandler.setIsRootRequest(false);
		new SAXCallbackInputHandler(ioHandler, false).parse(stream, false, false);

		return ioHandler.getVersionNumber();
	}

}
