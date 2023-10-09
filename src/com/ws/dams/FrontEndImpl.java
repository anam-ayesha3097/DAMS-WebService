package com.ws.dams;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

@WebService(endpointInterface = "com.ws.dams.FrontEndInterface")
@SOAPBinding(style = Style.RPC)
public class FrontEndImpl implements FrontEndInterface {

	Sequencer sequencer = new Sequencer();
	static String replyClient = null;
	static String replyClient2 = null;
	static HashMap<String, String> resultHash = new HashMap<String,String>();
	static String resultR3 = null;
	static String resultR1 = null;
	static String resultR2 = null;
	static Logger LOGGER = Logger.getLogger(FrontEndImpl.class.getName());
	
	@Override
	public String receiveRequestFromClient(String userID, String appointmentType, String appointmentID, int capacity, String patientID, int menuOption) throws IOException {
		System.out.println("Welcome from Request Method "+userID);
		String reqWithSeqNum = sequencer.tagSequenceNumber(userID, appointmentType, appointmentID, capacity, patientID, menuOption);
		System.out.println("Request with Sequence Number "+reqWithSeqNum);
		String reply[] = null;

		Handler fileHandler = null;
		SimpleFormatter simpleFormatter = null;
		fileHandler = new FileHandler("E:/COMP-6231/DAMS Project/logs/FronEndImplementation.txt");
		simpleFormatter = new SimpleFormatter();
		LOGGER.setUseParentHandlers(false);
		LOGGER.addHandler(fileHandler);
		fileHandler.setFormatter(simpleFormatter);
		
		LOGGER.info("Log File: Front End Implementation");
		LOGGER.info("Method : Receive Request from Client");
		LOGGER.info("Request with Sequence Number : "+reqWithSeqNum);
		
		
		 CompletableFuture<String> replica1Result3 = getReplyFromReplica1Async3();
		
		 CompletableFuture<String> replica1Result1 = getReplyFromReplica1Async1();
		 
		 CompletableFuture<String> replica1Result2 = getReplyFromReplica1Async2();
		
		 /*Runnable t3 = () -> {
			String resultR1 = null;
			resultR1 = udpReceiveRequestFromReplica();
			System.out.println("Result Inside Thread "+resultR1);
			setResult(resultR1);
		}; new Thread(t3).start();*/
		
		/*Runnable t4 = () -> { 
			udpReceiveRequestFromReplica2(); 
			}; new Thread(t4).start();*/
			
		multicastRequest(reqWithSeqNum); 
		//multicastSocket(reqWithSeqNum);
		
		try {
			resultR3 = replica1Result3.get();
			resultR1 = replica1Result1.get();
			resultR2 = replica1Result2.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		 System.out.println("Result of Replica 3 in the client return method " +resultR3);
		 System.out.println("Result of Replica 1 in the client return method " +resultR1);
		 System.out.println("Result of Replica 2 in the client return method " +resultR2);
		 
		 //resultR2 = resultR1;
		 String finalCorrectResult = null;
		 finalCorrectResult = compareReplyFromReplicas(resultR1, resultR2, resultR3);
		
		return finalCorrectResult;
		
	}
	
	public static CompletableFuture<String> getReplyFromReplica1Async3() {
		return CompletableFuture.supplyAsync(() -> udpReceiveRequestFromReplica3() );
	}
	
	public static CompletableFuture<String> getReplyFromReplica1Async1() {
		return CompletableFuture.supplyAsync(() -> udpReceiveRequestFromReplica1() );
	}
	
	public static CompletableFuture<String> getReplyFromReplica1Async2() {
		return CompletableFuture.supplyAsync(() -> udpReceiveRequestFromReplica2() );
	}
	
	@Override
	public String receiveRequestForSwap(String userID,String oldAppointmentType, String oldAppointmentID,String newAppointmentID,
									  String newAppointmentType, String patientID,
									  int menuOption) throws IOException {
		System.out.println("Welcome from Request Method "+userID);
		String reqWithSeqNum = sequencer.tagSequenceNumberSwap(userID, oldAppointmentType, oldAppointmentID, newAppointmentID,newAppointmentType, patientID, menuOption);
		System.out.println("Request with Sequence Number "+reqWithSeqNum);
		
		Handler fileHandler = null;
		SimpleFormatter simpleFormatter = null;
		fileHandler = new FileHandler("E:/COMP-6231/DAMS Project/logs/FronEndSwapImplementation.txt");
		simpleFormatter = new SimpleFormatter();
		LOGGER.setUseParentHandlers(false);
		LOGGER.addHandler(fileHandler);
		fileHandler.setFormatter(simpleFormatter);
		
		LOGGER.info("Log File: Front End Implementation");
		LOGGER.info("Method : Receive Request from Client for Swap");
		LOGGER.info("Request for Swap with Sequence Number : "+reqWithSeqNum);
		
		 CompletableFuture<String> replica1Result3 = getReplyFromReplica1Async3();
		
		 CompletableFuture<String> replica1Result1 = getReplyFromReplica1Async1();
		 
		 CompletableFuture<String> replica1Result2 = getReplyFromReplica1Async2();
		
		 multicastRequest(reqWithSeqNum);
		
		
		// multicastSocket(reqWithSeqNum);
	
		 try {
				resultR3 = replica1Result3.get();
				resultR1 = replica1Result1.get();
				//resultR2= replica1Result2.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
			 System.out.println("Result of Replica 1 in the client return method " +resultR3);
			 System.out.println("Result of Replica 1 in the client return method " +resultR1);
			 System.out.println("Result of Replica 1 in the client return method " +resultR2);
			 
			 resultR2 = resultR1;
			 String finalCorrectResult = null;
			 finalCorrectResult = compareReplyFromReplicas(resultR1, resultR2, resultR3);
			
			 return finalCorrectResult;
	
	}

	@Override
	public void sayHello(String userID) {
		System.out.println("Welcome "+userID);
		
	}
	
	public static void multicastRequest(String reqWithSeqNum) {
		LOGGER.info("Log Method: Multicast Request to all the RM");
		try {
			DatagramSocket udpClient = new DatagramSocket();
			InetAddress udpClientHost = InetAddress.getByName("localhost");
			int serverPort = 2001; //RM1 UDP Agreed Port
			
			
			udpClient.connect(udpClientHost, serverPort);
			
			byte[] udpBytesRequest =  reqWithSeqNum.getBytes();
			 
			DatagramPacket request = null;
			request = new DatagramPacket(udpBytesRequest, reqWithSeqNum.length(), udpClientHost, serverPort);
			System.out.println("Before send " +request);
			udpClient.send(request);
			udpClient.close();
			
			//System.out.println(request);
			
			
			//byte[] udpBytesReply = new byte[1000];
			//DatagramPacket reply = new DatagramPacket(udpBytesReply, udpBytesReply.length);
			
			//udpClient.receive(reply);
			
			//System.out.println(new String(reply.getData()));
			//System.out.println(reply.getLength());
			//replyStringRM1 = new String(reply.getData());
			//replyStringRM1  = cleanUDPReplyString(replyStringRM1);
			//System.out.println("Reply from RM1 Multicast "+replyStringRM1);
		}
		catch(Exception e) {
			System.out.println("Multicast Exception "+e);
		}

	
		try {
			DatagramSocket udpClient = new DatagramSocket();
			InetAddress udpClientHost = InetAddress.getByName("172.20.10.2");
			int serverPort = 2002; //RM2 UDP Agreed Port
			
			
			udpClient.connect(udpClientHost, serverPort);
			
			byte[] udpBytesRequest =  reqWithSeqNum.getBytes();
			 
			DatagramPacket request = null;
			request = new DatagramPacket(udpBytesRequest, reqWithSeqNum.length(), udpClientHost, serverPort);
			System.out.println("Before send " +request);
			udpClient.send(request);
			udpClient.close();
			//System.out.println(request);
			
			
			//byte[] udpBytesReply = new byte[1000];
			//DatagramPacket reply = new DatagramPacket(udpBytesReply, udpBytesReply.length);
			
			//udpClient.receive(reply);
			
			//System.out.println(new String(reply.getData()));
			//System.out.println(reply.getLength());
			//replyStringRM2 = new String(reply.getData());
			//replyStringRM2 = cleanUDPReplyString(replyStringRM2);
			//System.out.println("Reply from RM2 Multicast "+replyStringRM2);
		}
		catch(Exception e) {
			System.out.println("Multicast Exception "+e);
		}
		
		try {
			DatagramSocket udpClient = new DatagramSocket();
			InetAddress udpClientHost = InetAddress.getByName("172.20.10.3");
			int serverPort = 2003; //RM1 UDP Agreed Port
			
			
			udpClient.connect(udpClientHost, serverPort);
			
			byte[] udpBytesRequest =  reqWithSeqNum.getBytes();
			 
			DatagramPacket request = null;
			request = new DatagramPacket(udpBytesRequest, reqWithSeqNum.length(), udpClientHost, serverPort);
			System.out.println("Before send " +request);
			udpClient.send(request);
			udpClient.close();
			//System.out.println(request);
			
			
			//byte[] udpBytesReply = new byte[1000];
			//DatagramPacket reply = new DatagramPacket(udpBytesReply, udpBytesReply.length);
			
			//udpClient.receive(reply);
			
			//System.out.println(new String(reply.getData()));
			//System.out.println(reply.getLength());
			//replyStringRM2 = new String(reply.getData());
			//replyStringRM2 = cleanUDPReplyString(replyStringRM2);
			//System.out.println("Reply from RM3 Multicast "+replyStringRM2);
		}
		catch(Exception e) {
			System.out.println("Multicast Exception "+e);
		}

	}
	
	public static void multicastSocket(String reqWithSeqNum) throws IOException {
		
		String group = "226.4.5.6";
		
		MulticastSocket ms = new MulticastSocket();
		
		DatagramPacket dp = new DatagramPacket(reqWithSeqNum.getBytes(), reqWithSeqNum.length(), InetAddress.getByName(group), 5000);
		
		ms.send(dp);
	
		//ms.close();
		
	}

	public static String udpReceiveRequestFromReplica3() {
		String resultR = null;
		LOGGER.info("Receive Result UDP for Replica 3");
		System.out.println("Before FE3 Try");
		try {
			DatagramSocket udpSocket = new DatagramSocket(2004);
			byte[] buffer = new byte[1000];
			System.out.println("UDP Server is Running");
				System.out.println("Inside While of FE3 UDP");
				DatagramPacket request = null;
				request = new DatagramPacket(buffer, buffer.length);
				System.out.println("Before Receieve");
				udpSocket.receive(request);
				System.out.println(request.getLength());
				//System.out.println(new String (request.getData()));
				resultR = new String(request.getData());
				
				resultR = cleanUDPReplyString(resultR);
				System.out.println(resultR);
				
				if(!resultR.isEmpty()) {
				replyClient = resultR;
				
					DatagramPacket reply = null;
					byte[] udpByteReply = new byte[1000];
					
					udpByteReply = "received request".getBytes();
					
					reply = new DatagramPacket(udpByteReply, udpByteReply.length, request.getAddress(), request.getPort());
					udpSocket.send(reply);
					
				}
				udpSocket.close();
				return resultR;
		}
		catch(Exception e) {
			System.out.println("Replica Manager 3 Exception "+e);
		}
		
		return "";
		
	}
	public static String udpReceiveRequestFromReplica1() {
		String resultR = null;
		LOGGER.info("Receieve Result UDP for Replica 1");
		try {
			DatagramSocket udpSocket = new DatagramSocket(6001);
			byte[] buffer = new byte[1000];
			DatagramPacket request = null;
			request = new DatagramPacket(buffer, buffer.length);
			System.out.println("UDP Server is Running");
			
				System.out.println("Inside While of FE1 UDP");
				udpSocket.receive(request);
				System.out.println(request.getLength());
				//System.out.println(new String (request.getData()));
				resultR = new String(request.getData());
				
				resultR = cleanUDPReplyString(resultR);
				System.out.println(resultR);
				
				if(!resultR.isEmpty()) {
				replyClient = resultR;
				
					DatagramPacket reply = null;
					byte[] udpByteReply = new byte[1000];
					
					udpByteReply = "received request".getBytes();
					
					reply = new DatagramPacket(udpByteReply, udpByteReply.length, request.getAddress(), request.getPort());
					udpSocket.send(reply);
					
		}
				udpSocket.close();
				return resultR;
				
				//System.out.println("Socket Closed");
		}
		catch(Exception e) {
			System.out.println("Replica Manager 1 Exception "+e);
		}
		
		return "";
	}
	
	public static String udpReceiveRequestFromReplica2(){
		String resultR = null;
		LOGGER.info("Receieve Result UDP for Replica 2");
		try {
		DatagramSocket udpSocket = new DatagramSocket(6002);
		byte[] buffer = new byte[1000];
		System.out.println("UDP Server is Running");
			System.out.println("Inside While of FE UDP");
			DatagramPacket request = null;
			request = new DatagramPacket(buffer, buffer.length);
			udpSocket.receive(request);
			System.out.println(request.getLength());
			//System.out.println(new String (request.getData()));
			resultR = new String(request.getData());
			
			resultR = cleanUDPReplyString(resultR);
			System.out.println(resultR);
			
			if(!resultR.isEmpty()) {
			replyClient = resultR;
			
				DatagramPacket reply = null;
				byte[] udpByteReply = new byte[1000];
				
				udpByteReply = "received request".getBytes();
				
				reply = new DatagramPacket(udpByteReply, udpByteReply.length, request.getAddress(), request.getPort());
				udpSocket.send(reply);
				
			}
			udpSocket.close();
			return resultR;
	}
	catch(Exception e) {
		System.out.println("Replica Manager 2 Exception "+e);
	}
		return "" ;
	}
	
	public static String compareReplyFromReplicas(String r1, String r2, String r3) {
		LOGGER.info("Compare All Replicas Result");
		String r1Split[] = r1.split(":");
		String r2Split[] = r2.split(":");
		String r3Split[] = r3.split(":");
		
		System.out.println(r1Split[0]);
		System.out.println(r2Split[0]);
		System.out.println(r3Split[0]);
		
		resultHash.put(r1Split[1],r1Split[0]);
		resultHash.put(r1Split[1],r1Split[0]);
		resultHash.put(r1Split[1],r1Split[0]);
		
		System.out.println("Before All Replicas If Statement");
		
		if(r1Split[0].equals(r2Split[0]) && !r1Split[0].equals(r3Split[0])) {
			Runnable t1 = () -> {
				sendErrorMessage(r3Split[1]);
				}; new Thread(t1).start(); 
			System.out.println("R3 is incorrect");
			LOGGER.info("Error Replica : R3");
			return r1Split[0];
			}
		else if(r1Split[0].equals(r3Split[0]) && !r1Split[0].equals(r2Split[0])) {
			Runnable t1 = () -> {
				sendErrorMessage(r2Split[1]);
				}; new Thread(t1).start(); 
			System.out.println("R2 is incorrect ");
			LOGGER.info("Error Replica : R2");
			return r1Split[0];
			}
		else if(r2Split[0].equals(r3Split[0]) && !r2Split[0].equals(r1Split[0])) {
			Runnable t1 = () -> {
				sendErrorMessage(r1Split[1]);
				}; new Thread(t1).start(); 
			System.out.println("R1 is incorrect");
			LOGGER.info("Error Replica : R1");
			return r2Split[0];
		}
		else {
			System.out.println("All Replicas are working Correctly");
			LOGGER.info("All Replicas are Working Correctly!");
			return r1Split[0];
		}
		
	}
	
	public static void sendErrorMessage(String errorReplica) {
		String error = errorReplica + ":" +"Error";
		LOGGER.info("Error Replica "+errorReplica);
		try {
			DatagramSocket udpClient = new DatagramSocket();
			InetAddress udpClientHost = InetAddress.getByName("172.20.10.3");
			int serverPort = 1001; //RM Error UDP Agreed Port
			
			
			udpClient.connect(udpClientHost, serverPort);
			
			byte[] udpBytesRequest =  error.getBytes();
			 
			DatagramPacket request = null;
			request = new DatagramPacket(udpBytesRequest, error.length(), udpClientHost, serverPort);
			//System.out.println("Before send " +request);
			udpClient.send(request);
			
			//System.out.println(request);
			
			
			//byte[] udpBytesReply = new byte[1000];
			//DatagramPacket reply = new DatagramPacket(udpBytesReply, udpBytesReply.length);
			
			//udpClient.receive(reply);
			//String replyStringRM1 = null;
			//System.out.println(new String(reply.getData()));
			//System.out.println(reply.getLength());
			//replyStringRM1 = new String(reply.getData());
			//replyStringRM1  = cleanUDPReplyString(replyStringRM1);
			//System.out.println("Reply from RM1 Multicast "+replyStringRM1);
		}
		catch(Exception e) {
			System.out.println("Multicast Exception "+e);
		}
		
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
}
