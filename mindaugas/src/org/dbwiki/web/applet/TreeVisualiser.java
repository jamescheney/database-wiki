package org.dbwiki.web.applet;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;

import javax.swing.JApplet;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;


public class TreeVisualiser extends JApplet implements  HyperlinkListener, ChangeListener, ActionListener
{
   
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	final static String NodeTypeMode = "Node type";
	final static String UserMode     = "User";
	final static String ActionMode   = "Action";
	final static String TestMode   = "Test";
	
	final static String ActionCommandIncrease = "+";
	final static String ActionCommandDecrease = "-";
	final static String ActionCommandSet = "Set";
	
	
	static String HistoryVisualiserParameter="history_dot";
	static String StructureVisualiserParameter="structure_dot";
	static String colorMode;
	static int currentVersion;
	static int lastVersion;
	static int firstVersion;
	public static int databaseVersion;
	
	static JPanel cards;
	static String typeParameter;
	static String codeBase;
	static Hashtable<String, Color> userColors;
	
	private GraphWorker gw; 
	private String nodeId=null;
	private boolean isFirstRun=true;
	
	
	
	
	
    public void init() {	
    	long start = System.currentTimeMillis();
    	if (this.isFirstRun)
    		setInitialValues();
    	cards = new JPanel(new CardLayout());
    	setContentPane(cards);
    	GridBagLayout gb = new GridBagLayout();
    	
		JPanel loading =  new JPanel(gb); 
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.CENTER;
		gb.setConstraints(loading,gbc);
				
		
		cards.add(loading,"load");
		setContentPane(cards);
		GridLayout layout = new GridLayout(10,1);
		loading.setLayout(layout);
		JProgressBar progress=new JProgressBar ();
		progress.setIndeterminate(true);
		loading.add(new JLabel(""));
		loading.add(new JLabel(""));
		loading.add(new JLabel(""));
		loading.add(new JLabel("     Please wait, rendering graph..."));
		loading.add(progress);
		
    	gw = new GraphWorker(this, this.nodeId);
		gw.execute();
		

	}
    
    private void setInitialValues() {
    	
    		this.isFirstRun=false;
	    	colorMode=NodeTypeMode;
	    	currentVersion=-1;
	    	lastVersion=-1;
	    	firstVersion=-1;
	    	databaseVersion=-1; 	
	    	cards=null;
	    	userColors=new Hashtable<String, Color>();
	    	codeBase=getCodeBase().toString();
	    	this.nodeId=this.getParameter("nodeId");
	    	typeParameter=getParameter("typeParameter");
	    	if (typeParameter==null){
	    		 typeParameter="structure_dot";
	    		 codeBase="http://localhost:8080/CIAWFB";
	    		 nodeId="2F35E";
	    	}
		
	}

	public void hyperlinkUpdate(HyperlinkEvent event) {
    	
 
        if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
         	try {		
      
       	        
         		String link=event.getURL().toString();
         		if (link.substring(link.lastIndexOf(".")).contains(".red")){      
         			this.nodeId=link.substring(7,link.indexOf(".red"));
         			getAppletContext().showDocument(new URL(codeBase+nodeId+"?version=current"));
         		}else { 
         			
         			this.nodeId=link.substring(7,link.indexOf(".com"));
         			CardLayout cl = (CardLayout)(TreeVisualiser.cards.getLayout());
         			cl.show( TreeVisualiser.cards, "load");
         			init();
        		}
         	}
 			catch (MalformedURLException e) {
 				e.printStackTrace();
 			}
         }
        
}

	@Override
	public void stateChanged(ChangeEvent e) {		
		JSlider source = (JSlider)e.getSource();
		int ver = (int)source.getValue();
		if (ver!=currentVersion)
			gw.getVersionChanger().changeDisplayedVersion(ver, false);

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String object = e.getSource().getClass().getName();
		if (object.equals("javax.swing.JRadioButton")){
			JRadioButton source = (JRadioButton)e.getSource();
			String type = source.getText();
			gw.getColorChanger().setColourScheme(type,currentVersion);

		}
		else if(e.getActionCommand().equals(ActionCommandSet)){
			try{
				Integer inputedVer=Integer.parseInt(gw.versionTextField.getText());
				GraphWorker.versionSlider.setValue(inputedVer);
			}catch(Exception e1){
				gw.versionTextField.setText(Integer.toString(GraphWorker.versionSlider.getValue()));
			}
		}
		else if (e.getActionCommand().equals(ActionCommandDecrease)){
			int ver= GraphWorker.versionSlider.getValue()-1;
			GraphWorker.versionSlider.setValue(ver);
		}
		else if (e.getActionCommand().equals(ActionCommandIncrease)){
			int ver= GraphWorker.versionSlider.getValue()+1;
			GraphWorker.versionSlider.setValue(ver);
		}
		
	}
    
	public boolean colorModeIsByUser(){
		return colorMode.equals(UserMode);
	}
	
	public boolean colorModeIsByAction(){
		return colorMode.equals(ActionMode);
	}
	
	public boolean colorModeIsByNodeType(){
		return colorMode.equals(NodeTypeMode);
	}
	
   
}
