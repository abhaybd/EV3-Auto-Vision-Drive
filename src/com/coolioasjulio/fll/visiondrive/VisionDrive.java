package com.coolioasjulio.fll.visiondrive;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.bytedeco.javacpp.opencv_core.IplImage;

public class VisionDrive {
	
	public static final int ERROR_THRESHOLD = 10;
	
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
						
						int messageLength = in.readInt();
						int[] data = readInts(in, messageLength);
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
	
	private int[] readInts(DataInputStream in, int messageLength) throws IOException{
		int[] data = new int[messageLength];
		for(int i = 0; i < messageLength; i++){
			data[i] = in.readInt();
		}
		return data;
	}
}
