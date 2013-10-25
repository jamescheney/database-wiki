package org.dbwiki.web.applet;

import java.util.ArrayList;
import java.util.Iterator;

import att.grappa.Edge;
import att.grappa.Element;
import att.grappa.Graph;


public class Version {
	private ArrayList <String> newNodeNames;
	private ArrayList <String> removedNodeNames;
	private Graph graph;
	public Version (Graph graph){
		newNodeNames = new ArrayList <String> ();
		removedNodeNames = new ArrayList <String> ();
		this.graph=graph;
	}

	public void addNewNodeName(String name){
		newNodeNames.add(name);
	}
	
	public void addRemovedNodeName(String name){
		removedNodeNames.add(name);
	}
	public ArrayList <String> getNodeNames(){
		return newNodeNames;
	}
	public void elementsVisability(Boolean verInc) {
		for (String name: newNodeNames){
			Element elem=graph.findNodeByName(name);
			elem.selectable=verInc;
			elem.visible=verInc;
			
			Iterator <Edge> it =graph.findNodeByName(name).inEdgeElements();
			Edge edge=it.next();
			edge.selectable=verInc;
			edge.visible=verInc;
		}
		for (String name: removedNodeNames){
			Element elem=graph.findNodeByName(name);
			elem.selectable=!verInc;
			elem.visible=!verInc;
			
			Iterator <Edge> it =graph.findNodeByName(name).inEdgeElements();
			Edge edge=it.next();
			edge.selectable=!verInc;
			edge.visible=!verInc;
		}
		
	}
	
	

	
	

}
