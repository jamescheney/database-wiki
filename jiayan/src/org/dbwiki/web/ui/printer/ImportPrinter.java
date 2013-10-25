package org.dbwiki.web.ui.printer;

import org.dbwiki.exception.WikiException;
import org.dbwiki.web.html.HtmlLinePrinter;

/** Prints out the JavaScript imports.  
 * 
 * 
 * @author jcheney
 *
 */
public class ImportPrinter implements HtmlContentPrinter {

	@Override
	public void print(HtmlLinePrinter printer) throws WikiException {
		
		printer.text("<script type=\"text/javascript\" src=\"http://maps.google.com/maps/api/js?sensor=false&language=en\"></script>");
		printer.text("<script type=\"text/javascript\" src=\"http://jqueryjs.googlecode.com/files/jquery-1.2.6.min.js\"></script>");
		printer.text("<script type=\"text/javascript\" src=\"http://code.jquery.com/jquery-latest.js\"></script>");
		printer.text("<script type=\"text/javascript\" src=\"http://www.google.com/jsapi\"></script>");
		printer.text("<script type=\"text/javascript\" src=\"/js/dialog.js\"></script>");
		printer.text("<script type=\"text/javascript\" src=\"/js/visualisation.js\"></script>");
	//	printer.text("<script type=\"text/javascript\" src=\"http://maps.googleapis.com/maps/api/js?key=AIzaSyAtI5JtBqhE5EgYXgbAxJJITULbC8-4L-4&sensor=true&callback=drawmap\"></script>");


	}

}
