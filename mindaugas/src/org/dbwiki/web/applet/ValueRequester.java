package org.dbwiki.web.applet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import javax.swing.SwingWorker;

public class ValueRequester extends SwingWorker<Void, Void> {
	HtmlGenerator htmlGen;
	ArrayList<String[]> versionsData;
	String id;
	BufferedReader in;



	ValueRequester (HtmlGenerator htmlGen, ArrayList<String[]> versionData, int ver){
		try {
			this.htmlGen=htmlGen;
			if (TreeVisualiser.typeParameter.equals(TreeVisualiser.StructureVisualiserParameter))
				this.id=htmlGen.getElem().getVersionElementId(ver);
			else if(TreeVisualiser.typeParameter.equals(TreeVisualiser.HistoryVisualiserParameter))
				this.id=htmlGen.getElem().getId();
			this.versionsData=versionData;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	@Override
	protected Void doInBackground() throws Exception {
	 	   try{
	 		  String value="";
		    	URL historyURL = new URL(TreeVisualiser.codeBase+id+"?value");
		 	    
		    	URLConnection hc = historyURL.openConnection(); 
		 	    InputStream inputStrm=hc.getInputStream();
		 	   InputStreamReader inputStrmRdr= new InputStreamReader(inputStrm);
		 	    this.in = new BufferedReader(inputStrmRdr);  
		 	    String line;
		 	    while ((line = in.readLine()) != null) {
		 	    	value+=line;
		 	    }
		 	    this.htmlGen.formDisplayerHead();
				this.htmlGen.formNodeDataHtml(value,this.versionsData);
				this.htmlGen.formDisplayerTail();
	 	   }catch (Exception e){
	 		 
	 		  e.printStackTrace();
	 	   }

		return null;
	}
	
	protected void done()
    {
		try {
			if (in!=null)
				in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (!this.isCancelled())
			this.htmlGen.dataDisplayer.setText(this.htmlGen.getHtml());
		
		
    }

}
