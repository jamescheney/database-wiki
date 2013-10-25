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

/** Syntax parser for XAQL queries.
 * 
 * @author hmueller
 *
 */
import java.util.Vector;

import org.dbwiki.data.query.condition.ValueOpFactory;
import org.parboiled.BaseParser;
import org.parboiled.Rule;

import org.parboiled.annotations.BuildParseTree;

@BuildParseTree
public class XAQLSyntaxParser extends BaseParser<XAQLToken> {

	/*
	 * Public Constants
	 */

	public static final String KeywordAfter = "AFTER";
	public static final String KeywordArchive = "ARCHIVE";
	public static final String KeywordAll = "ALL";
	public static final String KeywordAnd = "AND";
	public static final String KeywordAny = "ANY";
	public static final String KeywordAs = "AS";
	public static final String KeywordAt = "AT";
	public static final String KeywordBefore = "BEFORE";
	public static final String KeywordBetween = "BETWEEN";
	public static final String KeywordBy = "BY";
	public static final String KeywordChanges = "CHANGES";
	public static final String KeywordCoincides = "COINCIDES";
	public static final String KeywordCopy = "COPY";
	public static final String KeywordDelete = "DELETE";
	public static final String KeywordExists = "EXISTS";
	public static final String KeywordFile = "FILE";
	public static final String KeywordFor = "FOR";
	public static final String KeywordFrom = "FROM";
	public static final String KeywordHas = "HAS";
	public static final String KeywordInsert = "INSERT";
	public static final String KeywordInto = "INTO";
	public static final String KeywordModified = "MODIFIED";
	public static final String KeywordNot = "NOT";
	public static final String KeywordNow = "NOW";
	public static final String KeywordSelect = "SELECT";
	public static final String KeywordSet = "SET";
	public static final String KeywordSince = "SINCE";
	public static final String KeywordTimestamp = "TIMESTAMP";
	public static final String KeywordUntil = "UNTIL";
	public static final String KeywordUpdate = "UPDATE";
	public static final String KeywordUsing = "USING";
	public static final String KeywordValues = "VALUES";
	public static final String KeywordVersion = "VERSION";
	public static final String KeywordComparing = "COMPARING";
	public static final String KeywordWas = "WAS";
	public static final String KeywordWhere = "WHERE";
	
	
	/*
	 * Public Methods
	 */
	
	//
	// Keywords
	//
	
	public final Rule ARCHIVE       = IgnoreCase(KeywordArchive);
	public final Rule ALL_VALUES    = Sequence(IgnoreCase(KeywordAll), Spacing(), IgnoreCase(KeywordValues));
	public final Rule AND           = Sequence(Spacing(), IgnoreCase(KeywordAnd), Spacing());
	public final Rule AS            = Sequence(IgnoreCase(KeywordAs), Spacing());
	public final Rule AT            = Sequence(IgnoreCase(KeywordAt), Spacing());
	public final Rule BY            = Sequence(IgnoreCase(KeywordBy), Spacing());
	public final Rule COINCIDES     = IgnoreCase(KeywordCoincides);
	public final Rule DELETE        = Sequence(IgnoreCase(KeywordDelete), Spacing());
	public final Rule EQ            = String(ValueOpFactory.EQ);
	public final Rule EXISTS        = IgnoreCase(KeywordExists);
	public final Rule FOR_ALL       = Sequence(IgnoreCase(KeywordFor), Spacing(), IgnoreCase(KeywordAll), Spacing());
	public final Rule FOR_ANY       = Sequence(IgnoreCase(KeywordFor), Spacing(), IgnoreCase(KeywordAny), Spacing());
	public final Rule FROM          = Sequence(IgnoreCase(KeywordFrom), Spacing());
	public final Rule FROM_FILE     = Sequence(IgnoreCase(KeywordFrom), Spacing(), IgnoreCase(KeywordFile), Spacing());
	public final Rule FULLSELECT    = Sequence(Ch('*'), Spacing());
	public final Rule GEQ           = String(ValueOpFactory.GEQ);
	public final Rule GT            = String(ValueOpFactory.GT);
	public final Rule HAS_CHANGES   = Sequence(IgnoreCase(KeywordHas), Spacing(), IgnoreCase(KeywordChanges));
	public final Rule IN            = Sequence(IgnoreCase(ValueOpFactory.IN), Spacing());
	public final Rule INSERT_INTO   = Sequence(IgnoreCase(KeywordInsert), Spacing(), IgnoreCase(KeywordInto), Spacing());
	public final Rule LIKE          = Sequence(IgnoreCase(ValueOpFactory.LIKE), Spacing());
	public final Rule LEQ           = String(ValueOpFactory.LEQ);
	public final Rule LT            = String(ValueOpFactory.LT);
	public final Rule MATCHES       = Sequence(IgnoreCase(ValueOpFactory.MATCHES), Spacing());
	public final Rule NEQ           = FirstOf(String(ValueOpFactory.NEQ1), String(ValueOpFactory.NEQ2));
	public final Rule NOW           = IgnoreCase(KeywordNow);
	public final Rule NOT           = Sequence(IgnoreCase(KeywordNot), Spacing());
	public final Rule PATHSEPARATOR = Ch('/');
	public final Rule SELECT        = Sequence(IgnoreCase(KeywordSelect), Spacing());
	public final Rule SET           = Sequence(IgnoreCase(KeywordSet), Spacing());
	public final Rule TIMESTAMP     = IgnoreCase(KeywordTimestamp);
	public final Rule UPDATE        = Sequence(IgnoreCase(KeywordUpdate), Spacing());
	public final Rule USING         = Sequence(IgnoreCase(KeywordUsing), Spacing());
	public final Rule VARINDICATOR  = Ch('$');
	public final Rule VERSION       = Sequence(IgnoreCase(KeywordVersion), Spacing());
	public final Rule COMPARING       = Sequence(IgnoreCase(KeywordComparing), Spacing());
	public final Rule WAS_MODIFIED  = Sequence(IgnoreCase(KeywordWas), Spacing(), IgnoreCase(KeywordModified));
	public final Rule WHERE         = Sequence(IgnoreCase(KeywordWhere), Spacing());

	//
	// XAQLStatement
	//
	
	public Rule QueryStatement() {
		
		return Sequence(
				push(new XAQLToken(XAQLToken.QUERY_STATEMENT, new Vector<XAQLToken>())),
				SelectClause(),
				FromClause(),
				Optional(FirstOf(VersionClause(),ComparingVersionClause())),
				Optional(WhereClause()),
				Optional(Spacing()),
				EOI			
			);
	}

	public Rule XPathStatement() {
		
		return Sequence(
				push(new XAQLToken(XAQLToken.QUERY_STATEMENT, new Vector<XAQLToken>())),
				AbsoluteTargetPath(),
				EOI			
			);
	}

	//
	// SELECT
	//
	
	public Rule SelectClause() {
		
		return Sequence(
				SELECT,
				Spacing(),
				push(new XAQLToken(XAQLToken.SELECT_CLAUSE, new Vector<XAQLToken>())),
				SelectSubtree(),
				push(new XAQLToken(pop(1), pop()))
			);
	}
	
	public Rule SelectSubtree() {
		
		return Sequence(
				push(new XAQLToken(XAQLToken.SUBTREE_SELECT_CLAUSE, new Vector<XAQLToken>())),
				SubtreeSelectList(),
				Spacing(),
				push(new XAQLToken(pop(1), pop()))
			);
	}

	public Rule SubtreeSelectList() {
		
		return Sequence(
				push(new XAQLToken(XAQLToken.SUBTREE_SELECT_LIST, new Vector<XAQLToken>())),
				SubtreeSelectStatement(),
				ZeroOrMore(
					Sequence(
						Optional(Spacing()),
						Ch(','),
						Optional(Spacing()),
						SubtreeSelectStatement()
					)
				),
				push(new XAQLToken(pop(1), pop()))
			);
	}

	public Rule SubtreeSelectStatement() {
	
		return Sequence(
					push(new XAQLToken(XAQLToken.VARIABLE_SELECT_STATEMENT, new Vector<XAQLToken>())),
					VariableOptionalTargetPath(),
					Optional(
							Spacing(),
							AS,
							Spacing(),
							Identifier(),
							push(new XAQLToken(pop(), new XAQLToken(XAQLToken.SELECT_STATEMENT_LABEL, matchOrDefault(""))))
					),
					push(new XAQLToken(pop(1), pop()))
				);
	}
	

	//
	// FROM
	//
	
	public Rule FromClause() {
		
		return Sequence(
				push(new XAQLToken(XAQLToken.FROM_CLAUSE, new Vector<XAQLToken>())),
				FROM,
				Variable(),
				Spacing(),
				IN,
				AbsoluteTargetPath(),
				ZeroOrMore(
					Optional(Spacing()),
					',',
					Optional(Spacing()),
					Variable(),
					Spacing(),
					IN,
					VariableTargetPath()
				),
				Spacing(),
				push(new XAQLToken(pop(1), pop()))
			);
	}
	
	//
	// VERSION
	//
	
	public Rule VersionClause() {
		
		return Sequence(
				push(new XAQLToken(XAQLToken.VERSION_CLAUSE, new Vector<XAQLToken>())),
				VERSION,
				Timestamp(),
				Spacing(),
				push(new XAQLToken(pop(1), pop()))
			);
	}
	
	public Rule ComparingVersionClause() {
				
				return Sequence(
						push(new XAQLToken(XAQLToken.COMPARING_VERSION_CLAUSE, new Vector<XAQLToken>())),
						COMPARING, 
						VERSION,
						TimeInterval(),
						Spacing(),
						push(new XAQLToken(pop(1), pop()))
					);
			}
			
	public Rule TimeInterval() {
				return Sequence(
						FirstOf(NumericVersion(), DateVersion()),
						push(new XAQLToken(pop(), new XAQLToken(XAQLToken.TIMESTAMP_VALUE, matchOrDefault("")))),
						'-', 
					push(new XAQLToken(pop(), new XAQLToken(XAQLToken.TIMESTAMP_DELIMITER, matchOrDefault("")))),
						Optional(Spacing()),
						FirstOf(NumericVersion(), DateVersion(), NowVersion()),
						push(new XAQLToken(pop(), new XAQLToken(XAQLToken.TIMESTAMP_VALUE, matchOrDefault(""))))
					);
			
			}
		

	public Rule Timestamp() {
	
		return Sequence(
				FirstOf(NumericVersion(), DateVersion(), NowVersion()),
				push(new XAQLToken(pop(), new XAQLToken(XAQLToken.TIMESTAMP_VALUE, matchOrDefault("")))),
				ZeroOrMore(
						Optional(Spacing()),
						FirstOf('-', ','),
						push(new XAQLToken(pop(), new XAQLToken(XAQLToken.TIMESTAMP_DELIMITER, matchOrDefault("")))),
						Optional(Spacing()),
						Timestamp()
				)
			);
	}
	
	public Rule DateVersion() {
		
		return Sequence(
				AT, '(', '\'',
					DateLiteral(),
					'\'', ')'
				);
	}
	
	public Rule NumericVersion() {
		
		return Sequence(Digit(), ZeroOrMore(Digit()));
	}
	
	public Rule NowVersion() {
		
		return NOW;
	}

	//
	// WHERE
	//
	
	public Rule WhereClause() {
		
		return Sequence(
				push(new XAQLToken(XAQLToken.WHERE_CLAUSE, new Vector<XAQLToken>())),
				WHERE,
				Spacing(),
				WhereClauseExpression(),
				ZeroOrMore(Sequence(AND, WhereClauseExpression())),
				Spacing(),
				push(new XAQLToken(pop(1), pop()))
			);
	}
	
	public Rule WhereClauseExpression() {

		return Sequence(
				push(new XAQLToken(XAQLToken.WHERE_CLAUSE_EXPRESSION, new Vector<XAQLToken>())),
				Optional(NotOperator()),
				FirstOf(QuantifiedWhereClauseExpression(), CoincidesExpression()),
				push(new XAQLToken(pop(1), pop()))
			);
	}
	
	public Rule NotOperator() {
		
		return Sequence(
					NOT,
					push(new XAQLToken(pop(), new XAQLToken(XAQLToken.NOT_OPERATOR, matchOrDefault(""))))
				);
	}
	public Rule QuantifiedWhereClauseExpression() {
		
		return Sequence(
				Optional(Quantifier()),
				VariableOptionalTargetPath(),
				FirstOf(
					NodeValueExpression(),
					NodeExpression()
				)
			);
	}
	
	public Rule CoincidesExpression() {
		
		return Sequence(
				COINCIDES,
				'(',
				Optional(Spacing()),
				CoincidesExpressionList(),
				Optional(Spacing()),
				')',
				push(new XAQLToken(pop(1), pop()))
			);
	}

	public Rule CoincidesExpressionList() {
		
		return Sequence(
					push(new XAQLToken(XAQLToken.COINCIDES_EXPRESSION, new Vector<XAQLToken>())),
					CoincideValueExpression(),
					ZeroOrMore(
						AND,
						CoincideValueExpression()
					)
				);
	}
	
	public Rule CoincideValueExpression() {
		
		return Sequence(
					push(new XAQLToken(XAQLToken.COINCIDES_LIST_ELEMENT, new Vector<XAQLToken>())),
					VariableOptionalTargetPath(),
					NodeValueExpression(),
					push(new XAQLToken(pop(1), pop()))
				);
	}

	//
	// Path Expressions
	//
	
	public Rule Variable() {
		
		return Sequence(
				VARINDICATOR,
				Identifier(),
				push(new XAQLToken(pop(), new XAQLToken(XAQLToken.VARIABLE_NAME, matchOrDefault(""))))
			);
	}
	
	public Rule AbsoluteTargetPath() {
		
		return Sequence(
				PATHSEPARATOR,
				RelativeTargetPath()
			);
	}
	
	public Rule RelativeTargetPath() {
		
		return Sequence(
				push(new XAQLToken(XAQLToken.RELATIVE_TARGET_PATH, new Vector<XAQLToken>())),
				RelativeTargetPathExpression(),
				ZeroOrMore(AbsoluteTargetPathExpression()),
				Optional(PATHSEPARATOR),
				push(new XAQLToken(pop(1), pop()))
			);
	}
	
	public Rule VariableTargetPath() {
		
		return Sequence(
				push(new XAQLToken(XAQLToken.VARIABLE_TARGET_PATH, new Vector<XAQLToken>())),
				Variable(),
				OneOrMore(AbsoluteTargetPathExpression()),
				Optional(PATHSEPARATOR),
				push(new XAQLToken(pop(1), pop()))
			);
	}
	
	public Rule VariableOptionalTargetPath() {
		
		return Sequence(
				push(new XAQLToken(XAQLToken.VARIABLE_TARGET_PATH, new Vector<XAQLToken>())),
				Variable(),
				ZeroOrMore(AbsoluteTargetPathExpression()),
				Optional(PATHSEPARATOR),
//				Optional(AsExpression()),
				push(new XAQLToken(pop(1), pop()))
			);
	}

//	public Rule AsExpression() {
//		return Sequence(
//				Spacing(),
//				AS,
//				Spacing(),
//				Identifier()
//				);
//	}
	
	public Rule AbsoluteTargetPathExpression() {
		
		return Sequence(
				PATHSEPARATOR,
				RelativeTargetPathExpression()
			);
	}
	
	public Rule RelativeTargetPathExpression() {
		
		return Sequence(
				push(new XAQLToken(XAQLToken.TARGET_PATH_EXPRESSION, new Vector<XAQLToken>())),
				Identifier(),
				push(new XAQLToken(pop(), new XAQLToken(XAQLToken.ENTITY_LABEL, matchOrDefault("")))),
				Optional(FirstOf(IndexCondition(), SubPathCondition())),
				push(new XAQLToken(pop(1), pop()))
			);
	}

	public Rule IndexCondition() {
		
		return Sequence(
				push(new XAQLToken(XAQLToken.INDEX_CONDITION, new Vector<XAQLToken>())),
				Ch(':'),
				IntegerLiteral(),
				push(new XAQLToken(pop(), new XAQLToken(XAQLToken.INDEX_VALUE, matchOrDefault("")))),
				push(new XAQLToken(pop(1), pop()))
			);
	}
	
	public Rule SubPathCondition() {
		
		return Sequence(
				Ch('['),
				Optional(Spacing()),
				push(new XAQLToken(XAQLToken.SUB_PATH_CONDITION, new Vector<XAQLToken>())),
				SubPathConditionExpression(),
				ZeroOrMore(
						Spacing(),
						AND,
						Spacing(),
						SubPathConditionExpression()
					),
				Optional(Spacing()),
				Ch(']'),
				push(new XAQLToken(pop(1), pop()))
			);
	}
	
	public Rule SubPathConditionExpression() {
				
		return Sequence(
				push(new XAQLToken(XAQLToken.SUB_PATH_CONDITION_EXPRESSION, new Vector<XAQLToken>())),
				Optional(NotOperator()),
				Optional(Quantifier()),
				RelativeTargetPath(),
				FirstOf(
					NodeExpression(),
					NodeValueExpression()
				),
				push(new XAQLToken(pop(1), pop()))
			);
	}

	public Rule NodeValueExpression() {
		
		return Sequence(
				Optional(
					Spacing(),
					ALL_VALUES,
					push(new XAQLToken(pop(), new XAQLToken(XAQLToken.FOR_ALL_VALUES_QUANTIFIER, KeywordAll)))

				),
				FirstOf(ValueExpression(), StringMatchExpression())
			);
	}
	
	public Rule ValueExpression() {
		
		return FirstOf(LNGEQExpression(), InExpression());
	}
	
	public Rule LNGEQExpression() {
		
		return Sequence(
				Optional(Spacing()),
				LGNEQOperator(),
				push(new XAQLToken(pop(), new XAQLToken(XAQLToken.LGNEQ_OPERATOR, matchOrDefault("")))),
				Optional(Spacing()),
				FirstOf(StringLiteral(), NumericLiteral()),
				push(new XAQLToken(pop(), new XAQLToken(XAQLToken.VALUE, matchOrDefault(""))))
			);

	}

	public Rule LGNEQOperator() {
		
		return FirstOf(EQ, NEQ, GT, GEQ, LT, LEQ);
	}

	public Rule InExpression() {
		
		return Sequence(
					Spacing(),
					IN,
					push(new XAQLToken(pop(), new XAQLToken(XAQLToken.IN_OPERATOR, ValueOpFactory.IN))),
					Optional(Spacing()),
					'(',
					StringLiteral(),
					push(new XAQLToken(pop(), new XAQLToken(XAQLToken.VALUE, matchOrDefault("")))),
					ZeroOrMore(
							Optional(Spacing()),
							',',
							Optional(Spacing()),
							StringLiteral(),
							push(new XAQLToken(pop(), new XAQLToken(XAQLToken.VALUE, matchOrDefault(""))))
						),
					')'
				);
	}
	
	public Rule StringMatchExpression() {
		
		return Sequence(
				Spacing(),
				StringPatternOperator(),
				Spacing(),
				StringLiteral(),
				push(new XAQLToken(pop(), new XAQLToken(XAQLToken.VALUE, matchOrDefault(""))))
			);
	}

	public Rule StringPatternOperator() {
		
		return FirstOf(
					Sequence(
						LIKE,
						push(new XAQLToken(pop(), new XAQLToken(XAQLToken.LIKE_OPERATOR, matchOrDefault(""))))
					),
					Sequence(
						MATCHES,
						push(new XAQLToken(pop(), new XAQLToken(XAQLToken.MATCHES_OPERATOR, matchOrDefault(""))))
					)
				);
	}

	public Rule NodeExpression() {
		
		return Sequence(
				Spacing(),
				FirstOf(
					Sequence(
						EXISTS,
						push(new XAQLToken(pop(), new XAQLToken(XAQLToken.EXISTS_PREDICATE)))
					),
					Sequence(
						HAS_CHANGES,
						push(new XAQLToken(pop(), new XAQLToken(XAQLToken.HAS_CHANGES_PREDICATE))),
						Optional(Sequence(Spacing(), ProvenanceExpression()))
						),
					Sequence(
						WAS_MODIFIED,
						push(new XAQLToken(pop(), new XAQLToken(XAQLToken.WAS_MODIFIED_PREDICATE))),
						Optional(Sequence(Spacing(), ProvenanceExpression()))
					)
				)
			);
	}
	
	public Rule ProvenanceExpression() {
		
		return Sequence(
				push(new XAQLToken(XAQLToken.PROVENANCE_EXPRESSION, new Vector<XAQLToken>())),
				FirstOf(
					ProvenanceDateExpression(),
					ProvenanceUserExpression()
				),
				push(new XAQLToken(pop(1), pop()))
			);
	}
	
	public Rule ProvenanceDateExpression() {
		
		return Sequence(
				FirstOf(
					ProvenanceSingleDateExpression(),
					ProvenanceDateRangeExpression()
				),
				Optional(Sequence(Spacing(), ProvenanceByUserExpression())),
				Optional(Sequence(Spacing(), ProvenanceUsingExpression()))
			);
	}
	
	public Rule ProvenanceSingleDateExpression() {
		
		return Sequence(
				FirstOf(
					IgnoreCase(KeywordAfter),
					IgnoreCase(KeywordBefore),
					IgnoreCase(KeywordSince),
					IgnoreCase(KeywordUntil)
				),
				push(new XAQLToken(pop(), new XAQLToken(XAQLToken.PROVENANCE_DATE_OPERATOR, matchOrDefault("")))),
				Spacing(),
				DateLiteral(),
				push(new XAQLToken(pop(), new XAQLToken(XAQLToken.PROVENANCE_DATE, matchOrDefault(""))))
			);
	}
	
	public Rule ProvenanceDateRangeExpression() {
		
		return Sequence(
				IgnoreCase(KeywordBetween),
				push(new XAQLToken(pop(), new XAQLToken(XAQLToken.PROVENANCE_DATE_OPERATOR, matchOrDefault("")))),
				Spacing(),
				DateLiteral(),
				push(new XAQLToken(pop(), new XAQLToken(XAQLToken.PROVENANCE_DATE, matchOrDefault("")))),
				Spacing(),
				IgnoreCase(KeywordAnd),
				Spacing(),
				DateLiteral(),
				push(new XAQLToken(pop(), new XAQLToken(XAQLToken.PROVENANCE_DATE, matchOrDefault(""))))
			);
	}

	public Rule ProvenanceUserExpression() {
		
		return Sequence(
				ProvenanceByUserExpression(),
				Optional(Sequence(Spacing(), ProvenanceUsingExpression()))
			);
	}
	
	public Rule ProvenanceByUserExpression() {
		
		return Sequence(
				BY,
				UserName(),
				push(new XAQLToken(pop(), new XAQLToken(XAQLToken.PROVENANCE_USER_NAME, matchOrDefault(""))))
			);
	}
	
	public Rule ProvenanceUsingExpression() {
		
		return Sequence(
				USING,
				FirstOf(
					IgnoreCase(KeywordCopy),
					IgnoreCase(KeywordDelete),
					IgnoreCase(KeywordInsert),
					IgnoreCase(KeywordUpdate)
				),
				push(new XAQLToken(pop(), new XAQLToken(XAQLToken.PROVENANCE_OPERATION, matchOrDefault(""))))
			);
	}
	
	public Rule UserName() {
		
		return StringLiteral();
	}
	
	
	//
	// Quantifier
	//
	
	public Rule Quantifier() {
		
		return Sequence(
				FirstOf(ForAllQuantifier(), ForAnyQuantifier()),
				Spacing()
			);
	}
	
	public Rule ForAllQuantifier() {
		
		return Sequence(
				FOR_ALL,
				push(new XAQLToken(pop(), new XAQLToken(XAQLToken.FOR_ALL_QUANTIFIER)))
			);
	}
	
	public Rule ForAnyQuantifier() {
		
		return Sequence(
				FOR_ANY,
				push(new XAQLToken(pop(), new XAQLToken(XAQLToken.FOR_ANY_QUANTIFIER)))
			);
	}

	//
	// Helpers
	//
	
    
	public Rule BinaryExponent() {
		
		return Sequence(AnyOf("pP"), Optional(AnyOf("+-")), OneOrMore(Digit()));
	}
	
	public Rule DecimalFloat() {
		
		return FirstOf(
				Sequence(OneOrMore(Digit()), '.', ZeroOrMore(Digit()), Optional(Exponent()), Optional(AnyOf("fFdD"))),
				Sequence('.', OneOrMore(Digit()), Optional(Exponent()), Optional(AnyOf("fFdD"))),
				Sequence(OneOrMore(Digit()), Exponent(), Optional(AnyOf("fFdD"))),
				Sequence(OneOrMore(Digit()), Optional(Exponent()), AnyOf("fFdD"))
			);
	}

	public Rule DecimalNumeral() {
		
		return FirstOf('0', Sequence(CharRange('1', '9'), ZeroOrMore(Digit())));
	}

	public Rule Digit() {
		
		return CharRange('0', '9');
	}
	
	public Rule DateLiteral() {
		
		return Sequence(
					Digit(),
					Digit(),
					Digit(),
					Digit(),
					'-',
					Digit(),
					Digit(),
					'-',
					Digit(),
					Digit(),
					Optional(
							Spacing(),
							TimeLiteral()
						)
				);
	}

	public Rule Escape() {
    	
    	return Sequence('\\', AnyOf("btnfr\"\'\\"));
    }

	public Rule Exponent() {
		
		return Sequence(AnyOf("eE"), Optional(AnyOf("+-")), OneOrMore(Digit()));
	}

	public Rule HexFloat() {
		
        return Sequence(HexSignificant(), BinaryExponent(), Optional(AnyOf("fFdD")));
	}

	public Rule FloatLiteral() {
		
		return FirstOf(HexFloat(), DecimalFloat());
	}

	public Rule HexDigit() {
		
		return FirstOf(CharRange('a', 'f'), CharRange('A', 'F'), CharRange('0', '9'));
	}

	public Rule HexNumeral() {
		
        return Sequence('0', IgnoreCase('x'), OneOrMore(HexDigit()));
	}

	public Rule HexSignificant() {
		
		return FirstOf(
			Sequence(FirstOf("0x", "0X"), ZeroOrMore(HexDigit()), '.', OneOrMore(HexDigit())),
			Sequence(HexNumeral(), Optional('.'))
		);
	}

	public Rule Identifier() {
    	
    	return Sequence(Letter(), ZeroOrMore(LetterOrDigitOrUnderline()));
	}

	public Rule IntegerLiteral() {
		
		return Sequence(FirstOf(HexNumeral(), OctalNumeral(), DecimalNumeral()), Optional(AnyOf("lL")));
	}

	public Rule Letter() {

    	return FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'));
    }

	public Rule LetterOrDigitOrUnderline() {

    	return FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'), CharRange('0', '9'), '_');
    }

	public Rule NumericLiteral() {
    	
    	return FirstOf(
                FloatLiteral(),
                IntegerLiteral()
    		);
    }

	public Rule OctalNumeral() {
		
		return Sequence('0', OneOrMore(CharRange('0', '7')));
	}

	public Rule Spacing() {
    	
        return ZeroOrMore(FirstOf(

                // whitespace
                OneOrMore(AnyOf(" \t\r\n\f").label("Whitespace")),

                // traditional comment
                Sequence("/*", ZeroOrMore(TestNot("*/"), ANY), "*/"),

                // end of line comment
                Sequence(
                        "//",
                        ZeroOrMore(TestNot(AnyOf("\r\n")), ANY),
                        FirstOf("\r\n", '\r', '\n', EOI)
                )
        ));
    }
    
	public Rule StringLiteral() {
    	
    	return Sequence(
                '\'',
                ZeroOrMore(
                        FirstOf(
                                Escape(),
                                Sequence(TestNot(AnyOf("\r\n\'\\")), ANY)
                        )
                ),
                '\''
        );
    }
	
	public Rule Filename() {
    	
    	return ZeroOrMore(
    				Sequence(TestNot(AnyOf("\r\n\'")), ANY)
                );
    }

	public Rule XMLText() {
    	
    	return Sequence(
                '<',
                ZeroOrMore(
                        FirstOf(
                                Escape(),
                                Sequence(TestNot(AnyOf("\r\n\'\\")), ANY)
                        )
                )
        );
    }

	public Rule TimeLiteral() {
		
		return Sequence(
				Digit(),
				Optional(Digit()),
				':',
				Digit(),
				Digit(),
				Optional(
						':',
						Digit(),
						Digit(),
						Optional(
								':',
								Digit(),
								Digit(),
								Digit()
							)
					)
			);
	}
}
