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

import org.dbwiki.data.resource.ResourceIdentifier;
import org.dbwiki.data.time.TimeSequence;

/** A subclass of DatabaseNode for text nodes.
 * 
 * @author jcheney
 *
 */
public abstract class DatabaseTextNode extends DatabaseNode {
	/*
	 * Private Variables
	 */
	
	private String _value = null;

	
	/*
	 * Constructors
	 */
	
	public DatabaseTextNode(DatabaseAttributeNode parent, TimeSequence timestamp, String value, AnnotationList annotation, int pre, int post) {
		super(parent, timestamp, annotation, pre, post);

		_value = value;
	}
	
	
	/*
	 * Public Methods
	 */
	
	public String getValue() {
		return this.value();
	}
	
	public boolean isElement() {
		return false;
	}
	
	public void setValue(String value) {
		_value = value;
	}
	
	public String text() {
		return this.value();
	}
	
	public String value() {
		return _value;
	}
	
	public String toString() {
		return value();
	}
	
	public DatabaseNode find(ResourceIdentifier identifier) {
		return null;
	}
	
	public  String findLocation(){
		String findLocation;
		//Map<String, String> locationMap = new Map<String , String>();
		String locationList = "";
		
			findLocation=_value.toString();
			//System.out.println("111"+_value);
			Boolean containment=findLocation.contains(";");
			//System.out.println(containment);
			if(containment)
			{
				String s = findLocation;
				int index=s.indexOf(";");
				int beginIndex=index+1, endIndex=beginIndex+3;
				//int begin=index;
				int flag=1;
				if(s.substring(beginIndex).length()>=15&&s.substring(beginIndex).indexOf(";")!=-1)
				{
					//System.out.println(s.substring(beginIndex).indexOf(";"));
				while(flag==1){
					//System.out.println("left length"+s.substring(beginIndex).length());
				for (int i=0; i<((s.substring(index+1).length()+1)/4); i++){
					//System.out.println((s.substring(beginIndex).length()+1)/4);
					if(flag==1){
					if(s.substring(beginIndex,endIndex).compareTo("000")<0||s.substring(beginIndex,endIndex).compareTo("500")>0){
						flag=flag*0;
					}
					else{
						locationList+=s.substring(beginIndex,endIndex);
					}
					
					//System.out.println("substring"+s.substring(beginIndex,endIndex));
					beginIndex+=4; endIndex+=4;
					if(s.substring(beginIndex-1,beginIndex).compareTo(";")==0){
						locationList+=";";
						break;
					}
					else if(flag!=0){
						locationList+=",";
					}
					}
				}
				//System.out.println(flag);
				/*if (flag==1){
					String location=s.substring(beginIndex-16, endIndex-3);
					locationList+=location;
					//locationMap.put(s.substring(s.indexOf("NAME/")+5,begin),s.substring(beginIndex-16, endIndex-4));
				}*/
				if(s.length()<beginIndex){
					flag=0;
				}
				else if(s.substring(beginIndex-1,beginIndex).compareTo(";")==0&&s.substring(beginIndex).length()>=15){
					flag=1;
				}
				else{
					flag=0;
				}
				}
				//System.out.println("location length"+locationList.length());
				//System.out.println("location"+locationList);
				if(locationList==""){
					return null;
				}
				else{
					return locationList;
				}
				}
				else{
					return null;
				}
			}
			else{
				return null;
			}
			
		
	}
	public String findAnnotation(String location){
		//String loc=location.substring(0,location.length()-1);
		String ann=_value.toString().replace(location, "");
		return ann;
	}

}
