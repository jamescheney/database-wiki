package org.dbwiki.web.log;

//import java.io.BufferedWriter;
import java.io.IOException;
import java.util.logging.Logger;


public class AppEngineServerLog extends ServerLog {
	
	private static final Logger log = Logger.getLogger(AppEngineServerLog.class.getName());

	
	@Override
	public void closeLog() throws IOException {
		
	}

	@Override
	public void openLog() throws IOException {
		
		
	}

	@Override
	public void writeln(String line) throws IOException {
		
		log.info(line);
	}

}
