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

import org.dbwiki.data.annotation.AnnotationList;

import org.dbwiki.data.resource.NodeIdentifier;
import org.dbwiki.data.schema.SchemaNode;

import org.dbwiki.data.time.TimeSequence;

/** A subclass of DatabaseNode providing common functionality for "group" and "attribute" nodes.
 * 
 * @author jcheney
 *
 */
public abstract class DatabaseElementNode extends DatabaseNode {
	/*
	 * Private Variables
	 */
	
	private SchemaNode _schema;
	private String _label;
	
	
	/*
	 * Constructors
	 */
	
	public DatabaseElementNode(SchemaNode schema, DatabaseGroupNode parent, TimeSequence timestamp, AnnotationList annotation, int pre, int post) {
		super(parent, timestamp, annotation, pre, post);
		_schema = schema;
		_label = schema.label();
	}
	
	
	/*
	 * Public Methods
	 */
		
	public SchemaNode schema() {
		return _schema;
	}
	
	public boolean isAttribute() {
		return _schema.isAttribute();
	}

	public boolean isElement() {
		return true;
	}

	public boolean isGroup() {
		return _schema.isGroup();
	}

	public String label() {
		return _label;
	}
	
	public abstract NodeIdentifier identifier();
	
	public void setLabel(String label) {
		_label = label;
	}
	
	/*public  String findLocation(){
		String findLocation;
		//Map<String, String> locationMap = new Map<String , String>();
		String locationList = "";
		
			findLocation=_schema.toString();
			System.out.println("fingLocation"+findLocation+"label"+_label+"schema"+_schema);
			Boolean containment=findLocation.contains(";");
			//System.out.println(containment);
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
					locationList+=location+";";
					//locationMap.put(s.substring(s.indexOf("NAME/")+5,begin),s.substring(beginIndex-16, endIndex-4));
				}
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
				System.out.println(locationList.length());
				System.out.println(locationList);
				return locationList;
			}
			else{
				return null;
			}
			
		
	}*/

}
