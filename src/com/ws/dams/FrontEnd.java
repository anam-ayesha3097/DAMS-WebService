package com.ws.dams;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.xml.ws.Endpoint;

import com.ws.client.*;

public class FrontEnd {
	static Logger LOGGER = Logger.getLogger(FrontEnd.class.getName());
	public static void main(String args[]) throws SecurityException, IOException {
		try {
		FrontEndImpl feImpl = new FrontEndImpl();
		Endpoint endpoint = Endpoint.publish("http://172.20.10.6:8080/FrontEnd", feImpl);
		if(endpoint.isPublished()) {
			System.out.println("Front End has started");
			
			Handler fileHandler = null;
			SimpleFormatter simpleFormatter = null;
			fileHandler = new FileHandler("E:/COMP-6231/DAMS Project/logs/Front End.txt");
			simpleFormatter = new SimpleFormatter();
			LOGGER.setUseParentHandlers(false);
			LOGGER.addHandler(fileHandler);
			fileHandler.setFormatter(simpleFormatter);
			
			LOGGER.info("Log File: Front End Server");
			LOGGER.info("FrontEnd EndPoint Details: "+endpoint);
			LOGGER.info("Front End : Started");
		}
		}
		catch(Exception e) {
			System.out.println("Front End Server Error "+e);
		}
	}
}
