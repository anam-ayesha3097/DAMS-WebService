package com.ws.client;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import com.ws.dams.FrontEndInterface;
import com.ws.dams.ReplicaManager1;

public class Client {

	static Client client = new Client();
	static Logger LOGGER = Logger.getLogger(Client.class.getName());
	
	public void sendRequestToFE(String userID, String appointmentType, String appointmentID, int capacity, String patientID,  int menuOption, FrontEndInterface frontEnd) {
		try {
			String reply =  null;
			reply = frontEnd.receiveRequestFromClient(userID, appointmentType, appointmentID, capacity, patientID, menuOption);	
			LOGGER.info("Server Reply : "+reply);
			System.out.println(reply);
		}
		catch(Exception e) {
			System.out.println("Send Request To FE "+e);
		}
		
	}
	
	public void sendRequestToFESwap(String userID, String oldAppointmentType, String oldAppointmentID, String newAppointmentType,
			String newAppointmentID,String patientID, int menuOption, FrontEndInterface frontend){
		try{
			String reply =  null;
			reply = frontend.receiveRequestForSwap(userID,oldAppointmentType,oldAppointmentID,newAppointmentType,newAppointmentID,patientID,menuOption);
			System.out.println(reply);
			LOGGER.info("Server Reply : "+reply);
		}
		catch(Exception e){
		System.out.println("Send Request to FE for SWAP "+ e);
			}
	}
	
	public static void main(String args[]) throws MalformedURLException {
		
		String userID = null;
		String userAccess = null;
		int continueOption = 1;
		int menuOption = 0;
		String appointmentID = null;
		String appointmentType = null;
		String patientID = null;
		int capacity = 0;
		String oldAppointmentType =null;
		String newAppointmentID= null;
		String oldAppointmentID=null;
		String newAppointmentType= null;
		
		try {
			
		
			Scanner sc = new Scanner(System.in);
			System.out.println("Enter User ID\n");
			userID = sc.next();
			userAccess = userID.substring(3, 4);	
			
			
		URL url = new URL("http://172.20.10.6:8080/FrontEnd?wsdl");
		
		QName qName = new QName("http://dams.ws.com/", "FrontEndImplService");
		
		Service service = Service.create(url, qName);
		
		FrontEndInterface frontEnd = service.getPort(FrontEndInterface.class);
		
		frontEnd.sayHello(userID);
		
		Handler fileHandler = null;
		SimpleFormatter simpleFormatter = null;
		fileHandler = new FileHandler("E:/COMP-6231/DAMS Project/logs/Client.txt");
		simpleFormatter = new SimpleFormatter();
		LOGGER.setUseParentHandlers(false);
		LOGGER.addHandler(fileHandler);
		fileHandler.setFormatter(simpleFormatter);
		
		LOGGER.info("Log File: Client");
		LOGGER.info("User LoggedIn: "+userID);
		
		switch(userAccess) {
		case "A": {
			while(continueOption != 0) {
				System.out.println("******Welcome Admin******");
				System.out.println("Select any one of the option\n 1. Add Appointment \n 2. Remove Appointment \n 3. List Available Appointment");
				System.out.println(" 4. Book an Appointment\n 5. Get Appointment Schedule \n 6. Cancel Appointment \n 7. Swap Appointment");
				System.out.println("Press 1 to continue or 0 to exit");
				menuOption = sc.nextInt();
				switch(menuOption) {
				case 1: {
					LOGGER.info("Admin Method : Add Appointment");
					System.out.println("Enter the Appointment Type, ID and its capacity \n");
	    			appointmentType = sc.next();
	    			appointmentID = sc.next();
	    			capacity = sc.nextInt();
	    			client.sendRequestToFE(userID, appointmentType, appointmentID, capacity, "", menuOption, frontEnd);
					break;
				}
				
				case 2: {
					LOGGER.info("Admin Method : Remove Appointment");
					System.out.println("Enter the Appointment Type, ID to Remove Appointment \n");
					appointmentType = sc.next();
					appointmentID = sc.next();
					client.sendRequestToFE(userID, appointmentType, appointmentID, 0, "", menuOption, frontEnd);
					break;
				}
				case 3: {
					LOGGER.info("Admin Method : List Available Appointment");
					System.out.println("Enter the Appointment Type to view it's Availability \n");
					appointmentType = sc.next();
					client.sendRequestToFE(userID, appointmentType, "", 0, "", menuOption, frontEnd);
					break;
				}
				case 4:{
					LOGGER.info("Admin Method : Book Appointment");
					System.out.println("Enter the Patient ID, Appointment Type and Appointment ID to Book the Appointment\n ");
					patientID = sc.next();
					appointmentType = sc.next();
					appointmentID = sc.next();
					client.sendRequestToFE(userID,appointmentType,appointmentID,0,patientID,menuOption,frontEnd);
					break;
				}
				case 5: {
					LOGGER.info("Admin Method : Get Appointment Schedule");
	            	System.out.println("Enter the Patient ID to display the Scheduled Appointments");
	            	patientID =  sc.next();
					System.out.println("Displayingn all Appointments for Patient " +patientID);
					client.sendRequestToFE(userID,"","", 0,patientID,menuOption,frontEnd);
					break;
					}
				case 6:{
					LOGGER.info("Admin Method : Cancel Appointment");
					System.out.println("Enter patient ID and Appointment ID: ");
					patientID=sc.next();
					appointmentID=sc.next();
					client.sendRequestToFE(userID,"",appointmentID,0,patientID,menuOption,frontEnd);
					break;
				}
				case 7:{
					LOGGER.info("Admin Method : Swap Appointment");
					System.out.println("Enter Patient ID");
					patientID= sc.next();
					System.out.println("Enter Old appointment Type and Old Appointment ID: ");
					oldAppointmentType=sc.next();
					oldAppointmentID=sc.next();
					System.out.println("Enter New Appointment Type and new Appointment ID: ");
					newAppointmentType=sc.next();
					newAppointmentID=sc.next();
					client.sendRequestToFESwap(userID,oldAppointmentType,oldAppointmentID,newAppointmentType,
											   newAppointmentID,patientID,menuOption,frontEnd);
					break;
				}

				default: { 
	            	System.out.println("Invalid Menu Option");
	            	LOGGER.info("Default Case Admin: Invalid Menu Option");
	            	sc.close();
	            	break;
	            }
				}
				
				System.out.println("Press 1 to continue or 0 to exit");
				continueOption = sc.nextInt();
				if(continueOption == 0)
				{
					System.out.println("Bye!");
					sc.close();
					System.exit(0);
				}	
			}
			
		break;
		}
		case "P": {
			while(continueOption!=0) {
				System.out.println("********Welcome Patient*******");
				System.out.println("Select any one of the option\n 1. Book Appointment \n 2. Cancel Appointment \n 3. Get Scheduled Appointment \n 4. Swap Appointment");
				System.out.println("Press 1 to continue or 0 to exit");
				menuOption = sc.nextInt();
				switch(menuOption) {
				case 1:{
					LOGGER.info("Patient Method : Book Appointment");
					System.out.println("Enter Appointment ID and Appointment Type to Book Appointment ");
					appointmentID = sc.next();
					appointmentType = sc.next();
					patientID = userID;
					client.sendRequestToFE(userID,appointmentType,appointmentID,0,patientID,menuOption,frontEnd);
					break;
				}
				case 2:{
					LOGGER.info("Patient Method : Cancel Appointment");
					System.out.println("Enter the Appointment ID to Cancel the Appointment");
					appointmentID = sc.next();
					patientID = userID;
					client.sendRequestToFE(userID,"",appointmentID,0,userID,menuOption,frontEnd);
					break;
				}
				case 3: {
					LOGGER.info("Patient Method : Get Appointment Schedule");
					System.out.println("Displayingn all Appointments for Patient " +userID);
					patientID = userID;
					client.sendRequestToFE(userID,"","",0,patientID,menuOption,frontEnd);
					break;
				}
				case 4:{
					LOGGER.info("Patient Method : Swap Appointment");
					patientID = userID;
					System.out.println("Enter Old appointment Type and Old Appointment ID ");
					oldAppointmentType = sc.next();
					oldAppointmentID = sc.next();
					System.out.println("Enter New Appointment Type and new Appointment ID ");
					newAppointmentType = sc.next();
					newAppointmentID = sc.next();
					client.sendRequestToFESwap(userID,oldAppointmentType,oldAppointmentID,newAppointmentType,
											   newAppointmentID,patientID,menuOption,frontEnd);
					break;
				}
				
				default:{
					System.out.println("Invalid Menu Option");
					LOGGER.info("Default Case for Patient : Invalid Input");
					sc.close();
					break;
				}
			}
				System.out.println("Press 1 to continue or 0 to exit");
				continueOption = sc.nextInt();
				if(continueOption == 0)
				{
					System.out.println("Bye!");
					sc.close();
					System.exit(0);
				}
				}
			
			break;
		}
		default : {
			System.out.println("Invalid User neither Admin nor Patient");
			LOGGER.info("Default Case for User: Invalid User ID ");
			break;
			}
		}
	}
		catch(Exception e) {
			System.out.println("Server is Sleeping");
			LOGGER.info("Exception: Front End is not Active");
		}
	}
}
