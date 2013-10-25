package org.dbwiki.web.applet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

import att.grappa.Graph;
import att.grappa.Node;

public class DataCollector {
	
	private Graph graph;
	Version[] graphVersions;
	TreeSet<String> users=new TreeSet<String>();

	public DataCollector(String typeParameter, Graph graph){
		setInitialVersions(graph);
		this.graph=graph;
		if (typeParameter.equals("structure_dot"))
			getDataForStructureTree();
		else if (typeParameter.equals("history_dot"))
			getDataForHistoryTree();
	}
	
	private void getDataForHistoryTree() {	
       	Iterator <Node> it=this.graph.nodeElements();
   	    while (it.hasNext()){
   	    	HistoryElement node=new HistoryElement(it.next());
   	    	if (!node.isDatabaseNode())	    		
		    	users.add(node.getLastElementAttributes(HistoryElement.VersionAttributeUser));
   	    }  	    
	}
	
	private void getDataForStructureTree() {	
       	Iterator <Node> it=this.graph.nodeElements();
       	this.graphVersions = new Version[TreeVisualiser.lastVersion];
       	int nodeCnt=0;
   	    while (it.hasNext()){
   	    	nodeCnt++;
   	    	HistoryElement node=new HistoryElement(it.next());
   	    	if (!node.isDatabaseNode()){
   	    		ArrayList <Integer []> intervals=node.getActivityIntervals();
	   	    	for (Integer[] interval :intervals){
	   	    		if (interval[0]>=TreeVisualiser.firstVersion && interval[0]<=TreeVisualiser.lastVersion ){
		   	    		if (this.graphVersions[interval[0]]==null)
		   	    			this.graphVersions[interval[0]]=new Version(this.graph);
				    	this.graphVersions[interval[0]].addNewNodeName(node.getName());
	   	    		}
	   	    		
			    	 if (interval[1]!=TreeVisualiser.databaseVersion && interval[1]>=TreeVisualiser.firstVersion && interval[1]<=TreeVisualiser.lastVersion){
			    		 if (this.graphVersions[interval[1]]==null)
			    			 this.graphVersions[interval[1]]=new Version(this.graph);
					     this.graphVersions[interval[1]].addRemovedNodeName(node.getName());
			    	 }   
			    	 this.users.addAll(node.getVersionUpdatesAttributes(HistoryElement.VersionAttributeUser));
			    	
	   	    	}
   	    	}
   	    } 
   	    System.out.println("Number of nodes: "+nodeCnt);
	}

	public TreeSet<String> getUsers() {		
		return this.users;
	}
	

	
	public Version[] getVersions() {
		return this.graphVersions;
	}
	
	
	private int getMainNodeLastVersion(Graph graph) {
		String mainNodeId=(new HistoryElement(graph)).getMainNodeId();
		HistoryElement node=new HistoryElement(mainNodeId,graph);
		int ver=node.getLastVer();
		return ver;
	}

	private int getMainNodeFirstVersion(Graph graph) {
		String mainNodeId=(new HistoryElement(graph)).getMainNodeId();
		HistoryElement node=new HistoryElement(mainNodeId,graph);
		return node.getFirstVer();
	}
	
	public void setInitialVersions(Graph graph) {
		if (TreeVisualiser.databaseVersion==-1)
			TreeVisualiser.databaseVersion=
			Integer.parseInt(graph.findNodeByName("database").getAttributeValue("versionNumber").toString());
		

		TreeVisualiser.lastVersion=getMainNodeLastVersion(graph); 
		if (TreeVisualiser.lastVersion==-1){
			TreeVisualiser.lastVersion=
					Integer.parseInt(graph.findNodeByName("database").getAttributeValue("versionNumber").toString());
			if (TreeVisualiser.currentVersion==-1)
				TreeVisualiser.currentVersion=TreeVisualiser.lastVersion;
		}
		else{
			if (TreeVisualiser.currentVersion==-1)
				TreeVisualiser.currentVersion=TreeVisualiser.lastVersion;
			TreeVisualiser.lastVersion++;
		}
		
		
		


		TreeVisualiser.firstVersion=getMainNodeFirstVersion(graph);
		
	}
}
