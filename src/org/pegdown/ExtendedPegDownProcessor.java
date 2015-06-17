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
 * Copyright (C) 2010 Mathias Doenitz
 *
 * Based on peg-markdown (C) 2008-2010 John MacFarlane
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pegdown;

import org.parboiled.Parboiled;
import org.parboiled.support.ParsingResult;
import org.pegdown.ast.Node;

import static org.parboiled.errors.ErrorUtils.printParseErrors;

/**
 * A clean and lightweight Markdown-to-HTML filter based on a PEG parser implemented with parboiled.
 *
 * @see <a href="http://daringfireball.net/projects/markdown/">Markdown</a>
 * @see <a href="http://www.parboiled.org/">parboiled.org</a>
 */
public class ExtendedPegDownProcessor {
    /**
     * Defines the number of spaces in a tab, can be changed externally if required.
     */
    public static int TABSTOP = 4;

    private final Parser parser;
    private ParsingResult<Node> lastParsingResult;

    /**
     * Creates a new processor instance without any enabled extensions.
     */
    public ExtendedPegDownProcessor() {
        this(Extensions.NONE);
    }

    /**
     * Creates a new processor instance with the given {@link org.pegdown.Extensions}.
     *
     * @param options the flags of the extensions to enable as a bitmask
     */
    //@SuppressWarnings({"unchecked"})
    public ExtendedPegDownProcessor(int options) {
        parser = Parboiled.createParser(Parser.class, options);
    }

    /**
     * Returns the underlying parboiled parser object
     *
     * @return the parser
     */
    Parser getParser() {
        return parser;
    }

    ParsingResult<Node> getLastParsingResult() {
        return lastParsingResult;
    }

    /**
     * Converts the given markdown source to HTML.
     *
     * @param markdownSource the markdown source to convert
     * @param 
     * @return the HTML
     */
    public String markdownToHtml(String markdownSource, Object extension) {
        parser.references.clear();
        parser.abbreviations.clear();

        lastParsingResult = parser.parseRawBlock(prepare(markdownSource));
        if (lastParsingResult.hasErrors()) {
            throw new RuntimeException("Internal error during markdown parsing:\n--- ParseErrors ---\n" +
                    printParseErrors(lastParsingResult)/* +
                    "\n--- ParseTree ---\n" +
                    printNodeTree(result)*/
            );
        }

        ExtendedPrinter printer = new ExtendedPrinter(parser.references, parser.abbreviations, extension);

        lastParsingResult.resultValue.print(printer);
        printer.println();

        return printer.getString();
    }

    // perform tabstop expansion and add two trailing newlines

    static String prepare(String markDownSource) {
        StringBuilder sb = new StringBuilder(markDownSource.length() + 2);
        int charsToTab = TABSTOP;
        for (int i = 0; i < markDownSource.length(); i++) {
            char c = markDownSource.charAt(i);
            switch (c) {
                case '\t':
                    while (charsToTab > 0) {
                        sb.append(' ');
                        charsToTab--;
                    }
                    break;
                case '\n':
                    sb.append('\n');
                    charsToTab = TABSTOP;
                    break;
                default:
                    sb.append(c);
                    charsToTab--;
            }
            if (charsToTab == 0) charsToTab = TABSTOP;
        }
        sb.append('\n');
        sb.append('\n');
        return sb.toString();
    }

}
