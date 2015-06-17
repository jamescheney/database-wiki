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

package org.pegdown.ast;

import org.pegdown.Printer;

public class SimpleNode extends Node implements SimpleNodeTypes {
    private final int type;

    public SimpleNode(int type) {
        this.type = type;
    }

    @Override
    public void print(Printer p) {
        switch (type) {
            case APOSTROPHE:
            case ELLIPSIS:
            case EMDASH:
            case ENDASH:
            case NBSP:
                p.print(getText());
                return;
            case HRULE:
                p.printOnNL("<hr/>");
                return;
            case LINEBREAK:
                p.print("<br/>").println();
                return;
            default:
        		throw new IllegalStateException();
        }
    }

        

    @Override
    public String getText() {
        switch (type) {
            case APOSTROPHE:
                return "&rsquo;";
            case ELLIPSIS:
                return "&hellip;";
            case EMDASH:
                return "&mdash;";
            case ENDASH:
                return "&ndash;";
            case HRULE:
                return "<hr/>";
            case LINEBREAK:
                return "<br/>";
            case NBSP:
                return "&nbsp;";
            default:
            	throw new IllegalStateException();
        }
        
    }

}