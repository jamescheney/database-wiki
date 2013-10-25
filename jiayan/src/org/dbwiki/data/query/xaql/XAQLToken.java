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
package org.dbwiki.data.query.xaql;

/** Constants to identify the different tokens in a XAQL query definition.
 * 
 * @author hmueller
 *
 */
import java.util.Vector;

import org.parboiled.trees.ImmutableTreeNode;

public class XAQLToken extends ImmutableTreeNode<XAQLToken> {

	/*
	 * Public Constants
	 */
	
	// STATEMENT
	public static final int DELETE_STATEMENT              = 0;
	public static final int INSERT_STATEMENT              = 1;
	public static final int QUERY_STATEMENT               = 2;
	public static final int UPDATE_STATEMENT              = 3;
	
	// SELECT CLAUSE
	public static final int SELECT_CLAUSE                 = 1000;
	public static final int TIMESTAMP_SELECT_CLAUSE       = 1001;
	public static final int SUBTREE_SELECT_CLAUSE         = 1002;
	public static final int FULL_SELECT_STATEMENT         = 1003;
	public static final int SUBTREE_SELECT_LIST           = 1004;
	public static final int VARIABLE_SELECT_STATEMENT     = 1006;
	public static final int DELETE_SELECT_CLAUSE          = 1007;
	public static final int SELECT_STATEMENT_LABEL        = 1008;
	
	// FROM CLAUSE
	public static final int FROM_CLAUSE                   = 1500;
	public static final int SUBQUERY_FROM_CLAUSE          = 1501;
	
	// VERSION CLAUSE
	public static final int VERSION_CLAUSE                = 7000;
	public static final int TIMESTAMP_VALUE               = 7001;
	public static final int TIMESTAMP_DELIMITER           = 7002;
	public static final int COMPARING_VERSION_CLAUSE      = 7003;
	
	// WHERE CLAUSE
	public static final int WHERE_CLAUSE                  = 8000;
	public static final int WHERE_CLAUSE_EXPRESSION       = 8001;
	public static final int COINCIDES_EXPRESSION          = 8002;
	public static final int COINCIDES_LIST_ELEMENT        = 8003;
	public static final int PROVENANCE_EXPRESSION         = 8004;
	public static final int PROVENANCE_USER_NAME          = 8005;
	public static final int PROVENANCE_OPERATION          = 8006;
	public static final int PROVENANCE_DATE               = 8007;
	public static final int PROVENANCE_DATE_OPERATOR      = 8008;
	
	// Path Expression
	public static final int ABSOLUTE_TARGET_PATH          = 3000;
	public static final int RELATIVE_TARGET_PATH          = 3001;
	public static final int VARIABLE_TARGET_PATH          = 3002;
	public static final int TARGET_PATH_EXPRESSION        = 3003;
	public static final int INDEX_CONDITION               = 3004;
	public static final int SUB_PATH_CONDITION            = 3007;
	public static final int SUB_PATH_CONDITION_EXPRESSION = 3008;
	
	// Quantifier
	public static final int FOR_ALL_QUANTIFIER            = 5000;
	public static final int FOR_ANY_QUANTIFIER            = 5001;
	public static final int FOR_NOW_QUANTIFIER            = 5002;
	public static final int FOR_ALL_VALUES_QUANTIFIER     = 5003;
	
	// Identifier & Values
	public static final int ENTITY_LABEL                  = 4000;
	public static final int VARIABLE_NAME                 = 4001;
	public static final int INDEX_VALUE                   = 4002;
	public static final int ARCHIVE_NAME                  = 4003;
	public static final int VALUE                         = 4004;
	
	// Functions & Predicates
	public static final int EXISTS_PREDICATE              = 6000;
	public static final int LIKE_OPERATOR                 = 6001;
	public static final int MATCHES_OPERATOR              = 6002;
	public static final int LGNEQ_OPERATOR                = 6003;
	public static final int IN_OPERATOR                   = 6004;
	public static final int HAS_CHANGES_PREDICATE         = 6005;
	public static final int WAS_MODIFIED_PREDICATE        = 6006;
	public static final int NOT_OPERATOR                  = 6007;
	public static final int FUNCTION                      = 6008;
	public static final int AT_FUNCTION                   = 6009;
	public static final int AT_DATE_FUNCTION              = 6010;
	public static final int AT_VERSION_FUNCTION           = 6012;
	public static final int DATE_OPERATION                = 6013;
	public static final int LASTAT_FUNCTION               = 6014;
	
	/*
	 * Private Variables
	 */
	
	private Vector<XAQLToken> _children = null;
	private int _type;
	private String _value = null;
	
	/*
	 * Constructors
	 */
	
	public XAQLToken(int type) {
		
		_type = type;
	}
	
	public XAQLToken(int type, Vector<XAQLToken> children) {
		
		_type = type;
		_children = children;
	}

	public XAQLToken(int type, String value) {
		
		_type = type;
		_value = value;
	}
	
	public XAQLToken(XAQLToken node, XAQLToken nextChild) {
		
		_type = node.type();
		_children = new Vector<XAQLToken>();
		for (XAQLToken child : node.children()) {
			_children.add(child);
		}
		_children.add(nextChild);
	}
	
	
	/*
	 * Public Methods
	 */
	
	public Vector<XAQLToken> children() {
		
		return _children;
	}
	
	public void print(String indention, String extension) {
		
		System.out.print(indention + this.type());
		if (this.value() != null) {
			System.out.print("[" + this.value() + "]");
		}
		if (this.children() != null) {
			System.out.println(" {");
			for (XAQLToken child : this.children()) {
				child.print(indention + extension, extension);
			}
			System.out.println(indention + "}");
		} else {
			System.out.println();
		}
	}

	public int type() {
		
		return _type;
	}
	
	public String value() {
		
		return _value;
	}
}
