package com.ws.dams;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Sequencer {

	int sequenceNumber = 0;
	String request = null;
	static Logger LOGGER = Logger.getLogger(Sequencer.class.getName());
	public String tagSequenceNumber(String userID, String appointmentType, String appointmentID, int capacity, String patientID, int menuOption) throws SecurityException, IOException {
		try {
		sequenceNumber += 1;
		Handler fileHandler = null;
		SimpleFormatter simpleFormatter = null;
		fileHandler = new FileHandler("E:/COMP-6231/DAMS Project/logs/Sequencer.txt");
		simpleFormatter = new SimpleFormatter();
		LOGGER.setUseParentHandlers(false);
		LOGGER.addHandler(fileHandler);
		fileHandler.setFormatter(simpleFormatter);
		
		LOGGER.info("Log File: Sequencer");
		//Add Appointment
		if(patientID.isEmpty() && capacity != 0) {
			System.out.println("Add Appointment Request");
			request = sequenceNumber +":" +userID + ":" + appointmentType + ":" + appointmentID +":" + capacity +":" +menuOption;
		}
		//Remove Appointment 
		else if(patientID.isEmpty() && capacity == 0 && !appointmentID.isEmpty() && !appointmentType.isEmpty()) {
			System.out.println("Remove Appointment Request");
			request = sequenceNumber +":" +userID + ":" + appointmentType + ":" + appointmentID +":" +menuOption;
		}
		//Book Appointment
		else if(capacity == 0 && !patientID.isEmpty() && !appointmentID.isEmpty() && !appointmentType.isEmpty() ) {
			System.out.println("Book Appointment Request");
			request = sequenceNumber +":" +userID + ":" + appointmentType + ":" + appointmentID +":" +patientID + ":" +menuOption;
		}
		//Get Appointment Schedule
		else if(appointmentID.isEmpty() && appointmentType.isEmpty() && !patientID.isEmpty() && capacity == 0) {
			System.out.println("Get Appointment Schedule Request");
			request = sequenceNumber + ":" +userID + ":" +patientID + ":" +menuOption;
		}
		//List Available Appointment
		else if(appointmentID.isEmpty() && capacity == 0 && patientID.isEmpty())
		{
			System.out.println("List Avaialable Appointment Request");
			request = sequenceNumber +":" +userID + ":" + appointmentType + ":" +menuOption;
		}
		//Cancel Appointment
		else if (!appointmentID.isEmpty() && appointmentType.isEmpty() && !patientID.isEmpty()) {
			System.out.println("Cancel Appointment: ");
			request= sequenceNumber+":"+userID+":"+appointmentID+":"+patientID+":"+menuOption;
		}
		
		//request = sequenceNumber +":" +userID + ":" + appointmentType + ":" + appointmentID +":" + capacity +":" +patientID + ":" +menuOption;
		System.out.println("Request " +request);
		LOGGER.info("Request With Sequencer Number : "+request);
		return request;
		}
		catch(Exception e) {
			System.out.println("Sequencer Exception "+e);
			LOGGER.info("Sequencer Exception : "+e);
		}
		return "";
	}
	
	public String tagSequenceNumberSwap(String userID, String oldAppointmentType, String oldAppointmentID, String newAppointmentType,
			String newAppointmentID, String patientID, int menuOption){
		try {
			
			Handler fileHandler = null;
			SimpleFormatter simpleFormatter = null;
			fileHandler = new FileHandler("E:/COMP-6231/DAMS Project/logs/Sequencer.txt");
			simpleFormatter = new SimpleFormatter();
			LOGGER.setUseParentHandlers(false);
			LOGGER.addHandler(fileHandler);
			fileHandler.setFormatter(simpleFormatter);
			
			LOGGER.info("Log File: Sequencer Swap");
	
		sequenceNumber += 1;

		request = sequenceNumber +":" +userID + ":" + oldAppointmentType + ":" + oldAppointmentID +":" + newAppointmentType+
				":"+newAppointmentID +":" +patientID+":"+menuOption;

		System.out.println("Request " +request);
		LOGGER.info("Swap Request with Sequencer Number "+request);
		return request;
		}
		catch(Exception e) {
			System.out.println("Sequencer Swap Exception "+e);
		}
		return "";
}
}
