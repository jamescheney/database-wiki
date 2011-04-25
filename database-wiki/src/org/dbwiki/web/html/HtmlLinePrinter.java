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
package org.dbwiki.web.html;

public class HtmlLinePrinter  {
	/*
	 * Private Constant
	 */
	
	private static final String indentExtension = "\t";
	
	
	/*
	 * Private Variables
	 */
	
	private String _indentation;
	private LineSet _lines;
	
	
	/*
	 * Constructors
	 */
	
	public HtmlLinePrinter(LineSet lines, String indentation) {
		_lines = lines;
		
		_indentation = indentation;
	}
	
	public HtmlLinePrinter(LineSet lines) {
		this(lines, "");
	}

	public HtmlLinePrinter() {
		this(new LineSet(), "");
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void add(String line) {
		_lines.add(line);
	}
	
	public void add(LineSet lines) {
		for (int iLine = 0; iLine < lines.size(); iLine++) {
			this.add(_indentation + lines.get(iLine));
		}
	}
	
	public void addAnnotationIndicator() {
		this.text("<img src=\"/pictures/annotation.gif\">");
	}
	
	public void addBR() {
		this.add(_indentation + indentExtension + "<br>");
	}

	public void addBUTTON(String type, String name, String src) {
		this.add(_indentation + indentExtension + "<input type=\"" + type + "\" name=\"" + name + "\" src=\"" + src + "\"/>");
	}

	public void addBUTTON(String type, String name, String value, String src) {
		this.add(_indentation + indentExtension + "<input type=\"" + type + "\" name=\"" + name + "\" value=\"" + value + "\" src=\"" + src + "\"/>");
	}
	
	public void addHIDDEN(String name, String value) {
		this.add(_indentation + indentExtension + "<input value=\"" + value + "\" name=\"" + name +"\" type=\"hidden\"/>");
	}
	
	public void addLINE(String line) {
		this.add(_indentation + indentExtension + line);
	}
	
	public void addOPTION(String label, String value, boolean selected) {
		String line = null;
		if (selected) {
			line = "<option value=\"" + value + "\" selected=\"selected\">" + label + "</option>";
		} else {
			line = "<option value=\"" + value + "\">" + label + "</option>";
		}
		this.add(_indentation + indentExtension + line);
	}
	
	
	public void addRADIOBUTTON(String label, String name, String value, boolean selected) {
		String line = "<input type=\"radio\" name=\"" + name + "\" value=\"" + value + "\"";
		if (selected) {
			line = line + " CHECKED";
		}
		line = line + "/>" + label;
		this.add(_indentation + indentExtension + line);
	}

	public void addSPAN(String text, String cssClass) {
		this.add(_indentation + indentExtension + "<span class=\"" + cssClass + "\">" + text + "</span>");
	}

	public String attachSPAN(String prefix, String text, String cssClass) {
		return prefix + " <span class=\"" + cssClass + "\">" + text + "</span>";
	}

	public void addTEXTAREA(String name, String cols, String rows, boolean wrap, String value) {
		if (!wrap) {
			this.add(_indentation + indentExtension + "<textarea name=\"" + name + "\" cols=" + cols + " rows=" + rows + " wrap=OFF>" + value + "</textarea>");
		} else {
			this.add(_indentation + indentExtension + "<textarea name=\"" + name + "\" cols=" + cols + " rows=" + rows + ">" + value + "</textarea>");
		}
	}

	public void addTEXTAREA(String name, String cols, String value) {
		this.add(_indentation + indentExtension + "<textarea name=\"" + name + "\" cols=" + cols + " style=height:1.2em>" + value + "</textarea>");
	}

	public void addTEXTBOX(String name) {
		this.add(_indentation + indentExtension + "<input type=\"text\" name=\"" + name + "\"/>");
	}
	
	public void addFILE(String name) {
		this.add(_indentation + indentExtension + "<input type=\"file\" name=\"" + name + "\"/>");
	}
	public void addSCRIPTSubmitForm(String scriptName, String formName) {
		this.add(_indentation + indentExtension + "<script type=\"text/javascript\">");
		this.add(_indentation + indentExtension + "function " + scriptName + "()");
		this.add(_indentation + indentExtension + "{");
		this.add(_indentation + indentExtension + indentExtension + "document." + formName + ".submit();");
		this.add(_indentation + indentExtension + "}");
		this.add(_indentation + indentExtension + "</script>");
	}
	
	public void closeCENTER() {
		this.closeElement("center");
	}

	public void closeDIV() {
		this.add(_indentation + indentExtension + "</div>");
	}

	public void closeFORM() {
		this.closeElement("form");
	}

	public void closeLIST() {
		this.closeElement("ul");
	}

	public void closeOPTGROUP() {
		this.closeElement("optgroup");
	}

	public void closePARAGRAPH() {
		this.add(_indentation + indentExtension + "</p>");
	}

	public void closeSELECT() {
		this.closeElement("select");
	}

	public void closeTABLE() {
		this.closeElement("table");
	}
	
	public void closeTD() {
		this.closeElement("td");
	}
	
	public void closeTH() {
		this.closeElement("th");
	}
	
	public void closeTR() {
		this.closeElement("tr");
	}
	
	public String get(int index) {
		return _lines.get(index);
	}
	
	public void h1(String text, String cssClass) {
		this.add(_indentation + indentExtension + "<h1 class=\"" + cssClass + "\">" + text + "</h1>");
	}

	
	public void h1(String target, String text, String cssClass) {
		this.add(_indentation + indentExtension + "<h1 class=\"" + cssClass + "\"><a class=\"" + cssClass + "\" href=\"" + target + "\">" + text + "</a></h1>");
	}

	public LineSet lines() {
		return _lines;
	}
	
	public void link(String target, String text, String cssClass) {
		this.add(_indentation + indentExtension + this.getLink(target, text, cssClass));
	}

	public void linkWithTitle(String target, String title, String text, String cssClass) {
		String line = "<a";
		if (cssClass != null) {
			line = line + " class=\"" + cssClass + "\"";
		}
		this.add(_indentation + indentExtension + line + " href=\"" + target + "\" title=\"" + title + "\">" + text + "</a> ");
	}

	public void linkWithOnClick(String target, String onclick, String text, String cssClass) {
		String line = "<a";
		if (cssClass != null) {
			line = line + " class=\"" + cssClass + "\"";
		}
		this.add(_indentation + indentExtension + line + " href=\"" + target + "\" onclick=\"" + onclick + "\">" + text + "</a> ");
	}

	public void linkWithAddition(String target, String text, String addition, String cssClass) {
		this.add(_indentation + indentExtension + this.getLink(target, text, addition, cssClass));
	}

	public void link(String target, String text) {
		this.link(target, text, null);
	}

	public void listITEM(String text, String cssClass) {
		this.add(_indentation + indentExtension + "<li class=\"" + cssClass + "\">" + text + "</li>");
	}

	public void listITEM(String target, String text, String cssClass) {
		this.add(_indentation + indentExtension + "<li class=\"" + cssClass + "\"><a class=\"" + cssClass + "\" href=\"" + target + "\">" + text + "</a></li>");
	}

	public void openCENTER() {
		this.openElement("<center>");
	}

	public void openDIVID(String cssClass, String style) {
		this.add(_indentation + indentExtension + "<div id=\"" + cssClass + "\" style=\"display:" + style + "\">");
	}
	
	public void openFORM(String name, String method, String action, String onChange) {
		this.openElement("<form name=\"" + name + "\" method=\"" + method + "\" action=\"" + action + "\" onChange=\"" + onChange + "\">");
	}
	
	public void openFORM(String name, String method, String action) {
		this.openElement("<form name=\"" + name + "\" method=\"" + method + "\" action=\"" + action + "\">");
	}
	
	public void openLIST(String cssClass) {
		this.openElement("<ul class=\"" + cssClass + "\">");
	}
	
	public void openOPTGROUP(String label) {
		this.openElement("<optgroup label=\"" + label + "\">");
	}

	public void openOPTGROUP() {
		this.openElement("<optgroup>");
	}

	public void openPARAGRAPH(String cssClass) {
		this.add(_indentation + indentExtension + "<p class=\"" + cssClass + "\">");
	}
	
	public void openSELECT(String name) {
		this.openElement("<select name=\"" + name + "\">");
	}

	public void openTABLE(String cssClass) {
		this.openTableElement("table", cssClass);
	}
	
	public void openTABLE() {
		this.openTableElement("table", null);
	}

	public void openTD(String cssClass) {
		this.openTableElement("td", cssClass);
	}
	
	public void openTD() {
		this.openTableElement("td", null);
	}

	public void openTH(String cssClass) {
		this.openTableElement("th", cssClass);
	}
	
	public void openTH() {
		this.openTableElement("th", null);
	}

	public void openTR(String cssClass) {
		this.openTableElement("tr", cssClass);
	}
	
	public void openTR() {
		this.openTableElement("tr", null);
	}
	
	public void openUPLOADFORM(String name, String method, String action) {
		this.openElement("<form ENCTYPE=\"multipart/form-data\" name=\"" + name + "\" method=\"" + method + "\" action=\"" + action + "\">");
	} 

	public void paragraph(String target, String text, String cssClass) {
		this.add(_indentation + indentExtension + "<p class=\"" + cssClass + "\"><a class=\"" + cssClass + "\" href=\"" + target + "\">" + text + "</a></p>");
	}
	
	public void paragraph(String text, String cssClass) {
		this.add(_indentation + indentExtension + "<p class=\"" + cssClass + "\">" + text + "</p>");
	}
	
	public void row(String target, String text, String cssClass) {
		String line = this.getOpenElement("tr", cssClass) + this.getOpenElement("td", cssClass);
		if (target != null) {
			line = line + this.getLink(target, text, cssClass);
		} else {
			line = line + text;
		}
		line = line + "</td></tr>";
		this.add(_indentation + indentExtension + line);
	}

	public void row(String text, String cssClass) {
		this.row(null, text, cssClass);
	}

	public int size() {
		return _lines.size();
	}
	
	public void text(String text) {
		this.add(_indentation + indentExtension + text);
	}
	
	
	/*
	 * Private Methods
	 */
	
	private void closeElement(String line) {
		this.add(_indentation + "</" + line + ">");
		_indentation = _indentation.substring(0, _indentation.length() - indentExtension.length());
	}

	private String getLink(String target, String text, String cssClass) {
		String line = "<a";
		if (cssClass != null) {
			line = line + " class=\"" + cssClass + "\"";
		}
		return line + " href=\"" + target + "\">" + text + "</a>";
	}
	
	private String getLink(String target, String text, String addition, String cssClass) {
		String line = "<a";
		if (cssClass != null) {
			line = line + " class=\"" + cssClass + "\"";
		}
		return line + " href=\"" + target + "\">" + text + "</a> " + addition;
	}
	
	private String getOpenElement(String elementName, String cssClass) {
		if (cssClass != null) {
			return "<" + elementName +" class=\"" + cssClass + "\">";
		} else {
			return "<" + elementName + ">";
		}
	}
	
	private void openElement(String line) {
		_indentation = _indentation + indentExtension;
		this.add(_indentation + line);
	}

	private void openTableElement(String elementName, String cssClass) {
		this.openElement(this.getOpenElement(elementName, cssClass));
	}
}
