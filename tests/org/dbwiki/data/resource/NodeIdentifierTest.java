package org.dbwiki.data.resource;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class NodeIdentifierTest {

    private NodeIdentifier justANodeID;

    @Before
    public void setUp() throws Exception {
        justANodeID = new NodeIdentifier(11);
    }

    @Test
    public void testNodeIdentifierString() {
        NodeIdentifier shouldBeEqual = new NodeIdentifier("/B");
        NodeIdentifier shouldBeDifferent = new NodeIdentifier("/C");
        assertTrue("nodes should be equal", justANodeID.equals(shouldBeEqual));
        assertFalse("nodes should be different", justANodeID.equals(shouldBeDifferent));
    }

    @Test
    public void testEqualsObject() {
        NodeIdentifier anotherNodeIDEqual = new NodeIdentifier(11);
        NodeIdentifier anotherNodeIDDiff = new NodeIdentifier(15);
        assertTrue("equals doesn't work for nodeIdentfier", justANodeID.equals(anotherNodeIDEqual));
        assertFalse("equals should return false", justANodeID.equals(anotherNodeIDDiff));
    }

}
