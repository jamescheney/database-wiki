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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.dbwiki.data.security;
import java.util.regex.*;
/**
 *
 * @author Tom
 */
public class Rule {
	private String id;
	private String user;
	private String path;
	private String action;
	private String sign;


	public void setId(String id) {
		this.id = id;
	}
	public String getId(){
		return this.id;   
	} 


	public void setUser(String user) {
		this.user = user;
	}
	public String getUser(){
		return this.user;   
	}

	public void setPath(String path) { 
		this.path = path;
	}
	public String getPath(){
		return this.path;   
	}


	public void setAction(String action) {
		this.action = action;
	}
	
	public String getAction() {
		return this.action;   
	}


	public void setSign(String sign) {
		this.sign = sign;
	}
	public String getSign(){
		return this.sign;   
	}


	public boolean matchPath(String path, String URL)
	{    
		if(path.indexOf("*")!=-1){
			path = path.replace("*", "[0-9]*");

		}


		if(path.length()<=URL.length()&&path.indexOf("//")==-1)
		{
			return false;

		}
		else{
			path = path.replace("//", "/(?d).*");
			path = path + "$";
			Pattern pattern = Pattern.compile(path);
			Matcher matcher = pattern.matcher(URL);
			boolean matchFound = matcher.find();
			return matchFound; 
		}
	}


}







