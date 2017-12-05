package com.coolioasjulio.fll.visiondrive;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.bytedeco.javacpp.opencv_core.IplImage;

public class VisionDrive {
	
	public static final int ERROR_THRESHOLD = 10;
	public static final int CAMERA_FOV_HORIZONTAL = 75;
	public static final int CAMERA_FOV_VERTICAL = 47;
	
	public static void main(String[] args){
		
	}
	
	private int device;
	public VisionDrive(int device){
		this.device = device;
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
						
						consecutiveErrors = 0;
					} catch (IOException e) {
						e.printStackTrace();
						consecutiveErrors++;
						if(consecutiveErrors >= ERROR_THRESHOLD){
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
		double dist = halfWidth / Math.atan((double)CAMERA_FOV_HORIZONTAL/2d);
		double center = (double)(xMin+xMax)/2d;
		double offset = Math.abs(center - halfWidth);
		return Math.atan(offset/dist);
	}
	
	private double getTargetAOE(int imageHeight, int yMin, int yMax){
		double halfHeight = (double)imageHeight/2d;
		double dist = halfHeight / Math.atan((double)CAMERA_FOV_HORIZONTAL/2d);
		double center = (double)(yMin+yMax)/2d;
		double offset = Math.abs(center - halfHeight);
		return Math.atan(offset/dist);
	}
	
	private int[] readInts(DataInputStream in, int messageLength) throws IOException{
		int[] data = new int[messageLength];
		for(int i = 0; i < messageLength; i++){
			data[i] = in.readInt();
		}
		return data;
	}
}
