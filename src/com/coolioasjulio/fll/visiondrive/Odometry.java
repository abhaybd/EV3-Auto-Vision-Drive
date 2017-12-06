package com.coolioasjulio.fll.visiondrive;

public class Odometry {
	private double heading;
	private double angleOfElevation;
	
	public Odometry(double heading, double angleOfElevation){
		this.heading = heading;
		this.angleOfElevation = angleOfElevation;
	}
	
	public synchronized double getHeading(){
		return heading;
	}
	
	public synchronized double getAOE(){
		return angleOfElevation;
	}
	
	public synchronized void setOdometry(double heading, double angleOfElevation){
		this.heading = heading;
		this.angleOfElevation = angleOfElevation;
	}
	
}
