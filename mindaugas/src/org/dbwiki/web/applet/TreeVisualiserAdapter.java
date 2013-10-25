package org.dbwiki.web.applet;


import java.awt.event.InputEvent;
import java.util.Vector;
import javax.swing.JEditorPane;
import att.grappa.Element;
import att.grappa.GrappaAdapter;
import att.grappa.GrappaPanel;
import att.grappa.GrappaPoint;
import att.grappa.Subgraph;


public class TreeVisualiserAdapter extends GrappaAdapter
{
	
	
	 JEditorPane dataDisplayer = null;
	 String databaseName=null;
	 public void setDisplayAdapter(JEditorPane dataDisplayer){
		 this.dataDisplayer=dataDisplayer;
	 }
	 
	 public void grappaPressed(Subgraph subg, Element elem, GrappaPoint pt, int modifiers, GrappaPanel panel) {
	        if((modifiers&(InputEvent.BUTTON2_MASK|InputEvent.BUTTON3_MASK)) != 0 && (modifiers&(InputEvent.BUTTON2_MASK|InputEvent.BUTTON3_MASK)) == modifiers) {
	            // pop-up menu if button2 or button3
	            javax.swing.JPopupMenu popup = new javax.swing.JPopupMenu();
	            javax.swing.JMenuItem item = null;

	            popup.add(item = new javax.swing.JMenuItem("Print"));
	            item.addActionListener(this);
	            popup.addSeparator();
	            popup.add(item = new javax.swing.JMenuItem("Zoom to Sweep"));
	            item.addActionListener(this);
	            popup.add(item = new javax.swing.JMenuItem("Zoom Out"));
	            item.addActionListener(this);
	            popup.add(item = new javax.swing.JMenuItem("Scale to Fit"));
	            item.addActionListener(this);
	            java.awt.geom.Point2D mpt = panel.getTransform().transform(pt,null);
	            popup.show(panel,(int)mpt.getX(),(int)mpt.getY());
	        }
	  }
	 
	   
	   
	   public void grappaClicked(Subgraph subg, Element elem, GrappaPoint pt, int modifiers, int clickCount, GrappaPanel panel) {
		   if((modifiers&InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {
			    if(clickCount == 1) {
				// looks like Java has a single click occur on the way to a
				// multiple click, so this code always executes (which is
				// not necessarily a bad thing)
				if(subg.getGraph().isSelectable()) {
				    if(modifiers == InputEvent.BUTTON1_MASK) {
					// select element
					if(elem == null) {
					    if(subg.currentSelection != null) {
						if(subg.currentSelection instanceof Element) {
						    ((Element)(subg.currentSelection)).highlight &= ~HIGHLIGHT_MASK;
						} else {
						    Vector vec = ((Vector)(subg.currentSelection));
						    for(int i = 0; i < vec.size(); i++) {
							((Element)(vec.elementAt(i))).highlight &= ~HIGHLIGHT_MASK;
						    }
						}
						subg.currentSelection = null;
						subg.getGraph().repaint();
					    }
					} else {
					    if(subg.currentSelection != null) {
						if(subg.currentSelection == elem) return;
						if(subg.currentSelection instanceof Element) {
						    ((Element)(subg.currentSelection)).highlight = ~HIGHLIGHT_MASK;
						} else {
						    Vector vec = ((Vector)(subg.currentSelection));
						    for(int i = 0; i < vec.size(); i++) {
						    	((Element)(vec.elementAt(i))).highlight &= ~HIGHLIGHT_MASK;
						    }
						}
						subg.currentSelection = null;
					    }
					    elem.highlight |= SELECTION_MASK;
					    subg.currentSelection = elem;
					    subg.getGraph().repaint();
					    HistoryElement element=new HistoryElement(elem);
					    HtmlGenerator htmlGen=new HtmlGenerator(element.getGraph(),dataDisplayer);
			    		htmlGen.showData(TreeVisualiser.currentVersion);

						
					}
				    } 
				}
			    } 
			}
		    }
	    ///////////////////////////////////////////////////////////////////////////

	   
	   

}