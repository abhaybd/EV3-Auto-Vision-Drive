package com.coolioasjulio.fll.visiondrive;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;

import lejos.robotics.RegulatedMotor;
import lejos.utility.Stopwatch;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.Power;
import lejos.hardware.ev3.EV3;

public class VisionDrive {
	
	public static void main(String[] args) throws IOException{
		VisionDrive vd = new VisionDrive();
		vd.startDriving(RobotInfo.SERVER_TCP_PORT, RobotInfo.SERVER_UDP_PORT);
	}
	
	// Odometry objects
	private Odometry currentOdometry; // currentOdometry is in absolute degrees.
	private Odometry targetOdometry; // targetOdometry is in relative degrees to currentOdometry.
	
	// Motors
	private RegulatedMotor leftMotor, rightMotor;
	private RegulatedMotor shooterElevationMotor, shooterMotor;
	
	// LeJOS utilities
	private Stopwatch shooterTimer;
	private Power power;
	
	// Debug
	private Socket debugSocket;
	private DataOutputStream debugStream;
	
	public VisionDrive(){
		currentOdometry = new Odometry(0,0);
		targetOdometry = new Odometry(0,0);
		leftMotor = new EV3LargeRegulatedMotor(MotorPort.B);
		rightMotor = new EV3LargeRegulatedMotor(MotorPort.C);
		shooterElevationMotor = new EV3LargeRegulatedMotor(MotorPort.A);
		shooterMotor = new EV3LargeRegulatedMotor(MotorPort.D);
		power = ((EV3)BrickFinder.getLocal()).getPower();
	}
	
	/**
	 * Start driving autonomously. Blocks indefinitely.
	 * @param tcpPort TCP port of the server, for communication.
	 * @param udpPort UDP port of the server, only used to find the server, then TCP is used.
	 * @throws IOException
	 */
	public void startDriving(int tcpPort, int udpPort) throws IOException {
		startDriving(null, tcpPort, udpPort);
	}
	
	/**
	 * Start driving autonomously. Blocks indefinitely.
	 * @param server Address of the server for vision processing. Pass null to automatically search LAN and find server.
	 * @param port TCP port of the server, for communication.
	 * @param udpPort UDP port of the server, only used to find the server, then TCP is used.
	 * @throws IOException
	 */
	public void startDriving(String hostname, int port, int udpPort) throws IOException {
		Thread t;
		if(hostname == null){
			InetAddress server = NetworkUtils.pingAll(udpPort);
			t = startVisionThread(server.getHostAddress(), port);
		}
		else{
			t = startVisionThread(hostname, port);
		}
		shooterTimer = new Stopwatch();
		while(true) {
			double headingError = Math.abs(targetOdometry.getHeading());
			if(headingError <= RobotInfo.HEADING_ERROR_THRESHOLD) {
				leftMotor.backward();
				rightMotor.backward();
				shooterElevationMotor.rotateTo((int)-targetOdometry.getAOE(), true);
				fireIfReady();
			} else {
				leftMotor.stop();
				rightMotor.stop();
				rotate(targetOdometry.getHeading());
			}
			if(Button.ESCAPE.isDown()) {
				debug("Closing!");
				t.interrupt();
				break;
			}
		}
	}
	
	private void setUpDebug() throws IOException{
		setUpDebug(NetworkUtils.pingAll(RobotInfo.SERVER_UDP_PORT));
	}
	
	private void setUpDebug(InetAddress server) throws IOException{
		debugSocket = new Socket(server, RobotInfo.SERVER_DBG_PORT);
		debugStream = new DataOutputStream(debugSocket.getOutputStream());
	}
	
	private void debug(String message) throws IOException{
		if(debugStream == null){
			try {
				setUpDebug();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			byte[] bytes = message.getBytes("UTF-8");
			debugStream.writeInt(bytes.length);
			debugStream.write(bytes);
			debugStream.flush();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	private void fireIfReady() {
		if(shooterTimer.elapsed() >= RobotInfo.FIRE_RATE){
			int speed = (int) (power.getVoltage() * 65);
			try {
				debug("Shooting at speed: " + speed);
			} catch (IOException e) {
				e.printStackTrace();
			}
			shooterMotor.setSpeed(speed);
			shooterMotor.rotate(RobotInfo.DEGREES_PER_SHOT);
			shooterTimer.reset();
		}
	}
	
	private Odometry rotate(double degrees) {
		if(degrees == 0) {
			return currentOdometry;
		}
		double circumference = 2d * RobotInfo.TURN_RADIUS * Math.PI;
		double rotationDistance = (degrees * circumference)/360d;
		double motorDegrees = rotationDistance / RobotInfo.MOTOR_CIRCUMFERENCE;
		leftMotor.rotate((int) (motorDegrees * Math.abs(degrees)/degrees), true);
		rightMotor.rotate((int) (-motorDegrees * Math.abs(degrees)/degrees), false);
		currentOdometry.setOdometry(currentOdometry.getHeading() + degrees, currentOdometry.getAOE());
		targetOdometry.setOdometry(0, targetOdometry.getAOE());
		return currentOdometry;
	}
	
	private Thread startVisionThread(final String hostname, final int port) throws IOException{
		Thread thread = new Thread(
				new Runnable(){
					@Override
					public void run(){
						try(Socket socket = new Socket(hostname, port)){
							DataInputStream in = new DataInputStream(socket.getInputStream());
							DataOutputStream out = new DataOutputStream(socket.getOutputStream());
							int consecutiveErrors = 0;
							ImageCapture imageCapture = new ImageCapture(160, 120);
							while(!Thread.interrupted()){
								try {
									byte[] bytes = imageCapture.captureImage();
									out.writeInt(imageCapture.imageWidth());
									out.writeInt(imageCapture.imageHeight());
									out.flush();
									out.writeInt(bytes.length);
									out.write(bytes);
									out.flush();
									
									int xMin = in.readInt();
									int yMin = in.readInt();
									int xMax = in.readInt();
									int yMax = in.readInt();
									
									double targetHeading = getTargetHeading(imageCapture.imageWidth(), xMin, xMax);
									double targetAOE = getTargetAOE(imageCapture.imageHeight(), yMin, yMax);
									
									targetOdometry.setOdometry(targetHeading, targetAOE);
									debug(String.format("Detected bounding box: %s,%s,%s,%s", xMin,yMin,xMax,yMax));
									debug(String.format("New targetHeading: %.2f - New targetAOE: %.2f", targetHeading, targetAOE));
									consecutiveErrors = 0;
								} catch (IOException e) {
									e.printStackTrace();
									consecutiveErrors++;
									if(consecutiveErrors >= RobotInfo.CONSECUTIVE_ERROR_THRESHOLD){
										System.exit(-1);
									}
								}
							}
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
		});
		thread.start();
		return thread;
	}
	
	private double getTargetHeading(int imageWidth, int xMin, int xMax){
		if(xMin == -1 && xMax == -1){
			return 0d;
		}
		double halfWidth = (double)imageWidth/2d;
		double dist = halfWidth / Math.atan(RobotInfo.CAMERA_FOV_HORIZONTAL/2d);
		double center = (double)(xMin+xMax)/2d;
		double offset = Math.abs(center - halfWidth);
		return Math.atan(offset/dist);
	}
	
	private double getTargetAOE(int imageHeight, int yMin, int yMax){
		if(yMin == -1 && yMax == -1){
			return 0d;
		}
		double halfHeight = (double)imageHeight/2d;
		double dist = halfHeight / Math.atan(RobotInfo.CAMERA_FOV_HORIZONTAL/2d);
		double center = (double)(yMin+yMax)/2d;
		double offset = Math.abs(center - halfHeight);
		return Math.atan(offset/dist);
	}
}
