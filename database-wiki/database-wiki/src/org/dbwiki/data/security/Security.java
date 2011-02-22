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

import java.io.File;

public class Security {
	private Policy policy = new Policy();

	public void loadPolicy(File file){
		//   long start = new java.util.Date().getTime();
		policy.loadFromFile(file);
		//    long end = new java.util.Date().getTime();
		//    System.out.print("loading time cost: " + (end - start)+"ms. \n");
	}

	public boolean checkUpdate(String username, String updType, String URL){
		//  long start=System.currentTimeMillis();   
		Boolean mark = false;
		boolean finalDecision = false;

		//here all these variables are the parameters from front-end

		/* under the condition of conflict resolution is “+”, check whether
		 * got any conflict rules, if yes then positive rules are assigned with
		 * higher priority 
		 */

		if (policy.getConflict()){
			if (mark == false && policy.positiveRules(username, updType, URL)){
				finalDecision = true;
				mark = true;
				//    System.out.println("Step 1, allow-overrides, sign is +");
			} 
			else if (mark == false && policy.negativeRules(username, updType, URL)){
				finalDecision = false;
				mark = true;
				//      System.out.println("Step 2, allow-overrides, sign is -");
			}
		}
		/* under the condition of conflict resolution is “-”, check whether
		 * got any conflict rules, if yes then negative rules are assigned with
		 * higher priority */
		else {
			if (mark==false && policy.negativeRules(username, updType, URL)){
				finalDecision = false;
				mark = true;
				//     System.out.println("Step 3, deny-overrides, effect is -");                
			}
			else if (mark==false&&policy.positiveRules(username, updType, URL)){
				finalDecision = true; 
				mark = true;
				// System.out.println("Step 4, deny-overrides, effect is +");
			}
		}
		if(mark==false){
			finalDecision = policy.getDefault();
			//  System.out.println("No rule to apply, use default");
		}
		// long end=System.currentTimeMillis();
		//System.out.print("Checkup time cost: " + (end - start)+"ms. \n");
		return finalDecision;
	}
}
