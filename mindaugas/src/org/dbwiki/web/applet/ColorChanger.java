package org.dbwiki.web.applet;

import java.awt.Color;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;

import att.grappa.Graph;
import att.grappa.Node;

public class ColorChanger {
	private final static Color AboveMainNodeColor=new Color (154,192,205);
	private final static Color OutdatedNodeColor=new Color (224,224,224);
	private final static Color ValueNodeColor=new Color (152,251,152);
	private final static Color OutdatedSubtreeColor=new Color (230,230,100);
	private final static Color SubtreeNodeColor=new Color (255,255,0);
	private final static Color NodeColor=new Color(0,0,0);
	
	private final static Color UnknownActionTypeColor=new Color(128,128,128);
	private final static Color CreateActionTypeColor=new Color(255,51,102);
	
	private final static Color ActivateActionTypeColor=new Color(255,204,51);
	private final static Color CopyActionTypeColor=new Color(204,51,255);
	private final static Color ImportActionTypeColor=new Color(255,117,71);
	private final static Color InsertActionTypeColor=new Color(51,204,0);
	private final static Color UpdateActionTypeColor=new Color(0,153,204);
	
	Graph graph;
	Hashtable<String, Color> actionColors;
	Hashtable<String, Color> nodeTypeColors;
	public ColorChanger(Graph graph, String typeParameter) {
		actionColors=new Hashtable<String, Color>();
		actionColors.put("UNKNOWN", UnknownActionTypeColor);
		actionColors.put("CREATE", CreateActionTypeColor);
		actionColors.put("ACTIVATE", ActivateActionTypeColor);
		actionColors.put("COPY", CopyActionTypeColor);
		actionColors.put("IMPORT", ImportActionTypeColor);
		actionColors.put("INSERT", InsertActionTypeColor);
		actionColors.put("UPDATE", UpdateActionTypeColor);
		
		nodeTypeColors=new Hashtable<String, Color>();
		this.graph=graph;
	}
	
	void setColourScheme(String colorMode, int ver){
		TreeVisualiser.colorMode=colorMode;
		if (colorMode.equals(TreeVisualiser.NodeTypeMode))
			colorByType();
		else if (colorMode.equals(TreeVisualiser.UserMode))
			colorByUser();
		else if (colorMode.equals(TreeVisualiser.ActionMode))
			colourByAction();
		else if (colorMode.equals(TreeVisualiser.TestMode))
			colorTestMode();
		if (TreeVisualiser.colorMode.equals(TreeVisualiser.StructureVisualiserParameter))
			changeVersion(ver);
		graph.repaint();
	}

	private void colourByAction() {
		Iterator <Node> it=graph.nodeElements();
	    while (it.hasNext()){
	    	HistoryElement node=new HistoryElement(it.next());
	    	if (!node.getName().equals("database")){
	    		String action =node.getLastElementAttributes(HistoryElement.VersionAttributeAction);
	    		setNodeColor(node,actionColors.get(action));
	    	}
	    }
		
	}

	private void colorByUser() {
		Iterator <Node> it=graph.nodeElements();
	    while (it.hasNext()){
	    	HistoryElement node=new HistoryElement(it.next());
	    	if (!node.getName().equals("database")){
	    		String user =node.getLastElementAttributes(HistoryElement.VersionAttributeUser);
	    		setNodeColor(node,TreeVisualiser.userColors.get(user));
	    	}
	    }
	}
	
	private void setNodeColor(HistoryElement node, Color color) {
		node.setAttribute("color", color);
		if (node.isLeaf() || node.isSubtree()){

			double brightness  = 0.299*color.getRed()+ 0.587*color.getGreen()+ 0.114*color.getBlue();
			
			if (brightness<100)
				node.setAttribute("fontcolor", "white");
			else 
				node.setAttribute("fontcolor", "black");
		}else{
			node.setAttribute("fontcolor", color);
		}
		
	}


	public void colorTestMode(){
		int [] rgbValues1 = { 255, 192, 64, 96, 160, 224, 32}; 
		int [] rgbValues2 = {0, 128}; 
		Hashtable<String, Color> colors = new Hashtable<String, Color>();
		HistoryElement node=null;
		Iterator <Node> it=graph.nodeElements();
		int i=0;
	    while (it.hasNext()){
			node=new HistoryElement(it.next());
			if (!colors.containsKey(node)){
				int index1=(i/6)%7;	
				int index2=((i/6)/7)%2;
				int value1=rgbValues1[index1];
				int value2=rgbValues2[index2];
				setNodeColor(node,createColor(i, value1, value2));	
				i++;
			}		
		}
	}
	
	private void colorByType() {
		Iterator <Node> it=graph.nodeElements();

        while (it.hasNext()){

		HistoryElement node=new HistoryElement(it.next());
    	
	    if (!node.getName().equals("database")){  		
	    		Color color=NodeColor;
	    		if(node.hasAttributeForKey("nodeType")){
	    			String type=node.getAttributeValue("nodeType");
		    		if (type.equals("outdated") || type.equals("outdatedValue"))
		    			color=OutdatedNodeColor;
		    		else if (type.equals("value"))
		    			color=ValueNodeColor;
		    		else if (type.equals("outdatedSubtree"))
		    			color=OutdatedSubtreeColor;
		    		else if (type.equals("subtree"))
		    			color=SubtreeNodeColor;
		    		else if (type.equals("aboveMain"))
		    			color=AboveMainNodeColor;
	    		}
	    		setNodeColor(node,color);		    
	    	}
        }
	}	  
	
	
	private void ColorGenerator(TreeSet<String> users, Hashtable<String, Color> colorHash){
		int [] rgbValues1 = { 255, 192, 64, 96, 160, 224, 32}; 
		int [] rgbValues2 = {0, 128}; 
		Iterator<String> it = users.iterator();
		int i=colorHash.size();
		String user=null;
		while (it.hasNext()){		
			user=it.next();
			if (!colorHash.containsKey(user)){
				int index1=(i/6)%7;	
				int index2=((i/6)/7)%2;
				int value1=rgbValues1[index1];
				int value2=rgbValues2[index2];
				colorHash.put(user, createColor(i, value1, value2));
				i++;
			}
				
		}
	}
	
	
	public void makeColors(TreeSet<String> users){		
		ColorGenerator(users,TreeVisualiser.userColors);
	}

	static Color createColor(int i, int value, int value2){
		Color color;		
		if (i % 6==0)
			color=new Color(value2,value,value2);
		else if (i % 6 == 1)
			color=new Color(value,value2,value2);
		else if (i % 6 == 2)
			color=new Color(value2,value2,value);
		else if (i % 6==3)
			color=new Color(value2,value,value);
		else if (i % 6 == 4)
			color=new Color(value,value2,value);
		else 
			color=new Color(value,value,value2);
		return color;
	
	}

	public void changeVersion(int ver) {
		if (TreeVisualiser.colorMode.equals(TreeVisualiser.UserMode)){
			Iterator <Node> it=graph.nodeElements();
		    while (it.hasNext()){
		    	HistoryElement node=new HistoryElement(it.next());
		    	if (node.isVisible() && !node.isDatabaseNode()){
		    		String user = node.getVersionAttribute(ver, HistoryElement.VersionAttributeUser);
		    		if (user!=null)
		    			setNodeColor(node,TreeVisualiser.userColors.get(user));	
		    		
		    	}
		    }
		
		}
		else if (TreeVisualiser.colorMode.equals(TreeVisualiser.ActionMode)){
			Iterator <Node> it=graph.nodeElements();
		    while (it.hasNext()){
		    	HistoryElement node=new HistoryElement(it.next());
		    	if (node.isVisible() && !node.isDatabaseNode()){
		    		String action = node.getVersionAttribute(ver, HistoryElement.VersionAttributeAction);
		    		if (action!=null)
		    			setNodeColor(node,actionColors.get(action));	
		    	}
		    }
		}
	}

}
