package org.dbwiki.web.applet;

import java.util.ArrayList;

import javax.swing.JEditorPane;

import att.grappa.Element;
import att.grappa.Graph;

public class HtmlGenerator {
	private HistoryElement elem;
	private String HTML="";
	JEditorPane dataDisplayer;
	ValueRequester vr;
	Graph graph;

	
	public HtmlGenerator(Graph graph, JEditorPane dataDisplayer) {	
		this.graph=graph;
		this.dataDisplayer=dataDisplayer;
	
	}
	
	private void write (String s){
		this.HTML+=s;
	}

	
	public void formDisplayerHead(){
		   this.HTML="";
		   if (this.elem.isVisible())
			   write("<input type='hidden' value='eId:%"+elem.getNodeId()+"%'/>");
		   else	   
			   write("<input type='hidden' value='eId:%NO_ELEMENT%'/>");
		   write("<div width='100%' height='180px' style='padding:0px 10px; background-color:#EEEEEE;'>");
	}
	
	public void formDisplayerTail(){
		write("</div>");
	}
	
	public void formHtml(int ver) { 
		formDisplayerHead();
		try {
			if (!this.elem.isVisible())
				 formNonExistingElementDisplay();
			else if (this.elem.isGraph() || this.elem==null)
			   formGraphDisplay();
			else  if (this.elem.isDatabaseNode())
			   formDBDataDisplay(ver);   
			else if (this.elem.isRelation())
			   formRelationDataDisplay();		   
			else		 
				formNodeDataDisplay(this.elem.isSubtree(), ver);
			

		} catch (Exception e) {
		   e.printStackTrace();
		}
		formDisplayerTail();
	}

	private void formNonExistingElementDisplay() {
		 
		write("<h2>General info:</h2>");	
		write("<b>This Element does not exist in current version of database.</b>");
	
		
	}

	public boolean graphDataIsDisplayed(){
		String curHTML=dataDisplayer.getText();
		if (!curHTML.contains("eId:%"))
			return false;
		curHTML=curHTML.substring(curHTML.indexOf("eId:%")+5);
		return curHTML.startsWith("\"");
	}
	
	
	private void formGraphDisplay() {
		String dbName=this.elem.getDBName();
		String mainNodeId=this.elem.getMainNodeElementId();
		String mainNodeLabel=this.elem.getAttributeValue("mainElementLabel");
		
		
		write("<h2>General info:</h2>");	
		write("<table>");
		write("<tr>");		
			write("<td>");
				write("<table>");
					write("<tr>");
						write("<td><span><b>Object:</b></span></td></td>");	
						if (TreeVisualiser.HistoryVisualiserParameter.equals(TreeVisualiser.HistoryVisualiserParameter))
							write("<td><span>History Visualisation Graph</span>");
						else if (TreeVisualiser.HistoryVisualiserParameter.equals(TreeVisualiser.StructureVisualiserParameter)) 
							write("<td><span>Structure Visualisation Graph</span>");
					write("</tr>");
					write("<tr>");
						write("<td><span><b>Visualised database:</b></span></td>");
						write("<td><span>"+dbName+"</span></td>");
					write("</tr>");
					write("<tr>");
						write("<td><span ><b>Type of the main node:</b></span></td>");
						write("<td><span >"+mainNodeLabel+"</span></td>");
					write("</tr>");
					write("<tr>");
						write("<td><span><b>ID of the main node:</b></span></td>");
						write("<td><span>"+mainNodeId+"</span></td>");
					write("</tr>");
				write("</table>");
			write("</td>");
		write("</tr>");
		write("</table>");
		
	}

	

	private void formRelationDataDisplay() throws Exception {
		 String childLabel=this.elem.getAttributeValue("childLabel");
		 String parentLabel=this.elem.getAttributeValue("parentLabel");
		 String parentId= this.elem.getParentId();
		 String childId=this.elem.getChildId();
		
		 write("<table width='100%'><tr>");	
			write("<td width='50%'>");
				write("<h2>General info:</h2>");	
				write("<table>");
						write("<tr>");
							write("<td><span><b>Object:</b></span></td></td>");					
							write("<td><span>Edge</span>");
						write("</tr>");
						write("<tr>");
							write("<td><span><b>Parent type:</b></span></td>");
							write("<td><span>"+parentLabel+"</span></td>");
						write("</tr>");
						write("<tr>");
							write("<td><span><b>Parent ID:</b></span></td>");
							write("<td><span>"+parentId+"</span></td>");
						write("</tr>");
						write("<tr>");
							write("<td><span><b>Child type:</b></span></td>");
							write("<td><span>"+childLabel+"</span></td>");
						write("</tr>");
						write("<tr>");
							write("<td><span><b>Child ID:</b></span></td>");
							write("<td><span>"+childId+"</span></td>");
					write("</tr>");
				write("</table>");
			write("</td>");
			write("<td width='50%' valign=top>");
				if(!parentId.equals("atabase"))
					write("<br/><a href='http://"+parentId+".com'>Use parent node as main</a>");
				write("<br/><br/><a href='http://"+childId+".com'>Use child node as main</a>");
			write("</td>");
		write("</tr></table>");
	}
	

	private void formNodeDataDisplay(boolean isSubtree, int version) throws Exception {		
		String nodeId=this.elem.getId();
		ArrayList<String[]> versionsData = this.elem.getVersionsData(version);
		
		if (isSubtree)
			 formSubtreeDataHtml(versionsData,nodeId);	 	 
		 else{
			 formNodeDataHtml("Loading...",versionsData);		 
			 if (this.elem.isLeaf()){
				 this.vr=new ValueRequester(this,versionsData, version);		
				 try{
					 this.vr.execute();
				 }catch(Exception e){
					 e.printStackTrace();
					 
				 }
			}
		 }
	}	
	
	void formNodeDataHtml(String value, ArrayList<String[]> versionsData) throws Exception{
		
		
		if (elem.isLeaf()){
			write("<b>Value:</b><br/>");
			write(value);
			write("<hr />");
		}
		write("<table width='100%'><tr>");	
				write("<td width='30%' valign=top>");
					write("<h2>General info:</h2>");	
					write("<table width=90%>");
						write("<tr>");
							write("<td width='30%'><span ><b>Object:</b></span></td>");					
							if (elem.isLeaf())
								write("<td><span>Leaf Node</span>");
							else
								write("<td><span >Node</span>");
						write("</tr>");
						write("<tr>");
							write("<td><span ><b>Node type:</b></span></td>");
							write("<td><span >"+elem.getLabel(false)+"</span></td>");
						write("</tr>");
						write("<tr>");
							write("<td><span ><b>Node id:</b></span></td>");
							write("<td><span >"+elem.getId()+"</span></td>");
						write("</tr>");
					
					write("</table>");	
				write("</td>");
				write("<td width='70%' valign=top>");
				write("<h2>Activity history:</h2>");	
				write("<table border=1 width=100%>");
						write("<tr>");
							write("<td><b>Active at versions</b></td>");
							write("<td><b>Active from</b></td>");
							write("<td><b>Active until</b></td>");
							write("<td><b>User</b></td>");
							write("<td><b>Create action</b></td>");
						write("</tr>");
						
						for (int i=0; i<versionsData.size(); i++){
							write("<tr>");
								write("<td>"+versionsData.get(i)[0]+"</td>");
								write("<td>"+versionsData.get(i)[1]+"</td>");
								write("<td>"+versionsData.get(i)[2]+"</td>");
								write("<td>"+versionsData.get(i)[3]+"</td>");
								write("<td>"+versionsData.get(i)[4]+"</td>");
							write("</tr>");	
							
						}
					write("</table>");
				write("<br/><a href='http://"+elem.getId()+".com'>Use this node as main</a>");
				
				if (elem.getLastVer()==-1)
					write("<br/><a href='http://"+elem.getId()+".red'>Redirect browser to main page of this node</a>");
				write("</td>");
		write("</tr></table>");	
	}
	
	private void formSubtreeDataHtml(ArrayList<String[]> versionsData, String nodeId) throws Exception {
		write("<table width='100%'><tr>");	
				write("<td width='30%' valign=top>");
					write("<h2>General info:</h2>");	
					write("<table width=90%>");
						write("<tr>");
						write("<td width='30%'><span ><b>Object:</b></span></td>");					
							write("<td><span >Subtree</span>");
						write("</tr>");
						write("<tr>");
							write("<td><span ><b>Root type:</b></span></td>");
							write("<td><span>"+elem.getLabel(true)+"</span></td>");
						write("</tr>");
						write("<tr>");
							write("<td><span ><b>Root id:</b></span></td>");
							write("<td><span >"+nodeId+"</span></td>");
						write("</tr>");
					
					write("</table>");	
				write("</td>");
				write("<td width='70%' valign=top>");
				write("<h2>Activity history:</h2>");	
				write("<table border=1 width=100%>");
						write("<tr>");
							write("<td><b>Active at versions</b></td>");
							write("<td><b>Active from</b></td>");
							write("<td><b>Active until</b></td>");
							write("<td><b>User</b></td>");
							write("<td><b>Action</b></td>");
						write("</tr>");
						for (int i=0; i<versionsData.size(); i++){
							write("<tr>");
								write("<td>"+versionsData.get(i)[0]+"</td>");
								write("<td>"+versionsData.get(i)[1]+"</td>");
								write("<td>"+versionsData.get(i)[2]+"</td>");
								write("<td>"+versionsData.get(i)[3]+"</td>");
								write("<td>"+versionsData.get(i)[4]+"</td>");
							write("</tr>");	
							
						}
					write("</table>");
				write("<br/><a href='http://"+nodeId+".com'>Use root node as main</a>");				
				write("<br/><a href='http://"+nodeId+".red'>Redirect browser to main page of this node</a>");
				write("</td>");
		write("</tr></table>");	
	}

	private void formDBDataDisplay(int ver) {
		String dbName=elem.getAttributeValue("dbName");
		String created=elem.getAttributeValue("created");		
		
		write("<h2>General info</h2>");	
		write("<table>");
			write("<tr>");		
				write("<td>");
					write("<table>");
						write("<tr>");
							write("<td><span><b>Object:</b></span></td></td>");					
							write("<td><span>Database</span>");
						write("</tr>");
						write("<tr>");
							write("<td><span><b>Database name:</b></span></td>");
							write("<td><span>"+dbName+"</span></td>");
						write("</tr>");
						write("<tr>");
							write("<td><span><b>Current version:</b></span></td>");
							write("<td><span>"+ver+"</span></td>");
						write("</tr>");
						write("<tr>");
							write("<td><span><b>Database created:</b></span></td>");
							write("<td><span>"+created+"</span></td>");
						write("</tr>");
					write("</table>");
				write("</td>");
			write("</tr>");
		write("</table>");
	}
	
	public String getHtml() {
		return HTML;
	}

	
	public void clearHtml() {
		this.HTML="";
		
	}
	
	public ValueRequester getValueRequester(){
		return this.vr;
	}


	public void showData(int ver) {
		
		if (this.graph.currentSelection instanceof Element)
			this.elem=new HistoryElement((Element)this.graph.currentSelection);	
		else 
			this.elem=new HistoryElement(this.graph);
		
		//if (!this.elem.isVisible())
			//this.elem=new HistoryElement(this.graph);
		
		formHtml(ver);
		this.dataDisplayer.setText(this.HTML);	
		
	}
	
	public boolean isValueLoaded(){
		if (vr==null) return true;
		else return !this.vr.isCancelled();
	}

	public HistoryElement getElem(){
		return this.elem;
	}

	public boolean noElementInfoIsDisplayed() {
		String curHTML=dataDisplayer.getText();
		if (!curHTML.contains("eId:%NO_ELEMENT%"))
			return false;
		curHTML=curHTML.substring(curHTML.indexOf("eId:%")+5);
		return curHTML.startsWith("NO_ELEMENT");
	}
	
}
