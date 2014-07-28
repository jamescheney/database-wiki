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

/** Generates WHERE condition from a given set of XAQLTokens.
 * 
 * @author hmueller
 *
 */
import java.util.Date;
import java.util.Vector;

import org.dbwiki.data.provenance.Provenance;

import org.dbwiki.data.query.condition.Condition;
import org.dbwiki.data.query.condition.ExistsCondition;
import org.dbwiki.data.query.condition.HasChangesCondition;
import org.dbwiki.data.query.condition.INOp;
import org.dbwiki.data.query.condition.ProvenanceCondition;
import org.dbwiki.data.query.condition.Quantifier;
import org.dbwiki.data.query.condition.ValueCondition;
import org.dbwiki.data.query.condition.ValueOpFactory;
import org.dbwiki.data.query.condition.WasModifiedCondition;

import org.dbwiki.data.query.xpath.XPath;

import org.dbwiki.data.schema.SchemaNode;

import org.dbwiki.data.time.Version;
import org.dbwiki.data.time.VersionIndex;

import org.dbwiki.exception.WikiFatalException;

public class ConditionGenerator {
	
	/*
	 * Public Methods
	 */
	
	public Condition getCondition(SchemaNode entity, VersionIndex versionIndex, Vector<XAQLToken> tokens, TargetPathGenerator pathGenerator) throws org.dbwiki.exception.WikiException {
		
		int index = 0;
		
		boolean negated = false;
		if (tokens.get(index).type() == XAQLToken.NOT_OPERATOR) {
			negated = true;
			index++;
		}
		
		int quantifier = Quantifier.FOR_ANY;
		if (tokens.get(index).type() == XAQLToken.FOR_ALL_QUANTIFIER) {
			quantifier = Quantifier.FOR_ALL;
			index++;
		}
		
		XPath targetPath = pathGenerator.getTargetPath(entity, versionIndex, tokens.get(index++).children().iterator());
		
		int valueQuantifier = Quantifier.FOR_ANY;
		if (tokens.get(index).type() == XAQLToken.FOR_ALL_VALUES_QUANTIFIER) {
			valueQuantifier = Quantifier.FOR_ALL;
			index++;
		}
		
		XAQLToken operator = tokens.get(index++);
		switch (operator.type()) {
		case XAQLToken.EXISTS_PREDICATE:
			return new ExistsCondition(targetPath, quantifier, negated);
		case XAQLToken.HAS_CHANGES_PREDICATE:
			if (tokens.size() > index) {
				return new HasChangesCondition(targetPath, quantifier, negated, this.getProvenanceCondition(tokens.get(index), versionIndex));
			} else {
				return new HasChangesCondition(targetPath, quantifier, negated);
			}
		case XAQLToken.WAS_MODIFIED_PREDICATE:
			if (tokens.size() > index) {
				return new WasModifiedCondition(targetPath, quantifier, negated, this.getProvenanceCondition(tokens.get(index), versionIndex));
			} else {
				return new WasModifiedCondition(targetPath, quantifier, negated);
			}
		case XAQLToken.LGNEQ_OPERATOR:
		case XAQLToken.LIKE_OPERATOR:
		case XAQLToken.MATCHES_OPERATOR:
			return new ValueCondition(targetPath, quantifier, negated, new ValueOpFactory().get(operator.value().trim(), tokens.get(index++).value()), valueQuantifier);
		case XAQLToken.IN_OPERATOR:
			INOp inOp = (INOp)new ValueOpFactory().get(operator.value().trim(), tokens.get(index++).value());
			while (index < tokens.size()) {
				inOp.add(new ValueOpFactory().get("=", tokens.get(index++).value()));
			}
			return new ValueCondition(targetPath, quantifier, negated, inOp, valueQuantifier);
		default:
			throw new WikiFatalException("Unknown operator type");
		}
	}
	
	
	/*
	 * Private Methods
	 */
	
	private ProvenanceCondition getProvenanceCondition(XAQLToken collection, VersionIndex versionIndex) throws org.dbwiki.exception.WikiException {
		
		if (collection.type() != XAQLToken.PROVENANCE_EXPRESSION) {
			throw new WikiFatalException("Invalid token type " + collection.type() + " in getProvenanceCondition()");
		}
		
		Date startDate = null;
		Date endDate = null;
		String dateOp = null;
		byte provenanceType = Provenance.ProvenanceTypeUnknown;
		String username = null;
		
		for (int iToken = 0; iToken < collection.children().size(); iToken++) {
			XAQLToken token = collection.children().get(iToken);
			if (token.type() == XAQLToken.PROVENANCE_DATE_OPERATOR) {
				dateOp = token.value();
				iToken++;
				try {
					if (dateOp.equalsIgnoreCase(XAQLSyntaxParser.KeywordAfter)) {
						startDate = org.dbwiki.lib.DateTime.getDate(collection.children().get(iToken).value());
					} else if (dateOp.equalsIgnoreCase(XAQLSyntaxParser.KeywordBefore)) {
						endDate = org.dbwiki.lib.DateTime.getDate(collection.children().get(iToken).value());
					} else if (dateOp.equalsIgnoreCase(XAQLSyntaxParser.KeywordSince)) {
						startDate = org.dbwiki.lib.DateTime.getDate(collection.children().get(iToken).value());
					} else if (dateOp.equalsIgnoreCase(XAQLSyntaxParser.KeywordUntil)) {
						endDate = org.dbwiki.lib.DateTime.getDate(collection.children().get(iToken).value());
					} else if (dateOp.equalsIgnoreCase(XAQLSyntaxParser.KeywordBetween)) {
						startDate = org.dbwiki.lib.DateTime.getDate(collection.children().get(iToken).value());
						iToken++;
						endDate = org.dbwiki.lib.DateTime.getDate(collection.children().get(iToken).value());
					} else {
						throw new WikiFatalException("Unknown date operation " + token.value() + " in getProvenanceCondition()");
					}
				} catch (java.text.ParseException parseException) {
					throw new WikiFatalException(parseException);
				}
			} else if (token.type() == XAQLToken.PROVENANCE_USER_NAME) {
				username = token.value().substring(1, token.value().length() - 1);
			} else if (token.type() == XAQLToken.PROVENANCE_OPERATION) {
				if (token.value().equalsIgnoreCase(XAQLSyntaxParser.KeywordCopy)) {
					provenanceType = Provenance.ProvenanceTypeCopy;
				} else if (token.value().equalsIgnoreCase(XAQLSyntaxParser.KeywordDelete)) {
					provenanceType = Provenance.ProvenanceTypeDelete;
				} else if (token.value().equalsIgnoreCase(XAQLSyntaxParser.KeywordInsert)) {
					provenanceType = Provenance.ProvenanceTypeInsert;
				} else if (token.value().equalsIgnoreCase(XAQLSyntaxParser.KeywordUpdate)) {
					provenanceType = Provenance.ProvenanceTypeUpdate;
				} else {
					throw new WikiFatalException("Unknown operation name " + token.value() + " in getProvenanceCondition()");
				}
			} else {
				throw new WikiFatalException("Invalid token type " + token.type() + " in getProvenanceCondition()");
			}
		}
		
		ProvenanceCondition versions = new ProvenanceCondition();
		
		for (int iVersion = 0; iVersion < versionIndex.size(); iVersion++) {
			Version version = versionIndex.get(iVersion);
			boolean matches = true;
			if (dateOp != null) {
				if (dateOp.equalsIgnoreCase(XAQLSyntaxParser.KeywordAfter)) {
					matches = version.time() > startDate.getTime();
				} else if (dateOp.equalsIgnoreCase(XAQLSyntaxParser.KeywordBefore)) {
					matches = version.time() < endDate.getTime();
				} else if (dateOp.equalsIgnoreCase(XAQLSyntaxParser.KeywordSince)) {
					matches = version.time() >= startDate.getTime();
				} else if (dateOp.equalsIgnoreCase(XAQLSyntaxParser.KeywordUntil)) {
					matches = version.time() <= endDate.getTime();
				} else if (dateOp.equalsIgnoreCase(XAQLSyntaxParser.KeywordBetween)) {
					matches = ((version.time() >= startDate.getTime()) && (version.time() <= endDate.getTime()));
				}
			}
			if ((matches) && (username != null) ) {
				if (version.provenance().user() != null) {
					matches = version.provenance().user().login().equals(username);
				} else { // unknown user differs from any specific user name
					matches = false;
				}
			}
			if ((matches) && (provenanceType != Provenance.ProvenanceTypeUnknown)) {
				matches = (version.provenance().type() == provenanceType);
			}
			if (matches) {
				versions.add(version);
			}
		}
		
		return versions;
	}
}
