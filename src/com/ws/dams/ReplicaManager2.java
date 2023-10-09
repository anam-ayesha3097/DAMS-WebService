package com.ws.dams;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReplicaManager1 {
	DatagramSocket socket1=null;
	DatagramPacket packet1=null;
	static int count=0;
	static int rCount=1;

	static int errorCountMTL, errorCountQUE, errorCountSHE=0;

	static ReplicaManager1 rm1 = new ReplicaManager1();
	static MTLServer mtlServer = new MTLServer();
	static QUEServer queServer= new QUEServer();
	static SHEServer sheServer= new SHEServer();
	HashMap<Integer, String> hash_map = new HashMap<Integer, String>();
	static Logger LOGGER = Logger.getLogger(ReplicaManager1.class.getName());

	public static void main(String args[]) throws IOException {

		Handler fileHandler=null;
		SimpleFormatter simpleFormatter=null;
		fileHandler = new FileHandler("D:/Concordia University/COMP 6231/logs/Replica Manager 2.txt");
		simpleFormatter=new SimpleFormatter();
		LOGGER.setUseParentHandlers(false);
		LOGGER.addHandler(fileHandler);
		fileHandler.setFormatter(simpleFormatter);
		LOGGER.info("Log File: Replica Manager 2");
		rm1.udpConnect();

		//rm1.multicastSocket();

	}

	public void udpConnect() {
		Runnable t1 = () -> {
		try {
			DatagramSocket udpSocket = new DatagramSocket(2002);
			byte[] buffer = new byte[1000];
			System.out.println("UDP Server is Running");
			mtlServer.createHashMap();
			queServer.createHashMap();
			sheServer.createHashMap();
			//To start the UDP Server for List/Swap Appointment and Get Appointment Schedule
			Runnable t3 = () -> {
				mtlServer.udp_list_get_swap(mtlServer);

			}; new Thread(t3).start();
			Runnable t4 = () -> {
				queServer.udp_list_get_swap(queServer);
			}; new Thread(t4).start();
			Runnable t5 = () -> {
				sheServer.udp_list_get_swap(sheServer);
			}; new Thread(t5).start();
			LOGGER.info("Method: Get Request From Front End.");
			while(true) {
				System.out.println("Inside While of RM1 UDP");
				DatagramPacket request = null;
				request = new DatagramPacket(buffer, buffer.length);
				udpSocket.receive(request);
				System.out.println(request.getLength());
				//System.out.println(new String (request.getData()));
				String requestString = new String(request.getData());

				requestString = cleanUDPReplyString(requestString);
				System.out.println(requestString);
				String str = new String(request.getData(), 0, request.getLength());
				if(!requestString.isEmpty()) {

					System.out.println(str);

					StringTokenizer data = new StringTokenizer(str,":");
					int id = Integer.parseInt(data.nextToken());
					System.out.println("Sequence Number "+id);
					System.out.println("RCount: "+rCount);

					String replicaName = data.nextToken();
					System.out.println(replicaName);


					String finalRequestString = str.substring(str.indexOf(":") + 1);
					System.out.println("Final Request "+finalRequestString);


					if(rCount == id || hash_map.containsKey(rCount)) {
						try {
							rm1.send(finalRequestString, replicaName);
							rCount++;
							System.out.println("RCount: "+rCount);
						} catch (Exception e) {
							System.out.println("Exception " + e);
						}
					}
					else if(rCount > id){
						System.out.println("This request has executed before ");//Discard
					}
					else if(rCount<id){
						hash_map.put(id,finalRequestString);
						System.out.println("put it in the Hashmap and will execute later "+hash_map);
					}


					DatagramPacket reply = null;
					byte[] udpByteReply = new byte[1000];

					udpByteReply = "received request".getBytes();

					reply = new DatagramPacket(udpByteReply, udpByteReply.length, request.getAddress(), request.getPort());
					udpSocket.send(reply);
				}
			}
		}
		catch(Exception e) {
			System.out.println("Replica Manager 1 Exception "+e);
		}
		}; new Thread(t1).start();

		Runnable t2 = () -> {
			//DatagramSocket err = null;
			try {
				DatagramSocket udpSocket = new DatagramSocket(1001);
				byte[] buffer = new byte[1000];
				System.out.println("Receiving Error Message From FE at port:1001");
				while(true) {

					DatagramPacket request = null;
					request = new DatagramPacket(buffer, buffer.length);
					udpSocket.receive(request);

					String requestString = new String(request.getData()).trim();
					System.out.println(requestString);

					if(requestString.substring(0, 3).equalsIgnoreCase("MTL")){

					}
					else if(requestString.substring(0, 3).equalsIgnoreCase("QUE")){

					}
					else if(requestString.substring(0, 3).equalsIgnoreCase("SHE")){

					}

				}
			}
			catch(Exception e) {
				System.out.println("Replica Manager 1 Exception "+e);
			}
		};
		new Thread(t2).start();
	}

	public void multicastSocket() throws IOException {

		Runnable t1 = () -> {
			try {
				String group = "226.4.5.6";

				MulticastSocket ms = new MulticastSocket(5000);

				ms.joinGroup(InetAddress.getByName(group));

				String[] splitRequest = null;

				mtlServer.createHashMap();

				//To start the UDP Server for List/Swap Appointment and Get Appointment Schedule
				Runnable t3 = () -> {
					mtlServer.udp_list_get_swap(mtlServer);
					queServer.udp_list_get_swap(queServer);
				}; new Thread(t3).start();

				while(true) {
					System.out.println("Replica Manager has Started");
					byte[] buf = new byte[1024];


					DatagramPacket dp = new DatagramPacket(buf,1024);

					ms.receive(dp);

					String str = new String(dp.getData(), 0, dp.getLength());

					System.out.println(str);

					StringTokenizer data = new StringTokenizer(str,":");
					int id = Integer.parseInt(data.nextToken());
					System.out.println("Sequence Number "+id);
					System.out.println("RCount: "+rCount);

					String replicaName = data.nextToken();
					System.out.println(replicaName);


					String finalRequestString = str.substring(str.indexOf(":") + 1);
					System.out.println("Final Request "+finalRequestString);


					if(rCount == id || hash_map.containsKey(rCount)) {
						try {
							rm1.send(finalRequestString, replicaName);
							rCount++;
							System.out.println("RCount: "+rCount);
						} catch (Exception e) {
							System.out.println("Exception " + e);
						}
					}
					else if(rCount > id){
						System.out.println("This request has executed before ");//Discard
					}
					else if(rCount<id){
						hash_map.put(id,finalRequestString);
						System.out.println("put it in the Hashmap and will execute later "+hash_map);
					}
				}
				//ms.leaveGroup(InetAddress.getByName(group));

				//ms.close();
			}
			catch(Exception e) {
				System.out.println("Replica Manager 1 Exception "+e);
			}
		};
		new Thread(t1).start();

		///////////////////
		Runnable t2 = () -> {
			//DatagramSocket err = null;
			try {
				DatagramSocket udpSocket = new DatagramSocket(1001);
				byte[] buffer = new byte[1000];
				System.out.println("Receiving Error Message From FE at port:1001");
				while(true) {

					DatagramPacket request = null;
					request = new DatagramPacket(buffer, buffer.length);
					udpSocket.receive(request);
					String requestString = new String(request.getData());
					rm1.count++;
					System.out.println("count+"+rm1.count);
					if(rm1.count==3){
						socket1=new DatagramSocket();

						InetAddress host = InetAddress.getByName("localhost");

						socket1.send(packet1);
					}

				}
			}
			catch(Exception e) {
				System.out.println("Replica Manager 1 Exception "+e);
			}
		};
		new Thread(t2).start();
	}

	public static String cleanUDPReplyString(String dirtyReply) {
		String cleanXMLString = null;
		Pattern pattern = null;
		Matcher matcher = null;
		pattern = Pattern.compile("[\\000]*");
		matcher = pattern.matcher(dirtyReply);
		if (matcher.find()) {
			cleanXMLString = matcher.replaceAll("");
			System.out.println("Cleaned" +cleanXMLString);
		}
		return cleanXMLString;
	}

	public void send(String finalRequestString, String replicaName) {
		LOGGER.info("Request Received: "+ finalRequestString);
		String[] split = null;

		split = finalRequestString.split(":");
		int menuNumber= Integer.parseInt(finalRequestString.substring(finalRequestString.length()-1));
		System.out.println(menuNumber);
		System.out.println("Admin/Patient " +replicaName.substring(3, 4));
		LOGGER.info(" User Connected A-> Admin/P-> Patient "+ replicaName.substring(3,4));
		switch(replicaName.substring(3, 4)) {
			case "A" : {
				switch(menuNumber) {
					case 1: {
						LOGGER.info("Method Request By Admin: Add Appointment");
						System.out.println("Inside Case 1");
						System.out.println("Replica Name "+replicaName);
						String appointmentID = split[2];
						String appointmentType = split[1];
						int capacity = Integer.parseInt(split[3]);
						String replyToFE = null;
						if(replicaName.substring(0, 3).equalsIgnoreCase("MTL")) {
							boolean addAppointment = mtlServer.addAppointment(appointmentID, appointmentType, capacity);
							if(addAppointment == true)
								replyToFE = "Appointment Added Successfully!";
							else
								replyToFE = "Appointment Could'nt be Added";
							System.out.println("Add Appointment Return "+addAppointment);
							mtlServer.sendRequestToFE(replyToFE);
							LOGGER.info("Replica Stared: Montreal");
						}
						else if(replicaName.substring(0, 3).equalsIgnoreCase("QUE")) {
							boolean addAppointment = queServer.addAppointment(appointmentID, appointmentType, capacity);
							if(addAppointment == true)
								replyToFE = "Appointment Added Successfully!";
							else
								replyToFE = "Appointment Could'nt be Added";
							System.out.println("Add Appointment Return "+addAppointment);
							queServer.sendRequestToFE(replyToFE);
							LOGGER.info("Replica Started: Quebec");

						}
						else if(replicaName.substring(0, 3).equalsIgnoreCase("SHE")) {
							boolean addAppointment = sheServer.addAppointment(appointmentID, appointmentType, capacity);
							if(addAppointment == true)
								replyToFE = "Appointment Added Successfully!";
							else
								replyToFE = "Appointment Could'nt be Added";
							System.out.println("Add Appointment Return "+addAppointment);
							sheServer.sendRequestToFE(replyToFE);
							LOGGER.info("Replica Started: Sherbrooke");
						}

						break;
					}

					case 2: {
						LOGGER.info("Method Request By Admin: Remove Appointment");
						System.out.println("Inside Case 2");
						System.out.println("Replica Name "+replicaName);
						String appointmentID = split[2];
						String appointmentType = split[1];
						String replyToFE = null;
						if(replicaName.substring(0, 3).equalsIgnoreCase("MTL")) {
							boolean removeAppointment = mtlServer.removeAppointment(appointmentID,appointmentType);
							if(removeAppointment == true)
								replyToFE = "Appointment Removed Successfully!";
							else
								replyToFE = "Appointment Could'nt be Removed";
							System.out.println("Remove Appointment Return "+removeAppointment);
							mtlServer.sendRequestToFE(replyToFE);
							LOGGER.info("Replica Started: Montreal ");
						}
						else if(replicaName.substring(0, 3).equalsIgnoreCase("QUE")) {
							boolean removeAppointment = queServer.removeAppointment(appointmentID,appointmentType);
							if(removeAppointment == true)
								replyToFE = "Appointment Removed Successfully!";
							else
								replyToFE = "Appointment Could'nt be Removed";
							System.out.println("Remove Appointment Return "+removeAppointment);
							queServer.sendRequestToFE(replyToFE);
							LOGGER.info("Replica Started: Quebec");
						}
						else if(replicaName.substring(0, 3).equalsIgnoreCase("SHE")) {
							boolean removeAppointment = sheServer.removeAppointment(appointmentID,appointmentType);
							if(removeAppointment == true)
								replyToFE = "Appointment Removed Successfully!";
							else
								replyToFE = "Appointment Could'nt be Removed";
							System.out.println("Remove Appointment Return "+removeAppointment);
							sheServer.sendRequestToFE(replyToFE);
							LOGGER.info("Replica Started: Sherbrooke");
						}
						break;
					}

					case 3 : {
						LOGGER.info("Method Request by Admin: List Availability");
						String appointmentType = split[1];
						//System.out.println("Appointment Type of Case 3 "+appointmentType);
						if(replicaName.substring(0, 3).equalsIgnoreCase("MTL")) {
							String serverReplies = null;
							serverReplies = mtlServer.listAppointmentAvailability(appointmentType);
							if(serverReplies!=null)
								//System.out.println(serverReplies);
								mtlServer.sendRequestToFE(serverReplies);
							else
								mtlServer.sendRequestToFE("MTL Server might not be Active");
							LOGGER.info("Replica Started: Montreal");
						}
						else if(replicaName.substring(0, 3).equalsIgnoreCase("QUE")) {
							String serverReplies = null;
							serverReplies = queServer.listAppointmentAvailability(appointmentType);
							if(serverReplies!=null)
								//System.out.println(serverReplies);
								queServer.sendRequestToFE(serverReplies);
							else
								queServer.sendRequestToFE("QUE Server might not be Active");
							LOGGER.info("Replica Started: Quebec");
						}
						else if(replicaName.substring(0, 3).equalsIgnoreCase("SHE")) {
							String serverReplies = null;
							serverReplies = sheServer.listAppointmentAvailability(appointmentType);
							if(serverReplies!=null)
								//System.out.println(serverReplies);
								sheServer.sendRequestToFE(serverReplies);
							else
								sheServer.sendRequestToFE("SHE Server might not be Active");
							LOGGER.info("Replica Started: Sherbrooke");
						}

						break;
					}
					case 4 : {
						LOGGER.info("Method Request By Admin: Book Appointment");
						String appointmentType = split[1];
						String appointmentID = split[2];
						String patientID = split[3];
						String replyToFE = null;
						if(replicaName.substring(0, 3).equalsIgnoreCase("MTL")) {

							int serverReplies = mtlServer.bookAppointment(patientID,appointmentID,appointmentType);
							if(serverReplies == 1) {
								replyToFE = "Appointment Booked Successfully!";
							}
							else if (serverReplies == 2) {
								replyToFE = "No Appointment " +appointmentID +" Exists for " +appointmentType;
							}
							else if(serverReplies == 3) {
								replyToFE = "Patient's "+patientID +" Appointment Already Exists " +appointmentID;
							}
							else if(serverReplies == 4) {
								replyToFE =  "Patient "+patientID +" has Booked Maximum Number of Appointments i.e 3 for " +appointmentID +" week";
							}
							else if(serverReplies == 0)
							{
								replyToFE = "No Appointment " +appointmentID +" Available for " +appointmentType ;
							}

							mtlServer.sendRequestToFE(replyToFE);
							LOGGER.info("Replica Started: Montreal");
						}
						else if(replicaName.substring(0, 3).equalsIgnoreCase("QUE")) {
							int serverReplies = queServer.bookAppointment(patientID,appointmentID,appointmentType);
							if(serverReplies == 1) {
								replyToFE = "Appointment Booked Successfully!";
							}
							else if (serverReplies == 2) {
								replyToFE = "No Appointment " +appointmentID +" Exists for " +appointmentType;
							}
							else if(serverReplies == 3) {
								replyToFE = "Patient's "+patientID +" Appointment Already Exists " +appointmentID;
							}
							else if(serverReplies == 4) {
								replyToFE =  "Patient "+patientID +" has Booked Maximum Number of Appointments i.e 3 for " +appointmentID +" week";
							}
							else if(serverReplies == 0)
							{
								replyToFE = "No Appointment " +appointmentID +" Available for " +appointmentType ;
							}
							queServer.sendRequestToFE(replyToFE);
							LOGGER.info("Replica Started: Quebec");
						}
						else if(replicaName.substring(0, 3).equalsIgnoreCase("SHE")) {
							int serverReplies = sheServer.bookAppointment(patientID,appointmentID,appointmentType);
							if(serverReplies == 1) {
								replyToFE = "Appointment Booked Successfully!";
							}
							else if (serverReplies == 2) {
								replyToFE = "No Appointment " +appointmentID +" Exists for " +appointmentType;
							}
							else if(serverReplies == 3) {
								replyToFE = "Patient's "+patientID +" Appointment Already Exists " +appointmentID;
							}
							else if(serverReplies == 4) {
								replyToFE =  "Patient "+patientID +" has Booked Maximum Number of Appointments i.e 3 for " +appointmentID +" week";
							}
							else if(serverReplies == 0)
							{
								replyToFE = "No Appointment " +appointmentID +" Available for " +appointmentType ;
							}
							sheServer.sendRequestToFE(replyToFE);
							LOGGER.info("Replica Started: Sherbrooke");
						}
						break;
					}
					case 5 : {
						LOGGER.info("Method Request by Admin: Get Appointment Schedule");
						String patientID = split[1];
						if(replicaName.substring(0, 3).equalsIgnoreCase("MTL")) {
							String serverReplies = null;
							serverReplies = mtlServer.getAppointmentSchedule(patientID);
							if(serverReplies!=null)
								mtlServer.sendRequestToFE(serverReplies);
							else
								mtlServer.sendRequestToFE("MTL Server might not be Active");
							LOGGER.info("Replica Started: Montreal ");
						}
						else if(replicaName.substring(0, 3).equalsIgnoreCase("QUE")) {
							String serverReplies = null;
							serverReplies = queServer.getAppointmentSchedule(patientID);
							if(serverReplies!=null)
								queServer.sendRequestToFE(serverReplies);
							else
								queServer.sendRequestToFE("QUE Server might not be Active");
							LOGGER.info("Replica Started: Quebec");
						}
						else if(replicaName.substring(0, 3).equalsIgnoreCase("SHE")) {
							String serverReplies = null;
							serverReplies = sheServer.getAppointmentSchedule(patientID);
							if(serverReplies!=null)
								sheServer.sendRequestToFE(serverReplies);
							else
								sheServer.sendRequestToFE("SHE Server might not be Active");
							LOGGER.info("Replica Started: Sherbrooke");
						}
						break;
					}
					case 6:{
						LOGGER.info("Method Request by Admin: Cancel Appointment");
						System.out.println("Replica Name "+ replicaName);
						String appointmentID = split[1];
						String patientID = split[2];
						//int capacity = Integer.parseInt(split[3]);
						String replyToFE = null;
						if(replicaName.substring(0, 3).equalsIgnoreCase("MTL")) {
							boolean cancelAppointment = mtlServer.cancelAppointment(patientID,appointmentID);
							if(cancelAppointment == true)
								replyToFE = "Appointment Cancelled Successfully!";
							else
								replyToFE = "Appointment Could'nt be Cancelled";
							System.out.println("Cancel Appointment Return "+cancelAppointment);
							mtlServer.sendRequestToFE(replyToFE);
							LOGGER.info("Replica Started: Montreal");
						}
						else if(replicaName.substring(0, 3).equalsIgnoreCase("QUE")) {
							boolean cancelAppointment = queServer.cancelAppointment(patientID,appointmentID);
							if(cancelAppointment == true)
								replyToFE = "Appointment Cancelled Successfully!";
							else
								replyToFE = "Appointment Could'nt be Cancelled";
							System.out.println("Cancel Appointment Return "+cancelAppointment);
							queServer.sendRequestToFE(replyToFE);
							LOGGER.info("Replica Started: Quebec");
						}
						else if(replicaName.substring(0, 3).equalsIgnoreCase("SHE")) {
							boolean cancelAppointment = sheServer.cancelAppointment(patientID,appointmentID);
							if(cancelAppointment == true)
								replyToFE = "Appointment Cancelled Successfully!";
							else
								replyToFE = "Appointment Could'nt be Cancelled";
							System.out.println("Cancel Appointment Return "+cancelAppointment);
							sheServer.sendRequestToFE(replyToFE);
							LOGGER.info("Replica Started: Sherbrooke");
						}
						break;
					}
					case 7:{
						LOGGER.info("Method request by Admin:Swap Appointment");
						System.out.print("Replica Name: "+ replicaName);
						String oldAppointmentType=split[1];
						String oldAppointmentID= split[2];
						String newAppointmentType=split[3];
						String newAppointmentID=split[4];
						String patientID=split[5];
						String replyToFE=null;
						if(replicaName.substring(0,3).equalsIgnoreCase("MTL")) {
							int swapResult= mtlServer.swapAppointment(patientID, oldAppointmentID, oldAppointmentType, newAppointmentID, newAppointmentType);
							if(swapResult == 1) {
								mtlServer.sendRequestToFE("Appointment Swaped Successfully!");
							}
							else if(swapResult == 2){
								mtlServer.sendRequestToFE("Old Appointment ID "+oldAppointmentID+"is not valid! ");
							}
							else if(swapResult == 3){
								mtlServer.sendRequestToFE("Patient's "+patientID +" Appointment Already Exists " +newAppointmentID);
							}

							else if(swapResult == 0){
								mtlServer.sendRequestToFE("No Appointment " +newAppointmentID +" Available for " +newAppointmentType);
							}

							else if(swapResult == 4  ){
								mtlServer.sendRequestToFE("Patient "+patientID +" has Booked Maximum Number of Appointments i.e 3 for " +newAppointmentID +" week");
							}
							else if(swapResult == -1  ){
								mtlServer.sendRequestToFE("New Appointment ID " +newAppointmentID +" for New Appointment Type " +newAppointmentType +" does not exists");
							}
							LOGGER.info("Replica Started: Montreal");
						}
						else if(replicaName.substring(0,3).equalsIgnoreCase("QUE")) {
							int swapResult= queServer.swapAppointment(patientID, oldAppointmentID, oldAppointmentType, newAppointmentID, newAppointmentType);
							if(swapResult == 1) {
								queServer.sendRequestToFE("Appointment Swaped Successfully!");
							}
							else if(swapResult == 2){
								queServer.sendRequestToFE("Old Appointment ID "+oldAppointmentID+"is not valid! ");
							}
							else if(swapResult == 3){
								queServer.sendRequestToFE("Patient's "+patientID +" Appointment Already Exists " +newAppointmentID);
							}

							else if(swapResult == 0){
								queServer.sendRequestToFE("No Appointment " +newAppointmentID +" Available for " +newAppointmentType);
							}

							else if(swapResult == 4  ){
								queServer.sendRequestToFE("Patient "+patientID +" has Booked Maximum Number of Appointments i.e 3 for " +newAppointmentID +" week");
							}
							else if(swapResult == -1  ){
								queServer.sendRequestToFE("New Appointment ID " +newAppointmentID +" for New Appointment Type " +newAppointmentType +" does not exists");
							}
							LOGGER.info("Replica Started: Quebec");
						}
						else if(replicaName.substring(0,3).equalsIgnoreCase("SHE")) {
							int swapResult= sheServer.swapAppointment(patientID, oldAppointmentID, oldAppointmentType, newAppointmentID, newAppointmentType);
							if(swapResult == 1) {
								sheServer.sendRequestToFE("Appointment Swaped Successfully!");
							}
							else if(swapResult == 2){
								sheServer.sendRequestToFE("Old Appointment ID "+oldAppointmentID+"is not valid! ");
							}
							else if(swapResult == 3){
								sheServer.sendRequestToFE("Patient's "+patientID +" Appointment Already Exists " +newAppointmentID);
							}

							else if(swapResult == 0){
								sheServer.sendRequestToFE("No Appointment " +newAppointmentID +" Available for " +newAppointmentType);
							}

							else if(swapResult == 4  ){
								sheServer.sendRequestToFE("Patient "+patientID +" has Booked Maximum Number of Appointments i.e 3 for " +newAppointmentID +" week");
							}
							else if(swapResult == -1  ){
								sheServer.sendRequestToFE("New Appointment ID " +newAppointmentID +" for New Appointment Type " +newAppointmentType +" does not exists");
							}
							LOGGER.info("Replica Started: Sherbrooke");
						}
						break;
					}
					default : {
						System.out.println("Invalid Input");
						break;
					}
				}
				break;
			}

			case "P" : {
				switch(menuNumber) {
					case 1 : {
						LOGGER.info("Method Request by Pateint: Book Appointment");
						String appointmentType = split[1];
						String appointmentID = split[2];
						String patientID = split[3];
						String replyToFE = null;
						if(replicaName.substring(0, 3).equalsIgnoreCase("MTL")) {

							int serverReplies = mtlServer.bookAppointment(patientID,appointmentID,appointmentType);
							if(serverReplies == 1) {
								replyToFE = "Appointment Booked Successfully!";
							}
							else if (serverReplies == 2) {
								replyToFE = "No Appointment " +appointmentID +" Exists for " +appointmentType;
							}
							else if(serverReplies == 3) {
								replyToFE = "Patient's "+patientID +" Appointment Already Exists " +appointmentID;
							}
							else if(serverReplies == 4) {
								replyToFE =  "Patient "+patientID +" has Booked Maximum Number of Appointments i.e 3 for " +appointmentID +" week";
							}
							else if(serverReplies == 0)
							{
								replyToFE = "No Appointment " +appointmentID +" Available for " +appointmentType ;
							}

							mtlServer.sendRequestToFE(replyToFE);
							LOGGER.info("Replica Started:Montreal");
						}
						else if(replicaName.substring(0, 3).equalsIgnoreCase("QUE")) {
							int serverReplies = queServer.bookAppointment(patientID,appointmentID,appointmentType);
							if(serverReplies == 1) {
								replyToFE = "Appointment Booked Successfully!";
							}
							else if (serverReplies == 2) {
								replyToFE = "No Appointment " +appointmentID +" Exists for " +appointmentType;
							}
							else if(serverReplies == 3) {
								replyToFE = "Patient's "+patientID +" Appointment Already Exists " +appointmentID;
							}
							else if(serverReplies == 4) {
								replyToFE =  "Patient "+patientID +" has Booked Maximum Number of Appointments i.e 3 for " +appointmentID +" week";
							}
							else if(serverReplies == 0)
							{
								replyToFE = "No Appointment " +appointmentID +" Available for " +appointmentType ;
							}
							queServer.sendRequestToFE(replyToFE);
							LOGGER.info("Replica Started: Quebec");
						}
						else if(replicaName.substring(0, 3).equalsIgnoreCase("SHE")) {
							int serverReplies = sheServer.bookAppointment(patientID,appointmentID,appointmentType);
							if(serverReplies == 1) {
								replyToFE = "Appointment Booked Successfully!";
							}
							else if (serverReplies == 2) {
								replyToFE = "No Appointment " +appointmentID +" Exists for " +appointmentType;
							}
							else if(serverReplies == 3) {
								replyToFE = "Patient's "+patientID +" Appointment Already Exists " +appointmentID;
							}
							else if(serverReplies == 4) {
								replyToFE =  "Patient "+patientID +" has Booked Maximum Number of Appointments i.e 3 for " +appointmentID +" week";
							}
							else if(serverReplies == 0)
							{
								replyToFE = "No Appointment " +appointmentID +" Available for " +appointmentType ;
							}
							sheServer.sendRequestToFE(replyToFE);
							LOGGER.info("Replica Started: Sherbrooke");
						}
						break;
					}
					case 3 : {
						LOGGER.info("Method Request by Patient: get Appointment Schedule");
						String patientID = split[1];
						if(replicaName.substring(0, 3).equalsIgnoreCase("MTL")) {
							String serverReplies = null;
							serverReplies = mtlServer.getAppointmentSchedule(patientID);
							if(serverReplies!=null)
								mtlServer.sendRequestToFE(serverReplies);
							else
								mtlServer.sendRequestToFE("MTL Server might not be Active");
							LOGGER.info("Replica Started: Montreal");

						}
						else if(replicaName.substring(0, 3).equalsIgnoreCase("QUE")) {
							String serverReplies = null;
							serverReplies = queServer.getAppointmentSchedule(patientID);
							if(serverReplies!=null)
								queServer.sendRequestToFE(serverReplies);
							else
								queServer.sendRequestToFE("QUE Server might not be Active");
							LOGGER.info("Replica Started: Quebec");
						}
						else if(replicaName.substring(0, 3).equalsIgnoreCase("SHE")) {
							String serverReplies = null;
							serverReplies = sheServer.getAppointmentSchedule(patientID);
							if(serverReplies!=null)
								sheServer.sendRequestToFE(serverReplies);
							else
								sheServer.sendRequestToFE("SHE Server might not be Active");
							LOGGER.info("Replica Started: Sherbrooke");
						}
						break;
					}
					case 2  :{
						LOGGER.info("Method Request by Patient: Cancel Appointment");
						System.out.println("Replica Name "+ replicaName);
						String appointmentID = split[1];
						String patientID = split[2];
						//int capacity = Integer.parseInt(split[3]);
						String replyToFE = null;
						if(replicaName.substring(0, 3).equalsIgnoreCase("MTL")) {
							boolean cancelAppointment = mtlServer.cancelAppointment(patientID,appointmentID);
							if(cancelAppointment == true)
								replyToFE = "Appointment Cancelled Successfully!";
							else
								replyToFE = "Appointment Could'nt be Cancelled";
							System.out.println("Cancel Appointment Return "+cancelAppointment);
							mtlServer.sendRequestToFE(replyToFE);
							LOGGER.info("Replica Started: Montreal");
						}
						else if(replicaName.substring(0, 3).equalsIgnoreCase("QUE")) {
							boolean cancelAppointment = queServer.cancelAppointment(patientID,appointmentID);
							if(cancelAppointment == true)
								replyToFE = "Appointment Cancelled Successfully!";
							else
								replyToFE = "Appointment Could'nt be Cancelled";
							System.out.println("Cancel Appointment Return "+cancelAppointment);
							queServer.sendRequestToFE(replyToFE);
							LOGGER.info("Replica Started: Quebec");
						}
						else if(replicaName.substring(0, 3).equalsIgnoreCase("SHE")) {
							boolean cancelAppointment = sheServer.cancelAppointment(patientID,appointmentID);
							if(cancelAppointment == true)
								replyToFE = "Appointment Cancelled Successfully!";
							else
								replyToFE = "Appointment Could'nt be Cancelled";
							System.out.println("Cancel Appointment Return "+cancelAppointment);
							sheServer.sendRequestToFE(replyToFE);
							LOGGER.info("Replica Started: Sherbrooke");
						}
						break;
					}
					case 4:{
						LOGGER.info("Method Request by Patient: Swap Appointment");
						System.out.print("Replica Name: "+ replicaName);
						String oldAppointmentType=split[1];
						String oldAppointmentID= split[2];
						String newAppointmentType=split[3];
						String newAppointmentID=split[4];
						String patientID=split[5];
						String replyToFE=null;
						if(replicaName.substring(0,3).equalsIgnoreCase("MTL")) {
							int swapResult= mtlServer.swapAppointment(patientID, oldAppointmentID, oldAppointmentType, newAppointmentID, newAppointmentType);
							if(swapResult == 1) {
								mtlServer.sendRequestToFE("Appointment Swaped Successfully!");
							}
							else if(swapResult == 2){
								mtlServer.sendRequestToFE("Old Appointment ID "+oldAppointmentID+"is not valid! ");
							}
							else if(swapResult == 3){
								mtlServer.sendRequestToFE("Patient's "+patientID +" Appointment Already Exists " +newAppointmentID);
							}

							else if(swapResult == 0){
								mtlServer.sendRequestToFE("No Appointment " +newAppointmentID +" Available for " +newAppointmentType);
							}

							else if(swapResult == 4  ){
								mtlServer.sendRequestToFE("Patient "+patientID +" has Booked Maximum Number of Appointments i.e 3 for " +newAppointmentID +" week");
							}
							else if(swapResult == -1  ){
								mtlServer.sendRequestToFE("New Appointment ID " +newAppointmentID +" for New Appointment Type " +newAppointmentType +" does not exists");
							}
							LOGGER.info("Replica Started: MOntreal");
						}
						else if(replicaName.substring(0,3).equalsIgnoreCase("QUE")) {
							int swapResult= queServer.swapAppointment(patientID, oldAppointmentID, oldAppointmentType, newAppointmentID, newAppointmentType);
							if(swapResult == 1) {
								queServer.sendRequestToFE("Appointment Swaped Successfully!");
							}
							else if(swapResult == 2){
								queServer.sendRequestToFE("Old Appointment ID "+oldAppointmentID+"is not valid! ");
							}
							else if(swapResult == 3){
								queServer.sendRequestToFE("Patient's "+patientID +" Appointment Already Exists " +newAppointmentID);
							}

							else if(swapResult == 0){
								queServer.sendRequestToFE("No Appointment " +newAppointmentID +" Available for " +newAppointmentType);
							}

							else if(swapResult == 4  ){
								queServer.sendRequestToFE("Patient "+patientID +" has Booked Maximum Number of Appointments i.e 3 for " +newAppointmentID +" week");
							}
							else if(swapResult == -1  ){
								queServer.sendRequestToFE("New Appointment ID " +newAppointmentID +" for New Appointment Type " +newAppointmentType +" does not exists");
							}
							LOGGER.info("Replica STarted: Quebec");
						}
						else if(replicaName.substring(0,3).equalsIgnoreCase("SHE")) {
							int swapResult= sheServer.swapAppointment(patientID, oldAppointmentID, oldAppointmentType, newAppointmentID, newAppointmentType);
							if(swapResult == 1) {
								sheServer.sendRequestToFE("Appointment Swaped Successfully!");
							}
							else if(swapResult == 2){
								sheServer.sendRequestToFE("Old Appointment ID "+oldAppointmentID+"is not valid! ");
							}
							else if(swapResult == 3){
								sheServer.sendRequestToFE("Patient's "+patientID +" Appointment Already Exists " +newAppointmentID);
							}

							else if(swapResult == 0){
								sheServer.sendRequestToFE("No Appointment " +newAppointmentID +" Available for " +newAppointmentType);
							}

							else if(swapResult == 4  ){
								sheServer.sendRequestToFE("Patient "+patientID +" has Booked Maximum Number of Appointments i.e 3 for " +newAppointmentID +" week");
							}
							else if(swapResult == -1  ){
								sheServer.sendRequestToFE("New Appointment ID " +newAppointmentID +" for New Appointment Type " +newAppointmentType +" does not exists");
							}
							LOGGER.info("Replica Started: Sherbrooke");
						}
						break;
					}
					default : {
						System.out.println("Invalid Input");
						LOGGER.info("Invalid Input");
						break;
					}
				}
				break;
			}

			default: {
				System.out.println("Not a Valid User!");
				break;
			}
		}
	}
}