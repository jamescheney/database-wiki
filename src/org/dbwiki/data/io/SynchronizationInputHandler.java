package org.dbwiki.data.io;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.dbwiki.data.database.DatabaseElementList;
import org.dbwiki.data.database.DatabaseElementNode;
import org.dbwiki.data.database.DatabaseGroupNode;
import org.dbwiki.data.database.DatabaseNode;
import org.dbwiki.data.schema.AttributeSchemaNode;
import org.dbwiki.data.schema.GroupSchemaNode;
import org.dbwiki.data.time.TimeSequence;
import org.dbwiki.driver.rdbms.RDBMSDatabaseAttributeNode;
import org.dbwiki.driver.rdbms.RDBMSDatabaseGroupNode;
import org.dbwiki.driver.rdbms.RDBMSDatabaseTextNode;
import org.dbwiki.exception.WikiException;
import org.dbwiki.exception.WikiFatalException;

public class SynchronizationInputHandler implements InputHandler {
	
	/*
	 * Private Variables
	 */
	
	//private PasteDatabaseInfo _databaseInfo;
	private String databaseName;
	private int versionNumber;
	private Exception _exception;
	private Stack<DatabaseNode> _readStack;
	private DatabaseNode _root = null;
	private DatabaseElementNode _parentNode = null;
	private boolean isRootRequest = false;
	private List<DatabaseNode> nodeList;

	@Override
	public void endDocument() throws WikiException {
		// TODO Auto-generated method stub
		if (!_readStack.isEmpty()) {
			throw new WikiFatalException("Invalid document format");
		}
	}

	@Override
	public void endElement(String label) throws WikiException {
		// TODO Auto-generated method stub
		if (label.equals(SynchronizeConstants.ElementLabelNode)) {
			DatabaseNode node = _readStack.pop();
			if(node.isElement() && node != _root){
				_parentNode = (DatabaseElementNode)node.parent();
			}
			if(isRootRequest && node == _root && _readStack.isEmpty()){
				nodeList.add(node);
				_root = null;
			}
		}
	}

	@Override
	public void exception(Exception excpt) {
		// TODO Auto-generated method stub
		_exception = excpt;
	}

	@Override
	public Exception getException() {
		// TODO Auto-generated method stub
		return _exception;
	}

	@Override
	public boolean hasException() {
		// TODO Auto-generated method stub
		return (_exception != null);
	}

	@Override
	public void startDocument() throws WikiException {
		// TODO Auto-generated method stub
		//_databaseInfo = null;
		databaseName = null;
		versionNumber = -1;
		_readStack = new Stack<DatabaseNode>();
		_root = null;
		if(isRootRequest){
			nodeList = new ArrayList<DatabaseNode>();
		}
	}

	@Override
	public void startElement(String label) throws WikiException {
		// TODO Auto-generated method stub
		throw new WikiFatalException("Invalid method call: " + this.getClass().getName() + ".startElement(" + label + ")");
	}
	
	@Override
	public void startElement(String label, Attribute[] attrs)
			throws WikiException {
		// TODO Auto-generated method stub
		if ((label.equals(SynchronizeConstants.ElementLabelDatabase)) && (attrs.length == 2) && (databaseName == null)) {
			databaseName = this.getAttribute(attrs, SynchronizeConstants.AttributeLabelDatabaseName).value();
			versionNumber = Integer.parseInt(this.getAttribute(attrs, SynchronizeConstants.AttributeLabelVersion).value());
		} else if ((label.equals(SynchronizeConstants.ElementLabelNode)) && (attrs.length == 6) && (databaseName != null)) {
			if (Integer.parseInt(this.getAttribute(attrs, SynchronizeConstants.AttributeLabelType).value()) == SynchronizeConstants.NodeTypeText) {
				int id = Integer.parseInt((this.getAttribute(attrs, "id").value()));
				int pre = Integer.parseInt((this.getAttribute(attrs, "pre").value()));
				int post = Integer.parseInt((this.getAttribute(attrs, "post").value()));
				TimeSequence ts = new TimeSequence(this.getAttribute(attrs, SynchronizeConstants.AttributeLabelTimeSequence).value());
				RDBMSDatabaseTextNode node = new RDBMSDatabaseTextNode(id, (_parentNode == null? null : (RDBMSDatabaseAttributeNode)_parentNode), ts, "", pre, post);
				if (_root == null) {
					_root = node;
				} else {
					((RDBMSDatabaseAttributeNode)_readStack.peek()).value().add(node);
				}
				_readStack.push(node);
			} else {
				throw new WikiFatalException("Invalid node type in copy & paste data stream");
			}
		} else if ((label.equals(SynchronizeConstants.ElementLabelNode)) && (attrs.length == 8) && (databaseName != null)) {
			int type = Integer.parseInt(this.getAttribute(attrs, SynchronizeConstants.AttributeLabelType).value());
			String schemaName = this.getAttribute(attrs, SynchronizeConstants.AttributeLabelSchemaNodeName).value();
			int schemaId = Integer.parseInt((this.getAttribute(attrs, SynchronizeConstants.AttributeLabelSchemaNodeId).value()));
			int id = Integer.parseInt((this.getAttribute(attrs, "id").value()));
			int pre = Integer.parseInt((this.getAttribute(attrs, "pre").value()));
			int post = Integer.parseInt((this.getAttribute(attrs, "post").value()));
			TimeSequence ts = new TimeSequence(this.getAttribute(attrs, SynchronizeConstants.AttributeLabelTimeSequence).value());
			DatabaseElementNode node = null;
			if (type == SynchronizeConstants.NodeTypeAttribute) {
				DatabaseGroupNode parentNode = null;
				AttributeSchemaNode schemaNode = null;
				if((_parentNode != null) && (_parentNode.isGroup())){
					parentNode = (DatabaseGroupNode)_parentNode;
					for(int i = 0; i < ((GroupSchemaNode)_parentNode.schema()).children().size(); i++){
						if(schemaName.equals(((GroupSchemaNode)_parentNode.schema()).children().get(i).label())){
							schemaNode = (AttributeSchemaNode)((GroupSchemaNode)_parentNode.schema()).children().get(i);
						}
					}
					if(schemaNode == null){
						schemaNode = new AttributeSchemaNode(schemaId, schemaName, (GroupSchemaNode)_parentNode.schema());
					}
				}
				else if(_parentNode == null){
					schemaNode = new AttributeSchemaNode(schemaId, schemaName, null);
				}
				node = new RDBMSDatabaseAttributeNode(id, schemaNode, parentNode, ts, pre, post);
			} else if (type == SynchronizeConstants.NodeTypeGroup) {
				DatabaseGroupNode parentNode = null;
				GroupSchemaNode schemaNode = null;
				if((_parentNode != null) && (_parentNode.isGroup())){
					parentNode = (DatabaseGroupNode)_parentNode;
					for(int i = 0; i < ((GroupSchemaNode)_parentNode.schema()).children().size(); i++){
						if(schemaName.equals(((GroupSchemaNode)_parentNode.schema()).children().get(i).label())){
							schemaNode = (GroupSchemaNode)((GroupSchemaNode)_parentNode.schema()).children().get(i);
						}
					}
					if(schemaNode == null){
						schemaNode = new GroupSchemaNode(schemaId, schemaName, (GroupSchemaNode)_parentNode.schema());
					}
				}
				else if(_parentNode == null){
					schemaNode = new GroupSchemaNode(schemaId, schemaName, null);
				}
				
				node = new RDBMSDatabaseGroupNode(id, schemaNode, parentNode, ts, pre, post);
			} else {
				throw new WikiFatalException("Invalid node type in synchronization data stream");
			}
			if (_root == null) {
				_root = node;
			} else {
				DatabaseGroupNode gnode = (RDBMSDatabaseGroupNode)_readStack.peek();
				DatabaseElementList list = gnode.children();
				list.add(node);
			}
			_readStack.push(node);
			_parentNode = node;
		} else {
			throw new WikiFatalException("Invalid element in synchronization data stream");
		}
	}
	
	@Override
	public void text(char[] value) throws WikiException {
		// TODO Auto-generated method stub
		String text = new String(value);
		if (!text.trim().equals("")) {
			((RDBMSDatabaseTextNode)_readStack.peek()).setValue(text);
		}
	}

	/*
	 * Private Methods
	 */
	
	private Attribute getAttribute(Attribute[] attrs, String name) throws org.dbwiki.exception.WikiException {
		for (int iAttr = 0; iAttr < attrs.length; iAttr++) {
			if (attrs[iAttr].name().equals(name)) {
				return attrs[iAttr];
			}
		}
		throw new WikiFatalException("Missing attribute " + name);
	}

	public void setIsRootRequest(boolean isRootRequest){
		this.isRootRequest = isRootRequest;
	}
	
	public DatabaseNode getSynchronizeDatabaseNode() {
		return _root;
	}
	
	public List<DatabaseNode> getSynchronizeDatabaseNodeList(){
		return this.nodeList;
	}
	
	public int getVersionNumber(){
		return versionNumber;
	}
}
