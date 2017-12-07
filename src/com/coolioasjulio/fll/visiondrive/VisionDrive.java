package com.coolioasjulio.fll.visiondrive;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.bytedeco.javacpp.opencv_core.IplImage;

import lejos.robotics.RegulatedMotor;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;

public class VisionDrive {
	
	public static final int CONSECUTIVE_ERROR_THRESHOLD = 10;
	public static final double CAMERA_FOV_HORIZONTAL = 70.42;
	public static final double CAMERA_FOV_VERTICAL = 43.3;
	
	public static final double TURN_RADIUS = 5;
	public static final double MOTOR_CIRCUMFERENCE = 2;
	
	public static final double HEADING_ERROR_THRESHOLD = 2;
	
	public static final String SERVER_IP = "10.0.0.1";
	public static final int SERVER_PORT = 4444;
	
	public static void main(String[] args) throws IOException{
		VisionDrive vd = new VisionDrive(0);
		vd.startDriving(SERVER_IP, SERVER_PORT);
	}
	
	private int device;
	private Odometry currentOdometry;
	private Odometry targetOdometry;
	private RegulatedMotor leftMotor, rightMotor;
	
	public VisionDrive(int device){
		this.device = device;
		currentOdometry = new Odometry(0,0);
		targetOdometry = new Odometry(0,0);
		leftMotor = new EV3LargeRegulatedMotor(MotorPort.B);
		rightMotor = new EV3LargeRegulatedMotor(MotorPort.C);
	}
	
	public void startDriving(String server, int port) throws IOException {
		Thread t = startVisionThread(server,port);
		while(true) {
			double headingError = Math.abs(targetOdometry.getHeading() - currentOdometry.getHeading());
			if(headingError <= HEADING_ERROR_THRESHOLD) {
				leftMotor.forward();
				rightMotor.forward();
			} else {
				leftMotor.stop();
				rightMotor.stop();
				rotate(targetOdometry.getHeading());
			}
		}
	}
	
	public Odometry rotate(double degrees) {
		if(degrees == 0) {
			return currentOdometry;
		}
		double circumference = 2d * TURN_RADIUS * Math.PI;
		double rotationDistance = (degrees * circumference)/360d;
		double motorDegrees = rotationDistance / MOTOR_CIRCUMFERENCE;
		leftMotor.rotate((int) (motorDegrees * Math.abs(degrees)/degrees), true);
		rightMotor.rotate((int) (-motorDegrees * Math.abs(degrees)/degrees), false);
		currentOdometry.setOdometry(currentOdometry.getHeading() + degrees, currentOdometry.getHeading());
		return currentOdometry;
	}
	
	public Thread startVisionThread(String hostname, int port) throws IOException{
		Thread thread = new Thread(()->{
			try(Socket socket = new Socket(hostname, port)){
				DataInputStream in = new DataInputStream(socket.getInputStream());
				DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				int consecutiveErrors = 0;
				while(!Thread.interrupted()){
					try {
						socket.getInputStream();
						IplImage image = ImageUtils.captureImage(device);
						byte[] bytes = ImageUtils.getBytes(image);
						out.writeInt(bytes.length);
						out.write(bytes);
						out.flush();
						
						int xMin = in.readInt();
						int yMin = in.readInt();
						int xMax = in.readInt();
						int yMax = in.readInt();
						
						double targetHeading = getTargetHeading(image.width(), xMin, xMax);
						double targetAOE = getTargetAOE(image.height(), yMin, yMax);
						
						targetOdometry.setOdometry(targetHeading, targetAOE);
						
						consecutiveErrors = 0;
					} catch (IOException e) {
						e.printStackTrace();
						consecutiveErrors++;
						if(consecutiveErrors >= CONSECUTIVE_ERROR_THRESHOLD){
							System.exit(-1);
						}
					}
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		});
		thread.start();
		return thread;
	}
	
	private double getTargetHeading(int imageWidth, int xMin, int xMax){
		double halfWidth = (double)imageWidth/2d;
		double dist = halfWidth / Math.atan(CAMERA_FOV_HORIZONTAL/2d);
		double center = (double)(xMin+xMax)/2d;
		double offset = Math.abs(center - halfWidth);
		return Math.atan(offset/dist);
	}
	
	private double getTargetAOE(int imageHeight, int yMin, int yMax){
		double halfHeight = (double)imageHeight/2d;
		double dist = halfHeight / Math.atan(CAMERA_FOV_HORIZONTAL/2d);
		double center = (double)(yMin+yMax)/2d;
		double offset = Math.abs(center - halfHeight);
		return Math.atan(offset/dist);
	}
}
