package com.coolioasjulio.fll.visiondrive;

public class RobotInfo {
	// Quit program if this many consecutive errors happen in vision thread
	public static final int CONSECUTIVE_ERROR_THRESHOLD = 10;
	
	// Gotten from specs sheet of webcam
	public static final double CAMERA_FOV_HORIZONTAL = 70.42;
	public static final double CAMERA_FOV_VERTICAL = 43.3;
	
	// Measured specs on the robot
	public static final double TURN_RADIUS = 5; // Half the distance between the treads
	public static final double MOTOR_DIAMETER = 2; // Diameter of motor wheel
	public static final double MOTOR_CIRCUMFERENCE = Math.PI * MOTOR_DIAMETER;
	
	//
	// Shooter subsystem
	//
	// Minimum number of milliseconds that must elapse before firing again.
	public static final int FIRE_RATE = 1500;
	public static final int DEGREES_PER_SHOT = 360;
	
	// Only turn if the current heading is more than this many degrees away from target heading
	public static final double HEADING_ERROR_THRESHOLD = 2;
	
	//
	// Server variables
	//
	public static final String SERVER_IP = null;
	public static final int SERVER_PORT = 4444; // Port for main TCP communication
	public static final int SERVER_UDP_PORT = 4445; // Port for intial UDP discovery server
}
