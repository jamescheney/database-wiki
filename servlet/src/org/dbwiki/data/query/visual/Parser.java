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
package org.dbwiki.data.query.visual;

import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.annotations.SkipActionsInPredicates;
import org.parboiled.common.Factory;
import org.parboiled.parserunners.ParseRunner;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;


@SkipActionsInPredicates
@BuildParseTree
public class Parser extends BaseParser<Node> {
	Rule Query() {
		return Sequence(
				VisualisationType(),
				Arguments(),
				":",
				Body(),
				push(new VisualisationNode((VisualisationTypeNode)pop(2),
											(ArgumentsNode)pop(1),
											(BodyNode)pop())));
	}
	
	Rule Body() {
		return Sequence(
				ANY,
				push(new BodyNode(match())));
	}
	
	Rule VisualisationType() {
		return Sequence(
				ZeroOrMore(TestNot("(:")),
				push(new VisualisationTypeNode(match())));
	}
	
	Rule Arguments() {
		return Sequence(
				push(new ArgumentsNode()),
				FirstOf(Sequence(
						'(',
						ZeroOrMore(Argument()),
						')'),
						EMPTY));			
	}
	
	Rule Argument() {
		return Sequence(
				SkipSpace(),
				FirstOf(DoubleQuotedArg(),
						PlainArg()),
				((ArgumentsNode)peek(0)).add((ArgumentNode)pop()),
				SkipSpace(),
				",");
	}
	
	Rule PlainArg() {
		return Sequence(
				ZeroOrMore(TestNot(Spacechar(), ",")),
				push(new ArgumentNode(match())));
	}
	
    Rule DoubleQuotedArg() {
        return Sequence(
                '"',
                ZeroOrMore(TestNot('"')),
                push(new ArgumentNode(match())),
                '"');
    }
	
    Rule SkipSpace() {
        return ZeroOrMore(Spacechar());
    }
    
    Rule Spacechar() {
        return AnyOf(" \t\n\r");
    }
    
    public Factory<ParseRunner<Node>> parseRunnerFactory = new Factory<ParseRunner<Node>>() {
        public ParseRunner<Node> create() {
            return new ReportingParseRunner<Node>(Query());
        }
    };
    
    public ParsingResult<Node> parse(String text) {
        ParsingResult<Node> result = parseRunnerFactory.create().run(text);
        if (!result.matched) {
            String errorMessage = "Internal error";
            if (result.hasErrors()) errorMessage += ": " + result.parseErrors.get(0);
            throw new RuntimeException(errorMessage);
        }
        return result;
    }
}
