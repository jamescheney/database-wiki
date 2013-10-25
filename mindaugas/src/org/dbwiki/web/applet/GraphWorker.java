package org.dbwiki.web.applet;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;


import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.text.DefaultCaret;

import att.grappa.Graph;
import att.grappa.GrappaPanel;
import att.grappa.Parser;

class GraphWorker  extends SwingWorker<Void, Void> 
{
	 private final class MyCaret extends DefaultCaret {

	        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

			@Override
	        protected void adjustVisibility(Rectangle nloc) {

	        }

	    }
	 private GrappaPanel gp;
	 private TreeVisualiser tv;
	 private String nodeId;
	 private ColorChanger cc;
	 private VersionChanger vc;
	 
	 JTextField versionTextField;
	 static JSlider versionSlider;
	 JPanel jpanel;
	 JEditorPane dataDisplayer;
	 long start;
	 

	public GraphWorker(TreeVisualiser tv, String nodeId) {
		this.tv=tv;
		this.nodeId=nodeId;
	}
	
	public VersionChanger getVersionChanger(){
		return this.vc;
	}
	
	private String getDot() throws IOException{
    	String graph="";
       	URL historyURL = new URL(TreeVisualiser.codeBase+nodeId+"?"+TreeVisualiser.typeParameter);
 	    URLConnection hc = historyURL.openConnection();  	
 	    BufferedReader in = new BufferedReader(new InputStreamReader(hc.getInputStream()));  
 	    String line;
 	    start = System.currentTimeMillis();
 	    while ((line = in.readLine()) != null) {
 	    	graph+=line;
 	    }
 	    in.close();
 	    return graph;
    }
        protected Void doInBackground() throws Exception
        {
        	try
    		{
        		Graph graph=getGraph();
        		DataCollector dc = new DataCollector(TreeVisualiser.typeParameter, graph);
     
        		renderInterface(graph);
        		
        		this.cc= new ColorChanger (graph,TreeVisualiser.typeParameter);
        		this.cc.makeColors(dc.getUsers());
        		
	    		//this.cc.setColourScheme(TreeVisualiser.colorMode,TreeVisualiser.currentVersion);
	    		this.cc.setColourScheme(TreeVisualiser.TestMode,TreeVisualiser.currentVersion);	
				if (TreeVisualiser.typeParameter.equals(TreeVisualiser.StructureVisualiserParameter)){
	    			this.vc = new VersionChanger (this.versionTextField, graph, 
	    					dc.getVersions(),this.dataDisplayer,this.cc);   		
	    			this.vc.changeDisplayedVersion(TreeVisualiser.currentVersion, true);  
				}
				long end = System.currentTimeMillis();
				System.out.println("Tree rendering time:"+ (end - start)+" ms");
	    			
	    	}	
		    catch(Exception x)
		   	{
		   		x.printStackTrace();
		   	}
			return null;
        }

     

		


		private void renderInterface(Graph graph) {
			this.gp = new GrappaPanel(graph);
    		TreeVisualiserAdapter ga = new TreeVisualiserAdapter();
    		this.gp.addGrappaListener(ga);
    		this.gp.setToolTipText(null);
    		this.gp.setScaleToFit(true);		
    		
    		
    		JScrollPane jspGraph = new JScrollPane(gp);
    	
    		GridBagLayout gb = new GridBagLayout();
    		jpanel = new JPanel(gb);
    		GridBagConstraints gbc = new GridBagConstraints();  
    		
    		
    		
    		//Tree visualiser
    		gbc.fill = GridBagConstraints.BOTH;
    		gbc.gridwidth = GridBagConstraints.REMAINDER;
    		gbc.weightx = 1.0;
    		gbc.weighty = 1.0;
    		gbc.anchor = GridBagConstraints.CENTER;
    		gb.setConstraints(jspGraph,gbc);
    		jpanel.add(jspGraph);
    		
    		//control panel
    		gbc.fill = GridBagConstraints.HORIZONTAL;
    		gbc.gridwidth = 1;
    		gbc.gridheight = 1;
    		gbc.weightx = 0.0;
    		gbc.weighty = 0.0;
    		gbc.gridwidth = GridBagConstraints.REMAINDER;
    		gbc.anchor = GridBagConstraints.CENTER;
    		JPanel controlPanel = new JPanel();
    		
    		if (TreeVisualiser.typeParameter.equals(TreeVisualiser.StructureVisualiserParameter)){
    			
    			controlPanel.add(new JLabel(" Version slider: "));
    			
    			JButton decButton=new JButton(TreeVisualiser.ActionCommandDecrease);
    			decButton.setFont(decButton.getFont().deriveFont(7f));
    			decButton.setActionCommand(TreeVisualiser.ActionCommandDecrease);
                decButton.addActionListener(tv);
        		controlPanel.add(decButton);
    			
    			versionSlider = new JSlider(TreeVisualiser.firstVersion,
    					TreeVisualiser.lastVersion,TreeVisualiser.lastVersion);
    			controlPanel.add(versionSlider);
    			versionSlider.addChangeListener(tv);
    			
    			JButton incButton=new JButton(TreeVisualiser.ActionCommandIncrease);
    			incButton.setFont(incButton.getFont().deriveFont(7f));
    			incButton.setActionCommand(TreeVisualiser.ActionCommandIncrease);
    			incButton.addActionListener(tv);
        		controlPanel.add(incButton);
    			
    			controlPanel.add(new JLabel("  Version:")); 			
    			
    			versionTextField=new JTextField("",5);
    			controlPanel.add(this.versionTextField);
    			
    			
    			
    			JButton setButton=new JButton(TreeVisualiser.ActionCommandSet);
    			setButton.setActionCommand(TreeVisualiser.ActionCommandSet);
        		setButton.addActionListener(tv);
        		controlPanel.add(setButton);
        		controlPanel.add(new JLabel("        "));
    		}

    		controlPanel.add(new JLabel("Colour graph by:"));
    		ButtonGroup graphColouringTypes = new ButtonGroup();
    		JRadioButton byNodeType = new JRadioButton(TreeVisualiser.NodeTypeMode);
    		byNodeType.addActionListener(this.tv);
    		graphColouringTypes.add(byNodeType);
    		controlPanel.add(byNodeType);
    		JRadioButton byUser = new JRadioButton(TreeVisualiser.UserMode);
    		byUser.addActionListener(this.tv);
    		graphColouringTypes.add(byUser);
    		controlPanel.add(byUser);
    		JRadioButton byAction = new JRadioButton(TreeVisualiser.ActionMode);
    		byAction.addActionListener(tv);
    		graphColouringTypes.add(byAction);
    		controlPanel.add(byAction);
    		
       		
    		if(tv.colorModeIsByAction())
    			byAction.setSelected(true);
    		else if(tv.colorModeIsByNodeType())
    			byNodeType.setSelected(true);
    		else if(tv.colorModeIsByUser())
    			byUser.setSelected(true);
    		
    		
    		gb.setConstraints(controlPanel,gbc); 		
    		jpanel.add(controlPanel);
    		
    		//data displayer
    		gbc.fill = GridBagConstraints.HORIZONTAL;
    		gbc.gridwidth = 1;
    		gbc.gridheight = 1;
    		gbc.weightx = 0.0;
    		gbc.weighty = 0.0;
    		gbc.gridwidth = GridBagConstraints.REMAINDER;
    		gbc.anchor = GridBagConstraints.CENTER;
    		
    		dataDisplayer = new JEditorPane();
    		dataDisplayer.setEditable(false);
    	    dataDisplayer.setCaret(new MyCaret());
    		JScrollPane jspDisplay = new JScrollPane(dataDisplayer);
    		jspDisplay.setMinimumSize(new Dimension(400,200));
    		gb.setConstraints(jspDisplay,gbc);
    		this.jpanel.add(jspDisplay);
    		ga.setDisplayAdapter(this.dataDisplayer);
    		
    		this.dataDisplayer.setContentType("text/html");     
    		this.dataDisplayer.addHyperlinkListener(tv);
    		HtmlGenerator htmlGen=new HtmlGenerator(graph,this.dataDisplayer);
    		htmlGen.showData(TreeVisualiser.currentVersion);
    		TreeVisualiser.cards.add(this.jpanel,"graph");
			
		}


		private Graph getGraph() throws Exception {
			Parser program =null;
    	    InputStream input;
    		
    			String sgraph = getDot();
    			input = new ByteArrayInputStream(sgraph.getBytes("UTF-8"));
    			program = new Parser(input);
    			program.parse();
    		
    		return program.getGraph();
		}


		protected void done()
        {
                try
                {
                	if (!this.isCancelled()){
                		CardLayout cl = (CardLayout)(TreeVisualiser.cards.getLayout());
                		cl.show( TreeVisualiser.cards, "graph");
                	}
                }
                catch (Exception e)
                {
                        e.printStackTrace();
                }
        }

		public ColorChanger getColorChanger() {
			return this.cc;
			
		}      
}