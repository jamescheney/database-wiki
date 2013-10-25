package org.dbwiki.web.applet;

import java.util.ArrayList;
import java.util.Iterator;


import att.grappa.Edge;
import att.grappa.Element;
import att.grappa.Graph;


public class GraphVersion {
	private ArrayList <String> newNodeNames;
	private ArrayList <String> removedNodeNames;
	private Graph graph;
	public GraphVersion (Graph graph){
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
			if (verInc)
				elem.highlight&= ~Element.HIGHLIGHT_MASK;
			//graph.currentSelection = null;
			elem.visible=verInc;
			
			Iterator <Edge> it =graph.findNodeByName(name).inEdgeElements();
			it.next().visible=verInc;
		}
		for (String name: removedNodeNames){
			Element elem=graph.findNodeByName(name);
			if (!verInc)
				elem.highlight&= ~Element.HIGHLIGHT_MASK;
			//graph.currentSelection = null;
			elem.visible=!verInc;
			
			Iterator <Edge> it =graph.findNodeByName(name).inEdgeElements();
			it.next().visible=!verInc;
		}
		
	}
	
	static String getVersionInterval(String interval, int ver){
		String [] intervals=interval.split(";");
		for (String ints: intervals){
			for (String i: ints.split(",")){
				String verInt=i.substring(0,i.indexOf("#"));
				if (verInt.contains("-")){
					String [] boundaries = verInt.split("-");
					if (Integer.parseInt(boundaries[0])<=ver &&
					   (boundaries[1].equals("cur.") || Integer.parseInt(boundaries[1])>ver))
							return ints;
				}
				else{
					if (Integer.parseInt(verInt)==ver)
						return ints;
				}
			}
		}
			
		return null;
	}
	
	static String getVersionUser(String interval, int ver){
		String [] intervals=interval.split(";");
		for (String ints: intervals){
			for (String i: ints.split(",")){
				String verInt=i.substring(0,i.indexOf("#"));
				if (verInt.contains("-")){
					String [] boundaries = verInt.split("-");
					if (Integer.parseInt(boundaries[0])<=ver &&
					   (boundaries[1].equals("cur.") || Integer.parseInt(boundaries[1])>ver))
							return ints;
				}
				else{
					if (Integer.parseInt(verInt)==ver)
						return ints;
				}
			}
		}
			
		return null;
	}

	public static String getUser(String intervals) {
		
		String [] intervalsArr =intervals.split(","); 
		String [] intervalAttributes=intervalsArr[0].split("#");
		return intervalAttributes[2];
		
	}
	

}
