package org.dbwiki.data.database;

import org.dbwiki.data.annotation.AnnotationList;
import org.dbwiki.data.document.DocumentNode;
import org.dbwiki.data.resource.NodeIdentifier;
import org.dbwiki.data.schema.AttributeSchemaNode;
import org.dbwiki.data.time.TimeSequence;
import org.dbwiki.driver.rdbms.DatabaseConstants;
import org.dbwiki.driver.rdbms.RDBMSDatabaseTextNode;

public class ResultAttributeNode extends DatabaseAttributeNode {
	public ResultAttributeNode(AttributeSchemaNode entity, TimeSequence timestamp) {
		// FIXME: Pre/post numbers for result attribute nodes are nonsense and should never be used 
		super(entity, null, timestamp, new AnnotationList(),-1,-1);
	}
	
	@Override
	public void add(String value, TimeSequence timestamp, int pre, int post) {
		this.value().add(new RDBMSDatabaseTextNode(DatabaseConstants.RelDataColIDValUnknown, this, timestamp, value,pre,post));
	}

	
	@Override
	public NodeIdentifier identifier() {
		return new NodeIdentifier();
	}

	@Override
	public DocumentNode toDocumentNode() {
		// TODO Auto-generated method stub
		return null;
	}

}
