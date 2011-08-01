package org.dbwiki.web.request;

import java.util.Hashtable;
import java.util.StringTokenizer;

import org.dbwiki.data.database.Database;
import org.dbwiki.data.database.DatabaseAttributeNode;
import org.dbwiki.data.database.DatabaseElementList;
import org.dbwiki.data.database.DatabaseGroupNode;
import org.dbwiki.data.database.DatabaseTextNode;

import org.dbwiki.data.index.DatabaseEntry;

import org.dbwiki.data.resource.ResourceIdentifier;

import org.dbwiki.data.schema.AttributeSchemaNode;
import org.dbwiki.data.schema.GroupSchemaNode;
import org.dbwiki.data.schema.DatabaseSchema;
import org.dbwiki.data.schema.SchemaNode;

import org.dbwiki.exception.data.WikiDataException;
import org.dbwiki.exception.data.WikiSchemaException;

import org.dbwiki.web.request.parameter.RequestParameterVersion;

public class URLDecodingRules {

	/*
	 * Private Variables
	 */
	
	private Hashtable<Integer, URLDecodingRule> _rules;
	
	/*
	 * Constructors
	 */
	
	public URLDecodingRules(DatabaseSchema schema, String definition) throws org.dbwiki.exception.WikiException {
		
		_rules = new Hashtable<Integer, URLDecodingRule>();
		
		StringTokenizer tokens = new StringTokenizer(definition, " \t\n\r\f,");
		
		while (tokens.hasMoreTokens()) {
			if  (tokens.nextToken().equalsIgnoreCase("IDENTIFY")) {
				try {
					String path = tokens.nextToken();
					if (tokens.nextToken().equalsIgnoreCase("BY")) {
						SchemaNode node = schema.get(path);
						if (node.isGroup()) {
							String value = tokens.nextToken();
							String valuePath = path;
							if (!valuePath.endsWith(SchemaNode.SchemaPathSeparator)) {
								valuePath = valuePath + SchemaNode.SchemaPathSeparator + value;
							} else {
								valuePath = valuePath + value;
							}
							SchemaNode valueNode = schema.get(valuePath);
							if (valueNode.isAttribute()) {
								this.add(new URLDecodingRule((GroupSchemaNode)node, (AttributeSchemaNode)valueNode));
							} else {
								throw new WikiSchemaException(WikiSchemaException.InvalidConstraintDefinition, valueNode + " is not an attribute node");
							}
						} else {
							throw new WikiSchemaException(WikiSchemaException.InvalidConstraintDefinition, path + " is not a group node");
						}
					} else {
						throw new WikiSchemaException(WikiSchemaException.InvalidConstraintDefinition, definition);
					}
				} catch (java.util.NoSuchElementException noSuchElementException) {
					throw new WikiSchemaException(WikiSchemaException.InvalidConstraintDefinition, definition);
				}
			} else {
				throw new WikiSchemaException(WikiSchemaException.InvalidConstraintDefinition, definition);
			}
		}
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void add(URLDecodingRule rule) throws org.dbwiki.exception.WikiException {
		Integer key = new Integer(rule.node().id());
		if (!_rules.containsKey(key)) {
			_rules.put(key, rule);
		} else {
			throw new WikiSchemaException(WikiSchemaException.InvalidConstraintDefinition, "Duplicate constraint for node " + rule.node());
		}
	}
	
	public ResourceIdentifier decode(Database database, RequestURL url, RequestParameterVersion versionParameter) throws org.dbwiki.exception.WikiException {
		
		// FORMAT: {/[<label>:<key-value>|<key-value>]}+ /[<label>|<label>:<key-value>|<key-value>]

		GroupSchemaNode root = database.schema().root();
		URLDecodingRule rule = this.get(root);
		if (rule != null) {
			String entryIdentifier = url.get(0).decodedText();
			int pos = entryIdentifier.indexOf(':');
			if (pos != -1) {
				if (entryIdentifier.substring(0, pos).equals(root.label())) {
					entryIdentifier = entryIdentifier.substring(pos + 1);
				} else {
					throw new WikiDataException(WikiDataException.UnknownResource, url.toString());
				}
			}
			DatabaseEntry entry = database.content().get(entryIdentifier);
			if (entry != null) {
				if (url.size() > 1) {
					return this.decode(database, (DatabaseGroupNode)database.get(entry.identifier()), versionParameter, url, 1);
				} else {
					return entry.identifier();
				}
			} else {
				throw new WikiDataException(WikiDataException.UnknownResource, url.toString());
			}
		} else {
			throw new WikiDataException(WikiDataException.UnknownResource, url.toString());
		}
	}
	
	public URLDecodingRule get(int index) {
		return _rules.get(index);
	}
	
	public URLDecodingRule get(SchemaNode node) {
		return _rules.get(new Integer(node.id()));
	}
	
	public String printConstraints() {
		
		String text = "";
		
		for (URLDecodingRule rule : _rules.values()) {
			String line = "IDENTIFY " + rule.node().path() + " BY " + rule.value().path().substring(rule.node().path().length() + 1);
			if (text.equals("")) {
				text = line;
			} else {
				text = text + ",\n" + line;
			}
		}
		return text;
	}
	
	public int size() {
		return _rules.size();
	}
	
	
	/*
	 * Private Methods
	 */
	
	public ResourceIdentifier decode(Database database, DatabaseGroupNode node, RequestParameterVersion versionParameter, RequestURL url, int pathIndex) throws org.dbwiki.exception.WikiException {
		
		// FORMAT: {/[<label>:<key-value>|<key-value>]}+ /[<label>|<label>:<key-value>|<key-value>]

		GroupSchemaNode schemaNode = (GroupSchemaNode)node.schema();
		String nodeIdentifier = url.get(pathIndex).decodedText();
		
		URLDecodingRule rule = null;
		String keyValue = nodeIdentifier;
		
		int pos = nodeIdentifier.indexOf(':');

		if (pos != -1) {
			String nodeLabel = nodeIdentifier.substring(0, pos);
			rule = this.get(schemaNode.children().get(nodeIdentifier.substring(0, pos)));
			keyValue = nodeIdentifier.substring(pos + 1);
		} else {
			// If it's the last element of the URL path the nodeIdentifier could be the node label
			// of an attribute node or a key value for an internal node.
			// First check whether the nodeIdentifier is a valid node label for the current
			// schema node.
			for (int iChild = 0; iChild < schemaNode.children().size(); iChild++) {
				SchemaNode childSchema = schemaNode.children().get(iChild);
				if ((childSchema.label().equals(nodeIdentifier)) && (childSchema.isAttribute())) {
					// Make sure that there is only one child
					DatabaseElementList nodes = node.find(childSchema.path().substring(node.schema().path().length() + 1));
					if (nodes.size() > 1) {
						throw new WikiDataException(WikiDataException.UnknownResource, url.toString());
					} else if (nodes.size() == 1) {
						if (pathIndex == (url.size() - 1)) {
							return nodes.get(0).identifier();
						} else {
							return this.decode(database, (DatabaseGroupNode)nodes.get(0), versionParameter, url, pathIndex + 1);
						}
					} else {
						throw new WikiDataException(WikiDataException.UnknownResource, url.toString());
					}
				}
			}
			// This part of the code is only reached if the nodeIdentifier does not point
			// to an attribute node. There should only be one schema node child with a rule defined
			// for that node.
			for (int iChild = 0; iChild < schemaNode.children().size(); iChild++) {
				URLDecodingRule childRule = this.get(schemaNode.children().get(iChild));
				if (childRule != null) {
					if (rule == null) {
						rule = childRule;
					} else {
						throw new WikiDataException(WikiDataException.UnknownResource, url.toString());
					}
				}
			}
		}

		if (rule != null) {
			DatabaseElementList nodes = node.find(rule.node().path().substring(node.schema().path().length() + 1));
			DatabaseGroupNode nextNode = null;
			for (int iNode = 0; iNode < nodes.size(); iNode++) {
				DatabaseGroupNode childNode = (DatabaseGroupNode)nodes.get(iNode);
				if (versionParameter.matches(childNode)) {
					int matches = 0;
					DatabaseElementList valueNodes = childNode.find(rule.value().path().substring(childNode.schema().path().length() + 1));
					for (int iValueNode = 0; iValueNode < valueNodes.size(); iValueNode++) {
						DatabaseAttributeNode attributeNode = (DatabaseAttributeNode)valueNodes.get(iValueNode);
						for (int iAttrValue = 0; iAttrValue < attributeNode.value().size(); iAttrValue++) {
							DatabaseTextNode textNode = attributeNode.value().get(iAttrValue);
							if ((versionParameter.matches(textNode)) && (textNode.value().equals(keyValue))) {
								matches++;
								break;
							}
						}
					}
					if (matches == 1) {
						if (nextNode == null) {
							nextNode = childNode;
						} else {
							throw new WikiDataException(WikiDataException.UnknownResource, url.toString());
						}
					}
				}
			}
			if (nextNode != null) {
				if (pathIndex == (url.size() - 1)) {
					return nextNode.identifier();
				} else {
					return this.decode(database, nextNode, versionParameter, url, pathIndex + 1);
				}
			} else {
				throw new WikiDataException(WikiDataException.UnknownResource, url.toString());
			}
		} else {
			throw new WikiDataException(WikiDataException.UnknownResource, url.toString());
		}
		
	}
}
