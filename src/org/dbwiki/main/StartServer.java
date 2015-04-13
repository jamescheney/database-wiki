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
package org.dbwiki.main;

import java.io.File;

import org.dbwiki.web.server.WikiServer;

/** Starts the server.
 *
 * @author jcheney
 *
 */
public class StartServer {
    /*
     * Private Constants
     */
    private static WikiServer server;

    private static final String commandLine = "StartServer <config-file>";

    private static String configFileName;
    /*
     * Public Methods
     */

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: " + commandLine);
            System.exit(0);
        }
        configFileName = args[0];

        try {
            server = new WikiServer(org.dbwiki.lib.IO.loadProperties(new File(configFileName)));
            server.start();
        } catch (Exception exception) {
            exception.printStackTrace();
            System.exit(0);
        }
    }

    public static void restartServer() {
        try {
            server.stop();
            Thread.sleep(2000);
            server = new WikiServer(org.dbwiki.lib.IO.loadProperties(new File(configFileName)));
            server.start();
        } catch (Exception exception) {
            exception.printStackTrace();
            System.exit(0);
        }
    }
}
