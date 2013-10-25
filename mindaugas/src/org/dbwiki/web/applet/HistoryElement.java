package org.dbwiki.web.applet;


import java.util.ArrayList;
import java.util.TreeSet;
import att.grappa.Element;
import att.grappa.Graph;


public class HistoryElement {

	private Element element;
	private String activeIntervals;
	private String fullIntervals;
	private String mainIntervals;
	
	public static int VersionAttributeUser=2;
	public static int VersionAttributeAction=3;
	
	public HistoryElement (Element element){
		this.element=element;
		if (element.isNode()){
			this.activeIntervals =(String) this.element.getAttributeValue("activeIntervals");  
			if (this.element.hasAttributeForKey("fullIntervals")){
				this.fullIntervals =(String) this.element.getAttributeValue("fullIntervals");
				this.mainIntervals=this.fullIntervals;
			}
			else{
				this.fullIntervals=null;
				this.mainIntervals=this.activeIntervals;
			}
		}
	}
	
	public HistoryElement(String id,Graph graph) {
		this.element=graph.findNodeByName(id);
		if (element.isNode()){
			this.activeIntervals =(String) this.element.getAttributeValue("activeIntervals");  
			if (this.element.hasAttributeForKey("fullIntervals")){
				this.fullIntervals =(String) this.element.getAttributeValue("fullIntervals");
				this.mainIntervals=this.fullIntervals;
			}
			else{
				this.fullIntervals=null;
				this.mainIntervals=this.activeIntervals;
			}
		}
	}

	/*public int getCurrentVer(){
		return Integer.parseInt(element.getGraph().findNodeByName("database").getAttributeValue("versionNumber").toString());
	}*/


	public boolean isDatabaseNode() {
			return this.element.getName().equals("database");
	}

	public int getFirstVer() {		
    	String interval=this.activeIntervals.substring(0, this.activeIntervals.indexOf(" "));
		int firstVersion;
		if (interval.contains("-")){
			firstVersion=Integer.parseInt(interval.substring(0, interval.indexOf("-")))-1;
	    			
		}
		else{
			firstVersion=Integer.parseInt(interval)-1;
		}
		return firstVersion;
	}

	
	public int getLastVer() {
		if (this.activeIntervals==null)
			return -2;
		String[] intervals = this.activeIntervals.split("   ");
    	String interval=intervals[intervals.length-1].substring(0, intervals[intervals.length-1].indexOf("  "));
		int lastVersion=-1;
		if (interval.contains("-")){
			if (!interval.contains("cur."))
				lastVersion=Integer.parseInt(interval.substring(interval.indexOf("-")+1));   	    			
		}
		else{
			lastVersion=Integer.parseInt(interval);
		}
		
		return lastVersion;
	}
	public Integer[] getNextInterval(int floor) {	
		Integer [] interval = new Integer[2];
		interval[0]=-2;
		interval[1]=-2;
		for (String elementInt: this.fullIntervals.split("    ")){
			for (String versionInt: elementInt.split("   ")){
				String versions=versionInt.substring(0,versionInt.indexOf("  "));
				
				int startVer; int endVer;
				
				if (versions.contains("-")){
					startVer=Integer.parseInt(versions.split("-")[0]);
					String sEnd=versions.split("-")[1];
					if (sEnd.equals("cur."))
						endVer=TreeVisualiser.databaseVersion;
					else
						endVer=Integer.parseInt(sEnd);
				}else{
					startVer=Integer.parseInt(versions);
					endVer=startVer;
				}
				

				if (startVer==floor+1) return new Integer [] {startVer-1,endVer};
				if ((startVer<interval[0] || interval[0]==-2) &&  startVer>floor){
					interval[0]=startVer;
					interval[1]=endVer;
				}
				
			}
		}
		interval[0]=interval[0]-1;
		return interval;
	}

	
	
	
	public String getName() {
		
		return this.element.getName();
	}

	

	public TreeSet<String> getVersionUpdatesAttributes(int att) {
		TreeSet<String> updateActions = new TreeSet<String> ();
		for (String values: this.mainIntervals.split("    ")){
		    for (String version: values.split("   ")){
		    	String [] attributes =version.split("  ");	
		    	updateActions.add(attributes[att]);
		    }
		}
		return updateActions;
	}




	public boolean isVisible() {
		return this.element.visible;

	}



	public boolean hasAttributeForKey(String string) {
		return this.element.hasAttributeForKey(string);
		
	}



	public String getAttributeValue(String string) {
		return this.element.getAttributeValue(string).toString();
	}
	
	public String getId() throws Exception{
		if (this.element.isNode())
			return this.element.getName().substring(1);		

		return "";
	}



	public String getLabel(boolean isSubtree) throws Exception {
		if (!this.element.isNode())
			return "";
			
		String label=this.element.getAttributeValue("label").toString();
		if (isSubtree){
			label=label.substring(label.indexOf("(")+1);
			 if (label.contains("\\n"))
				 label=label.substring(0,label.indexOf(")\\n"));
			 else
				 label=label.substring(0,label.length()-1);
		}else{
			if (label.contains("\\n"))
				 label=label.substring(0,label.indexOf("\\n"));
		}
	
		return label;
	}



	public ArrayList<String[]> getVersionsData(int ver) {
		String ai;
		ArrayList <String[]> versionsData = new ArrayList <String[]>();
		 if (this.fullIntervals!=null)
			 ai=getVersionInterval(ver);	 
		 else
			 ai=this.activeIntervals;	
		 
		 String[] activeIntervals = ai.split("   ") ;
		 for (int i=0; i<activeIntervals.length; i++){
				String [] versionData = new String[5];
			 	String [] par=activeIntervals[i].split("  ");
				String start=par[1].substring(0,par[1].indexOf("-"));
				String end=par[1].substring(par[1].indexOf("-")+1);	
				versionData[0]=par[0];
				versionData[1]=start;
				versionData[2]=end;
				versionData[3]=par[2];
				versionData[4]=par[3];
				versionsData.add(versionData);
				
			}
		
		return versionsData;
	}

	private Integer getFirstBoundary(String interval){
		String [] intervals=interval.split("   ");
		
		String verInt=intervals[0].substring(0,intervals[0].indexOf("  "));
		if (verInt.contains("-"))
			return Integer.parseInt(verInt.split("-")[0]);			
		else
			return Integer.parseInt(verInt);
	}
	

	public String getNodeId() {
		return this.element.toString();
	}

	public String getParentId() throws Exception {
		if (!this.element.isEdge())
			throw(new Exception("Element is not a node. Label cannot be extracted."));
		 String parentId = element.toString().substring(1, element.toString().indexOf("->")).trim();
		 return parentId;
	}



	public String getChildId() throws Exception {
		if (!this.element.isEdge())
			throw(new Exception("Element is not a node. Label cannot be extracted."));
		 String childId=this.element.toString().substring(this.element.toString().indexOf("->")+4).trim();
		 return childId;
	}



	public String getDBName() {
		String eId=element.getGraph().toString();
		String dbName=eId.substring(eId.indexOf("/")+1,eId.indexOf(" "));
		return dbName;
	}



	public String getMainNodeElementId() {
		String mainNodeId="";
		if (element.isSubgraph()){
			String eId=element.toString();
			mainNodeId=eId.substring(eId.indexOf(" ")).trim();
			mainNodeId=mainNodeId.substring(1, mainNodeId.indexOf("\""));
		}
		return mainNodeId;
	}
	

	
	public String getVersionInterval(int ver){
		
		for (String ints: this.mainIntervals.split("    ")){
			for (String i: ints.split("   ")){
				String verInt=i.substring(0,i.indexOf("  "));
				if (verInt.contains("-")){
					String [] boundaries = verInt.split("-");
					if (Integer.parseInt(boundaries[0])<=ver &&
					   (boundaries[1].equals("cur.") || Integer.parseInt(boundaries[1])>=ver))
							return ints;
				}
				else{
					if (Integer.parseInt(verInt)==ver)
						return ints;
				}
			}
		}
			
		return "NO_VALUE";
	}
	
	public String getVersionAttribute(int ver, int att){
		String [] intervals=this.mainIntervals.split("    ");
		for (String ints: intervals){
			for (String i: ints.split("   ")){
				String [] atts = i.split("  ");
				if (atts[0].contains("-")){
					String [] boundaries = atts[0].split("-");
					if (Integer.parseInt(boundaries[0])<=ver &&
					   (boundaries[1].equals("cur.") || Integer.parseInt(boundaries[1])>=ver))
							return atts[att];
				}
				else{
					if (Integer.parseInt(atts[0])==ver)
						return atts[att];
				}
			}
		}		
		return null;
	}
	
	
	
	
	public void setAttribute(String name, Object value) {
		this.element.setAttribute(name,value);
		
	}

	public String getFirstElementAttributes(int att) {
		 String firstVersion = this.activeIntervals.split("   ")[0];   
			return firstVersion.split("  ")[att];
	}

	public String getLastElementAttributes(int att) {
		 String[] attributes = this.activeIntervals.split("   ");
		 String lastVersion = attributes[attributes.length-1];   
			return lastVersion.split("  ")[att];
	}

	public Graph getGraph() {
	
		return this.element.getGraph();
	}

	public boolean isLeaf() {
		if (this.element.hasAttributeForKey("nodeType"))
			return (this.element.getAttributeValue("nodeType").equals("outdatedValue") ||
				this.element.getAttributeValue("nodeType").equals("value"));
		else 
			return false;
	}

	public boolean isRelation() {
		
		return element.toString().contains("->") || element.toString().contains("--");
	}

	public boolean isGraph() {
		return element.toString().startsWith("\"");
	}

	public boolean isSubtree() {
		if (this.element.hasAttributeForKey("nodeType"))
			return (this.element.getAttributeValue("nodeType").equals("subtree")
					||this.element.getAttributeValue("nodeType").equals("outdatedSubtree"));
		else 
			return false;
	}

	public String getVersionElementId(int ver) {
		String elementId="";
		int valueNumber=getFirstBoundary(getVersionInterval(ver));
		elementId=this.element.getAttributeValue("historyElementId"+valueNumber).toString();	 
		return elementId;
	}

	public String getMainNodeId() {
		String mainNodeId="";
		if (element.isSubgraph()){
			String eId=element.toString();
			mainNodeId=eId.substring(eId.indexOf(" ")).trim();
			mainNodeId=mainNodeId.substring(0, mainNodeId.length()-1);
		}
		return mainNodeId;
	}



	public ArrayList<Integer[]> getActivityIntervals() {
		ArrayList<Integer[]> intervals=new ArrayList<Integer[]>();
		if (this.isLeaf()){
			int lastIntervalEnd=-1;
			int startVersion=-1;
			while(lastIntervalEnd>-2){

				Integer[] interval=getNextInterval(lastIntervalEnd);
				if (startVersion==-1)
					startVersion=interval[0];
				
				if (lastIntervalEnd!=interval[0] && lastIntervalEnd!=-1)
				{
					Integer[] intervalToAdd={startVersion, lastIntervalEnd};
					intervals.add(intervalToAdd);
					startVersion=interval[0];
				}
				lastIntervalEnd=interval[1];
				

		}
		}else{
			 String[] allAttributes = this.activeIntervals.split("   ");
			 for (String intAtts: allAttributes){
				 int start;
				 int end;
				 String versionInterval=intAtts.split("  ")[0];
				 if (versionInterval.contains("-")){
					 String[] sInterval=versionInterval.split("-");
					 start=Integer.parseInt(sInterval[0]);
					 if (sInterval[1].equals("cur."))
						 end=-1;
					 else
						 end=Integer.parseInt(sInterval[1]);
				 }else{
					 start=Integer.parseInt(versionInterval);
					 end=start;
				 }
				 Integer[] interval={start-1,end};
				 intervals.add(interval);
			 }
		}
			
		return intervals;
	}
}