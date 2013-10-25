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
package org.dbwiki.web.ui.printer;

import java.io.FileNotFoundException;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.html.HtmlLinePrinter;
import org.dbwiki.web.request.WikiDataRequest;
import org.dbwiki.web.ui.CSS;

/** Prints out a version index as a form that can be used to visit past versions
 * 
 * @author jcheney
 *
 */
public class VisualisationPrinter implements HtmlContentPrinter {
	/*
	 * Private Variables
	 */
		
	/*
	 * Constructors
	 */
	private WikiDataRequest request; 

	public VisualisationPrinter(WikiDataRequest request) {
		this.request=request;
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void print(HtmlLinePrinter body) throws org.dbwiki.exception.WikiException, FileNotFoundException {
		if (this.request.type().isHistoryTree())
			body.paragraph("History Tree", CSS.CSSHeadline);
		else if (this.request.type().isStructureTree())
			body.paragraph("Structure Tree", CSS.CSSHeadline);
		
	
		String nodeId = request.wri().getURL().substring(request.wri().getURL().lastIndexOf("/")+1);
		body.add("<h2>Survey can be found <a href='http://www.rationalsurvey.com/s/4748'>here</a></h2>");
		body.add("<applet  archive='/jar/visualisation.jar, /jar/grappa1_2.jar' code='org.dbwiki.web.applet.TreeVisualiser.class'  width='100%' height='600'>");

		if (this.request.type().isHistoryTree())
			body.add("<PARAM name=\"typeParameter\" value=\""+RequestParameter.ParameterExportHistoryDOT+"\">");
		else if (this.request.type().isStructureTree())
			body.add("<PARAM name=\"typeParameter\" value=\""+RequestParameter.ParameterExportStructureDOT+"\">");
		
		body.add("<PARAM name=\"nodeId\" value=\""+nodeId+"\">");
		body.add("</applet>");

	}
	
	
	/*
	 * Private Methods
	 */
	

}
