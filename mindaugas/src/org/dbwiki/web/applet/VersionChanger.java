package org.dbwiki.web.applet;

import javax.swing.JEditorPane;
import javax.swing.JTextField;

import att.grappa.Element;
import att.grappa.Graph;


public class VersionChanger {
	private JTextField versionTextField;
	private Graph graph;
	private Version[] graphVersions;
	private ColorChanger colorChanger;
	HtmlGenerator htmlGen;

	public VersionChanger(JTextField versionTextField,Graph graph, Version [] graphVersions, 
			JEditorPane dataDisplayer, ColorChanger colorChanger){
		
		this.versionTextField=versionTextField;
		this.graph=graph;
		this.graphVersions=graphVersions;
		this.colorChanger=colorChanger;
		this.htmlGen = new HtmlGenerator(graph,dataDisplayer);
		
		
	}
	
	public void changeDisplayedVersion(int ver, boolean firstTime) {
		
       	long start = System.currentTimeMillis();
       	this.versionTextField.setText(Integer.toString(ver));
       	this.graph.findNodeByName("database").setAttribute("label", "DATABASE\\n Current version: "+ver);
       	this.graph.findNodeByName("database").setAttribute("versionNumber", Integer.toString(ver));
 
       	
        if (firstTime){
            ver=TreeVisualiser.currentVersion;
            for(int i=0; i<TreeVisualiser.currentVersion; i++){
            	if(this.graphVersions[i]!=null)
            	this.graphVersions[i].elementsVisability(true);
                                            
            }
        }

       	
       	if (ver<TreeVisualiser.currentVersion){
				for(int i=TreeVisualiser.currentVersion-1; i>ver-1; i--){
					if(this.graphVersions[i]!=null){
						this.graphVersions[i].elementsVisability(false);
					}
						
				}
       	}
       	else if (ver>TreeVisualiser.currentVersion)
       	{
       		for(int i=TreeVisualiser.currentVersion-1; i<ver; i++){
					if(this.graphVersions[i]!=null)
						this.graphVersions[i].elementsVisability(true);
						
				}
       	}
       	
       	this.colorChanger.changeVersion(ver);
       	
       	HistoryElement elem=null;
       	if (this.graph.currentSelection instanceof Element)
       		elem=new HistoryElement((Element) this.graph.currentSelection);
       	
       	if (this.htmlGen!=null && this.htmlGen.getValueRequester()!=null && !this.htmlGen.getValueRequester().isDone() && elem!=null
       			&& !elem.getVersionInterval(TreeVisualiser.currentVersion).equals(elem.getVersionInterval(ver)))
       		this.htmlGen.getValueRequester().cancel(true);  
       	
       	
      
       	if (elem!=null){	
       	 	boolean isVisible=((Element)this.graph.currentSelection).visible;
       		if (elem.isDatabaseNode()){
       			this.htmlGen.showData(ver);
       		}
       		else if(elem.isLeaf() && isVisible){
            	if (!elem.getVersionInterval(TreeVisualiser.currentVersion).equals(elem.getVersionInterval(ver)) 
	       			|| !this.htmlGen.isValueLoaded())
	       				this.htmlGen.showData(ver);
       		}
       		else if (isVisible && this.htmlGen.noElementInfoIsDisplayed()){
              		this.htmlGen.showData(ver);
       		}
       		else if (!isVisible && !this.htmlGen.noElementInfoIsDisplayed())	
       			this.htmlGen.showData(ver);
    	
       	}
       	else{ 
       		if(!this.htmlGen.graphDataIsDisplayed())
       			this.htmlGen.showData(ver);
       	}
 
       	this.graph.repaint();	
   
		
		long end = System.currentTimeMillis();
		
		
		TreeVisualiser.currentVersion=ver;
		GraphWorker.versionSlider.setValue(ver);
		System.out.println(System.currentTimeMillis()+" Version change speed:"+ (end - start)+" ms");
	}
	
		
		
}
