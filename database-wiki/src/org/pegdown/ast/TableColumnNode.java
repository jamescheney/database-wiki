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

public class TableColumnNode extends Node {
    private int alignment;

    public boolean markLeftAligned() {
        alignment |= 0x01;
        return true;
    }

    public boolean markRightAligned() {
        alignment |= 0x02;
        return true;
    }
    
    public void printAlignment(Printer printer) {
        switch (alignment) {
            case 0x00:
                return;
            case 0x01:
                printer.print(" align=\"left\"");
                return;
            case 0x02:
                printer.print(" align=\"right\"");
                return;
            case 0x03:
                printer.print(" align=\"center\"");
                return;
            default:
                throw new IllegalStateException();
        }
    }

}