package com.ws.dams;

import java.io.IOException;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

@WebService
@SOAPBinding(style = Style.RPC)
public interface FrontEndInterface {

	@WebMethod
	public String receiveRequestFromClient(String userID, String appointmentType, String appointmentID, int capacity, String patientID, int menuOption)throws IOException;
	
	@WebMethod
	public String receiveRequestForSwap(String userID, String oldAppointmentType, String oldAppointmentID,String newAppointmentType,
									  String newAppointmentID, String patientID,int menuOption) throws IOException;
	
	@WebMethod 
	public void sayHello(String userID);
	
}
