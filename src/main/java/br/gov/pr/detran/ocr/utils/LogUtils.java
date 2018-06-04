package br.gov.pr.detran.ocr.utils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Filter;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public class LogUtils {

	public static FileHandler fileHandler(String filename, final String type) throws SecurityException, IOException {
				
		FileHandler fileHandler = new FileHandler(filename+"_"+new SimpleDateFormat("yyyyMMdd").format(new Date())+".log", true);
		
		fileHandler.setFormatter(new SimpleFormatter());
		
		fileHandler.setFilter(new Filter() {			
			public boolean isLoggable(LogRecord arg0) {
				if (arg0.getParameters() != null) {
					return String.valueOf(arg0.getParameters()[0]).equalsIgnoreCase(type);
				}
				return false;
			}
		});
		
		return fileHandler;
	}
	
}
