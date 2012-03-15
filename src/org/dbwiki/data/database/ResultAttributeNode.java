package org.dbwiki.data.database;

import org.dbwiki.data.annotation.AnnotationList;
import org.dbwiki.data.resource.NodeIdentifier;
import org.dbwiki.data.resource.ResourceIdentifier;
import org.dbwiki.data.schema.AttributeSchemaNode;
import org.dbwiki.data.time.TimeSequence;
import org.dbwiki.driver.rdbms.DatabaseConstants;
import org.dbwiki.driver.rdbms.RDBMSDatabaseTextNode;

public class ResultAttributeNode extends DatabaseAttributeNode {
	public ResultAttributeNode(AttributeSchemaNode entity, TimeSequence timestamp) {
		super(entity, null, timestamp, new AnnotationList());
	}
	
	@Override
	public void add(String value, TimeSequence timestamp) {
		this.value().add(new RDBMSDatabaseTextNode(DatabaseConstants.RelDataColIDValUnknown, this, timestamp, value));
	}

	@Override
	public ResourceIdentifier identifier() {
		return new NodeIdentifier();
	}

}
