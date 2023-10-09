package com.ws.dams;

public class HashMapSubKey {

	private String appointmentID = "";
	private String additionInfo = "";
	
	public HashMapSubKey(String appointmentID, String additionalInfo) {
		this.appointmentID = appointmentID;
		this.additionInfo = additionalInfo;
	}
	
	public String getAppointmentID() {
		return appointmentID;
	}
	
	public String getAdditionInfo() {
		return additionInfo;
	}
	
	public void setAppointmentID(String appointmentID) {
		this.appointmentID = appointmentID;
	}
	
	public void setAdditionalInfo(String additionalInfo) {
		this.additionInfo = additionalInfo;
	}
	
	public void display() {
		System.out.println("AppointmentID: " +getAppointmentID()+ " Additional Info: " +getAdditionInfo());
	}
}
