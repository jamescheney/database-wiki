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

import java.io.*;   
import java.util.*;
import org.w3c.dom.Document;   
import javax.xml.parsers.DocumentBuilderFactory;   
import javax.xml.parsers.DocumentBuilder;   
import org.w3c.dom.Element;   
import org.w3c.dom.NodeList;
/**
 *
 * @author Tom
 */
public class Policy   
{   
	// policy contains a default semantics, conflict resolution and a list of rules
	// a rule contains a list of string, i.e. <requester, resource, action, effect>
	private ArrayList<Rule> ruleList = new ArrayList<Rule>();
	private boolean defaultSet;
	private boolean conflictRes;


	public Policy()
	{
	}



	public boolean getConflict(){
		return this.conflictRes;   
	}


	public boolean getDefault(){
		return this.defaultSet;   
	}

	public ArrayList<Rule> getRuleList(){
		return this.ruleList;   
	}




	//this method is to check all positive rules which match the front end request
	//and then save each matched rules into a new arraylist
	public boolean positiveRules(String user, String action, String URLpath){
		boolean check = false;
		for(int i=0; i<ruleList.size(); i++){
			if(((Rule)ruleList.get(i)).getUser().equals(user) && ((Rule)ruleList.get(i)).getAction().equals(action))
			{
				if(((Rule)ruleList.get(i)).matchPath(((Rule)ruleList.get(i)).getPath(), URLpath))
				{
					if(((Rule)ruleList.get(i)).getSign().equals("+")) 
					{
						check = true;
						break;
					}

				}

			}

		} return check;
	}

	//this method is to check all negative rules which match the front end request
	//and then save each matched rules into a new arraylist
	public boolean negativeRules(String user, String action, String URLpath){
		boolean check = false;
		for(int i=0; i<ruleList.size(); i++){
			if(((Rule)ruleList.get(i)).getUser().equals(user) && ((Rule)ruleList.get(i)).getAction().equals(action))
			{
				if(((Rule)ruleList.get(i)).matchPath(((Rule)ruleList.get(i)).getPath(), URLpath))
				{
					if(((Rule)ruleList.get(i)).getSign().equals("-")){
						check = true;
						break;
					}

				}

			}

		} 
		return check;
	}



	/*
	 * this method will check whether the path from the front end is contained in the rule's path
	 * some paths in the rule might contain a large scope of the path from front end
	 * */





	public void loadFromFile(File file)

	{
		try   
		{   
			DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();  //get a instance of DocumentBuilderFactory  
			DocumentBuilder db=dbf.newDocumentBuilder(); //generate a new DocumentBuilder  
			Document docs=db.parse(file); //Parse it by dom  



			//NodeList theDefault=docs.getElementsByTagName("default"); //fetch all the node with "default" tag
			//Element defaultElement = (Element)theDefault.item(0);
			String dfs=docs.getElementsByTagName("default").item(0).getFirstChild().getNodeValue(); //get the value

			if(dfs.equals("+")){
				defaultSet = true;
			}else{
				defaultSet = false; 
			}



			//NodeList theConflict=docs.getElementsByTagName("conflict_resolution"); //fetch all the node with "conflict resolution" tag
			//Element conflictElement = (Element)theConflict.item(0);
			String cfr=docs.getElementsByTagName("conflict_resolution").item(0).getFirstChild().getNodeValue(); //get the value 


			if(cfr.equals("+")){
				conflictRes = true;
			}else{
				conflictRes = false;
			}




			NodeList Rule=docs.getElementsByTagName("rule"); //get all rule tags  
			Element ruleElement; //declear element object  

			for (int i = 0; i < Rule.getLength(); i++) //for each rule  
			{
				ruleElement=(Element)Rule.item(i); //assgin value to each node 
				String id=ruleElement.getAttribute("id"); //fetch “id”vale, e.g. <rule id="1">  
				String user=docs.getElementsByTagName("user").item(i).getFirstChild().getNodeValue();   
				String path=docs.getElementsByTagName("path").item(i).getFirstChild().getNodeValue();   
				String action=docs.getElementsByTagName("action").item(i).getFirstChild().getNodeValue(); 
				String sign=docs.getElementsByTagName("effect").item(i).getFirstChild().getNodeValue();   

				Rule r = new Rule();
				r.setId(id);
				r.setUser(user);
				r.setPath(path);
				r.setAction(action);
				r.setSign(sign);
				ruleList.add(r);

			}

		}   
		catch(Exception e)   
		{   
			e.printStackTrace();   
		}   

	}   




	public void printAllPolicy(){
		System.out.println("---------------Rules----------------");  
		for(int i=0; i<ruleList.size(); i++){       
			System.out.println("id: " +((Rule)ruleList.get(i)).getId());
			System.out.println("user: " +((Rule)ruleList.get(i)).getUser());
			System.out.println("path: " +((Rule)ruleList.get(i)).getPath());
			System.out.println("Action: " +((Rule)ruleList.get(i)).getAction());
			System.out.println("Sign: " +((Rule)ruleList.get(i)).getSign());
			System.out.println("--------------------");
		}


	}


}   




