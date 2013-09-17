package org.dbwiki.web.request;

import org.dbwiki.data.schema.AttributeSchemaNode;
import org.dbwiki.data.schema.GroupSchemaNode;

public class URLDecodingRule {

	/*
	 * Private Variables
	 */
	
	private GroupSchemaNode _node;
	private AttributeSchemaNode _value;
	
	
	/*
	 * Constructors
	 */
	
	public URLDecodingRule(GroupSchemaNode node,  AttributeSchemaNode value) {
		_node = node;
		_value = value;
	}
	
	
	/*
	 * Public Methods
	 */
	
	public GroupSchemaNode node() {
		return _node;
	}
	
	public AttributeSchemaNode value() {
		return _value;
	}
}
