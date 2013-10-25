package org.dbwiki.web.applet;

import java.awt.event.InputEvent;

import att.grappa.Element;
import att.grappa.GrappaAdapter;
import att.grappa.GrappaPanel;
import att.grappa.GrappaPoint;
import att.grappa.Subgraph;


public class GrappaAdapterReadOnly extends GrappaAdapter
{
	 public void grappaPressed(Subgraph subg, Element elem, GrappaPoint pt, int modifiers, GrappaPanel panel) {
	        if((modifiers&(InputEvent.BUTTON2_MASK|InputEvent.BUTTON3_MASK)) != 0 && (modifiers&(InputEvent.BUTTON2_MASK|InputEvent.BUTTON3_MASK)) == modifiers) {
	            // pop-up menu if button2 or button3
	            javax.swing.JPopupMenu popup = new javax.swing.JPopupMenu();
	            javax.swing.JMenuItem item = null;
	            if(panel.getToolTipText() == null) {
	                popup.add(item = new javax.swing.JMenuItem("ToolTips On"));
	            } else {
	                popup.add(item = new javax.swing.JMenuItem("ToolTips Off"));
	            }
	            item.addActionListener(this);
	            popup.addSeparator();
	            popup.add(item = new javax.swing.JMenuItem("Print"));
	            item.addActionListener(this);
	            popup.addSeparator();
	            if(subg.currentSelection != null) {
	                popup.add(item = new javax.swing.JMenuItem("Clear Selection"));
	                item.addActionListener(this);
	                if(subg.currentSelection instanceof Element) {
	                    popup.add(item = new javax.swing.JMenuItem("Select Siblings in Subgraph"));
	                    item.addActionListener(this);
	                    popup.addSeparator();
	                }
	            }
	            if(panel.hasOutline()) {
	                popup.add(item = new javax.swing.JMenuItem("Zoom to Sweep"));
	                item.addActionListener(this);
	            }
	            popup.add(item = new javax.swing.JMenuItem("Zoom In"));
	            item.addActionListener(this);
	            popup.add(item = new javax.swing.JMenuItem("Zoom Out"));
	            item.addActionListener(this);
	            popup.add(item = new javax.swing.JMenuItem("Reset Zoom"));
	            item.addActionListener(this);
	            popup.add(item = new javax.swing.JMenuItem("Scale to Fit"));
	            item.addActionListener(this);
	            java.awt.geom.Point2D mpt = panel.getTransform().transform(pt,null);
	            popup.show(panel,(int)mpt.getX(),(int)mpt.getY());
	        }
	  }

}