package com.coolioasjulio.fll.visiondrive;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;

public class YUVTest {
	public static void main(String[] args) throws IOException, InterruptedException{
		ImageCapture ic = new ImageCapture();
		byte[] bytes = ic.captureImage();
		
		
		InetAddress addr = NetworkUtils.pingAll(4445);
		LCD.drawString(addr.toString(), 0, 0);
		Socket socket = new Socket(addr, 4444);
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		out.writeInt(ic.imageWidth());
		out.writeInt(ic.imageHeight());
		out.flush();
		out.writeInt(bytes.length);
		out.write(bytes);
		out.flush();
		socket.close();
		
		while(Button.ENTER.isUp()){
			
		}
	}
}
