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
package org.dbwiki.data.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.Comparator;
import org.dbwiki.data.database.DatabaseAttributeNode;
import org.dbwiki.data.database.DatabaseElementNode;
import org.dbwiki.data.database.DatabaseGroupNode;
import org.dbwiki.data.database.DatabaseNode;
import org.dbwiki.data.database.DatabaseTextNode;
import org.dbwiki.data.time.TimeSequence;
import org.dbwiki.data.time.VersionIndex;
import org.dbwiki.exception.WikiException;
import org.dbwiki.web.request.WikiDataRequest;


/** Class providing the capability to write a node out as dot.
 */

public class ExportHistoryWriter {
	/*
	 * Private Variables
	 */
	//http://www.graphviz.org/content/color-names

	private final String outdatedEdgeColor="grey88";
	
	private final String aboveMainEdgeColor="lightblue3";
	private final String databaseColor="lightblue3";

	
	
	
	private final String selectedNodeThickness="4";
	

	private BufferedWriter _out;
	private String sGraph;
	private DatabaseNode mainNode;
	private VersionIndex versionIndex;
	private WikiDataRequest request;
	
	/*
	 * Public Methods
	 */

	
	public ExportHistoryWriter(WikiDataRequest request, BufferedWriter out, VersionIndex versionIndex) {
		this.request=request;
		_out = out;
		sGraph="";
		this.versionIndex=versionIndex;
	}


	public void setMainNode(DatabaseNode node){
		
		this.mainNode=node;
				
		
	}
	
	
	private String getId(DatabaseNode node) {
		return node.identifier().toURLString().replace("/", "i");
	}


	
	public void write(String value) throws java.io.IOException {
		_out.write(value);
	}
	
	public void writeln(String value) throws java.io.IOException {
		_out.write(value);
		_out.newLine();
	}
	private String getNodeLabel(DatabaseNode node) {
		String label;
		if (node.isElement()){
			DatabaseElementNode nonLeafNode=(DatabaseElementNode)node;
			label=nonLeafNode.label();
		}
		else{
			label="VALUE";
		}
		return label;
	}
	////////////////////////////Graph forming functions
	
	public void writeGroupNode(DatabaseGroupNode node, int lvl) throws org.dbwiki.exception.WikiException {
		if (lvl!=0)
			formGroupNode(node, false);
		else
			formSubtreeNode(node);
		formRelation(node, false);
	}
	
	public void writeSubtreeNode(DatabaseGroupNode node) throws org.dbwiki.exception.WikiException {
		
		formSubtreeNode(node);
		formRelation(node, false);
	}
	public void writeAttributeNode(DatabaseAttributeNode node, int lvl) throws org.dbwiki.exception.WikiException {
		if (request.type().isExportHistoryDot())
			writeAttributeHistoryNode(node,lvl);
		else if (request.type().isExportStructureDot())
			writeAttributeStructureNode(node, lvl);
	}

	public void writeAttributeHistoryNode(DatabaseAttributeNode node, int lvl) throws org.dbwiki.exception.WikiException {
		
		if (lvl!=0)
			formGroupNode(node, false);
		else
			formSubtreeNode(node);
		
		formRelation(node, false);

		Collections.sort(node.value().getAll(), new Comparator<DatabaseTextNode>() {

			@Override
			public int compare(DatabaseTextNode x, DatabaseTextNode y) {
				int xVer=x.getTimestamp().lastValue();
				int yVer=y.getTimestamp().lastValue();
				if (xVer==-1)
					return 1;
				else if (yVer==-1)
					return -1;
				else if (xVer>yVer)
					return 1;
				else
					return -1;
			}
		    });

		if (lvl!=0){
			for (int iValue = 0; iValue < node.value().size(); iValue++){ 	
				formLeafNode(node.value().get(iValue), false,"", "");
				formRelation(node.value().get(iValue), false);
			}
		}
	}
	
public void writeAttributeStructureNode(DatabaseAttributeNode node, int lvl) throws org.dbwiki.exception.WikiException {
		
		if (lvl!=0)
			formGroupNode(node, false);
		else
			formSubtreeNode(node);
		
		formRelation(node, false);

		String intervals="";
		String elementIds="";
		if (lvl!=0){
			for (int iValue = 0; iValue < node.value().size(); iValue++){										
				
				elementIds+=", historyElementId"+node.value().get(iValue).getTimestamp().firstValue()+"=\""
                		+node.value().get(iValue).identifier().toURLString()+"\"";
				if (iValue>0)
					intervals+="    ";
				intervals+=node.value().get(iValue).getTimestamp().printActiveIntervals(versionIndex);
			}
			formLeafNode(node.value().get(node.value().size()-1), false,  intervals, elementIds);
			formRelation(node.value().get(node.value().size()-1), false);
		}
	}
	

	private void formRelation(DatabaseNode node, boolean isBottomUp) {
		DatabaseElementNode parent=node.parent();
		TimeSequence vs = node.getTimestamp();
		String nodeId=getId(node);
		if (!isMainNode(nodeId) || isBottomUp){	
			String parentId=getId(parent);
			String label=getNodeLabel(node);
			
				
			String parentLabel=parent.label();
			sGraph+=parentId+"->"+nodeId+" ";
			sGraph+="[";
			if(!vs.isCurrent() && request.type().isExportHistoryDot()){
				sGraph+="color="+outdatedEdgeColor+",";
			}
			if (isBottomUp)
				sGraph+="style=dotted, color="+aboveMainEdgeColor+",";
			
			sGraph+=" childLabel="+label+", parentLabel="+parentLabel+"];\n";
		}
	}

	

	
	private String formGroupNode(DatabaseElementNode node, boolean isBottomUp) throws WikiException {
		TimeSequence vs=node.getTimestamp();
		String nodeId=getId(node);		
		DatabaseElementNode parent=node.parent();
		String style="";

		//form node label
		sGraph+=nodeId+"[label=\""+node.label();	
		if(request.type().isExportHistoryDot() && (parent==null || !vs.equals(parent.getTimestamp())))
			sGraph+="\\n("+vs.toString()+")";
	
		sGraph+="\"";
		if (!vs.isCurrent() && request.type().isExportHistoryDot())			
			sGraph+=", nodeType=outdated";
		
		if (isBottomUp)
			sGraph+=", nodeType=aboveMain";
		if(isMainNode(nodeId)){
			if (!style.isEmpty())
				style+=", ";
			style+="linewidth("+selectedNodeThickness+")";
		}
		if (style.length()>0)
			sGraph+=", style=\""+style+"\"";
		sGraph+=", activeIntervals=\""+vs.printActiveIntervals(versionIndex)+"\"];\n";
		return nodeId;
		
	}
	
	public void formSubtreeNode(DatabaseElementNode node) throws WikiException{
		TimeSequence vs=node.getTimestamp();
		String nodeId=getId(node);		
		DatabaseElementNode parent=node.parent();

		//form node label
		sGraph+=nodeId+"[label=\"SUBTREE \\n ("+node.label()+")";	
		if(request.type().isExportHistoryDot() && (parent==null || !vs.equals(parent.getTimestamp())))
			sGraph+="\\n("+vs.toString()+")";
		
		if (vs.isCurrent() || request.type().isExportStructureDot())		
			sGraph+="\", nodeType=subtree";
		else
			sGraph+="\", nodeType=outdatedSubtree";
		
		
		sGraph+=", style=\"filled\", activeIntervals=\""+vs.printActiveIntervals(versionIndex)+"\"];\n";

	}

	
	public void formLeafNode(DatabaseTextNode databaseTextNode, boolean isBottomUp, 
			String intervals, String historyElementIds) throws WikiException {
		TimeSequence vs=databaseTextNode.getTimestamp();
		String nodeId=getId(databaseTextNode);	
		DatabaseElementNode parent=databaseTextNode.parent();
		String style="";
		this.sGraph+=nodeId+"[label=\"VALUE";
		style+="filled";

		if(this.request.type().isExportHistoryDot() && (parent==null || 
				!vs.equals(parent.getTimestamp())) ){
				this.sGraph+="\\n("+vs.toString()+")";
		}
		if (vs.isCurrent() || request.type().isExportStructureDot())		
			this.sGraph+="\", nodeType=value";
		else
			this.sGraph+="\", nodeType=outdatedValue";
		

		if(isMainNode(nodeId)){
			if (!style.isEmpty())
				style+=", ";
			style+="linewidth("+selectedNodeThickness+")";
		}
		if (style.length()>0)
			this.sGraph+=", style=\""+style+"\"";
		
		if(this.request.type().isExportHistoryDot())
			this.sGraph+=", activeIntervals=\""+vs.printActiveIntervals(versionIndex)+"\"]\n";
		else if(request.type().isExportStructureDot()){
			this.sGraph+=historyElementIds;
			this.sGraph+=", fullIntervals=\""+intervals+"\"";
			this.sGraph+=", activeIntervals=\""+databaseTextNode.parent().getTimestamp()
					.printActiveIntervals(versionIndex)+"\"]\n";
			
			
		}
	}
	
	
	
	public boolean isMainNode(String nodeId){
		return getId(mainNode).equals(nodeId);
	}
	
	

	public void startDatabase(String dbName) throws IOException {
		sGraph+="digraph \""+dbName.replace("\"", "")+" "+getId(mainNode)+"\" {" +
				"graph [mainElementLabel="+getNodeLabel(mainNode)+"];";	
	}

	public void endDatabase() throws IOException {
		sGraph+=" }";	
	}

	public void outputLayoutedGraph() throws IOException {
			
			String command = "dot";
			//run dot program to layout graph, created file as input
			Runtime rt = Runtime.getRuntime();
			final Process proc= rt.exec(command);
			final BufferedWriter outputWriter = new BufferedWriter (new OutputStreamWriter(proc.getOutputStream()));
			final BufferedReader inputReader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			final BufferedReader errorReader = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
			outputWriter.append(sGraph);
			outputWriter.close();
			//Output layouted graph
			String line;
			while ((line = inputReader.readLine()) != null) {
				if (line.substring(line.length()-1).equals("\\"))
					line=line.substring(0,line.length()-1);
			    write(line);
			}
			while ((line = errorReader.readLine()) != null) {
			    System.err.println("Graphviz "+line);
			}
	}

	public void bottomUp(DatabaseNode node, String dbName) throws WikiException {
		String nodeId=getId(mainNode);
		String label="VALUE";
		while (node.parent()!=null){
			nodeId=formGroupNode(node.parent(),true);	
			if (node.isElement())
				formRelation((DatabaseElementNode)node, true);
			else
				formRelation((DatabaseTextNode)node, true);
			
			node=node.parent();
			if (node.isElement())
				label=((DatabaseElementNode)node).label();
		}
	   sGraph+=" database [label=\"DATABASE\\n Current version: "+versionIndex.getLastVersion().number()+" \", shape=box, color="
		+databaseColor+", fontcolor="+databaseColor+", versionNumber="+versionIndex.getLastVersion().number()+", dbName="
			   +dbName.replace("\"", "")+", created=\""+versionIndex.get(0).name()+"\" ];\n";
	   sGraph+=" database->"+nodeId+"[color="+aboveMainEdgeColor+", style=\"dotted\", childLabel="+label+
			   ", parentLabel=DATABASE ];\n";
	
	}


	public void writeLeafNode(DatabaseTextNode node) throws WikiException {
		
		if(request.type().isExportHistoryDot())
			formLeafNode((DatabaseTextNode)node,false,"", "");
		else if(request.type().isExportStructureDot())
		{
			DatabaseAttributeNode parent = (DatabaseAttributeNode)node.parent();
			String intervals="";
			String historyElementIds="";
				for (int iValue = 0; iValue < parent.value().size(); iValue++){										
					historyElementIds+=", historyElementId"+parent.value().get(iValue).getTimestamp()
						.firstValue()+"=\""+parent.value().get(iValue).identifier().toURLString()+"\"";
					if (iValue>0)
						intervals+="    ";
					intervals+=parent.value().get(iValue).getTimestamp().printActiveIntervals(versionIndex);
				}
			
			formLeafNode((DatabaseTextNode)node,false,intervals,historyElementIds);
		}
		
	}
	
	public static String formForDot (String value){
        value=value.replace("\"", "\\\"");
        if (value.endsWith("\\"))
                value+=" ";
        value=value.replace("\\\\\"", "\\ \\\"");
        return value;
	}


}
