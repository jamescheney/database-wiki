/* 
    BEGIN LICENSE BLOCK
    Copyright 2010-2011, Heiko Mueller, Sam Lindley, James Cheney and
    University of Edinburgh

    This file is part of Database Wiki.

    Database Wiki is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Database Wiki is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Database Wiki.  If not, see <http://www.gnu.org/licenses/>.
    END LICENSE BLOCK
*/
package org.dbwiki.data.database;

import java.util.ArrayList;
import java.util.Stack;
import java.util.Vector;

import org.dbwiki.data.annotation.AnnotationList;

import org.dbwiki.data.resource.ResourceIdentifier;
import org.dbwiki.data.schema.SchemaNode;
import org.dbwiki.data.schema.GroupSchemaNode;

import org.dbwiki.data.time.TimeSequence;

/** Subclass of DatabaseElementNode representing group nodes. 
 * 
 * @author jcheney
 *
 */
public abstract class DatabaseGroupNode extends DatabaseElementNode {
	/*
	 * Private Variables
	 */
	
	private DatabaseElementList _children;
	
	
	/*
	 * Constructors
	 */
	
	public DatabaseGroupNode(GroupSchemaNode schema, DatabaseGroupNode parent, TimeSequence timestamp, AnnotationList annotation, int pre, int post) {
		super(schema, parent, timestamp, annotation, pre, post);
		
		_children = new DatabaseElementList();
		//System.out.println("aaa"+_children);
	}

	
	/*
	 * Public Methods
	 */
	
	public DatabaseElementList children() {
		return _children;
	}
	
	public DatabaseElementList find(SchemaNode schema) {
		Stack<SchemaNode> entities = new Stack<SchemaNode>();
		
		SchemaNode parent = schema.parent();
		while (parent != null) {
			if (parent.equals(this.schema())) {
				break;
			}
			entities.push(parent);
			parent = parent.parent();
		}
		
		if (parent == null) {
			return new DatabaseElementList();
		}
		
		Vector<DatabaseGroupNode> elements = new Vector<DatabaseGroupNode>();
		elements.add(this);
		
		while (!entities.isEmpty()) {
			SchemaNode pathSchema = entities.pop();
			Vector<DatabaseGroupNode> candidates = new Vector<DatabaseGroupNode>();
			for (int iElement = 0; iElement < elements.size(); iElement++) {
				DatabaseGroupNode node = elements.get(iElement);
				for (int iChild = 0; iChild < node.children().size(); iChild++) {
					if (node.children().get(iChild).schema().equals(pathSchema)) {
						candidates.add((DatabaseGroupNode)node.children().get(iChild));
					}
				}
			}
			elements = candidates;
		}
		
		DatabaseElementList matches = new DatabaseElementList();
		for (int iElement = 0; iElement < elements.size(); iElement++) {
			DatabaseGroupNode node = elements.get(iElement);
			for (int iChild = 0; iChild < node.children().size(); iChild++) {
				if (node.children().get(iChild).schema().equals(schema)) {
					matches.add(node.children().get(iChild));
				}
			}
		}
		return matches;
	}
	
	public DatabaseElementList find(String path) throws org.dbwiki.exception.WikiException {
		int pos = path.indexOf('/');
		if (pos != -1) {
			DatabaseElementList matches = new DatabaseElementList();
			DatabaseElementList children = this.find(path.substring(0, pos));
			for (int iChild = 0; iChild < children.size(); iChild++) {
				DatabaseElementList nodes = ((DatabaseGroupNode)children.get(iChild)).find(path.substring(pos + 1));
				for (int iNode = 0; iNode < nodes.size(); iNode++) {
					matches.add(nodes.get(iNode));
				}
			}
			return matches;
		} else {
			return this.children().get(path);
		}
	}
	
	public String toString() {
		int n = _children.size();
		if (n == 0) {
			return label();
		} else if (n == 1) {
			return label() + "/" + _children.get(0).toString();
		} else {
			StringBuffer buf = new StringBuffer();
			buf.append(label() + "/{");
			for(int i = 0; i < n-1; i++) {
				buf.append(_children.get(i).toString());
				buf.append(",");
			}
			buf.append(_children.get(n-1).toString());
			buf.append("}");
			//System.out.println("aaa"+buf.toString());
			return buf.toString();
		}
	}

	public DatabaseNode find(ResourceIdentifier identifier) {

		for (int iChild = 0; iChild < children().size(); iChild++) {
			DatabaseElementNode child = children().get(iChild);
			//System.out.println("ccc"+child);
			if (child.identifier().equals(identifier)) {
				//System.out.println("aba"+child);
				return child;
			} else {
				DatabaseNode node = child.find(identifier);
				
				if (node != null) {
					//System.out.println("abc"+node);
					return node;					
				}
			}
		}
		return null;
	}
	 
	public DatabaseImage findIMG(String type)
	{
		
		Boolean containment = null;
		String findImg = null;
		String label=null;
		int iChild=0;
		for(iChild=0; iChild < _children.size(); iChild++)
		{
			DatabaseElementNode Child=_children.get(iChild);
			findImg=Child.toString();
			label=Child.label();
			//System.out.println("111"+label+Child);
			containment=findImg.contains(type);
			if(containment)
			{
				break;
			}
		}
		if (containment)
		{
			int index1,index2;
			String imgname = "";
			index1=findImg.lastIndexOf("/");
			index2=findImg.indexOf(type)+type.length();
			for(int i=index1; i<index2; i++)
			{
				imgname=imgname+findImg.charAt(i);
			}
			//System.out.println(imgname+iChild);
			DatabaseImage imageInfo = new DatabaseImage(imgname,iChild);
			return imageInfo;
		}
		else
		{
			DatabaseImage imageInfo = new DatabaseImage("",-1);
			return imageInfo;
		}
	}

/*	public  ArrayList<String> findLocation(){
		String findLocation;
		//Map<String, String> locationMap = new Map<String , String>();
		ArrayList<String> locationList = new ArrayList<String>();
		for(int iChild=0; iChild < _children.size(); iChild++)
		{
			DatabaseElementNode Child=_children.get(iChild);
			findLocation=Child.toString();
			System.out.println("111"+Child);
			Boolean containment=findLocation.contains(";");
			System.out.println(containment);
			if(containment)
			{
				String s = findLocation;
				int index=s.indexOf(";");
				int beginIndex=index+1, endIndex=beginIndex+3;
				//int begin=index;
				int flag=1;
				while(flag==1){
				
				for (int i=0; i<4; i++){
					//System.out.println(s.substring(beginIndex,endIndex));
					if(s.substring(beginIndex,endIndex).compareTo("000")<0||s.substring(beginIndex,endIndex).compareTo("999")>0){
						flag=flag*0;
					}
					beginIndex+=4; endIndex+=4;
				}
				//System.out.println(flag);
				if (flag==1){
					String location=s.substring(beginIndex-16, endIndex-4);
					locationList.add(location);
					//locationMap.put(s.substring(s.indexOf("NAME/")+5,begin),s.substring(beginIndex-16, endIndex-4));
				}
				System.out.println();
				if(s.length()<beginIndex){
					flag=0;
				}
				else if(s.substring(beginIndex-1,beginIndex).compareTo(";")==0){
					flag=1;
				}
				else{
					flag=0;
				}
				}
			}
		}
		System.out.println(locationList);
		return locationList;
		
	}*/
	
}


	
