package com.ws.dams;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MTLServer {
	static Map<String, List<HashMapSubKey>> hashMap = new HashMap<String, List<HashMapSubKey>>();
	public static Map<String, List<HashMapSubKey>> udpHashMap = new HashMap<String, List<HashMapSubKey>>();
	static Map<String,List<String>> server = new HashMap<String,List<String>>();
	static Map<String,List<String>> listAvailableAppointments = new HashMap<String,List<String>>();
	static Map<String,List<String>> serverPatientReply = new HashMap<String,List<String>>();
	public int swapAppointmentFlag = 0;
	
	static void createHashMap() {
		hashMap.put("Physician", new LinkedList<HashMapSubKey>());
		hashMap.put("Physician", Arrays.asList(new HashMapSubKey("MTLA310122", "2"),
											   new HashMapSubKey("MTLA300122", "2:MTLP2234:booked"),
				                               new HashMapSubKey("MTLE090222", "8:MTLP1234:booked"),
				                               new HashMapSubKey("MTLM280122", "2"),
				                               new HashMapSubKey("MTLE230122", "2"),
				                               new HashMapSubKey("MTLA110222", "0")));
		hashMap.put("Dental", new LinkedList<HashMapSubKey>());
		hashMap.put("Dental", Arrays.asList(new HashMapSubKey("MTLE300122", "3:MTLP2234:booked"),
											new HashMapSubKey("MTLM050922", "5:MTLP2234:booked"),
											new HashMapSubKey("MTLA061022", "4")));
		hashMap.put("Surgeon", new LinkedList<HashMapSubKey>());
		hashMap.put("Surgeon",Arrays.asList(new HashMapSubKey("MTLE300122", "1:MTLP2234:booked"),
											new HashMapSubKey("MTLM121022", "2")));
		udpHashMap = hashMap;
		displayHashMap();
	}
	
	public static boolean searchAddHashMap(String key, List<HashMapSubKey> subHashMap) {
		Set<Map.Entry<String, List<HashMapSubKey>>> entries = hashMap.entrySet();
		int flag = 2;
		String hmkey;
		List<HashMapSubKey> hsub;
		try {
		for(Map.Entry<String, List<HashMapSubKey>> hm: entries) {
			hsub = hm.getValue();
			hmkey = hm.getKey();
			System.out.println(hmkey +" " +hsub);
			if(hm.getKey().equalsIgnoreCase(key)){
				for(HashMapSubKey hs: hsub) {
					hs.display();
					for(HashMapSubKey nhs: subHashMap) {
						nhs.display();
						if( nhs.getAppointmentID().equals(hs.getAppointmentID())) 
						{	
							flag = 0;	
						    break;
						}
							else 
							{
								System.out.println("Inside Else");
								List<HashMapSubKey> b = new ArrayList<>(hsub);
								b.add(nhs);
								hashMap.put(key, b);
								flag = 1 ;	
							}
					}
					if(flag == 0)
						break;
				}
				
			}
			else if(!hashMap.containsKey(key))
			{
			
				Map<String, List<HashMapSubKey>> hashMapAddNew = new HashMap<String, List<HashMapSubKey>>(hashMap);
				hashMapAddNew.put(key, subHashMap);
				hashMap = hashMapAddNew;
				flag = 1 ;

			}
			if(flag == 0)
				break;
			}
	}
	catch(Exception e) {
	System.out.println("Exception: "+e);	
	}
		
		if (flag == 1)
			return true;
		else
			return false;
	}	
	

	public boolean addAppointment(String appointmentID, String appointmentType, int capacity)  {
		boolean addAppointment = false;
	    List<HashMapSubKey> hashList = Arrays.asList(new HashMapSubKey(appointmentID, Integer.toString(capacity)));	
	    try {
	    	addAppointment = searchAddHashMap(appointmentType, hashList);
			udpHashMap = hashMap;
	    	displayHashMap();
			
	    }
		catch(Exception e) {
			System.out.println("Exception: "+e);
		}
		return addAppointment;		
	}
	
	public boolean removeAppointment(String appointmentID, String appointmentType){
		boolean removeAppointment = true;
		List<HashMapSubKey> hsRemove = new ArrayList<>();
		for(Iterator<Map.Entry<String, List<HashMapSubKey>>> iterator = hashMap.entrySet().iterator(); iterator.hasNext(); )
		{
			Map.Entry<String, List<HashMapSubKey>> entries = iterator.next();
			//System.out.println(entries);
			if (entries.getKey().equalsIgnoreCase(appointmentType))
			{
				List<HashMapSubKey> hsub = entries.getValue();
				hsRemove.clear();
				for(HashMapSubKey hs: hsub)
				{
					if (hs.getAppointmentID().equalsIgnoreCase(appointmentID) && hs.getAdditionInfo().contains("booked"))
					{
						removeAppointment = false;
						hsRemove.add(hs);
						System.out.println(removeAppointment);
					}

					else
					{
						//System.out.println("Inside Else");
						if ( !hs.getAppointmentID().equalsIgnoreCase(appointmentID) )
							hsRemove.add(hs);
					}

				}
				if (hsRemove != null )
				{
					//System.out.println(hsRemove);
					//System.out.println(hsub);
					Map<String, List<HashMapSubKey>> hashMapRemove = new HashMap<String, List<HashMapSubKey>>(hashMap);
					hashMapRemove.replace(appointmentType, hsub, hsRemove);
					hashMap = hashMapRemove;
					System.out.println("HashMap Value" +hashMap);
				}
			}
		}
		displayHashMap();
		return removeAppointment;
	}
	
	public String getAppointmentSchedule(String patientID) {
		ArrayList<String> MTLServerReply = new ArrayList<String>();
		ArrayList<String> QUEServerReply = new ArrayList<String>();
		ArrayList<String> SHEServerReply = new ArrayList<String>();
		byte[] udpBytesRequest =  patientID.getBytes();
			
		//Connection to Montreal UDP Server
		try {
			
			DatagramSocket udpClient = new DatagramSocket();
			InetAddress udpClientHost = InetAddress.getByName("localhost");
			int serverPort = 1111;  //Montreal UDP Connection Agreed Port
			udpClient.connect(udpClientHost, serverPort);
			
			DatagramPacket request = new DatagramPacket(udpBytesRequest, patientID.length(), udpClientHost, serverPort);
			udpClient.send(request);
			
			
			
			byte[] udpBytesReply = new byte[1024];
			DatagramPacket reply = new DatagramPacket(udpBytesReply, udpBytesReply.length);
			udpClient.receive(reply);
			System.out.println("Montreal Server:" +patientID);
			System.out.println(new String(reply.getData()));
			String replyString = new String(reply.getData());
			replyString = cleanUDPReplyString(replyString);
			MTLServerReply.add(replyString);
		}
		catch(Exception e) {
			System.out.println(e);
			MTLServerReply.add("Montreal Server is not Active");
		}
		
		//Connection to Quebec UDP Server
		try {
			
			DatagramSocket udpClient = new DatagramSocket();
			InetAddress udpClientHost = InetAddress.getByName("localhost");
			int serverPort = 2222; //Quebec UDP Connection Agreed Port
			udpClient.connect(udpClientHost, serverPort);
			
			
			
			DatagramPacket request = new DatagramPacket(udpBytesRequest, patientID.length(), udpClientHost, serverPort);
			udpClient.send(request);
			
			
			
			byte[] udpBytesReply = new byte[1024];
			DatagramPacket reply = new DatagramPacket(udpBytesReply, udpBytesReply.length);
			udpClient.receive(reply);
			System.out.println("Quebec Server:" +patientID);
			System.out.println(new String(reply.getData()));
			System.out.println(reply.getLength());
			String replyString = new String(reply.getData());
			replyString = cleanUDPReplyString(replyString);
			QUEServerReply.add(replyString);
		}
		catch(Exception e) {
			System.out.println(e);
			QUEServerReply.add("Quebec Server is not Active");
		}
		
		//Connection to Sherbrooke UDP Server
			try {
					
					DatagramSocket udpClient = new DatagramSocket();
					InetAddress udpClientHost = InetAddress.getByName("localhost");
					int serverPort = 3333; //Sherbrooke UDP Connection Agreed Port
					udpClient.connect(udpClientHost, serverPort);
					
					DatagramPacket request = new DatagramPacket(udpBytesRequest, patientID.length(), udpClientHost, serverPort);
					udpClient.send(request);
					
					
					
					byte[] udpBytesReply = new byte[1024];
					DatagramPacket reply = new DatagramPacket(udpBytesReply, udpBytesReply.length);
					udpClient.receive(reply);
					System.out.println("Sherbrooke Server:" +patientID);
					System.out.println(new String(reply.getData()));
					String replyString = new String(reply.getData());
					replyString = cleanUDPReplyString(replyString);
					SHEServerReply.add(replyString);
				}
				catch(Exception e) {
					System.out.println(e);
					SHEServerReply.add("Sherbrooke Server is not Active");
				}
		serverPatientReply.clear();
		//Map<String, List<String>> server = new HashMap<String, List<String>>();
		serverPatientReply.put("Montreal Server " +patientID, MTLServerReply);
		serverPatientReply.put("Quebec Server " +patientID, QUEServerReply);
		serverPatientReply.put("Sherbrooke Server " +patientID, SHEServerReply);
		
		String serverReply = null;
		serverReply = serverPatientReply.entrySet().toString();
		return serverReply;
	}

	public static void displayHashMap() {
		
		Set<Map.Entry<String, List<HashMapSubKey>>> entries = hashMap.entrySet();
		for(Map.Entry<String, List<HashMapSubKey>> hm: entries) {
			System.out.println(hm.getKey());
			List<HashMapSubKey> hsub = hm.getValue();
			for(HashMapSubKey hs: hsub) {
			hs.display();	
			}	
		}	
	}
	
	public String listAppointmentAvailability(String appointmentType)  {
		
		ArrayList<String> QuebecServerReply = new ArrayList<String>();
	    ArrayList<String> MontrealServerReply = new ArrayList<String>();
	    ArrayList<String> SherbrookeServerReply = new ArrayList<String>();
	    byte[] udpBytesRequest =  appointmentType.getBytes();
		
	    try {
			
			DatagramSocket udpClient = new DatagramSocket();
			InetAddress udpClientHost = InetAddress.getByName("localhost");
			int serverPort = 1111; //Montreal UDP Agreed Port
			
			udpClient.connect(udpClientHost, serverPort);
			System.out.println("Appt length" +appointmentType.length());
			System.out.println("bytes" +udpBytesRequest);
			System.out.println("udp client host" +udpClientHost);
			System.out.println("server port" +serverPort);
			
			DatagramPacket request = null;
			request = new DatagramPacket(udpBytesRequest, appointmentType.length(), udpClientHost, serverPort);
			System.out.println("Before send " +request);
			udpClient.send(request);
			
			System.out.println(request);
			
			
			byte[] udpBytesReply = new byte[1000];
			DatagramPacket reply = new DatagramPacket(udpBytesReply, udpBytesReply.length);
			
			udpClient.receive(reply);
			
			
			
			System.out.println("Montreal Server:" +appointmentType);
			System.out.println(new String(reply.getData()));
			System.out.println(reply.getLength());
			String replyString = new String(reply.getData());
			replyString = cleanUDPReplyString(replyString);
			//replyString.substring(0, reply.getLength());
			MontrealServerReply.add(replyString);
			//MontrealServerReply.add(replyString);
		}
		catch(Exception e) {
			System.out.println(e);
			MontrealServerReply.add("Montreal Server is not Active");
		}
		
		//UDP Connection to Quebec UDP Server
		try {
			
			DatagramSocket udpClient = new DatagramSocket();
			InetAddress udpClientHost = InetAddress.getByName("localhost");
			int serverPort = 2222; //Quebec UDP Agreed Port
				
			
			udpClient.connect(udpClientHost, serverPort);
			
			DatagramPacket request1 = new DatagramPacket(udpBytesRequest, appointmentType.length(), udpClientHost, serverPort);
			udpClient.send(request1);
			
			
			byte[] udpBytesReply = new byte[1000];
			DatagramPacket QueReply = new DatagramPacket(udpBytesReply, udpBytesReply.length);
			udpClient.receive(QueReply);
			
			
			System.out.println("Quebec Server:" +appointmentType);
			System.out.println(new String(QueReply.getData()));
			String replyString = new String(QueReply.getData());
			replyString = cleanUDPReplyString(replyString);
			QuebecServerReply.add(replyString);
		}
		catch(Exception e) {
			System.out.println(e);
			QuebecServerReply.add("Quebec Server is not Active");
		}
		
		//UDP Connection to Sherbrooke UDP Server
				try {
					
					DatagramSocket udpClient = new DatagramSocket();
					InetAddress udpClientHost = InetAddress.getByName("localhost");
					int serverPort = 3333; //Sherbrooke UDP Agreed Port
					
					udpClient.connect(udpClientHost, serverPort);
					
					DatagramPacket request = new DatagramPacket(udpBytesRequest, appointmentType.length(), udpClientHost, serverPort);
					udpClient.send(request);
					
					
					
					byte[] udpBytesReply = new byte[1024];
					DatagramPacket SheReply = new DatagramPacket(udpBytesReply, udpBytesReply.length);
					
					udpClient.receive(SheReply);
					System.out.println("Sherbrooke Server:" +appointmentType);
					System.out.println(new String(SheReply.getData()));
					String replyString = new String(SheReply.getData());
					replyString = cleanUDPReplyString(replyString);
					SherbrookeServerReply.add(replyString);
				}
				catch(Exception e) {
					System.out.println(e);
					SherbrookeServerReply.add("Sherbrooke Server is not Active");
				}
				
		
		//Map<String,List<String>> server = new HashMap<String,List<String>>();
		server.put("Montreal Server",MontrealServerReply);
		server.put("Quebec Server",QuebecServerReply);		
		server.put("Sherbrooke Server",SherbrookeServerReply);
		System.out.println("Server Map " +server );
		
		//To use it in Swap Method
		listAvailableAppointments = server;
		String serverReply = null;
		serverReply = server.entrySet().toString();
		System.out.println("Server Repy at Server in String "+serverReply);
		return serverReply;
	}
	
	
	public int bookAppointment(String patientID, String appointmentID, String appointmentType) {
		List<HashMapSubKey> hsUpdate = new ArrayList<>();
		int capacity = -1;
		String addPatientID = null;
		String[] addInfoSplit = null;
		char[] digits = null;
		StringBuilder digit = new StringBuilder();
		String addInfo = null;
		int flag = 0;
		Map<String,List<String>> serverReplies = new HashMap<String,List<String>>();
		char[] appointmentDate = null;
		StringBuilder date = new StringBuilder();
		String[] appoint = null;
		int week;
		int apptIDWeek = 0;
		List<Integer> prevWeek = new ArrayList<>();
		String format = "ddMMyy";
		Date dateInput = null;
		Date apptIDdateInput = null;
		Calendar cal = new GregorianCalendar();
		int count = 0;
		SimpleDateFormat df = new SimpleDateFormat(format);
		StringBuilder apptIDDate = new StringBuilder();
		char[] dateApptID;
		String serverReply = null;
		
		
		serverReply = getAppointmentSchedule(patientID);
		
		System.out.println("Schedule Global" +serverPatientReply);
		
		serverReplies = serverPatientReply;
		System.out.println("Book Appoitment HashMap "+serverReplies);
		
		dateApptID = appointmentID.toCharArray();
		apptIDDate = new StringBuilder();
		for(char c: dateApptID) {
			if(Character.isDigit(c)) {
				apptIDDate.append(c);
			}
			}
		try {
		apptIDdateInput = df.parse(apptIDDate.toString());
		cal.setTime(apptIDdateInput);
		apptIDWeek = cal.get(Calendar.WEEK_OF_YEAR);
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
		System.out.println(apptIDDate);
		System.out.println(apptIDWeek);
		
		for(Iterator<Map.Entry<String, List<String>>> iterate = serverReplies.entrySet().iterator(); iterate.hasNext(); )
		{
			Map.Entry<String, List<String>> entries = iterate.next();
			List<String> appointmentSchedule = entries.getValue();
			for(String appointment: appointmentSchedule) {
				System.out.println("Appt" +appointment);
				appoint = appointment.split(",");
				System.out.println(appoint.toString());
				for(String apptSplit: appoint) {
					appointmentDate = apptSplit.toCharArray();
					//System.out.println("Appointment Date "+appointmentDate);
					date = new StringBuilder();
					for(char c: appointmentDate) {
						if(Character.isDigit(c)) {
							date.append(c);
						}
						}
					System.out.println(date);
					System.out.println(apptIDDate);
					if(date.length()!=0 && apptIDDate.length()!=0)
					{
						System.out.println("Inside If");
						String dateS = date.toString();
						String apptIDDateS = apptIDDate.toString();
						try {
						System.out.println(date);
						if(date.length() == 6) 
						{
						dateInput = df.parse(date.toString());
						System.out.println(dateInput);
						System.out.println("SB" +date);
						cal.setTime(dateInput);
						week = cal.get(Calendar.WEEK_OF_YEAR);
						prevWeek.add(week);
						System.out.println("Week is "+cal.get(Calendar.WEEK_OF_YEAR));
						}
					} catch (ParseException e) {
						e.printStackTrace();
					}
					}
				}
			}
		}
		
		System.out.println("Prev Week " +prevWeek);
		System.out.println("Appointment ID Week:" +apptIDWeek);
		System.out.println(prevWeek.size());
		if(prevWeek.size() > 1)
		{
			
	   /*int k = 0;
		while (k < prevWeek.size()) {
			System.out.println("k "+k);
			int i = k;
			int j = i + 1; 
			while( j < prevWeek.size())
			{
				System.out.println("i" +i);
				System.out.println("j" +j);
				System.out.println("Previous i " +prevWeek.get(i));
				System.out.println("Previous j " +prevWeek.get(j));
				if( (prevWeek.get(i) == prevWeek.get(j)) && (apptIDWeek == prevWeek.get(i) || apptIDWeek == prevWeek.get(j)))
					count ++;
				//i ++;
				j++;
			}
			k ++;
			if(count > 3)
				break;
		}*/
			
			int k = 0;
			while (k < prevWeek.size()) {
				System.out.println(prevWeek.get(k));
				if(apptIDWeek == prevWeek.get(k))
					count ++;
				
				k ++;
				if(count >=3)
					break;
			}
			System.out.println("Count" +count);
		}
		
		System.out.println("Swap Appointment Flag in Book Appointment: "+swapAppointmentFlag);
		if(count < 3 || swapAppointmentFlag != 0)
	{
		for(Iterator<Map.Entry<String, List<HashMapSubKey>>> iterator = hashMap.entrySet().iterator(); iterator.hasNext(); )
		{
			Map.Entry<String, List<HashMapSubKey>> entries = iterator.next();
			//System.out.println(entries);
			if (entries.getKey().equalsIgnoreCase(appointmentType))
			{
				List<HashMapSubKey> hsub = entries.getValue();
				for(HashMapSubKey hs : hsub) {
					System.out.println("Inside For");
					hs.display();
					System.out.println("Appointment ID " +appointmentID);
					if(flag != 1)
					{
					if(hs.getAppointmentID().equalsIgnoreCase(appointmentID)) 
					{
						flag = 1;
						System.out.println("Inside If");
						hs.display();
						addInfoSplit = hs.getAdditionInfo().split(":");
						System.out.println("Add Info Length" +addInfoSplit.length);
						if(addInfoSplit.length > 1)
						{		
						for(String split: addInfoSplit)
						{
							System.out.println(split);
							System.out.println("Digit" +digit);
							System.out.println("Digit Len" +digit.length());
							if(digit.length() == 0) 
							{
							digits = split.toCharArray();
							for(char c: digits) 
							{
								if(Character.isDigit(c))
								{
									digit.append(c);
								}
							}
						  }
							else 
							{   System.out.println(split);
								if(!split.equalsIgnoreCase(patientID))
								{
									System.out.println(addInfo);
									if(addInfo == null)
										addInfo = "";
									
									addInfo = addInfo + split + ":";
								}
								
								else
								{
									System.out.println("Patient already exists");
									flag = 3;
									break;
								}
								}
									
						}
						System.out.println("Digit "+digit);
						capacity = Integer.parseInt(digit.toString());
						System.out.println("Capacity "+capacity);
						}
						else 
						{
						capacity = Integer.parseInt(hs.getAdditionInfo()) ;
						}
						if(capacity == 0 && flag!=3)
						{
							flag = 0;
							break;
						}
						System.out.println("Flag "+flag);
						if(flag == 1 && capacity !=0 )
							capacity = capacity - 1;
						if(addInfo != null && flag == 1) 
						{
							addPatientID = Integer.toString(capacity) + ":" + patientID + ":" +"booked" +":" +addInfo;
							hs.setAdditionalInfo(addPatientID);
							hsUpdate.add(hs);
						}	
						else if(flag == 1) 
						{
							addPatientID = Integer.toString(capacity) + ":" + patientID + ":" +"booked";
							hs.setAdditionalInfo(addPatientID);
							hsUpdate.add(hs); 
						}
					}
					else {
						System.out.println(flag);
						if (flag != 1 || flag != 3) 
						{
							flag = 2;
						}
						
						hsUpdate.add(hs);
					}
				}
					else if (flag == 1 && swapAppointmentFlag != 1)
						hsUpdate.add(hs);
					if( hsUpdate != null && flag == 1 && swapAppointmentFlag != 1) {
						hs.display();
						Map<String, List<HashMapSubKey>> hashMapUpdate = new HashMap<String, List<HashMapSubKey>>(hashMap);
						hashMapUpdate.replace(appointmentType, hsub, hsUpdate);
						hashMap = hashMapUpdate;
						displayHashMap();
					}
					if(flag == 3 )
						break;
				}
			}
			if(flag == 3 || flag == 1 || capacity == 0)
				break;
		}
	}
		else
			flag = 4;
		
		System.out.println(flag);
		return flag;
	}
	// cancel appointment
	public boolean cancelAppointment(String patientID, String appointmentID)  {
		String[] addInfoSplit;
		String appointmentType = null;
		int flag = 0;
		int patientNameFlag = 0;
		char[] digits;
		int capacity = 0;
		StringBuilder digit = new StringBuilder();
		String addInfo = null;
		String cancelAppointment = null;
		List<HashMapSubKey> hsCancelAppointment = new ArrayList<>();
		
		for(Iterator<Map.Entry<String, List<HashMapSubKey>>> iterator = hashMap.entrySet().iterator(); iterator.hasNext(); )
		{
			addInfo = "";
			patientNameFlag = 0;
			hsCancelAppointment.clear();
			Map.Entry<String, List<HashMapSubKey>> entries = iterator.next();
			//System.out.println(entries);
			List<HashMapSubKey> hsub = entries.getValue();
			
			for(HashMapSubKey hs : hsub) {
				//hs.display();
				if(hs.getAppointmentID().equalsIgnoreCase(appointmentID)) {
						addInfoSplit = hs.getAdditionInfo().split(":");
						for(String split: addInfoSplit) {
							if(digit.length() == 0) 
							{
								digits = split.toCharArray();
								for(char c: digits) 
								{
									if(Character.isDigit(c))
									{
										digit.append(c);
									}
								}
							}
							//System.out.println(digit);
							System.out.println(split);
							System.out.println("Add Info"+addInfo);
							if(addInfo.equals(":")) {
						      addInfo = "";}
							System.out.println("Add Info"+addInfo);
							if(split.equalsIgnoreCase(patientID)) 
							{
								split = "";
								capacity = Integer.parseInt(digit.toString());
								flag = 1;
								patientNameFlag = 1;
								//System.out.println("Capacity" +capacity);
								//System.out.println("Flag" +flag);
							}
							
							if( patientNameFlag == 1 && split.equalsIgnoreCase("booked"))
								split = "";
							
							
							if (patientNameFlag == 1 && !split.equalsIgnoreCase(patientID))
								addInfo = addInfo + ":" +split ;
							
						}
						
						//System.out.println(addInfo);
						if(patientNameFlag == 1)
						{	
						capacity = capacity + 1;
						System.out.println(addInfo);
						if(addInfo.equals(":")) {
						    addInfo = "";}
						System.out.println(addInfo);
						if(addInfo != null && addInfo !="" )
							cancelAppointment = Integer.toString(capacity) +addInfo +"booked";
						else
							cancelAppointment = Integer.toString(capacity);
						
						//System.out.println(cancelAppointment);

						hs.setAdditionalInfo(cancelAppointment);
						}
						//hs.display();
						hsCancelAppointment.add(hs);
					    //System.out.println("hsCancelAppointment" +hsCancelAppointment);
					    //System.out.println(hsub);
						if(hsCancelAppointment != null) 
						{
							//hs.display();
							Map<String, List<HashMapSubKey>> hashMapUpdate = new HashMap<String, List<HashMapSubKey>>(hashMap);
							hashMapUpdate.replace(appointmentType, hsub, hsCancelAppointment);
							hashMap = hashMapUpdate;
							displayHashMap();
						}
				}
			}
			}
		
		if(flag == 1)
			return true;
		else
			return false;


	}

	public int swapAppointment(String patientID, String oldAppointmentID, String oldAppointmentType,
							   String newAppointmentID, String newAppointmentType) 
	{
					int bookOldAppointmentCheck;
					int flagReturn = -1;
					boolean cancelOldAppointment = false;
					//Map<String, List<HashMapSubKey>> udpHashMap = new HashMap<String, List<HashMapSubKey>>();
					//Map<String, List<HashMapSubKey>> altHashMap = new HashMap<String, List<HashMapSubKey>>();
					
					
					Map<String,List<String>> serverReplies = new HashMap<String,List<String>>();
					char[] appointmentDate = null;
					StringBuilder date = new StringBuilder();
					String[] appoint = null;
					int week;
					int apptIDWeek = 0;
					List<Integer> prevWeek = new ArrayList<>();
					String format = "ddMMyy";
					Date dateInput = null;
					Date apptIDdateInput = null;
					Calendar cal = new GregorianCalendar();
					int count = 0;
					SimpleDateFormat df = new SimpleDateFormat(format);
					StringBuilder apptIDDate = new StringBuilder();
					char[] dateApptID;
					String serverReply = null;
					int swapFlag = -1;
					String[] aptSplit = null;
					
					System.out.println("Inside Swap Appointment First");
					swapAppointmentFlag = 1;
					bookOldAppointmentCheck = bookAppointment(patientID, oldAppointmentID, oldAppointmentType);
					System.out.println("Returned from book Appointment" +bookOldAppointmentCheck);
					//Means if the patient has already booked the old appointment
					if(bookOldAppointmentCheck == 3) {
					System.out.println("Patient has Already Booked the Old Appointment");
					
					//Check if the New Appointment patient exceeds the max book in a week constraint
					serverReply = getAppointmentSchedule(patientID);
					
					//System.out.println("Schedule Global" +serverPatientReply);
					
					serverReplies = serverPatientReply;
					//System.out.println("Book Appointment HashMap "+serverReplies);
					
					dateApptID = newAppointmentID.toCharArray();
					apptIDDate = new StringBuilder();
					for(char c: dateApptID) {
					if(Character.isDigit(c)) {
						apptIDDate.append(c);
					}
					}
					try {
					apptIDdateInput = df.parse(apptIDDate.toString());
					cal.setTime(apptIDdateInput);
					apptIDWeek = cal.get(Calendar.WEEK_OF_YEAR);
					}
					catch(Exception e)
					{
					System.out.println(e);
					}
					System.out.println(apptIDDate);
					System.out.println(apptIDWeek);
					
					for(Iterator<Map.Entry<String, List<String>>> iterate = serverReplies.entrySet().iterator(); iterate.hasNext(); )
					{
					Map.Entry<String, List<String>> entries = iterate.next();
					List<String> appointmentSchedule = entries.getValue();
					for(String appointment: appointmentSchedule) {
						System.out.println("Appt" +appointment);
						appoint = appointment.split(",");
						System.out.println(appoint.toString());
						for(String apptSplit: appoint) {
							appointmentDate = apptSplit.toCharArray();
							//System.out.println("Appointment Date "+appointmentDate);
							date = new StringBuilder();
							for(char c: appointmentDate) {
								if(Character.isDigit(c)) {
									date.append(c);
								}
								}
							System.out.println(date);
							System.out.println(apptIDDate);
							if(date.length()!=0 && apptIDDate.length()!=0)
							{
								System.out.println("Inside If");
								String dateS = date.toString();
								String apptIDDateS = apptIDDate.toString();
								try {
								System.out.println(date);
								if(date.length() == 6) 
								{
								dateInput = df.parse(date.toString());
								System.out.println(dateInput);
								System.out.println("SB" +date);
								cal.setTime(dateInput);
								week = cal.get(Calendar.WEEK_OF_YEAR);
								prevWeek.add(week);
								System.out.println("Week is "+cal.get(Calendar.WEEK_OF_YEAR));
								}
							} catch (ParseException e) {
								e.printStackTrace();
							}
							}
						}
					}
					}
					
					System.out.println("Prev Week " +prevWeek);
					System.out.println("Appointment ID Week:" +apptIDWeek);
					System.out.println(prevWeek.size());
					if(prevWeek.size() > 1)
					{	
					int k = 0;
					while (k < prevWeek.size()) {
						System.out.println(prevWeek.get(k));
						if(apptIDWeek == prevWeek.get(k))
							count ++;
						
						k ++;
						if(count >=3)
							break;
					}
					System.out.println("Count" +count);
					}
					
					
					
					if(count < 3) {
					
					String availableNewAppointmentType = listAppointmentAvailability(newAppointmentType);
					for(Iterator<Map.Entry<String, List<String>>> iterate = listAvailableAppointments.entrySet().iterator(); iterate.hasNext(); )
					{
					//System.out.println("Inside for for listingAvailableAppointments");
					Map.Entry<String, List<String>> entries = iterate.next();
					//System.out.println("Entry " +entries);
						List<String> appts = entries.getValue();
						//System.out.println("List "+appts);
						
						for(String apt : appts) {
							//System.out.println("Appointments " +apt);
							if(apt.contains(","))
							{
								aptSplit = apt.split(",");
							}
							else {
								aptSplit[1] = apt;
							}
							//System.out.println("Appointment Split " +aptSplit);
							for(String val : aptSplit) {
								//System.out.println("Value "+val);
								String[] valSplit = val.split("->");
								for(String aptID: valSplit) {
									//System.out.println("Appointment ID" +aptID);
									aptID = aptID.replaceAll("\\[", " ").replaceAll("\\]", " ").trim();
									//System.out.println("Appointment ID After Replace: " +aptID);
									//System.out.println(newAppointmentID);
									if(aptID.equalsIgnoreCase(newAppointmentID)) {
										System.out.println("Found the Appointment ID");
										valSplit[1] = valSplit[1].replaceAll("\\[", " ").replaceAll("\\]", " ").trim();
										//System.out.println("Checking the Capacity " +valSplit[1]);
										if(valSplit[1] != "0") {
											System.out.println("Inside Capacity > 0");
											swapAppointmentFlag = 0;
											if(newAppointmentID.substring(0, 3).equalsIgnoreCase(oldAppointmentID.substring(0, 3)))
											{
												System.out.println("Same Server Swap");
												bookOldAppointmentCheck = bookAppointment(patientID, newAppointmentID, newAppointmentType);
												System.out.println("Book Appointment Return Value: " +bookOldAppointmentCheck);
											}
											else
											{
												System.out.println("Inside Else Another Server");
												if(newAppointmentID.substring(0, 3).equalsIgnoreCase("QUE")) {
												try {
													String updateNewAppointment = newAppointmentType+"," +newAppointmentID+"," +patientID;
													byte[] udpBytesRequest =  updateNewAppointment.getBytes();
													DatagramSocket udpClient = new DatagramSocket();
													InetAddress udpClientHost = InetAddress.getByName("localhost");
													int serverPort = 2222; //Quebec UDP Connection Agreed Port
													udpClient.connect(udpClientHost, serverPort);
													
													DatagramPacket request = new DatagramPacket(udpBytesRequest, updateNewAppointment.length(), udpClientHost, serverPort);
													udpClient.send(request);
													
													byte[] udpBytesReply = new byte[10];
													DatagramPacket reply = new DatagramPacket(udpBytesReply, udpBytesReply.length);
													udpClient.receive(reply);
													System.out.println("Replied UDP Book Appointment " +new String(reply.getData()));
													String returnFlag = new String( reply.getData(),
																					reply.getOffset(),
																					reply.getLength(),
																		            StandardCharsets.UTF_8);
													System.out.println("Return Flag" +returnFlag);
													if(returnFlag.equalsIgnoreCase("1")) {
														System.out.println("Appointment Booked Successfully");
														swapFlag = 1;
													}
													else if(returnFlag.equalsIgnoreCase("3")) {
														swapFlag = 3;
														System.out.println("Patient's "+patientID +" Appointment Already Exists " +newAppointmentID);
													}
													else if(returnFlag.equalsIgnoreCase("0")) {
														swapFlag = 0;
														System.out.println("No Appointment " +newAppointmentID +" Available for " +newAppointmentType);
													}
													
					
												}
												catch(Exception e) {
													System.out.println(e);
												}
											}
												
												else if(newAppointmentID.substring(0, 3).equalsIgnoreCase("MTL")) {
												try {
													String updateNewAppointment = newAppointmentType+"," +newAppointmentID+"," +patientID;
													byte[] udpBytesRequest =  updateNewAppointment.getBytes();
													DatagramSocket udpClient = new DatagramSocket();
													InetAddress udpClientHost = InetAddress.getByName("localhost");
													int serverPort = 1111; //Montreal UDP Connection Agreed Port
													udpClient.connect(udpClientHost, serverPort);
													
													DatagramPacket request = new DatagramPacket(udpBytesRequest, updateNewAppointment.length(), udpClientHost, serverPort);
													udpClient.send(request);
													
													byte[] udpBytesReply = new byte[10];
													DatagramPacket reply = new DatagramPacket(udpBytesReply, udpBytesReply.length);
													udpClient.receive(reply);
													System.out.println("Replied UDP Book Appointment " +new String(reply.getData()));
													String returnFlag = new String( reply.getData(),
																					reply.getOffset(),
																					reply.getLength(),
																		            StandardCharsets.UTF_8);
													System.out.println("Return Flag" +returnFlag);
													if(returnFlag.equalsIgnoreCase("1")) {
														System.out.println("Appointment Booked Successfully");
														swapFlag = 1;
													}
													else if(returnFlag.equalsIgnoreCase("3")) {
														swapFlag = 3;
														System.out.println("Patient's "+patientID +" Appointment Already Exists " +newAppointmentID);
													}
													else if(returnFlag.equalsIgnoreCase("0")) {
														swapFlag = 0;
														System.out.println("No Appointment " +newAppointmentID +" Available for " +newAppointmentType);
													}
													
					
												}
												catch(Exception e) {
													System.out.println(e);
												}
											}
												
												else if(newAppointmentID.substring(0, 3).equalsIgnoreCase("SHE")) {
												try {
													String updateNewAppointment = newAppointmentType+"," +newAppointmentID+"," +patientID;
													byte[] udpBytesRequest =  updateNewAppointment.getBytes();
													DatagramSocket udpClient = new DatagramSocket();
													InetAddress udpClientHost = InetAddress.getByName("localhost");
													int serverPort = 3333; //Montreal UDP Connection Agreed Port
													udpClient.connect(udpClientHost, serverPort);
													
													DatagramPacket request = new DatagramPacket(udpBytesRequest, updateNewAppointment.length(), udpClientHost, serverPort);
													udpClient.send(request);
													
													byte[] udpBytesReply = new byte[10];
													DatagramPacket reply = new DatagramPacket(udpBytesReply, udpBytesReply.length);
													udpClient.receive(reply);
													System.out.println("Replied UDP Book Appointment " +new String(reply.getData()));
													String returnFlag = new String( reply.getData(),
																					reply.getOffset(),
																					reply.getLength(),
																		            StandardCharsets.UTF_8);
													System.out.println("Return Flag" +returnFlag);
													if(returnFlag.equalsIgnoreCase("1")) {
														System.out.println("Appointment Booked Successfully");
														swapFlag = 1;
													}
													else if(returnFlag.equalsIgnoreCase("3")) {
														swapFlag = 3;
														System.out.println("Patient's "+patientID +" Appointment Already Exists " +newAppointmentID);
													}
													else if(returnFlag.equalsIgnoreCase("0")) {
														swapFlag = 0;
														System.out.println("No Appointment " +newAppointmentID +" Available for " +newAppointmentType);
													}
													
					
												}
												catch(Exception e) {
													System.out.println(e);
												}
											}
												if(swapFlag == 1)
													break;
										   }
											if(swapFlag == 1)
												break;
										}
										if(swapFlag == 1)
											break;
									}
									if(swapFlag == 1)
										break;
								}
								if(swapFlag == 1)
									break;
							}
							if(swapFlag == 1)
								break;
						}
						if(swapFlag == 1)
							break;
					}
					if(swapFlag == -1) {
					System.out.println("New Appointment ID " +newAppointmentID +" for New Appointment Type " +newAppointmentType +" does not exists");
					}
					
					if(swapFlag == 1) {
					//Cancel Patient's Old Appointment
						cancelOldAppointment = cancelAppointment(patientID, oldAppointmentID);
						displayHashMap();
					}
					}
					else
					{
					swapFlag = 4;
					System.out.println("Cannot Swap Appointment cause ");
					System.out.print("Patient "+patientID +" has Booked Maximum Number of Appointments i.e 3 for " +newAppointmentID +" week");
					}
					}
					if(bookOldAppointmentCheck == 2) {
					System.out.println("Old Appointment ID " +oldAppointmentID +" is not Valid!");
					swapFlag = 2;
					}
					
					
					if( cancelOldAppointment == true && swapFlag == 1)
					flagReturn = 1;
					else if(cancelOldAppointment == false && swapFlag == 2)
					flagReturn = 2;
					else if(cancelOldAppointment == false && swapFlag == 3)
					flagReturn = 3;
					else if(cancelOldAppointment == false && swapFlag == 0)
					flagReturn = 0;
					else if(cancelOldAppointment == false && swapFlag == 4)
					flagReturn = 4;
					else if(cancelOldAppointment == false && swapFlag == -1)
					flagReturn = -1;
					return flagReturn;
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
	
	public void sendRequestToFE(String requestString) {
	
		String sendRequest = requestString +":" +"MTL1";
		
		try {
			DatagramSocket udpClient = new DatagramSocket();
			InetAddress udpClientHost = InetAddress.getByName("localhost");
			int serverPort = 6001; //FE UDP Agreed Port
			
			
			udpClient.connect(udpClientHost, serverPort);
			
			byte[] udpBytesRequest =  sendRequest.getBytes();
			 
			DatagramPacket request = null;
			request = new DatagramPacket(udpBytesRequest, sendRequest.length(), udpClientHost, serverPort);
			System.out.println("Before send " +request);
			udpClient.send(request);
			
			//System.out.println(request);
			
			
			byte[] udpBytesReply = new byte[1000];
			DatagramPacket reply = new DatagramPacket(udpBytesReply, udpBytesReply.length);
			
			udpClient.receive(reply);
			String replyStringRM1 = null;
			//System.out.println(new String(reply.getData()));
			//System.out.println(reply.getLength());
			replyStringRM1 = new String(reply.getData());
			replyStringRM1  = cleanUDPReplyString(replyStringRM1);
			System.out.println("Reply from RM1 Multicast "+replyStringRM1);
		}
		catch(Exception e) {
			System.out.println("Multicast Exception "+e);
		}

	}
	
	public static void udp_list_get_swap(MTLServer mtlServer) {
		
		try {
		DatagramSocket udpSocket = new DatagramSocket(1111);
		byte[] buffer = new byte[1000];
		int listAvailableAppointment = 0;
		System.out.println("Montreal UDP Server is Running");
		byte[] udpByteReply = new byte[100000];
		String apptType = null;
		String patientID = null;
		String[] getPatientID = null;
		List<String> patientAppointmentSchedule = new ArrayList<>();
		String[] apptCount = null;
		StringBuilder digit = new StringBuilder();
		char[] digits;
		List<String> str = new ArrayList<>();
		int bookAppointmentFlag = -1;
			
		while(true) {
			System.out.println("Inside While");
			DatagramPacket request = null;
			request = new DatagramPacket(buffer, buffer.length);
			udpSocket.receive(request);
			System.out.println(request.getLength());
			System.out.println(new String (request.getData()));
			listAvailableAppointment = 0;
			//System.out.println(udpHashMap);
			udpHashMap = mtlServer.getHashMapObj();
			String value;
			DatagramPacket reply = null;
			
			String requestHashMapObject = new String(request.getData(),
					 request.getOffset(),
					 request.getLength(),
					 StandardCharsets.UTF_8);
			String[] values = requestHashMapObject.split(",");
			String appointmentID = null;
			String appointmentType = null;
			String patient = null;
			try {
				appointmentType = values[0];
				appointmentID = values[1];
				patient = values[2];
				bookAppointmentFlag = 1;
			}
			catch(Exception e) {
				bookAppointmentFlag = 0;
			}
			System.out.println(appointmentID +appointmentType +patient);
			if(bookAppointmentFlag == 1)
			{
				int returnFlag = 0;
				System.out.println("Inside the quebec book appointment logic");
				returnFlag = bookAppointmentMontreal(appointmentID, appointmentType, patient, mtlServer);
				udpByteReply = Integer.toString(returnFlag).getBytes();

				reply = new DatagramPacket(udpByteReply, udpByteReply.length, request.getAddress(), request.getPort());
				udpSocket.send(reply);
			}

			for(Iterator<Map.Entry<String, List<HashMapSubKey>>> iterator = udpHashMap.entrySet().iterator(); iterator.hasNext(); )
			{
				Map.Entry<String, List<HashMapSubKey>> entries = iterator.next();
				apptType = new String (request.getData(),
						               request.getOffset(),
						               request.getLength(),
						               StandardCharsets.UTF_8);
				System.out.println(apptType);
				System.out.println(entries.getKey());
				if( entries.getKey().equalsIgnoreCase(apptType) )
				{
					listAvailableAppointment = 1;
					List<HashMapSubKey> hsub = entries.getValue();
					
					
					for(HashMapSubKey hs : hsub) {
						
						apptCount = hs.getAdditionInfo().split(":");
						for(String output: apptCount) {
							digit.setLength(0);
							digits = output.toCharArray();
							for(char c: digits) 
							{
								if(Character.isDigit(c))
								{  
									digit.append(c);
								}
							}
							if(digit.length()!=0)
								break;
						}
						
						if( Integer.parseInt(digit.toString()) != 0 )
						{	
							value = hs.getAppointmentID() +" -> " + digit ;
							str.add(value);
						}
					}
					udpByteReply = str.toString().getBytes();
			}
			}
			
			//System.out.println(listAvailableAppointment);
			if(listAvailableAppointment == 1)
			{
				reply = null;
				str.clear();
				reply = new DatagramPacket(udpByteReply, udpByteReply.length, request.getAddress(), request.getPort());
				udpSocket.send(reply);
			}
			else
			{
				reply = null;
				//System.out.println(reply);
				patientAppointmentSchedule.clear();
				//System.out.println(patientAppointmentSchedule);
				//System.out.println("Inside else");
				for(Iterator<Map.Entry<String, List<HashMapSubKey>>> iterator = udpHashMap.entrySet().iterator(); iterator.hasNext(); )
				{
					Map.Entry<String, List<HashMapSubKey>> entries = iterator.next();
					patientID = new String (request.getData(),
							               request.getOffset(),
							               request.getLength(),
							               StandardCharsets.UTF_8);
					System.out.println(patientID);
					List<HashMapSubKey> hsub = entries.getValue();
					for(HashMapSubKey hs : hsub) 
					{
						getPatientID = hs.getAdditionInfo().split(":");
						for(String parts: getPatientID) 
						{
							System.out.println(parts);
							if(parts.equalsIgnoreCase(patientID))
							{
								patientAppointmentSchedule.add(hs.getAppointmentID());
								//System.out.println(patientAppointmentSchedule);
							}
						}
						
					}
				}
					if(patientAppointmentSchedule.isEmpty())
						patientAppointmentSchedule.add("No Patient " +patientID +" Exists");
					
					udpByteReply = patientAppointmentSchedule.toString().getBytes();
				
				//System.out.println(patientAppointmentSchedule);
				System.out.println("Reply packet data"+udpByteReply+" "+ udpByteReply.length + request.getAddress() + request.getPort());
				reply = new DatagramPacket(udpByteReply, udpByteReply.length, request.getAddress(), request.getPort());
				udpSocket.send(reply);
			}
		}
		
	}
		catch(Exception e) {
			System.out.println(e);
		}
	}
	
	static int bookAppointmentMontreal(String appointmentID, String appointmentType, String patientID, MTLServer mtlServer){
		List<HashMapSubKey> hsUpdate = new ArrayList<>();
		int capacity = -1;
		String addPatientID = null;
		String[] addInfoSplit = null;
		char[] digits = null;
		StringBuilder digit = new StringBuilder();
		String addInfo = null;
		int flag = 0;
		System.out.println("Inside Book Appointment");
		
		
		for(Iterator<Map.Entry<String, List<HashMapSubKey>>> iterator = hashMap.entrySet().iterator(); iterator.hasNext(); ) {
			Map.Entry<String, List<HashMapSubKey>> entries = iterator.next();
			System.out.println(entries.getKey());
			System.out.println(appointmentType);
			if (entries.getKey().equalsIgnoreCase(appointmentType))
			{
				System.out.println("Inside 1st If");
				List<HashMapSubKey> hsub = entries.getValue();
				for(HashMapSubKey hs : hsub) {
					if(hs.getAppointmentID().equalsIgnoreCase(appointmentID)) {
						flag = 1;
						System.out.println("Inside 2nd If");
						hs.display();
						addInfoSplit = hs.getAdditionInfo().split(":");
						System.out.println("Add Info Length" +addInfoSplit.length);
						if(addInfoSplit.length > 1)
						{		
						for(String split: addInfoSplit)
						{
							System.out.println(split);
							System.out.println("Digit" +digit);
							System.out.println("Digit Len" +digit.length());
							if(digit.length() == 0) 
							{
							digits = split.toCharArray();
							for(char c: digits) 
							{
								if(Character.isDigit(c))
								{
									digit.append(c);
								}
							}
						  }
							else 
							{   System.out.println(split);
								if(!split.equalsIgnoreCase(patientID))
								{
									System.out.println(addInfo);
									if(addInfo == null)
										addInfo = "";
									
									addInfo = addInfo + split + ":";
								}
								
								else
								{
									System.out.println("Patient already exists");
									flag = 3;
									break;
								}
								}
									
						}
						System.out.println("Digit "+digit);
						capacity = Integer.parseInt(digit.toString());
						System.out.println("Capacity "+capacity);
						}
						else 
						{
						capacity = Integer.parseInt(hs.getAdditionInfo()) ;
						}
						if(capacity == 0)
						{
							flag = 0;
							break;
						}
						System.out.println("Flag "+flag);
						if(flag == 1 && capacity !=0 )
							capacity = capacity - 1;
						if(addInfo != null && flag == 1) 
						{
							addPatientID = Integer.toString(capacity) + ":" + patientID + ":" +"booked" +":" +addInfo;
							hs.setAdditionalInfo(addPatientID);
							hsUpdate.add(hs);
						}	
						else if(flag == 1) 
						{
							addPatientID = Integer.toString(capacity) + ":" + patientID + ":" +"booked";
							hs.setAdditionalInfo(addPatientID);
							hsUpdate.add(hs); 
						}
					}
					
					//System.out.println("Flag value "+flag);
					else if (flag == 1)
							hsUpdate.add(hs);
						if( hsUpdate != null && flag == 1) {
						hs.display();
						Map<String, List<HashMapSubKey>> hashMapUpdate = new HashMap<String, List<HashMapSubKey>>(hashMap);
						hashMapUpdate.replace(appointmentType, hsub, hsUpdate);
						hashMap = hashMapUpdate;
						try {
						mtlServer.displayHashMap();
						}
						catch(Exception e) {
							System.out.println(e);
						}
					}
						if(flag == 3 )
							break;
				}
			}
			if(flag == 3 || flag == 1 || capacity == 0)
				break;
		}
	System.out.println("Flag value:" +flag);
	return flag;
	}
	
	
	public Map<String, List<HashMapSubKey>> getHashMapObj() {
		System.out.println("Updated HashMap" +hashMap.entrySet());
		return hashMap;
	}
}
